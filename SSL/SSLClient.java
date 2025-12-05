package SSL;

import javax.net.ssl.*;
import java.io.*;
import java.security.cert.X509Certificate;

/**
 * SSL/TLS client for secure communication with SSL servers.
 * Supports both testing mode (trust all certificates) and production mode (strict validation).
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
     * Constructs an SSLClient with connection parameters.
     * 
     * @param host the server hostname or IP address
     * @param port the server port number
     * @param trustAllCerts if true, accepts all certificates (testing mode only)
     */
    public SSLClient(String host, int port, boolean trustAllCerts) {
        this.host = host;
        this.port = port;
        this.trustAllCerts = trustAllCerts;
    }
    
    /**
     * Establishes SSL connection to the server and performs handshake.
     * Displays connection information including protocol version and cipher suite.
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
     * Creates SSL context with appropriate trust manager configuration.
     * In test mode, creates a trust manager that accepts all certificates.
     * In production mode, uses default system trust store.
     * 
     * @return configured SSLContext instance
     * @throws Exception if SSL context creation fails
     */
    private SSLContext createSSLContext() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        
        if (trustAllCerts) {
            // WARNING: Only for testing! Trusts all certificates without validation
            TrustManager[] trustAll = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
            };
            sslContext.init(null, trustAll, new java.security.SecureRandom());
        } else {
            // Production mode: use default trust store
            sslContext.init(null, null, new java.security.SecureRandom());
        }
        
        return sslContext;
    }
    
    /**
     * Closes the SSL socket connection gracefully.
     * Handles any IOExceptions during closure.
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
     * Main method to run the SSL client from command line.
     * Connects to server, exchanges text messages, and disconnects on 'quit'.
     * 
     * @param args command-line arguments: [host] [port] [--test]
     *             --test flag enables trust-all mode for self-signed certificates
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java SSLClient <host> <port> [--test]");
            System.err.println("Example: java SSLClient localhost 8443 --test");
            System.exit(1);
        }
        
        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            boolean testMode = (args.length > 2 && args[2].equals("--test"));
            
            SSLClient client = new SSLClient(host, port, testMode);
            client.connect();
            
            // Set up I/O streams
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader serverReader = new BufferedReader(
                new InputStreamReader(client.socket.getInputStream(), "UTF-8"));
            PrintWriter serverWriter = new PrintWriter(
                new OutputStreamWriter(client.socket.getOutputStream(), "UTF-8"), true);
            
            // Read welcome message from server
            String welcome = serverReader.readLine();
            if (welcome != null) {
                System.out.println(welcome);
            }
            
            System.out.println("Enter messages (type 'quit' to exit):");
            
            // Message exchange loop
            String line;
            while ((line = consoleReader.readLine()) != null) {
                serverWriter.println(line);
                
                if (line.equalsIgnoreCase("quit")) {
                    String response = serverReader.readLine();
                    if (response != null) {
                        System.out.println(response);
                    }
                    break;
                }
                
                String response = serverReader.readLine();
                if (response != null) {
                    System.out.println(response);
                }
            }
            
            client.disconnect();
            
        } catch (NumberFormatException e) {
            System.err.println("Error: Invalid port number");
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
