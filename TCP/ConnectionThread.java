package TCP;
import java.net.*;
import java.io.*;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread class that handles communication with a single TCP client.
 * Each thread manages one client connection independently.
 * 
 * @author Arthur Jouve & Ewan Zahra Thenault
 * @version 1.0
 */
public class ConnectionThread extends Thread {
    private Socket clientSocket;
    private int clientId;
    private static AtomicInteger clientCounter = new AtomicInteger(0);
    
    /**
     * Constructs a ConnectionThread for a client connection.
     * 
     * @param clientSocket the socket connected to the client
     * @param clientId unique identifier for this client
     */
    public ConnectionThread(Socket clientSocket, int clientId) {
        this.clientSocket = clientSocket;
        this.clientId = clientId;
        this.setName("ClientHandler-" + clientId);
    }
    
    /**
     * Runs the client communication logic.
     * Handles echo messages until client disconnects.
     */
    @Override
    public void run() {
        System.out.println("[" + new Date() + "] Client " + clientId + " connected from " + clientSocket.getInetAddress());
        
        BufferedReader reader = null;
        PrintWriter writer = null;
        
        try {
            
            // Set up input/output streams
            InputStream inputStream = clientSocket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            
            OutputStream outputStream = clientSocket.getOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
            
            // Send welcome message with client ID
            writer.println("Hello! You are client #" + clientId);
            
            // Echo loop - read and echo messages
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[RECEIVED] Client " + clientId + ": " + line);
                
                // Handle quit command
                if (line.equalsIgnoreCase("quit")) {
                    writer.println("Goodbye client #" + clientId);
                    break;
                }
                
                // Echo the message back
                writer.println("[ECHO] Client " + clientId + ": " + line);
            }
            
        } catch (IOException e) {
            System.err.println("Client " + clientId + " error: " + e.getMessage());
        } finally {
            cleanup();
            System.out.println("[" + new Date() + "] Client " + clientId + " disconnected");
        }
    }
    
    /**
     * Closes all resources properly.
     */
    private void cleanup() {
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket for client " + clientId + ": " + e.getMessage());
        }
    }
}
