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


## Further Improvements

To be done on next sessions.



