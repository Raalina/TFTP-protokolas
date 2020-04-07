import java.net.*;
import java.io.*;
import java.util.*;

class TFTPclientRRQ {
	
	protected InetAddress server;
	protected String fileName;
	protected String dataMode;

	public TFTPclientRRQ(InetAddress ip, String name, String mode) {
		
		server = ip;
		fileName = name;
		dataMode = mode;

		try {
			DatagramSocket sock = new DatagramSocket();
			sock.setSoTimeout(2000);

			FileOutputStream outFile = new FileOutputStream("../"+fileName); //tevine direktorija su testiniais failais
			
			// Siunciam RRQ " Any transfer begins with a request to read or write a file, which   also serves to request a connection." -RFC1350
			TFTPread reqPak = new TFTPread(fileName, dataMode);
			reqPak.send(server, 6973, sock);

			TFTPack ack = null;
			InetAddress newIP = server;
			int newPort = 0;
			int timeoutLimit = 5;

			//Duomenu gavimas
			System.out.println("Downloading...");
			for (int blkNum = 1, bytesOut = 512; bytesOut == 512; blkNum++) {
				while (timeoutLimit != 0) {
					try {
						//A transfer is established by sending a request (WRQ or RRQ)
						//and receiving a positive reply, an acknowledgment packet 
						//for write, or the first data packet for read. - RFC1350
						TFTPpacket inPak = TFTPpacket.receive(sock);
						
						//tikrinam koki paketa gavom
						if (inPak instanceof TFTPerror) {
							TFTPerror p = (TFTPerror) inPak;
							throw new TftpException(p.message());
						} 
						else if (inPak instanceof TFTPdata) {
							TFTPdata p = (TFTPdata) inPak;
							newIP = p.getAddress();
							
							if (newPort != 0 && newPort != p.getPort()) {
								continue;
							}
							newPort = p.getPort();

							if (blkNum != p.blockNumber()) {
								throw new SocketTimeoutException();
							}
							
							// jei viskas ok - rasom i faila
							bytesOut = p.write(outFile);
							
							//siunciam ACK
							ack = new TFTPack(blkNum);
							ack.send(newIP, newPort, sock);
							break;
						} else
							throw new TftpException("Something went wrong.");
					}
					//Pasirupinam timeout
					catch (SocketTimeoutException t) {
						// bandom persiunti RRQ
						if (blkNum == 1) { 
							reqPak.send(server, 6973, sock);
							timeoutLimit--;
						} 
						// jei negavome atsakymo i paskutini ACK, persiunciam ACK
						else { 
							ack = new TFTPack(blkNum - 1);
							ack.send(newIP, newPort, sock);
							timeoutLimit--;
						}
					}
				}
				if (timeoutLimit == 0) {
					throw new TftpException("Connection failed");
				}
			}
			System.out.println("\nDownload Finished.");
			outFile.close();
			sock.close();
			
		} 
		catch (IOException e) {
			System.out.println("IO error");
			File wrongFile = new File(fileName);
			wrongFile.delete();
		} 
		catch (TftpException e) {
			System.out.println(e.getMessage());
			File wrongFile = new File(fileName);
			wrongFile.delete();
		}
	}
}