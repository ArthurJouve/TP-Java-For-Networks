package SSL;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Protocol handler for chat server operations.
 * Manages sessions, rooms, and message routing independently of SSL layer.
 * This separation follows the architecture requirement of having a dedicated protocol handler.
 * 
 * @author Arthur Jouve & Ewan Zahra Thenault
 * @version 1.0
 */
public class ChatProtocolServer {
    private Map<String, ClientSession> activeSessions;
    private Map<String, ChatRoom> chatRooms;
    private Map<String, DataOutputStream> clientOutputStreams;
    
    /**
     * Constructs a ChatProtocolServer with empty session and room maps.
     */
    public ChatProtocolServer() {
        this.activeSessions = new ConcurrentHashMap<>();
        this.chatRooms = new ConcurrentHashMap<>();
        this.clientOutputStreams = new ConcurrentHashMap<>();
    }
    
    /**
     * Processes a protocol message and returns updated session ID.
     * This is the main entry point for message handling.
     * 
     * @param messageData raw serialized message bytes
     * @param output client's output stream for responses
     * @param currentSessionId current session ID (null if not authenticated)
     * @return updated session ID after processing
     */
    public String handleMessage(byte[] messageData, DataOutputStream output, String currentSessionId) {
        try {
            ChatMessage message = ChatMessage.deserialize(messageData);
            
            System.out.println("[PROTOCOL] Type: " + message.getMessageType() + 
                             ", From: " + message.getSender());
            
            switch (message.getMessageType()) {
                case LOGIN_REQUEST:
                    return processLogin(message, output);
                    
                case JOIN_ROOM_REQUEST:
                    processJoinRoom(message, currentSessionId, output);
                    break;
                    
                case TEXT_MESSAGE:
                    broadcastToRoom(message, currentSessionId);
                    break;
                    
                case PRIVATE_MESSAGE:
                    sendPrivateMessage(message, currentSessionId);
                    break;
                    
                case USER_LIST_REQUEST:
                    sendUserList(output);
                    break;
                    
                default:
                    sendError(output, "Unknown message type: " + message.getMessageType());
            }
            
            return currentSessionId;
            
        } catch (Exception e) {
            System.err.println("[PROTOCOL ERROR] " + e.getMessage());
            e.printStackTrace();
            try {
                sendError(output, "Invalid message format");
            } catch (IOException ex) {
                System.err.println("[PROTOCOL ERROR] Cannot send error response");
            }
            return currentSessionId;
        }
    }
    
    /**
     * Processes login request and creates new session.
     * 
     * @param message login message
     * @param output client output stream
     * @return new session ID
     * @throws IOException if response sending fails
     */
    private String processLogin(ChatMessage message, DataOutputStream output) throws IOException {
        String username = message.getSender();
        
        // Check duplicate username
        for (ClientSession session : activeSessions.values()) {
            if (session.getUsername().equals(username)) {
                sendError(output, "Username '" + username + "' already taken");
                return null;
            }
        }
        
        String sessionId = UUID.randomUUID().toString();
        ClientSession session = new ClientSession(username, sessionId);
        activeSessions.put(sessionId, session);
        clientOutputStreams.put(sessionId, output);
        
        System.out.println("[LOGIN] User: " + username + " | SessionID: " + 
                         sessionId.substring(0, 8) + "... | Total: " + activeSessions.size());
        
        ChatMessage response = new ChatMessage(MessageType.LOGIN_RESPONSE, "server", 
                                              "Welcome " + username + "!");
        output.write(response.serialize());
        output.flush();
        
        return sessionId;
    }
    
    /**
     * Processes join room request.
     * 
     * @param message join room message
     * @param sessionId user session ID
     * @param output client output stream
     * @throws IOException if response fails
     */
    private void processJoinRoom(ChatMessage message, String sessionId, 
                                DataOutputStream output) throws IOException {
        if (sessionId == null) {
            sendError(output, "Not authenticated. Please login first.");
            return;
        }
        
        ClientSession session = activeSessions.get(sessionId);
        if (session == null) {
            sendError(output, "Invalid session");
            return;
        }
        
        String roomName = message.getContent();
        
        // Leave current room if in one
        if (session.getCurrentRoom() != null) {
            ChatRoom oldRoom = chatRooms.get(session.getCurrentRoom());
            if (oldRoom != null) {
                oldRoom.removeMember(session);
                System.out.println("[LEAVE] User: " + session.getUsername() + 
                                 " left room: " + session.getCurrentRoom());
            }
        }
        
        // Join new room
        ChatRoom room = chatRooms.computeIfAbsent(roomName, k -> new ChatRoom(roomName));
        room.addMember(session);
        session.setCurrentRoom(roomName);
        
        System.out.println("[JOIN] User: " + session.getUsername() + " -> Room: " + 
                         roomName + " (" + room.getMemberCount() + " members)");
        
        ChatMessage response = new ChatMessage(MessageType.JOIN_ROOM_REQUEST, "server", 
                                              "Joined room: " + roomName);
        output.write(response.serialize());
        output.flush();
        
        // Notify other members
        notifyRoom(roomName, session.getUsername() + " joined the room", sessionId);
    }
    
    /**
     * Broadcasts text message to all room members.
     * 
     * @param message text message
     * @param senderSessionId sender's session ID
     */
    private void broadcastToRoom(ChatMessage message, String senderSessionId) {
        if (senderSessionId == null) {
            System.err.println("[BROADCAST ERROR] No session");
            return;
        }
        
        ClientSession sender = activeSessions.get(senderSessionId);
        if (sender == null || sender.getCurrentRoom() == null) {
            System.err.println("[BROADCAST ERROR] User not in room");
            return;
        }
        
        String roomName = sender.getCurrentRoom();
        ChatRoom room = chatRooms.get(roomName);
        
        if (room != null) {
            System.out.println("[BROADCAST] Room: " + roomName + " | From: " + 
                             sender.getUsername() + " | Msg: " + message.getContent());
            
            String formattedMsg = "[" + sender.getUsername() + "]: " + message.getContent();
            ChatMessage broadcast = new ChatMessage(MessageType.TEXT_MESSAGE, "server", formattedMsg);
            
            int delivered = 0;
            for (ClientSession member : room.getMembers()) {
                DataOutputStream memberOutput = clientOutputStreams.get(member.getSessionId());
                if (memberOutput != null) {
                    try {
                        memberOutput.write(broadcast.serialize());
                        memberOutput.flush();
                        delivered++;
                    } catch (IOException e) {
                        System.err.println("[BROADCAST ERROR] Failed for " + member.getUsername());
                    }
                }
            }
            
            System.out.println("[BROADCAST] Delivered to " + delivered + " members");
        }
    }
    
    /**
     * Sends notification to all room members except sender.
     * 
     * @param roomName room name
     * @param notification message to send
     * @param excludeSessionId session to exclude (sender)
     */
    private void notifyRoom(String roomName, String notification, String excludeSessionId) {
        ChatRoom room = chatRooms.get(roomName);
        if (room != null) {
            ChatMessage msg = new ChatMessage(MessageType.TEXT_MESSAGE, "system", 
                                            "[SYSTEM] " + notification);
            
            for (ClientSession member : room.getMembers()) {
                if (!member.getSessionId().equals(excludeSessionId)) {
                    DataOutputStream output = clientOutputStreams.get(member.getSessionId());
                    if (output != null) {
                        try {
                            output.write(msg.serialize());
                            output.flush();
                        } catch (IOException e) {
                            System.err.println("[NOTIFY ERROR] " + member.getUsername());
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Sends private message to specific user.
     * 
     * @param message private message
     * @param senderSessionId sender's session ID
     */
    private void sendPrivateMessage(ChatMessage message, String senderSessionId) {
        if (senderSessionId == null) {
            System.err.println("[PM ERROR] No session");
            return;
        }
        
        ClientSession sender = activeSessions.get(senderSessionId);
        if (sender == null) {
            System.err.println("[PM ERROR] Invalid sender");
            return;
        }
        
        // Parse recipient:message format
        String content = message.getContent();
        String[] parts = content.split(":", 2);
        if (parts.length != 2) {
            System.err.println("[PM ERROR] Invalid format");
            return;
        }
        
        String recipientName = parts[0].trim();
        String privateMsg = parts[1].trim();
        
        System.out.println("[PM] " + sender.getUsername() + " -> " + recipientName);
        
        // Find recipient
        for (ClientSession session : activeSessions.values()) {
            if (session.getUsername().equals(recipientName)) {
                DataOutputStream output = clientOutputStreams.get(session.getSessionId());
                if (output != null) {
                    try {
                        ChatMessage pm = new ChatMessage(MessageType.PRIVATE_MESSAGE, "server", 
                                                        "[PM from " + sender.getUsername() + "]: " + privateMsg);
                        output.write(pm.serialize());
                        output.flush();
                        System.out.println("[PM] Delivered");
                        return;
                    } catch (IOException e) {
                        System.err.println("[PM ERROR] Delivery failed");
                    }
                }
            }
        }
        
        System.err.println("[PM ERROR] Recipient not found: " + recipientName);
    }
    
    /**
     * Sends list of active users to client.
     * 
     * @param output client output stream
     * @throws IOException if sending fails
     */
    private void sendUserList(DataOutputStream output) throws IOException {
        StringBuilder userList = new StringBuilder();
        
        for (ClientSession session : activeSessions.values()) {
            userList.append(session.getUsername());
            if (session.getCurrentRoom() != null) {
                userList.append(" (in ").append(session.getCurrentRoom()).append(")");
            }
            userList.append(", ");
        }
        
        if (userList.length() > 0) {
            userList.setLength(userList.length() - 2);
        } else {
            userList.append("No users online");
        }
        
        System.out.println("[USER_LIST] Sent: " + activeSessions.size() + " users");
        
        ChatMessage response = new ChatMessage(MessageType.USER_LIST_RESPONSE, "server", 
                                              "Active users: " + userList.toString());
        output.write(response.serialize());
        output.flush();
    }
    
    /**
     * Sends error message to client.
     * 
     * @param output client output stream
     * @param errorMessage error description
     * @throws IOException if sending fails
     */
    private void sendError(DataOutputStream output, String errorMessage) throws IOException {
        System.err.println("[ERROR] " + errorMessage);
        ChatMessage error = new ChatMessage(MessageType.ERROR_RESPONSE, "server", 
                                          "ERROR: " + errorMessage);
        output.write(error.serialize());
        output.flush();
    }
    
    /**
     * Removes a session on disconnection.
     * 
     * @param sessionId session to remove
     */
    public void removeSession(String sessionId) {
        if (sessionId != null) {
            ClientSession session = activeSessions.remove(sessionId);
            clientOutputStreams.remove(sessionId);
            
            if (session != null) {
                // Remove from room
                if (session.getCurrentRoom() != null) {
                    ChatRoom room = chatRooms.get(session.getCurrentRoom());
                    if (room != null) {
                        room.removeMember(session);
                        notifyRoom(session.getCurrentRoom(), 
                                 session.getUsername() + " left the room", sessionId);
                    }
                }
                
                System.out.println("[LOGOUT] User: " + session.getUsername() + 
                                 " | Remaining: " + activeSessions.size());
            }
        }
    }
}
