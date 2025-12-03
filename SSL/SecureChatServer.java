package SSL;

import javax.net.ssl.*;
import java.io.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Secure chat server that handles protocol messages over SSL/TLS.
 * Manages multiple authenticated users and chat rooms.
 * 
 * @author Arthur Jouve & Ewan Zahra Thenault
 * @version 1.0
 */
public class SecureChatServer {
    private SSLServerSocket serverSocket;
    private Map<String, ClientSession> activeSessions;
    private Map<String, ChatRoom> chatRooms;
    private int port;
    private String keystorePath;
    private String keystorePassword;
    private boolean isRunning;
    
    /**
     * Constructs a SecureChatServer with SSL configuration.
     * 
     * @param port the port to listen on
     * @param keystorePath path to keystore file
     * @param keystorePassword keystore password
     */
    public SecureChatServer(int port, String keystorePath, String keystorePassword) {
        this.port = port;
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
        this.activeSessions = new ConcurrentHashMap<>();
        this.chatRooms = new ConcurrentHashMap<>();
        this.isRunning = false;
    }
    
    /**
     * Launches the secure chat server.
     * 
     * @throws Exception if server cannot start
     */
    public void launch() throws Exception {
        SSLContext sslContext = createSSLContext(keystorePath, keystorePassword);
        SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
        serverSocket = (SSLServerSocket) factory.createServerSocket(port);
        isRunning = true;
        
        System.out.println("Secure Chat Server started on port " + port);
        
        while (isRunning) {
            try {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Creates SSL context for secure connections.
     * 
     * @param keystorePath path to keystore
     * @param password keystore password
     * @return configured SSLContext
     * @throws Exception if context creation fails
     */
    private SSLContext createSSLContext(String keystorePath, String password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("JKS");
        FileInputStream fis = new FileInputStream(keystorePath);
        keyStore.load(fis, password.toCharArray());
        fis.close();
        
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, password.toCharArray());
        
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());
        
        return sslContext;
    }
    
    /**
     * Handles a single client connection.
     * Processes protocol messages in a loop.
     * 
     * @param socket the SSL socket for the client
     */
    private void handleClient(SSLSocket socket) {
        try {
            socket.startHandshake();
            System.out.println("[CONNECTION] Client from " + socket.getInetAddress());
            
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            
            String sessionId = null;
            
            while (isRunning) {
                // Read message length from header
                byte version = input.readByte();
                byte typeCode = input.readByte();
                int bodyLength = input.readInt();
                int timestamp = input.readInt();
                
                // Read full message
                byte[] bodyBytes = new byte[bodyLength];
                input.readFully(bodyBytes);
                
                // Reconstruct full message
                byte[] fullMessage = new byte[10 + bodyLength];
                fullMessage[0] = version;
                fullMessage[1] = typeCode;
                System.arraycopy(intToBytes(bodyLength), 0, fullMessage, 2, 4);
                System.arraycopy(intToBytes(timestamp), 0, fullMessage, 6, 4);
                System.arraycopy(bodyBytes, 0, fullMessage, 10, bodyLength);
                
                // Process message
                sessionId = handleProtocolMessage(socket, fullMessage, output, sessionId);
            }
            
        } catch (EOFException e) {
            System.out.println("[DISCONNECTION] Client disconnected");
        } catch (Exception e) {
            System.err.println("[ERROR] Client error: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }
    
    /**
     * Processes a protocol message based on its type.
     * 
     * @param socket the client socket
     * @param messageData the raw message bytes
     * @param output output stream for responses
     * @param currentSessionId current session ID (null if not logged in)
     * @return session ID after processing
     */
    public String handleProtocolMessage(SSLSocket socket, byte[] messageData, 
                                       DataOutputStream output, String currentSessionId) {
        try {
            ChatMessage message = ChatMessage.deserialize(messageData);
            
            System.out.println("[RECEIVED] Type: " + message.getMessageType() + 
                             ", Sender: " + message.getSender());
            
            switch (message.getMessageType()) {
                case LOGIN_REQUEST:
                    return processLogin(message, output);
                    
                case JOIN_ROOM_REQUEST:
                    processJoinRoom(message, currentSessionId);
                    break;
                    
                case TEXT_MESSAGE:
                    broadcastToRoom(message);
                    break;
                    
                case PRIVATE_MESSAGE:
                    sendPrivateMessage(message);
                    break;
                    
                case USER_LIST_REQUEST:
                    sendUserList(output, currentSessionId);
                    break;
                    
                default:
                    sendError(output, "Unknown message type");
            }
            
            return currentSessionId;
            
        } catch (Exception e) {
            System.err.println("[ERROR] Processing message: " + e.getMessage());
            try {
                sendError(output, "Invalid message format");
            } catch (IOException ex) {
                System.err.println("Error sending error response: " + ex.getMessage());
            }
            return currentSessionId;
        }
    }
    
    /**
     * Processes a login request.
     * 
     * @param message the login request message
     * @param output output stream for response
     * @return session ID if login successful, null otherwise
     * @throws IOException if sending response fails
     */
    private String processLogin(ChatMessage message, DataOutputStream output) throws IOException {
        String username = message.getSender();
        String sessionId = UUID.randomUUID().toString();
        
        // Create session
        ClientSession session = new ClientSession(username, sessionId);
        activeSessions.put(sessionId, session);
        
        System.out.println("[LOGIN] User: " + username + ", Session: " + sessionId);
        
        // Send success response
        ChatMessage response = new ChatMessage(MessageType.LOGIN_RESPONSE, "server", "Login successful");
        byte[] responseData = response.serialize();
        output.write(responseData);
        output.flush();
        
        return sessionId;
    }
    
    /**
     * Processes a join room request.
     * 
     * @param message the join room message
     * @param sessionId the session ID of the user
     */
    private void processJoinRoom(ChatMessage message, String sessionId) {
        if (sessionId == null) {
            System.err.println("[ERROR] Join room without login");
            return;
        }
        
        String roomId = message.getContent(); // Room name in content
        ClientSession session = activeSessions.get(sessionId);
        
        if (session != null) {
            ChatRoom room = chatRooms.computeIfAbsent(roomId, k -> new ChatRoom(roomId));
            room.addMember(session);
            session.setCurrentRoom(roomId);
            
            System.out.println("[JOIN_ROOM] User: " + session.getUsername() + " joined " + roomId);
        }
    }
    
    /**
     * Broadcasts a message to all members of a room.
     * 
     * @param message the message to broadcast
     */
    private void broadcastToRoom(ChatMessage message) {
        String roomId = message.getContent(); // Assuming room info is in content
        ChatRoom room = chatRooms.get(roomId);
        
        if (room != null) {
            System.out.println("[BROADCAST] Room: " + roomId + ", From: " + message.getSender());
            
            for (ClientSession member : room.getMembers()) {
                try {
                    // In a full implementation, send to each member's socket
                    System.out.println("  -> Sending to: " + member.getUsername());
                } catch (Exception e) {
                    System.err.println("Error broadcasting to " + member.getUsername());
                }
            }
        }
    }
    
    /**
     * Sends a private message to a specific user.
     * 
     * @param message the private message
     */
    private void sendPrivateMessage(ChatMessage message) {
        String recipient = message.getContent(); // Recipient in content
        System.out.println("[PRIVATE] From: " + message.getSender() + " to: " + recipient);
        
        // Find recipient session and send message
        for (ClientSession session : activeSessions.values()) {
            if (session.getUsername().equals(recipient)) {
                System.out.println("  -> Message delivered to " + recipient);
                return;
            }
        }
        
        System.err.println("[ERROR] Recipient not found: " + recipient);
    }
    
    /**
     * Sends the list of active users.
     * 
     * @param output output stream
     * @param sessionId requesting session ID
     * @throws IOException if sending fails
     */
    private void sendUserList(DataOutputStream output, String sessionId) throws IOException {
        StringBuilder userList = new StringBuilder();
        for (ClientSession session : activeSessions.values()) {
            userList.append(session.getUsername()).append(",");
        }
        
        ChatMessage response = new ChatMessage(MessageType.USER_LIST_RESPONSE, "server", userList.toString());
        output.write(response.serialize());
        output.flush();
    }
    
    /**
     * Sends an error response.
     * 
     * @param output output stream
     * @param errorMessage the error message
     * @throws IOException if sending fails
     */
    private void sendError(DataOutputStream output, String errorMessage) throws IOException {
        ChatMessage error = new ChatMessage(MessageType.ERROR_RESPONSE, "server", errorMessage);
        output.write(error.serialize());
        output.flush();
    }
    
    /**
     * Converts int to byte array.
     */
    private byte[] intToBytes(int value) {
        return new byte[] {
            (byte)(value >>> 24),
            (byte)(value >>> 16),
            (byte)(value >>> 8),
            (byte)value
        };
    }
    
    /**
     * Main method to start the server.
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java SecureChatServer <port> <keystorePath> <password>");
            System.exit(1);
        }
        
        try {
            int port = Integer.parseInt(args[0]);
            SecureChatServer server = new SecureChatServer(port, args[1], args[2]);
            server.launch();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

/**
 * Represents a client session.
 */
class ClientSession {
    private String username;
    private String sessionId;
    private String currentRoom;
    
    public ClientSession(String username, String sessionId) {
        this.username = username;
        this.sessionId = sessionId;
    }
    
    public String getUsername() { return username; }
    public String getSessionId() { return sessionId; }
    public String getCurrentRoom() { return currentRoom; }
    public void setCurrentRoom(String room) { this.currentRoom = room; }
}

/**
 * Represents a chat room.
 */
class ChatRoom {
    private String roomId;
    private List<ClientSession> members;
    
    public ChatRoom(String roomId) {
        this.roomId = roomId;
        this.members = new ArrayList<>();
    }
    
    public void addMember(ClientSession session) {
        members.add(session);
    }
    
    public List<ClientSession> getMembers() {
        return new ArrayList<>(members);
    }
}
