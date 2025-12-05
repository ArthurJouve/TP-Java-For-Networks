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
    private ChatProtocolServer protocolHandler;
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
        this.protocolHandler = new ChatProtocolServer();
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
        
        System.out.println("=== Secure Chat Server ===");
        System.out.println("Port: " + port);
        System.out.println("SSL/TLS: Enabled");
        System.out.println("Waiting for connections...\n");
        
        while (isRunning) {
            try {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("[ERROR] Connection error: " + e.getMessage());
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
        DataInputStream input = null;
        DataOutputStream output = null;
        String sessionId = null;
        
        try {
            socket.startHandshake();
            System.out.println("[CONNECTION] Client from " + socket.getInetAddress());
            
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());
            
            while (isRunning) {
                try {
                    // Read message header (10 bytes)
                    byte version = input.readByte();
                    byte typeCode = input.readByte();
                    int bodyLength = input.readInt();
                    int timestamp = input.readInt();
                    
                    // Validate body length
                    if (bodyLength < 0 || bodyLength > 10000) {
                        System.err.println("[ERROR] Invalid body length: " + bodyLength);
                        continue;
                    }
                    
                    // Read full message body
                    byte[] bodyBytes = new byte[bodyLength];
                    input.readFully(bodyBytes);
                    
                    // Reconstruct full message
                    byte[] fullMessage = new byte[10 + bodyLength];
                    fullMessage[0] = version;
                    fullMessage[1] = typeCode;
                    System.arraycopy(intToBytes(bodyLength), 0, fullMessage, 2, 4);
                    System.arraycopy(intToBytes(timestamp), 0, fullMessage, 6, 4);
                    System.arraycopy(bodyBytes, 0, fullMessage, 10, bodyLength);
                    
                    // Process message through protocol handler
                    sessionId = handleProtocolMessage(socket, fullMessage, output, sessionId);
                    
                } catch (EOFException e) {
                    System.out.println("[DISCONNECTION] Client closed connection");
                    break;
                }
            }
            
        } catch (Exception e) {
            System.err.println("[ERROR] Client handler error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cleanup session
            if (sessionId != null) {
                protocolHandler.removeSession(sessionId);
            }
            
            // Close resources
            try {
                if (input != null) input.close();
                if (output != null) output.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.err.println("[ERROR] Closing resources: " + e.getMessage());
            }
        }
    }
    
    /**
     * Processes a protocol message based on its type.
     * Delegates to ChatProtocolServer for protocol handling.
     * 
     * @param socket the client socket
     * @param messageData the raw message bytes
     * @param output output stream for responses
     * @param currentSessionId current session ID (null if not logged in)
     * @return session ID after processing
     */
    public String handleProtocolMessage(SSLSocket socket, byte[] messageData, 
                                       DataOutputStream output, String currentSessionId) {
        return protocolHandler.handleMessage(messageData, output, currentSessionId);
    }
    
    /**
     * Converts int to byte array (big-endian).
     * 
     * @param value integer to convert
     * @return 4-byte array
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
     * Shuts down the server gracefully.
     */
    public void shutdown() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("\n[SHUTDOWN] Server stopped");
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Shutdown: " + e.getMessage());
        }
    }
    
    /**
     * Main method to start the server.
     * 
     * @param args [port] [keystorePath] [keystorePassword]
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java SSL.SecureChatServer <port> <keystorePath> <password>");
            System.err.println("Example: java SSL.SecureChatServer 8443 server.jks password123");
            System.exit(1);
        }
        
        try {
            int port = Integer.parseInt(args[0]);
            SecureChatServer server = new SecureChatServer(port, args[1], args[2]);
            
            // Add shutdown hook for graceful termination
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutdown signal received...");
                server.shutdown();
            }));
            
            server.launch();
            
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid port number");
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
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
    
    /**
     * Constructs a ClientSession.
     * 
     * @param username the username
     * @param sessionId the session ID
     */
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
    
    /**
     * Constructs a ChatRoom.
     * 
     * @param roomId the room identifier
     */
    public ChatRoom(String roomId) {
        this.roomId = roomId;
        this.members = new ArrayList<>();
    }
    
    /**
     * Adds a member to the room.
     * 
     * @param session the client session to add
     */
    public void addMember(ClientSession session) {
        if (!members.contains(session)) {
            members.add(session);
        }
    }
    
    /**
     * Removes a member from the room.
     * 
     * @param session the client session to remove
     */
    public void removeMember(ClientSession session) {
        members.remove(session);
    }
    
    /**
     * Gets all members of the room.
     * 
     * @return copy of members list
     */
    public List<ClientSession> getMembers() {
        return new ArrayList<>(members);
    }
    
    /**
     * Gets the number of members in the room.
     * 
     * @return member count
     */
    public int getMemberCount() {
        return members.size();
    }
    
    public String getRoomId() { return roomId; }
}
