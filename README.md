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

On the server side:

<img width="823" height="87" alt="image" src="https://github.com/user-attachments/assets/72e0b59d-5890-4d0b-85e5-e7d02167d467" />


2 – CReating a TCP Client-Server

2.1 TCP Server

<img width="496" height="86" alt="Capture d’écran 2025-11-19 à 14 26 44" src="https://github.com/user-attachments/assets/4b89c215-441f-41be-b89e-61808ade5f1e" />

<img width="492" height="61" alt="Capture d’écran 2025-11-19 à 14 27 31" src="https://github.com/user-attachments/assets/e61b0159-3588-479e-830c-583fca4c5ed3" />

2.2 TCP Client


When the TCPclient sends a messages, the server receives it and answer the adress of the client and its message:

<img width="1107" height="236" alt="image" src="https://github.com/user-attachments/assets/045d6596-35fd-45b3-aabc-6f9234ca634f" />

On the server side:

<img width="521" height="95" alt="image" src="https://github.com/user-attachments/assets/dd3ef3c5-765e-48a2-9185-ab788b32c9b7" />





<img width="1298" height="114" alt="Capture d’écran 2025-11-19 à 16 09 32" src="https://github.com/user-attachments/assets/1fa8e4e3-6ee0-491d-9ad2-82918e84b63b" />


<img width="1298" height="60" alt="Capture d’écran 2025-11-19 à 16 11 09" src="https://github.com/user-attachments/assets/b4be2869-1d17-45a0-80a2-27dc6fee1af2" />


<img width="1298" height="169" alt="Capture d’écran 2025-11-19 à 16 12 03" src="https://github.com/user-attachments/assets/f9c16bf2-9420-4e43-a815-0ad5ba48446f" />



<img width="565" height="197" alt="Capture d’écran 2025-11-19 à 16 12 54" src="https://github.com/user-attachments/assets/f89691c4-58fa-4908-940b-6cf8b9cabaa1" />






