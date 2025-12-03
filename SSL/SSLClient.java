package SSL;
import javax.net.ssl.*;
import java.io.*;
import java.security.cert.X509Certificate;

/**
 * SSL Client that connects to an SSL server with support for testing and production modes.
 * 
 * @author Arthur Jouve & Ewan Zahra Thenault
 * @version 1.0
 */
public class SSLClient {
    private SSLSocket socket;
    private String host;
    private int port;
    private boolean trustAllCerts;
    
    /**
     * Constructs an SSLClient with specified host, port and trust mode.
     * 
     * @param host the server hostname
     * @param port the server port
     * @param trustAllCerts true for testing mode (trust all), false for production
     */
    public SSLClient(String host, int port, boolean trustAllCerts) {
        this.host = host;
        this.port = port;
        this.trustAllCerts = trustAllCerts;
    }
    
    /**
     * Connects to the SSL server and performs handshake.
     * 
     * @throws Exception if connection or handshake fails
     */
    public void connect() throws Exception {
        SSLContext sslContext = createSSLContext();
        SSLSocketFactory factory = sslContext.getSocketFactory();
        
        socket = (SSLSocket) factory.createSocket(host, port);
        socket.startHandshake();
        
        System.out.println("Connected to " + host + ":" + port);
        System.out.println("Protocol: " + socket.getSession().getProtocol());
        System.out.println("Cipher: " + socket.getSession().getCipherSuite());
    }
    
    /**
     * Creates SSL context based on trust mode.
     * 
     * @return configured SSLContext
     * @throws Exception if context creation fails
     */
    private SSLContext createSSLContext() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        
        if (trustAllCerts) {
            // Testing mode: trust all certificates
            TrustManager[] trustAll = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
            };
            sslContext.init(null, trustAll, new java.security.SecureRandom());
        } else {
            // Production mode: validate certificates
            sslContext.init(null, null, new java.security.SecureRandom());
        }
        
        return sslContext;
    }
    
    /**
     * Sends a message to the server.
     * 
     * @param message the message to send
     * @throws IOException if sending fails
     */
    public void sendMessage(String message) throws IOException {
        PrintWriter writer = new PrintWriter(
            new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        writer.println(message);
    }
    
    /**
     * Receives a response from the server.
     * 
     * @return the server response
     * @throws IOException if receiving fails
     */
    public String receiveResponse() throws IOException {
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(socket.getInputStream(), "UTF-8"));
        return reader.readLine();
    }
    
    /**
     * Disconnects from the server.
     */
    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
    
    /**
     * Main method to test the SSL client.
     * 
    * @param args [host] [port] [--test for testing mode]
     */
    public static void main(String[] args) {
    if (args.length < 2) {
        System.err.println("Usage: java SSLClient <host> <port> [--test]");
        System.exit(1);
    }
    
    try {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        boolean testMode = (args.length > 2 && args[2].equals("--test"));
        
        SSLClient client = new SSLClient(host, port, testMode);
        client.connect();
        
        // Mode interactif
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        BufferedReader serverReader = new BufferedReader(
            new InputStreamReader(client.socket.getInputStream(), "UTF-8"));
        PrintWriter serverWriter = new PrintWriter(
            new OutputStreamWriter(client.socket.getOutputStream(), "UTF-8"), true);
        
        // Lit le message de bienvenue
        String welcome = serverReader.readLine();
        if (welcome != null) {
            System.out.println(welcome);
        }
        
        System.out.println("Enter messages (type 'quit' to exit):");
        
        String line;
        while ((line = consoleReader.readLine()) != null) {
            serverWriter.println(line);
            
            if (line.equalsIgnoreCase("quit")) {
                break;
            }
            
            String response = serverReader.readLine();
            if (response != null) {
                System.out.println(response);
            }
        }
        
        client.disconnect();
        
    } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
        e.printStackTrace();
    }
}

}
