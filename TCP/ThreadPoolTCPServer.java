package TCP;
import java.net.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Enhanced TCP server using thread pool for better resource management.
 * Limits concurrent threads to prevent resource exhaustion under high load.
 * 
 * @author Arthur Jouve & Ewan Zahra Thenault
 * @version 2.0
 */
public class ThreadPoolTCPServer {
    private int port;
    private static final int DEFAULT_PORT = 8006;
    private static final int THREAD_POOL_SIZE = 10;
    private ExecutorService threadPool;
    private ServerSocket serverSocket;
    private static AtomicInteger clientCounter = new AtomicInteger(0);
    private static AtomicInteger activeClients = new AtomicInteger(0);
    private volatile boolean running = true;
    
    /**
     * Constructs a ThreadPoolTCPServer that listens on the specified port.
     * @param port the port number on which the server is listening
     */
    public ThreadPoolTCPServer(int port) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }
    
    /**
     * Constructs a ThreadPoolTCPServer that listens on the default port.
     */
    public ThreadPoolTCPServer() {
        this(DEFAULT_PORT);
    }
    
    /**
     * Launches the thread pool TCP server.
     * Accepts client connections and submits them to the thread pool.
     * 
     * @throws IOException if an I/O error occurs
     */
    public void launch() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Thread Pool TCP Server started on port " + port);
        System.out.println("Thread pool size: " + THREAD_POOL_SIZE);
        System.out.println("Waiting for connections...");
        
        while (running) {
            try {
                // Accept new client connection
                Socket clientSocket = serverSocket.accept();
                
                // Generate unique client ID (thread-safe)
                int clientId = clientCounter.incrementAndGet();
                
                System.out.println("[CONNECTION] Client " + clientId + " from " + clientSocket.getInetAddress());
                
                // Increment active clients counter
                activeClients.incrementAndGet();
                
                // Submit task to thread pool
                threadPool.execute(() -> {
                    try {
                        handleClient(clientSocket, clientId);
                    } finally {
                        // Decrement counter when client handler finishes
                        activeClients.decrementAndGet();
                    }
                });
                
                // Display pool statistics
                printThreadStats();
                
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting connection: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Handles communication with a single client.
     * Executed by a thread from the pool.
     * 
     * @param clientSocket the socket connected to the client
     * @param clientId unique identifier for this client
     */
    private void handleClient(Socket clientSocket, int clientId) {
        System.out.println("[" + new java.util.Date() + "] Client " + clientId + " handler started");
        
        try (
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(), "UTF-8"), true)
        ) {
            // Send welcome message
            writer.println("Welcome! You are client #" + clientId);
            
            // Echo loop
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[RECEIVED] Client " + clientId + ": " + line);
                
                // Handle quit command
                if (line.equalsIgnoreCase("quit")) {
                    writer.println("Goodbye client #" + clientId);
                    break;
                }
                
                // Echo message back
                writer.println("[ECHO] Client " + clientId + ": " + line);
            }
            
        } catch (IOException e) {
            System.err.println("Client " + clientId + " error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                System.out.println("[" + new java.util.Date() + "] Client " + clientId + " disconnected");
            } catch (IOException e) {
                System.err.println("Error closing socket for client " + clientId);
            }
        }
    }
    
    /**
     * Displays current thread pool statistics.
     */
    private void printThreadStats() {
        Runtime runtime = Runtime . getRuntime () ;
        System . out . println ("=== Thread Statistics ===") ;
        System . out . println (" Active threads : " + ( Thread . activeCount () - 1) ) ;
        System . out . println (" Memory usage : " +( runtime . totalMemory () - runtime . freeMemory () ) / 1024 + " KB") ;
        System.out.println("===============================");
    }
    
    /**
     * Gracefully shuts down the server and thread pool.
     */
    public void shutdown() {
        System.out.println("Initiating server shutdown...");
        running = false;
        
        // Close server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
        
        // Shutdown thread pool
        threadPool.shutdown();
        try {
            // Wait for existing tasks to complete
            if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                // Force shutdown if tasks don't complete in time
                threadPool.shutdownNow();
            }
            System.out.println("Server shutdown completed");
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * @return a string representation of the ThreadPoolTCPServer.
     */
    @Override
    public String toString() {
        return "ThreadPoolTCPServer listening on port " + port + " (pool size: " + THREAD_POOL_SIZE + ")";
    }
    
    /**
     * Main method to start the thread pool TCP server.
     * 
     * @param args command-line arguments: args[0] is the optional port number
     */
    public static void main(String[] args) {
        ThreadPoolTCPServer server;
        
        try {
            if (args.length > 0) {
                int port = Integer.parseInt(args[0]);
                server = new ThreadPoolTCPServer(port);
            } else {
                server = new ThreadPoolTCPServer();
            }
            
            System.out.println(server.toString());
            
            // Add shutdown hook for graceful termination
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nShutdown signal received...");
                server.shutdown();
            }));
            
            server.launch();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
