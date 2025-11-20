import java.net.*;
import java.io.*;
import java.io.IOException;


/**
 * A TCP server that accepts a single client connection, reads lines of text from the client,
 * 
 * prints them to the console, and sends an echo response back to the client.
 * 
 * @author Arthur Jouve & Ewan Zahra Thenault
 * @version 1.0
 */
public class TCPServer {
    private int port;
    private static final int DEFAULT_PORT = 8006;
    private ServerSocket serverSocket;
    
    /**
     * Constructs a TCPServer that listens on the specified port.
     * @param port the port number on which the server is listening
     */
    public TCPServer(int port) {
        this.port = port;
    }
    
    /**
     * Constructs a TCPServer that listens on the default port (here 8006).
     */
    public TCPServer() {
        this(DEFAULT_PORT);
    }
    
    
    /**
     * Launches the TCP server to accept a single client connection, read lines of text from the client,
     * print them to the console, and send an echo response back to the client.
     * @throws IOException
     */
    public void launch() throws IOException {
        // Créer une instance de ServerSocket
        serverSocket = new ServerSocket(port);
        System.out.println("TCP Server started on port " + port);
        
        // Wait for connection request
        System.out.println("Waiting for connection...");
        Socket socket = serverSocket.accept();
        
        // Accept the connection and get address of client
        System.out.println("[CONNECTION] " + socket.getInetAddress());
        
        // Get input stream from the socket
        InputStream inputStream = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        
        // Get output stream for responses
        OutputStream outputStream = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
        
        // Read lines from the client until disconnection
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println("[RECEIVED] from " + socket.getInetAddress() + " : " + line);
            
            // Awnser (echo the client) with its address and the same line
            writer.println("[ECHO] to " + socket.getInetAddress() + " : " + line);
        }
        
        System.out.println("[DISCONNECTION] " + socket.getInetAddress());
        socket.close();
        serverSocket.close();
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
     * Optionally takes a command-line argument for the port number.
     * If no argument is provided, the default port is used.
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
