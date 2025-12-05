# TP-Java-For-Networks
**Authors:** Arthur Jouve & Ewan Zahra Thenault  
**Course:** 3RTS – Java for Networks
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
- Modify the main server to use threads instead of direct method calls (TCPServer file modified).



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

### Functionality Testing

<img width="1131" height="156" alt="Capture d’écran 2025-11-26 à 16 05 44" src="https://github.com/user-attachments/assets/a78b5d3d-e4b8-47b7-b71b-55ac1b2c6f8f" />

We can see that with a single client the connection is properly handle by the server. The client receives messages and if the client type "quit" it disconnect properly the client on the server side and does not crash. If we attempt to reconnect with the same client, it will have a different ID on the server side. The thread associate with the client is also cleaned up. 

The tests have been made for several clients and the server replies well to each client. Other clients cannot see the messages. 

### Robustness Testing

If we kill the client brutaly, the server handle perfectly the disconnection : 

<img width="349" height="60" alt="Capture d’écran 2025-12-03 à 13 51 01" src="https://github.com/user-attachments/assets/ea9ec4c4-62aa-47b7-8fda-52f76ad9a720" />

As we can see on the following screen, rapid consecutive connections is not a problem to connect to the server but we can't communicate any further after the connections.
<img width="2525" height="1071" alt="image" src="https://github.com/user-attachments/assets/bc4f740e-5570-4d75-8a93-37f369d0e344" />

If we have a look at the memory usage over time by using the monitoring implementation of the next part, we can see that for roughly 10 simulteous connections, the memory usage stabilise around 8500 kB. 

<img width="243" height="70" alt="Capture d’écran 2025-12-03 à 13 53 53" src="https://github.com/user-attachments/assets/7c9ed3bb-671f-4ff5-b276-f384312bd7c4" />


### Performance Metrics

If we try to connect more than 10 clients at the same time, the pool does not accept other connections. New connections are put in a "waiting line" and are waiting for a disconnection. 

On Client side : 

<img width="280" height="69" alt="Capture d’écran 2025-12-03 à 14 06 02" src="https://github.com/user-attachments/assets/5a0af747-9f0c-4829-b632-a355b21e53a3" />



# Secure Communication & Protocol Design



On Server side : 

<img width="356" height="185" alt="Capture d’écran 2025-12-03 à 14 07 02" src="https://github.com/user-attachments/assets/b15ef8c7-4bc2-4b28-97af-7552470efdce" />

If we try to see the response time, even with lot of clients connected, the server replies the echo within milliseconds. 



# Session 5
## Secure Communication & Protocol Design

Session Objective
Design and implement secure network communication using SSL/TLS and create custom applicationlayer protocols, building upon socket programming fundamentals to develop production-ready
network applications.

## 2 Practical Exercises
2.1 Exercise 1: Setting Up SSL/TLS Environment

2.1.2 Task 1.2: Understand Certificate Contents

<img width="1263" height="1111" alt="image" src="https://github.com/user-attachments/assets/f1de63ef-1a75-4401-aa12-b6ba7b722039" />

Analysis Questions:

1. What is the certificate’s owner name?

The Owner certificat is : CN=localhost, OU=Development, O=MyCompany, L=City, ST=State, C=FR

2. Who issued this certificate?

The issuer is the owner here: CN=localhost, OU=Development, O=MyCompany, L=City, ST=State, C=FR

3. What encryption algorithm was used?

Two algorithms are implemented: 

-Subject Public Key Algorithm: 2048-bit RSA key

-Signature algorithm name: SHA384withRSA

4. When does the certificate expire?

Valid from: Wed Dec 03 13:51:05 CET 2025 until: Thu Dec 03 13:51:05 CET 2026

## Exercise 2: SSL Server Implementation

This time, we are going to implement the SSL Server. Using what was previously done with TCP, loading SSL certificate from keystore file and by listenning for incoming SSL connections on configurable port, we can create the can create SSLTCPServer class.

If we start the SSL server on port 8443 and use OpenSSL command : 


<img width="650" height="526" alt="Capture d’écran 2025-12-03 à 15 56 31" src="https://github.com/user-attachments/assets/1a566daa-2106-4a16-b7b5-e20b39522b06" />

We can retrieve the certificate of the server (RSA encryption, the type of key...)

<img width="828" height="162" alt="Capture d’écran 2025-12-03 à 16 05 17" src="https://github.com/user-attachments/assets/7d5b5915-64d8-46ad-8bad-0f8df74837ff" />

Here we can also see that the handshake is successfull. 

If we send a message to the server, it replies to us : 

<img width="171" height="51" alt="Capture d’écran 2025-12-03 à 16 08 00" src="https://github.com/user-attachments/assets/04716c32-ee50-42e6-ad30-78c28e695eaa" />

## Exercise 3: SSL Client Implementation

This time, we are going to see the client side. The client needs to : 
• Connects to SSL server with proper certificate validation
• Supports both testing (trust all) and production modes (to accept self-signed certificates)
• Implements secure message exchange
• Handles SSL handshake errors appropriately


If we try to connect to the server using the testing mode : 

<img width="645" height="112" alt="Capture d’écran 2025-12-03 à 16 18 12" src="https://github.com/user-attachments/assets/99464569-0b8a-466e-a430-119f96ce1ef7" />

And we can see that on the server side, it accept the connection and print the name of the SSL cipher suite which is used : 

<img width="316" height="61" alt="Capture d’écran 2025-12-03 à 16 18 41" src="https://github.com/user-attachments/assets/97ab063f-c04b-45c0-87c6-b4e000b02d51" />


By running it with the production mode (no self-signed certificate) : 

<img width="856" height="85" alt="Capture d’écran 2025-12-03 à 16 15 01" src="https://github.com/user-attachments/assets/a503fb2e-7af4-4c9c-af09-6f52f84bc1d9" />

As the client can't locate the certificate, the server reject the certificate :

<img width="445" height="31" alt="Capture d’écran 2025-12-03 à 16 16 17" src="https://github.com/user-attachments/assets/c9771b45-eb63-419b-8c93-2f14f921d91a" />

## Exercise 5: Protocol Message Design


We first defined the message type in a dedicated file : 

<img width="341" height="168" alt="Capture d’écran 2025-12-03 à 16 45 12" src="https://github.com/user-attachments/assets/f3fbbe25-4558-40b2-b0af-d846b78bd2c2" />


Then we create a ChatMessage class with:
• Message type identifier
• Protocol version
• Timestamp
• Sender identifier
• Recipient information (for private messages)
• Message content
• Room identifier (for room-based messages)

And add different methods to it : 

- Serialization Method : Converts a ChatMessage object into a byte array for network transmission. The method transforms the complex object structure (with fields like sender, content, timestamp) into a compact binary format that combines a fixed-size binary header with a JSON body encoded in UTF-8. Format : Binary header (10 bytes) + JSON body (variable size). Header contains: version(1) + type(1) + length(4) + timestamp(4).

- Deserialization Method : Reconstructs a ChatMessage object from a byte array received over the network. This is the reverse process of serialization—it parses the binary header to extract metadata (version, type, length, timestamp) and then decodes the JSON body to rebuild the original message object.

- Character Encoding : Handles UTF-8 encoding to ensure proper representation of characters and special symbols.

- Message Validation : Adds checksums and length verification to detect corrupted or tampered messages. This includes validating that required fields are present, checking message size limits, verifying protocol versions match.

Everything is gathered in the ChatMessage class.



## Exercise 6: Protocol Server Implementation

Here follows on the screenshot three shells, on the left, there is the server, in the midle there is user1 (Arthur) and on the right, there is user2 (Ewan).
We can see that when a user connects itself to the server, it has to join (/join) a room to be able to speak with other personnes present in the same room.
The other users (outside of the room) don't receive these messages.

<img width="1025" height="517" alt="Capture d’écran 2025-12-05 à 14 28 31" src="https://github.com/user-attachments/assets/9782182e-c1d2-48f5-8e83-9d36428bcd50" />

On the screenshot up above, the user Arthur joins the room4. When he speaks no one receives any message as long as he is alone in the room.
When Ewan joins room4, he starts receiving messages. From now on, users can speak to one an other.

## Exercise 7: Security Testing 
# 4.1.1 SSL/TLS Configuration Testing

1. Test with any TLS version (1.2 or 1.3)

We have use TLS version 1.3 and it is working. Now let’s try to use TLS version 1.2. 

We launched the server normally and then connect to the client using openssl command : 

```
openssl s_client -connect localhost:8443 -tls1_2
```

<img width="601" height="319" alt="Capture d’écran 2025-12-05 à 15 12 49" src="https://github.com/user-attachments/assets/4c18449c-6425-438f-929c-09246615a2ac" />

The server replies to us its certificate and we can see that it support TLSv1.2 (here we cannot send messages because the client does not support the message format. 


2. Verify certificate validation works correctly

When we have connect to the server, we have seen that the following fields where replied by the server : notBefore=Dec  3 13:30:46 2025 GMT / notAfter=Dec  3 13:30:46 2026 GMT

It means that our certificate works correctly and is the one we’ve created on the 3 of December 2025.


3. Test connection with invalid certificates

Now if we generate a certificate with a validity period that has expired (start date = « -2d ») and we connect to the server using this certificate, we cannot connect to it has it return to us the error code 10 (certificate has expired).

<img width="394" height="169" alt="Capture d’écran 2025-12-05 à 15 13 34" src="https://github.com/user-attachments/assets/9a3d6fec-8d96-4c58-8b37-b2ae9109b472" />


4. Measure handshake performance

By using a light script with a for loop to create multiple openssl connections, we can compute the handshake time.

Here with a large number of connections, we have roughly 30-40ms of latency to connect.


<img width="211" height="235" alt="Capture d’écran 2025-12-05 à 15 14 10" src="https://github.com/user-attachments/assets/0b2fbb71-cc9f-4114-ad7b-4538d78a7785" />


# 4.1.2 Protocol Security Testing

1. Test message integrity (tamper detection)

If we connect and type every different types of commands, there is no error on server side : 


<img width="580" height="205" alt="Capture d’écran 2025-12-05 à 15 14 47" src="https://github.com/user-attachments/assets/04c6f8a9-b1c8-4706-bf45-e1d5b2d79c97" />


2. Verify no sensitive data in plaintext

For this one, we don’t know how we can verify this property. 


3. Test for common vulnerabilities

Our script is resistant to buffer overflow (see the test after). 

If we try to send wrong commands, it doesn’t work too.

4. Validate error handling doesn’t leak information

Even if we try to connect multiple clients, sending errors and multiple connections at the same time, the server does not crash and does not leak informations to non concerned users. 



## Exercise 8: Protocol Compliance Testing

As you can see on the following picture,
Serialisation/deserialisation works properly.
Special caracters are well dealt with.
<img width="1221" height="333" alt="image" src="https://github.com/user-attachments/assets/bb594b13-cd1a-4c3d-8864-f4d329ce8b69" />


Long messages can't be send to the server :

<img width="369" height="177" alt="Capture d’écran 2025-12-05 à 15 05 57" src="https://github.com/user-attachments/assets/d18f2f2e-cc97-4319-a3e6-da91714e6a7b" />
We tried to send 1000 "A", the server doesn't accepts the message.

