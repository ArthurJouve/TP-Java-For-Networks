package SSL;

import javax.net.ssl.*;
import java.io.*;
import java.security.cert.X509Certificate;

public class SSLClient {
    private SSLSocket socket;
    private String host;
    private int port;
    private boolean trustAllCerts;
    
    public SSLClient(String host, int port, boolean trustAllCerts) {
        this.host = host;
        this.port = port;
        this.trustAllCerts = trustAllCerts;
    }
    
    public void connect() throws Exception {
        SSLContext sslContext = createSSLContext();
        SSLSocketFactory factory = sslContext.getSocketFactory();
        
        socket = (SSLSocket) factory.createSocket(host, port);
        socket.startHandshake();
        
        System.out.println("Connected to " + host + ":" + port);
        System.out.println("Protocol: " + socket.getSession().getProtocol());
        System.out.println("Cipher: " + socket.getSession().getCipherSuite());
    }
    
    private SSLContext createSSLContext() throws Exception {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        
        if (trustAllCerts) {
            TrustManager[] trustAll = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
            };
            sslContext.init(null, trustAll, new java.security.SecureRandom());
        } else {
            sslContext.init(null, null, new java.security.SecureRandom());
        }
        
        return sslContext;
    }
    
    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java SSLClient <host> <port> [--test]");
            System.exit(1);
        }
        
        try {
            String host = args[0];
            int port = Integer.parseInt(args[1]);
            boolean testMode = (args.length > 2 && args[2].equals("--test"));
            
            SSLClient client = new SSLClient(host, port, testMode);
            client.connect();
            
            // Streams
            BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader serverReader = new BufferedReader(
                new InputStreamReader(client.socket.getInputStream(), "UTF-8"));
            PrintWriter serverWriter = new PrintWriter(
                new OutputStreamWriter(client.socket.getOutputStream(), "UTF-8"), true);
            
            // Lit le message de bienvenue
            String welcome = serverReader.readLine();
            if (welcome != null) {
                System.out.println(welcome);
            }
            
            System.out.println("Enter messages (type 'quit' to exit):");
            
            String line;
            while ((line = consoleReader.readLine()) != null) {
                serverWriter.println(line);
                
                if (line.equalsIgnoreCase("quit")) {
                    String response = serverReader.readLine();
                    if (response != null) {
                        System.out.println(response);
                    }
                    break;
                }
                
                String response = serverReader.readLine();
                if (response != null) {
                    System.out.println(response);
                }
            }
            
            client.disconnect();
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
