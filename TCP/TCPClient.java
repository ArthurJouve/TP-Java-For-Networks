import java.net.*;
import java.io.*;

/**
 * A TCP client that reads text lines from standard input and sends them to a specified server.
 * 
 * Read the response from the server and prints it to the console (echoed messages).
 * 
 * @author Arthur Jouve & Ewan Zahra Thenault
 * @version 1.0
 */
public class TCPClient {
    private String serverAddress;
    private int serverPort;
    private Socket socket;
    
    
    /**
     * Constructs a TCPClient with specified server address and port.
     * @param serverAddress address of the server
     * @param serverPort port number of the server
     */
    public TCPClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }
    
    /**
     * Launches the TCP client to connect to the server, read lines from standard input, send them to the server, and print the server's responses.
     * @throws IOException
     */
    public void launch() throws IOException {

        // Establish connection to the server
        socket = new Socket(serverAddress, serverPort);
        System.out.println("Connected to server " + serverAddress + ":" + serverPort);
        
        // Get output and imput stream of the server
        OutputStream outputStream = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
        
        InputStream inputStream = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        
        // Read from the console
        Console console = System.console();
        if (console == null) {
            System.err.println("No console available");
            socket.close();
            return;
        }
        
        String line;

        // Read and print the welcome message from the server
        String welcomeMessage = reader.readLine();
        if (welcomeMessage != null) {
            System.out.println(welcomeMessage);
        }
        
        // Read lines from the console, send to server, and print server responses (exept if CTRL+D is pressed)
        while ((line = console.readLine()) != null) {
            // Send the line to the server
            writer.println(line);
            
            // Read and print the server's response
            String response = reader.readLine();
            if (response != null) {
                System.out.println(response);
            }
        }
        
        // Close the connection
        socket.close();
        System.out.println("Connection closed");
    }
    

    /**
     * @return string representation of what the TCPClient is connected to.
     */
    @Override
    public String toString() {
        return "TCPClient connected to " + serverAddress + ":" + serverPort;
    }
    


    /**
     * Main method to run the TCPClient.
     * @param args arg[0] = server address, arg[1] = server port
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java TCPClient <server_address> <port>");
            System.exit(1);
        }
        
        try {
            String serverAddress = args[0];
            int serverPort = Integer.parseInt(args[1]);
            
            TCPClient client = new TCPClient(serverAddress, serverPort);
            System.out.println(client.toString());
            client.launch();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
