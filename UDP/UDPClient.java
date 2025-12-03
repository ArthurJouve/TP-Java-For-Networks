package UDP;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * A UDP client that reads text lines from standard input and sends them as datagrams to a specified server.
 * 
 * Each message is prefixed with a sequence number to track the order of
 * messages sent. Messages are encoded in UTF-8 and truncated to 1024 bytes if necessary.
 * 
 * @author Arthur Jouve & Ewan Zahra Thenault
 * @version 1.0
 */
public class UDPClient {
    
    /** Maximum size in bytes for messages to be sent */
    private static final int MAX_BYTES = 1024;
    
    /** Hostname or IP address of the server */
    private String host;
    
    /** Listening port number */
    private int port;
    
    /** Sequence number for tracking messages sent */
    private int sequenceNumber;
    
    /**
     * Constructs a UDPClient with a specified host and port. Sequence number is initialized to 0.
     * 
     * @param host the hostname or IP address of the server
     * @param port the port number on which the server is listening
     */
    public UDPClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.sequenceNumber = 0;
    }
    
    /**
     * Starts the UDP client and begins reading messages.
     * 
     * The client runs in an infinite loop, reading lines from the keyboard, adding a sequence number to each message, encoding it in UTF-8,
     * (truncating it if necessary), and sending it as a UDP datagram to the server.
     * 
     * The sequence number is incremented after each message is sent.
     * This method blocks indefinitely until an error occurs.
     */
    public void launch() {
        DatagramSocket socket = null;
        try {
            // Create UDP socket
            socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(host);
            
            // Read from keyboard
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Enter your messages:");
            
            while (true) {
                // Read line
                String message = reader.readLine();

                // Add sequence number to message
                String messageWithSeq = "[" + sequenceNumber + "] " + message;
                sequenceNumber++;

                byte[] data = messageWithSeq.getBytes("UTF-8");
                
                // Truncate if exceeds 1024 bytes
                if (data.length > MAX_BYTES) {
                    byte[] truncated = new byte[MAX_BYTES];
                    System.arraycopy(data, 0, truncated, 0, MAX_BYTES);
                    data = truncated;
                }
                
                // Create and send the packet
                DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                socket.send(packet);
                
            }
            
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close(); 
        }
    }
}
    /**
     * Returns a string representation of the client's configuration.
     * 
     * @return a string describing the client's host and port
     */
    @Override
    public String toString() {
        return "UDPClient{host='" + host + "', port=" + port + "}";
    }
    
    /**
     * Main method to start the UDP client.
     * 
     * Requires two command-line arguments: the server's hostname (or IP address)
     * and the port number.
     * 
     * @param args command-line arguments: args[0] is the host, args[1] is the port
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java UDPClient <host> <port>");
            System.exit(1);
        }
        
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        
        UDPClient client = new UDPClient(host, port);
        System.out.println(client);
        client.launch();
    }
}
