import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class UDPClient {
    
    private static final int MAX_BYTES = 1024;
    
    private String host;
    private int port;
     private int sequenceNumber;
    
    // Constructor with host and port
    public UDPClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.sequenceNumber = 0;
    }
    
    public void launch() {
        try {
            // Create UDP socket
            DatagramSocket socket = new DatagramSocket();
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
                
                // Convert to UTF-8 bytes
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
        }
    }
    
    @Override
    public String toString() {
        return "UDPClient{host='" + host + "', port=" + port + "}";
    }
    
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
