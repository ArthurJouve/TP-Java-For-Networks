import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

public class UDPServer {

    // Constants
    private static final int DEFAULT_PORT = 7565;     // pick any free port by default
    private static final int MAX_BYTES = 1024;        // max accepted UTF-8 bytes

    // State
    private final int port;
    private DatagramSocket socket;
    private boolean running = false;

    // Default constructor
    public UDPServer() {
        this(DEFAULT_PORT);
    }

    // Constructor with explicit port
    public UDPServer(int port) {
        this.port = port;
    }

    // Starts the server (no threads yet)
    public void launch() {
        try {
            socket = new DatagramSocket(new InetSocketAddress(port));
            running = true;
            System.out.println("[INFO] UDPServer listening on port " + port);

            // We’ll use a buffer large enough for typical UDP payloads, then truncate ourselves.
            // 2048 gives us room to detect and cut down to 1024 safely.
            byte[] recvBuf = new byte[2048];

            // UTF-8 decoder that ignores malformed/truncated sequences at the end when we cut to 1024
            CharsetDecoder decoder = StandardCharsets.UTF_8
                    .newDecoder()
                    .onMalformedInput(CodingErrorAction.IGNORE)
                    .onUnmappableCharacter(CodingErrorAction.IGNORE);

            while (true) {
                DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet); // blocks until a datagram arrives

                // Actual bytes received in this packet
                int receivedLen = packet.getLength();

                // Truncate to MAX_BYTES if needed
                int toDecode = Math.min(receivedLen, MAX_BYTES);

                // Decode only the first 'toDecode' bytes as UTF-8
                decoder.reset();
                ByteBuffer byteSlice = ByteBuffer.wrap(packet.getData(), 0, toDecode);
                String message = decoder.decode(byteSlice).toString();

                String client = packet.getAddress().getHostAddress() + ":" + packet.getPort();
                System.out.println(client + " -> " + message);
            }

        } catch (SocketException se) {
            System.err.println("[ERROR] Could not open socket on port " + port + ": " + se.getMessage());
        } catch (IOException ioe) {
            System.err.println("[ERROR] I/O error: " + ioe.getMessage());
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            running = false;
        }
    }

    // A description of the server’s state
    @Override
    public String toString() {
        return "UDPServer{port=" + port + ", running=" + running + "}";
    }

    // Entry point: java UDPServer 8080
    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        // Parse optional port from args[0]
        if (args != null && args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
                if (port < 1 || port > 65535) {
                    System.err.println("[WARN] Invalid port. Using default: " + DEFAULT_PORT);
                    port = DEFAULT_PORT;
                }
            } catch (NumberFormatException nfe) {
                System.err.println("[WARN] Could not parse port. Using default: " + DEFAULT_PORT);
                port = DEFAULT_PORT;
            }
        }

        UDPServer server = new UDPServer(port);
        System.out.println(server.toString());
        server.launch(); // blocking loop
    }
}
