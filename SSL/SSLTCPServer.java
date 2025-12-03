package SSL;
import javax.net.ssl.*;
import java.io.*;
import java.security.*;

/**
 * SSL/TLS Server that handles secure client connections with certificate authentication.
 * 
 * @author Arthur Jouve & Ewan Zahra Thenault
 * @version 1.0
 */
public class SSLTCPServer {
    private int port;
    private SSLServerSocket serverSocket;
    private boolean isRunning;
    private String keystorePath;
    private String keystorePassword;
    
    /**
     * Constructs an SSLTCPServer with specified port and keystore configuration.
     * 
     * @param port the port to listen on
     * @param keystorePath path to the keystore file
     * @param password keystore password
     */
    public SSLTCPServer(int port, String keystorePath, String password) {
        this.port = port;
        this.keystorePath = keystorePath;
        this.keystorePassword = password;
        this.isRunning = false;
    }
    
    /**
     * Launches the SSL server and accepts client connections.
     * 
     * @throws Exception if server launch fails
     */
    public void launch() throws Exception {
        SSLContext sslContext = createSSLContext(keystorePath, keystorePassword);
        SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
        
        serverSocket = (SSLServerSocket) factory.createServerSocket(port);
        isRunning = true;
        
        System.out.println("SSL Server started on port " + port);
        System.out.println("Waiting for connections...");
        
        while (isRunning) {
            try {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                
                // Log connection attempt
                System.out.println("[CONNECTION] Client from " + clientSocket.getInetAddress());
                
                // Handle client in separate thread
                new Thread(() -> handleClient(clientSocket)).start();
                
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Creates SSL context by loading keystore and initializing key managers.
     * 
     * @param keystorePath path to keystore file
     * @param password keystore password
     * @return configured SSLContext
     * @throws Exception if context creation fails
     */
    private SSLContext createSSLContext(String keystorePath, String password) throws Exception {
        // Load keystore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        FileInputStream fis = new FileInputStream(keystorePath);
        keyStore.load(fis, password.toCharArray());
        fis.close();
        
        // Initialize KeyManagerFactory
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, password.toCharArray());
        
        // Create and initialize SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), null, new SecureRandom());
        
        return sslContext;
    }
    
    /**
     * Handles communication with a single client.
     * Performs SSL handshake and implements echo functionality.
     * 
     * @param clientSocket the SSL socket connected to the client
     */
    private void handleClient(SSLSocket clientSocket) {
        try {
            // Perform SSL handshake
            clientSocket.startHandshake();
            
            // Log handshake success
            SSLSession session = clientSocket.getSession();
            System.out.println("[HANDSHAKE] Protocol: " + session.getProtocol());
            System.out.println("[HANDSHAKE] Cipher: " + session.getCipherSuite());
            
            // Set up I/O streams
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
            PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true);
            
            // Send welcome message
            writer.println("Welcome to SSL Server");
            
            // Echo loop
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[RECEIVED] " + line);
                writer.println("[ECHO] " + line);
            }
            
            System.out.println("[DISCONNECTION] Client disconnected");
            
        } catch (IOException e) {
            System.err.println("[ERROR] Client error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }
    
    /**
     * Shuts down the server gracefully.
     */
    public void shutdown() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Server shutdown completed");
            }
        } catch (IOException e) {
            System.err.println("Error during shutdown: " + e.getMessage());
        }
    }
    
    /**
     * Main method to start the SSL server.
     * 
     * @param args [port] [keystorePath] [keystorePassword]
     */
    public static void main(String[] args) {
        if (args.length < 3) {
            System.err.println("Usage: java SSLTCPServer <port> <keystorePath> <keystorePassword>");
            System.err.println("Example: java SSLTCPServer 8443 server.jks password123");
            System.exit(1);
        }
        
        try {
            int port = Integer.parseInt(args[0]);
            String keystorePath = args[1];
            String password = args[2];
            
            SSLTCPServer server = new SSLTCPServer(port, keystorePath, password);
            
            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutdown signal received...");
                server.shutdown();
            }));
            
            server.launch();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
