# TP-Java-For-Networks
TP project : UDP/TCP Chat App with Java sockets. From Arthur Jouve &amp; Ewan Zahra Thenault



TP #1 - room D055 UDP/TCP Chat App with Java sockets

1 – Creating a UDP Client-Server

1.1 UDP Server

We created a class UDPServer implementing four methods. 
UDPServer(DefaultPort) is a constructor using the Default Port 7565. 
UDPServer(port) is a constructor using the port chosen by the user.
Launch is the method which first creates a socket using the java.net.DatagramSocket module. Then puts the boolean running at true And enters in an infinite loop which receives the packet, decodes it and displays the message of the client.
Finally the method toString simply displays the Server information in the console.

<img width="832" height="497" alt="image" src="https://github.com/user-attachments/assets/95fd1918-51bf-4cbb-bd7d-3ee3b12af693" />

Launching the server and receiving the message of the client:

<img width="826" height="104" alt="image" src="https://github.com/user-attachments/assets/4ee4904c-b198-4811-bcca-05077e9bbd0a" />


Sending “hello du client” through a pipe with the netcat command(with -u fur UDP):

<img width="826" height="56" alt="image" src="https://github.com/user-attachments/assets/d4c7501b-af3b-4b48-b82f-57f89c7055d8" />



1.2 UDP Client

<img width="825" height="113" alt="image" src="https://github.com/user-attachments/assets/4cd8c64d-cb3c-426a-8601-be5111f47ee7" />

<img width="823" height="87" alt="image" src="https://github.com/user-attachments/assets/72e0b59d-5890-4d0b-85e5-e7d02167d467" />
