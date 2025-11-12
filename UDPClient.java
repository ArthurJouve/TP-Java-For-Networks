import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Console;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class UDPClient {

    private static final int MAX_BYTES = 1024;

    private final String host;
    private final int port;

    public UDPClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress addr = InetAddress.getByName(host);

            Console console = System.console();
            BufferedReader reader = null;
            if (console == null) {
                // Fallback quand Console est null (fréquent sous WSL/IDE)
                reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
                System.out.println("[INFO] Tapez vos messages puis Entrée. EOF: Ctrl+D. Quitter: /quit");
            } else {
                System.out.println("[INFO] Tapez vos messages puis Entrée. Quitter: /quit");
            }

            while (true) {
                String line;
                if (console != null) {
                    line = console.readLine("> ");
                } else {
                    line = reader.readLine();
                }

                if (line == null) {
                    System.out.println("[INFO] EOF détecté. Fermeture du client.");
                    break;
                }
                if ("/quit".equalsIgnoreCase(line.trim())) {
                    System.out.println("[INFO] Quit.");
                    break;
                }

                byte[] data = line.getBytes(StandardCharsets.UTF_8);
                if (data.length > MAX_BYTES) {
                    System.out.println("[WARN] Message > 1024 octets en UTF-8. Troncature.");
                    byte[] truncated = new byte[MAX_BYTES];
                    System.arraycopy(data, 0, truncated, 0, MAX_BYTES);
                    data = truncated;
                    // NB: si on coupe une séquence UTF‑8, le serveur doit décoder en tolérant la fin partielle.
                }

                DatagramPacket packet = new DatagramPacket(data, data.length, addr, port);
                socket.send(packet);
            }
        } catch (IOException e) {
            System.err.println("[ERROR] " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "UDPClient{" + "host='" + host + '\'' + ", port=" + port + '}';
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
        client.run();
    }
}
