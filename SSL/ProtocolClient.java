package SSL;

import javax.net.ssl.*;
import java.io.*;
import java.security.cert.X509Certificate;

/**
 * Protocol client with full command-line interface.
 * Supports /login, /join, /msg, /users, /quit commands.
 * 
 * @author Arthur Jouve & Ewan Zahra Thenault
 * @version 1.0
 */
public class ProtocolClient {
    private SSLSocket socket;
    private String host;
    private int port;
    private DataInputStream input;
    private DataOutputStream output;
    private String username;
    private boolean isLoggedIn;
    private boolean isRunning;
    
    /**
     * Constructs a ProtocolClient.
     * 
     * @param host server hostname
     * @param port server port
     */
    public ProtocolClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.isLoggedIn = false;
        this.isRunning = true;
    }
    
    /**
     * Connects to server with SSL (trusts all certificates for testing).
     * 
     * @throws Exception if connection fails
     */
    public void connect() throws Exception {
        // Create SSL context that trusts all certificates (for testing)
        SSLContext sslContext = SSLContext.getInstance("TLS");
        TrustManager[] trustAll = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                public void checkServerTrusted(X509Certificate[] certs, String authType) { }
            }
        };
        sslContext.init(null, trustAll, new java.security.SecureRandom());
        
        SSLSocketFactory factory = sslContext.getSocketFactory();
        socket = (SSLSocket) factory.createSocket(host, port);
        socket.startHandshake();
        
        input = new DataInputStream(socket.getInputStream());
        output = new DataOutputStream(socket.getOutputStream());
        
        System.out.println("✓ Connected to " + host + ":" + port);
        System.out.println("✓ Protocol: " + socket.getSession().getProtocol());
        System.out.println();
        
        // Start listener thread for server messages
        new Thread(this::messageListener).start();
    }
    
    /**
     * Listens for incoming messages from server.
     */
    private void messageListener() {
        try {
            while (isRunning) {
                ChatMessage message = readResponse();
                
                // Affiche les messages selon le type
                switch (message.getMessageType()) {
                    case LOGIN_RESPONSE:
                    case JOIN_ROOM_REQUEST:
                    case USER_LIST_RESPONSE:
                    case ERROR_RESPONSE:
                        // Réponses aux commandes - affiche directement
                        System.out.println(message.getContent());
                        break;
                        
                    case TEXT_MESSAGE:
                    case PRIVATE_MESSAGE:
                        // Messages de chat - affiche sans modification
                        System.out.println(message.getContent());
                        break;
                        
                    default:
                        // Autres types
                        System.out.println("[Server]: " + message.getContent());
                }
            }
        } catch (EOFException e) {
            System.out.println("\n[Server closed connection]");
            isRunning = false;
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("[Connection error: " + e.getMessage() + "]");
            }
        }
    }
    
    /**
     * Sends login request.
     * 
     * @param username username to login with
     * @throws IOException if sending fails
     */
    public void login(String username) throws IOException {
        this.username = username;
        ChatMessage loginMsg = new ChatMessage(MessageType.LOGIN_REQUEST, username, "login");
        output.write(loginMsg.serialize());
        output.flush();
        isLoggedIn = true;
    }
    
    /**
     * Joins a chat room.
     * 
     * @param roomName room name to join
     * @throws IOException if sending fails
     */
    public void joinRoom(String roomName) throws IOException {
        if (!isLoggedIn) {
            System.out.println("Please login first with /login <username>");
            return;
        }
        
        ChatMessage msg = new ChatMessage(MessageType.JOIN_ROOM_REQUEST, username, roomName);
        output.write(msg.serialize());
        output.flush();
    }
    
    /**
     * Sends text message to current room.
     * 
     * @param content message content
     * @throws IOException if sending fails
     */
    public void sendMessage(String content) throws IOException {
        if (!isLoggedIn) {
            System.out.println("Please login first with /login <username>");
            return;
        }
        
        ChatMessage msg = new ChatMessage(MessageType.TEXT_MESSAGE, username, content);
        output.write(msg.serialize());
        output.flush();
    }
    
    /**
     * Sends private message to user.
     * 
     * @param recipient recipient username
     * @param content message content
     * @throws IOException if sending fails
     */
    public void sendPrivateMessage(String recipient, String content) throws IOException {
        if (!isLoggedIn) {
            System.out.println("Please login first with /login <username>");
            return;
        }
        
        ChatMessage msg = new ChatMessage(MessageType.PRIVATE_MESSAGE, username, 
                                         recipient + ":" + content);
        output.write(msg.serialize());
        output.flush();
    }
    
    /**
     * Requests list of users.
     * 
     * @throws IOException if sending fails
     */
    public void requestUsers() throws IOException {
        if (!isLoggedIn) {
            System.out.println("Please login first with /login <username>");
            return;
        }
        
        ChatMessage msg = new ChatMessage(MessageType.USER_LIST_REQUEST, username, "list");
        output.write(msg.serialize());
        output.flush();
    }
    
    /**
     * Reads response from server.
     * 
     * @return ChatMessage received from server
     * @throws IOException if reading fails
     */
    private ChatMessage readResponse() throws IOException {
        byte version = input.readByte();
        byte typeCode = input.readByte();
        int bodyLength = input.readInt();
        int timestamp = input.readInt();
        
        byte[] bodyBytes = new byte[bodyLength];
        input.readFully(bodyBytes);
        
        byte[] fullMessage = new byte[10 + bodyLength];
        fullMessage[0] = version;
        fullMessage[1] = typeCode;
        fullMessage[2] = (byte)(bodyLength >>> 24);
        fullMessage[3] = (byte)(bodyLength >>> 16);
        fullMessage[4] = (byte)(bodyLength >>> 8);
        fullMessage[5] = (byte)bodyLength;
        fullMessage[6] = (byte)(timestamp >>> 24);
        fullMessage[7] = (byte)(timestamp >>> 16);
        fullMessage[8] = (byte)(timestamp >>> 8);
        fullMessage[9] = (byte)timestamp;
        System.arraycopy(bodyBytes, 0, fullMessage, 10, bodyLength);
        
        return ChatMessage.deserialize(fullMessage);
    }
    
    /**
     * Disconnects from server.
     */
    public void disconnect() {
        isRunning = false;
        try {
            if (socket != null) socket.close();
            System.out.println("✓ Disconnected");
        } catch (IOException e) {
            System.err.println("Error closing: " + e.getMessage());
        }
    }
    
    /**
     * Main method with command-line interface.
     * 
     * @param args [host] [port]
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java SSL.ProtocolClient <host> <port>");
            System.err.println("Example: java SSL.ProtocolClient localhost 8443");
            System.exit(1);
        }
        
        try {
            ProtocolClient client = new ProtocolClient(args[0], Integer.parseInt(args[1]));
            client.connect();
            
            // Affiche les commandes dès la connexion
            System.out.println("=== Secure Chat Client ===");
            System.out.println("Commands:");
            System.out.println("  /login <username>        - Login to server");
            System.out.println("  /join <roomname>         - Join a chat room");
            System.out.println("  /msg <user> <message>    - Send private message");
            System.out.println("  /users                   - List active users");
            System.out.println("  /quit                    - Disconnect");
            System.out.println("  <text>                   - Send message to room");
            System.out.println();
            
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            
            String line;
            while ((line = console.readLine()) != null) {
                if (line.startsWith("/login ")) {
                    String username = line.substring(7).trim();
                    if (username.isEmpty()) {
                        System.out.println("Usage: /login <username>");
                        continue;
                    }
                    client.login(username);
                    // Petit délai pour recevoir la réponse
                    Thread.sleep(200);
                    
                } else if (line.startsWith("/join ")) {
                    String room = line.substring(6).trim();
                    if (room.isEmpty()) {
                        System.out.println("Usage: /join <roomname>");
                        continue;
                    }
                    client.joinRoom(room);
                    Thread.sleep(100);
                    
                } else if (line.startsWith("/msg ")) {
                    String[] parts = line.substring(5).split(" ", 2);
                    if (parts.length == 2) {
                        client.sendPrivateMessage(parts[0], parts[1]);
                    } else {
                        System.out.println("Usage: /msg <username> <message>");
                    }
                    
                } else if (line.equals("/users")) {
                    client.requestUsers();
                    Thread.sleep(100);
                    
                } else if (line.equals("/quit")) {
                    break;
                    
                } else if (line.startsWith("/")) {
                    System.out.println("Unknown command. Type /login, /join, /msg, /users, or /quit");
                    
                } else if (!line.trim().isEmpty()) {
                    client.sendMessage(line);
                }
            }
            
            client.disconnect();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
