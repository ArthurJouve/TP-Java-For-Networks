package SSL;

import javax.net.ssl.*;
import java.io.*;
import java.security.cert.X509Certificate;

/**
 * SSL Client that communicates using the chat protocol.
 * Sends messages with binary header + JSON body format.
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
    
    /**
     * Constructs a ProtocolClient.
     * 
     * @param host server hostname
     * @param port server port
     * @param username username for login
     */
    public ProtocolClient(String host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = username;
    }
    
    /**
     * Connects to the server with SSL and trusts all certificates (testing mode).
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
    }
    
    /**
     * Sends a login request to the server.
     * 
     * @throws IOException if login fails
     */
    public void login() throws IOException {
        System.out.println("→ Sending LOGIN_REQUEST for user: " + username);
        
        ChatMessage loginMsg = new ChatMessage(MessageType.LOGIN_REQUEST, username, "login");
        byte[] data = loginMsg.serialize();
        output.write(data);
        output.flush();
        
        // Read response
        ChatMessage response = readResponse();
        System.out.println("← Server: " + response.getContent());
        System.out.println();
    }
    
    /**
     * Sends a text message to the current room.
     * 
     * @param content message content
     * @throws IOException if sending fails
     */
    public void sendTextMessage(String content) throws IOException {
        ChatMessage msg = new ChatMessage(MessageType.TEXT_MESSAGE, username, content);
        byte[] data = msg.serialize();
        output.write(data);
        output.flush();
        System.out.println("→ Sent: " + content);
    }
    
    /**
     * Requests the list of active users.
     * 
     * @throws IOException if request fails
     */
    public void requestUserList() throws IOException {
        System.out.println("→ Requesting user list...");
        
        ChatMessage msg = new ChatMessage(MessageType.USER_LIST_REQUEST, username, "list");
        byte[] data = msg.serialize();
        output.write(data);
        output.flush();
        
        // Read response
        ChatMessage response = readResponse();
        System.out.println("← Active users: " + response.getContent());
    }
    
    /**
     * Reads a response message from the server.
     * 
     * @return the received ChatMessage
     * @throws IOException if reading fails
     */
    private ChatMessage readResponse() throws IOException {
        // Read header
        byte version = input.readByte();
        byte typeCode = input.readByte();
        int bodyLength = input.readInt();
        int timestamp = input.readInt();
        
        // Read body
        byte[] bodyBytes = new byte[bodyLength];
        input.readFully(bodyBytes);
        
        // Reconstruct message
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
     * Disconnects from the server.
     */
    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
                System.out.println("\n✓ Disconnected");
            }
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
    
    /**
     * Main method to run the protocol client.
     * 
     * @param args [host] [port] [username]
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java SSL.ProtocolClient <host> <port> <username>");
            System.err.println("Example: java SSL.ProtocolClient localhost 8443 arthur");
            System.exit(1);
        }
        
        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            String username = args[2];
            
            ProtocolClient client = new ProtocolClient(host, port, username);
            
            // Connect and login
            client.connect();
            client.login();
            
            // Interactive mode
            BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Commands:");
            System.out.println("  /users   - List active users");
            System.out.println("  /quit    - Disconnect");
            System.out.println("  <text>   - Send text message");
            System.out.println();
            
            String line;
            while ((line = console.readLine()) != null) {
                if (line.equalsIgnoreCase("/quit")) {
                    break;
                } else if (line.equalsIgnoreCase("/users")) {
                    client.requestUserList();
                } else if (!line.trim().isEmpty()) {
                    client.sendTextMessage(line);
                }
            }
            
            client.disconnect();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
