import java.net.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Multithreaded TCP server that accepts multiple client connections concurrently.
 * Each client is handled in a separate thread for independent communication.
 * 
 * @author Arthur Jouve & Ewan Zahra Thenault
 * @version 1.0
 */
public class TCPServer {
    private int port;
    private static final int DEFAULT_PORT = 8006;
    private ServerSocket serverSocket;
    private static AtomicInteger clientCounter = new AtomicInteger(0);
    
    /**
     * Constructs a TCPServer that listens on the specified port.
     * @param port the port number on which the server is listening
     */
    public TCPServer(int port) {
        this.port = port;
    }
    
    /**
     * Constructs a TCPServer that listens on the default port.
     */
    public TCPServer() {
        this(DEFAULT_PORT);
    }
    
    /**
     * Launches the multithreaded TCP server.
     * Accepts multiple client connections and creates a thread for each.
     * 
     * @throws IOException if an I/O error occurs
     */
    public void launch() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Multithreaded TCP Server started on port " + port);
        System.out.println("Waiting for connections...");
        
        while (true) {
            // Accept new client connection
            Socket clientSocket = serverSocket.accept();
            
            // Generate unique client ID
            int clientId = clientCounter.incrementAndGet();
            
            // Create and start a new thread for this client
            ConnectionThread clientThread = new ConnectionThread(clientSocket, clientId);
            clientThread.start();
            
            // Display active thread count
            System.out.println("Active threads: " + (Thread.activeCount() - 1));
        }
    }
    
    /**
     * @return a string representation of the TCPServer.
     */
    @Override
    public String toString() {
        return "TCPServer listening on port " + port;
    }
    
    /**
     * Main method to start the TCP server.
     * 
     * @param args command-line arguments: args[0] is the optional port number
     */
    public static void main(String[] args) {
        try {
            TCPServer server;
            if (args.length > 0) {
                int port = Integer.parseInt(args[0]);
                server = new TCPServer(port);
            } else {
                server = new TCPServer();
            }
            System.out.println(server.toString());
            server.launch();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
