# TFTP-protokolas
RFC 1350
Instrukcija:
____________________________________________________________________
Serveriui:

1. Terminale atsidaryti /TFTP/TFTPServer/Server”.
2. Sukompiliuoti:
	javac TFTPpacket.java
	javac TFTPserverRRQ.java
	javac TFTPserverWRQ.java
	javac TFTPServer.java
3. Komanda: 
	java TFTPServer
___________________________________________________________________
Klientui:

1. Terminale atsidaryti /TFTP/TFTPClient/Client”.
2. Sukompiliuoti:
	javac TFTPpacket.java
	javac TFTPclientRRQ.java
	javac TFTPclientWRQ.java
	javac TFTPClient.java
3. Komanda: 
	java TFTPClient [host] [W/R] [Filename] (mode - nebutina)
	Pvz.:
	Atsisiusti: java TFTPClient 127.0.0.1 R server1.txt netascii
	Issiusti: java TFTPClient 127.0.0.1 W client1.jpg

Failai testavimui: client1.jpg client2.txt client3.txt server1.txt server2.jpg server3.jpg
