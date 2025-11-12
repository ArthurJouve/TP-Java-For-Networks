import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * UDP server that receives datagrams from clients and displays messages in the terminal (UTF-8 encoded strings up to 1024 bytes).
 * 
 * @author Your Name
 * @version 1.0
 */
public class UDPServer {
    
    /** Default listening port for the server */
    private static final int DEFAULT_PORT = 7565;
    
    /** Maximum size in bytes for received messages */
    private static final int MAX_BYTES = 1024;
    
    /** The port number on which the server listens */
    private int port;
    
    /** Indicates whether the server is currently running */
    private boolean running;
    
    /**
     * Constructs a UDPServer with the default port.
     * The server is initially not running.
     */
    public UDPServer() {
        this(DEFAULT_PORT);
    }
    
    /**
     * Constructs a UDPServer with a specified port.
     * The server is initially not running.
     * 
     * @param port the port number on which the server will listen
     */
    public UDPServer(int port) {
        this.port = port;
        this.running = false;
    }
    
    /**
     * Starts the UDP server and begins listening for incoming datagrams.
     * 
     * The server runs in an infinite loop, receiving datagrams from clients,
     * decoding them as UTF-8 strings (truncated to MAX_BYTES if necessary),
     * and displaying them on standard output prefixed with the client's address.
     * 
     * This method blocks indefinitely until an error occurs.
     */
    public void launch() {
        try {
            // Create UDP socket
            DatagramSocket socket = new DatagramSocket(port);
            running = true;
            System.out.println("UDP Server started on port " + port);
            System.out.println(this); // Display state after starting
            
            // Buffer to receive incoming data
            byte[] buffer = new byte[MAX_BYTES];
            
            while (true) {
                // Create a packet to receive data
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                
                // Receive the packet (blocking)
                socket.receive(packet);
                
                // Get the actual length of received data
                int length = Math.min(packet.getLength(), MAX_BYTES);
                
                // Decode the message in UTF-8
                String message = new String(packet.getData(), 0, length, "UTF-8");
                
                // Display the client address and message
                String clientAddress = packet.getAddress().getHostAddress() + ":" + packet.getPort();
                System.out.println(clientAddress + " -> " + message);
                
                // Reset the buffer for next reception
                buffer = new byte[MAX_BYTES];
            }
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        } 
    }
    
    /**
     * Returns a string representation of the server's state.
     * 
     * @return a string describing the server's port and running status
     */
    @Override
    public String toString() {
        return "UDPServer{port=" + port + ", running=" + running + "}";
    }
    
    /**
     * Main method to start the UDP server.
     * 
     * Accepts an optional port number as the first command-line argument.
     * If no port is provided or if the port is invalid, the default port is used.
     * 
     * @param args command-line arguments; args[0] can specify the port number
     */
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        
        // Check arg[0] port number
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port --> default port used: " + DEFAULT_PORT);
            }
        }
        
        UDPServer server = new UDPServer(port);
        System.out.println(server); // Display state before starting
        server.launch();
    }
}
