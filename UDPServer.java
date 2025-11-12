import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * UDP server that receives datagrams from clients and displays messages in the terminal (UTF-8 encoded strings up to 1024 bytes).
 * 
 * @author Arthur Jouve & Ewan Zahra Thenault
 * @version 1.0
 */
public class UDPServer {
    
    /** Default listening port */
    private static final int DEFAULT_PORT = 7565;
    
    /** Maximum size for received messages */
    private static final int MAX_BYTES = 1024;
    
    /** Listening port number */
    private int port;
    /** Status of the server (running or not) */
    private boolean running;
    
    /**
     * Constructs a UDPServer with the default port.
     * Initial state of the server : not running.
     */
    public UDPServer() {
        this(DEFAULT_PORT);
    }
    
    /**
     * Constructs a UDPServer with a specified port.
     * Initial state of the server : not running.
     * 
     * @param port port number to listen on
     */
    public UDPServer(int port) {
        this.port = port;
        this.running = false;
    }
    
    /**
     * Starts UDP server and listen for incoming datagrams.
     * 
     * Runs in an infinite loop, receiving datagrams from clients, decoding them as UTF-8 strings, 
     * and displaying them on standard output.
     * 
     * This method blocks indefinitely until an error occurs.
     */
    public void launch() {
        try {
            // Create UDP socket
            DatagramSocket socket = new DatagramSocket(port);
            running = true;
            System.out.println("UDP Server started on port " + port);
            System.out.println(this);// Display state after starting
            
            // Buffer for incoming data
            byte[] buffer = new byte[MAX_BYTES];
            
            while (true) {
                // Packet to receive data
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                // Length of received data
                int length = Math.min(packet.getLength(), MAX_BYTES);
                
                // UTF-8 decoding
                String message = new String(packet.getData(), 0, length, "UTF-8");
                
                // Display the client address and message
                String clientAddress = packet.getAddress().getHostAddress() + ":" + packet.getPort();
                System.out.println(clientAddress + " -> " + message);
                
                // Reset the buffer
                buffer = new byte[MAX_BYTES];
                socket.close();
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
     * Main method
     * 
     * Accepts an optional port number as the first command-line argument arg[0] (if no port provided, uses default port).
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
