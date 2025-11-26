# TP-Java-For-Networks
**Authors:** Arthur Jouve & Ewan Zahra Thenault  
**Course:** 3RTS – Java for Networks (TP #1)  
**Project:** UDP/TCP Chat Application with Java Sockets

## Table of Contents
- [Overview](#overview)
- [UDP Implementation](#udp-implementation)
  - [UDPServer](#udpserver)
  - [UDPClient](#udpclient)
- [TCP Implementation](#tcp-implementation)
  - [TCPServer](#tcpserver)
  - [TCPClient](#tcpclient)
  - [Testing & Results](#testing--results)
- [Further Improvements](#further-improvements)


---

## Overview

This project implements **Client-Servers** using UDP and TCP protocols in Java.

---

## UDP Implementation

The UDP implementation provides a connectionless and fast communication protocol suitable for applications where speed is prioritized.

#### Compilation

Navigate to the project directory and compile both UDP classes:

```
javac UDPServer.java
javac UDPClient.java
```

#### Running the Application

**Start the UDP Server**

Run the server with the default port (7565):

```
java UDPServer
```

Or specify a custom port:

```
java UDPServer <port_number>
```

**Connect the UDP Client**

In a new terminal, connect to the server :

```
java UDPClient <server_address> <port_number>
```

**Send Messages**

Type your message in the client terminal and press Enter. Each message is automatically prefixed with a sequence number


---

### UDPServer

#### Purpose
The UDPServer class creates a stateless UDP server that listens for incoming datagrams on a specified port. Unlike TCP, UDP does not establish a connection; it simply receives and processes datagrams as they arrive.

#### How It Works

1. **Socket Creation:** Creates a `DatagramSocket` bound to the specified port (default: 7565)
2. **Stateless Listening:** Waits for datagrams without establishing connections
3. **Datagram Reception:** Receives UDP packets using `DatagramSocket.receive()`
4. **Message Decoding:** Decodes received bytes as UTF-8 strings (max 1024 bytes)
5. **Display Output:** Prints client address and message to console 
6. **Continuous Operation:** Runs in infinite loop to receive multiple datagrams

#### Key Methods

- `UDPServer()`: Constructor using default port 7565
- `UDPServer(int port)`: Constructor with custom port
- `launch()`: Starts the server and begins listening for datagrams
- `toString()`: Returns server state information


---

### UDPClient

#### Purpose
The UDPClient class sends messages as UDP datagrams to a specified server. Each message is prefixed with a sequence number to track the order of transmission.

#### How It Works

1. **Socket Creation:** Creates a `DatagramSocket` (system assigns random port) 
2. **Address Resolution:** Resolves server hostname/IP using `InetAddress.getByName()`
3. **Console Input:** Reads user messages from standard input
4. **Sequence Numbering:** Adds sequence number prefix `[n]` to each message
5. **UTF-8 Encoding:** Encodes message as UTF-8 bytes 
6. **Size Management:** Truncates messages exceeding 1024 bytes
7. **Datagram Transmission:** Sends packet via `DatagramSocket.send()`


#### Key Methods

- `UDPClient(String host, int port)`: Constructor with server details
- `launch()`: Starts reading messages and sending datagrams
- `toString()`: Returns client configuration information


---

### Testing & Demonstrations

#### Successful Communication

The client sends messages with sequence numbers, and the server receives them:

**Client side:**

![UDP Client sending](https://github.com/user-attachments/assets/4cd8c64d-cb3c-426a-8601-be5111f47ee7)

**Server side:**

![UDP Server receiving](https://github.com/user-attachments/assets/72e0b59d-5890-4d0b-85e5-e7d02167d467)

#### Testing with netcat

You can test the UDP server without running the Java client by using `netcat`:

```
echo "hello du client" | nc -u localhost 7565
```

**Server receiving netcat message:**

![Server with netcat](https://github.com/user-attachments/assets/4ee4904c-b198-4811-bcca-05077e9bbd0a)

**netcat command:**

![netcat command](https://github.com/user-attachments/assets/d4c7501b-af3b-4b48-b82f-57f89c7055d8)

---


## TCP Implementation

The TCP implementation provides a reliable, connection-oriented communication between client and server with message echoing capabilities.

#### Compilation

Navigate to the project directory and compile both classes:

```
javac TCPServer.java
javac TCPClient.java
```

#### Running the Application

**Start the TCP Server**

Run the server with the default port (8006) :

```
java TCPServer
```

Or specify a custom port : 

```
java TCPServer <port_number>
```


**Connect the TCP Client**

In a new terminal, connect to the server :

```
java TCPClient <server_address> <port_number>
```


**Send Messages**

Type your message in the client terminal and press Enter. The server will echo back your message with connection information.



**Close the Connection**

To disconnect the client, press `CTRL+D`, the server will also stop.

---

### TCPServer

#### Purpose
The TCPServer class creates a TCP server that listens for incoming client connections on a specified port. It accepts one client at a time (then it will be several clients), receives messages, logs them, and sends echo responses back to the client.

#### How It Works

1. **Initialization:** Creates a `ServerSocket` bound to the specified port (default: 8006)
2. **Connection Accept:** Waits for a client connection using `accept()` method
3. **Communication Loop:** Continuously reads lines from the client using `BufferedReader`
4. **Echo Response:** For each received message, logs it and sends back an echo with the client's IP address
5. **Disconnection Handling:** Detects client disconnection when `readLine()` returns `null`
6. **Resource Cleanup:** Closes socket and server socket properly (resource cleanup)

#### Key Methods

- `TCPServer(int port)`: Constructor with custom port
- `TCPServer()`: Constructor using default port 8006
- `launch()`: Starts the server and handles client communication
- `toString()`: Returns server information string


---


### TCPClient

#### Purpose
The TCPClient class establishes a TCP connection to a server and enables bidirectional communication. It reads user input from the console, sends messages to the server, and displays server responses (echo).

#### How It Works

1. **Connection Establishment:** Creates a `Socket` to connect to the specified server address and port 
2. **Stream Setup:** Initializes input/output streams
3. **Console Input:** Reads user input line-by-line from the console using `Console.readLine()`
4. **Message Transmission:** Sends each line to the server via `PrintWriter`
5. **Response Handling:** Receives and displays server responses
6. **Graceful Shutdown:** Closes connection when user exits (CTRL+D)

#### Key Methods

- `TCPClient(String serverAddress, int serverPort)`: Constructor with server details (address + port)
- `launch()`: Establishes connection and handles communication loop
- `toString()`: Returns client connection information


---

## Testing & Results

### Successful Communication
The implementation supports multiple message exchanges in a single session :

![Multiple messages exchange](https://github.com/user-attachments/assets/1fa8e4e3-6ee0-491d-9ad2-82918e84b63b)

*Left: Client terminal | Right: Server terminal*

### Graceful Disconnection
When the client closes the connection (CTRL+D), the server detects the disconnection and terminates properly :

![Disconnection handling](https://github.com/user-attachments/assets/5aa0acf2-b71a-49e2-bb20-2798bdd639dd)

### Special Characters Support
The UTF-8 encoding ensures proper handling of special characters :

![Special characters](https://github.com/user-attachments/assets/b4be2869-1d17-45a0-80a2-27dc6fee1af2)

### Long Messages Handling
The implementation correctly processes messages of varying lengths :

![Long messages](https://github.com/user-attachments/assets/f9c16bf2-9420-4e43-a815-0ad5ba48446f)

### Error Handling: Wrong Port
Attempting to connect to an incorrect port results in connection refusal :

![Wrong port error](https://github.com/user-attachments/assets/f89691c4-58fa-4908-940b-6cf8b9cabaa1)

### Resource Cleanup Verification
Using `netstat`, we verified proper resource management (before the server start, after client connection, after shutdown). The server correctly releases the port after shutdown :

![Resource cleanup](https://github.com/user-attachments/assets/ea133f03-5e08-4851-bdb7-096191b9e5f5)

---


## Multithreaded architecture

The idea is now to transform the single-threaded TCP server into a multithreaded architecturecapable of handling multiple clients concurrently. 

**Single-threaded Limitations Analysis**

If we have a review to the previous TCP server file :


**- Where does serverSocket.accept() block?**

The method serverSocket.accept() blocks on the main thread, when the server waits for an incoming client connection. It waits indefinitely until a client connects.

**- How does your handleClient() method prevent new connections?**

In the single connection design, the handleClient() method processes the connected client synchronously on the main thread. During this client handling, the server does not accept new incoming connections. So, the server is "blocked" servicing a single client.

**- What happens when multiple clients try to connect simultaneously?**

Since the server accepts only one client at a time, if we try to connect two clients to the server, the second client wait undefinitely to send its message.


**- How is CPU utilization during I/O waits?**

The server's CPU utilization is low during I/O waits since both accept() and client input reads are blocking functions. CPU usage spikes only when data is available and processing occurs.

### Multithread implementation

Using the guide for the TP, we are able to : 

- Create a ConnectionThread.java file with proper constructor and run() method
- Move the Session 3 client handling logic into the run() method
- Modify the main server to use threads instead of direct method calls



## Testing & Results

4.1.3 Success Verification

Basic success:
<img width="2513" height="450" alt="image" src="https://github.com/user-attachments/assets/cee9145f-6911-45a1-b741-5dee951bce3e" />


We can see that a single client connects and communicates normally, the server shows connection message with correct client ID, client receives welcome message with assigned ID and echo functionality works as expected.


Finally "quit" command disconnects client cleanly: 
<img width="1535" height="55" alt="image" src="https://github.com/user-attachments/assets/727de1cf-5b15-4ec2-9578-98bb22aa7cc9" />

4.2 Exercise 2: Concurrent Client Testing
4.2.1 Testing Procedure

As we can see on this screen, several clients receive independant and correctly separated communications.
<img width="2550" height="1026" alt="image" src="https://github.com/user-attachments/assets/f4c0493d-b4de-4908-bdc1-d43caf9869bb" />
Messages from one client don’t block others.
Each client receives only their own echoes with correct client ID.
Server console shows messages from all clients interleaved.
Active thread count increases with each new connection.


4.3 Exercise 3: Thread Safety Validation
4.3.1 Race Condition Test

If we start a loop to create several clients (in one terminal), we can see that all clients are receiving a unique sequential ID (here 10 clients).

￼<img width="181" height="157" alt="9976 9977" src="https://github.com/user-attachments/assets/9f8afcdb-495c-4e8a-bc3d-5f6cd7f255f9" />


On the server side, it received all connections separately and there is no duplicate client ID in the logs.


￼
<img width="502" height="290" alt="Active threads 1" src="https://github.com/user-attachments/assets/fa804abf-3f97-495d-a9d8-cf8c95312167" />


When trying to run the command to make 50 connections to the server, none of the connections failed (Active threads : 50).

￼<img width="479" height="156" alt="client 45 connected from 127 0 0 1" src="https://github.com/user-attachments/assets/19f72e86-1e97-4719-aae4-d3354abaaaf0" />


Finally by using a pipe with a test message to be sent, it terminates the client. Thus we can see that all threads on server side are properly disconnected.

￼<img width="479" height="418" alt="152742" src="https://github.com/user-attachments/assets/81a7a0e2-5d67-4b51-9f1d-5a9fbca38672" />


## Thread Pool Solution

The idea is now to create a Thread Pool Solution.





## Testing and validation


<img width="1131" height="156" alt="Capture d’écran 2025-11-26 à 16 05 44" src="https://github.com/user-attachments/assets/a78b5d3d-e4b8-47b7-b71b-55ac1b2c6f8f" />

We can see that with a single client the connection is properly handle by the server. The client receives messages and if the client type "quit" it disconnect properly the client on the server side. If we attempt to reconnect with the same client, it will have a different ID on the server side. 

The tests have been made for several clients and the server replies well to each client. Other clients cannot see the messages. 

6.1.2 Robustness Testing

As we can see on the following screen, rapid consecutive connections is not a problem to connect to the server but we can't communicate any further after the connections.
<img width="2525" height="1071" alt="image" src="https://github.com/user-attachments/assets/bc4f740e-5570-4d75-8a93-37f369d0e344" />

