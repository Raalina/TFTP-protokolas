import java.net.*;
import java.io.*;
import java.util.*;

class TFTPclientWRQ {
	
	protected InetAddress server;
	protected String fileName;
	protected String dataMode;
	
	public TFTPclientWRQ(InetAddress ip, String name, String mode) {
		
		server = ip;
		fileName = name;
		dataMode = mode;
		
		try {
			DatagramSocket sock = new DatagramSocket();
			sock.setSoTimeout(2000);
			int timeoutLimit = 5;
			FileInputStream source = new FileInputStream("../"+fileName); //tevine direktorija su testiniais failais

			// Siunciam WRQ " Any transfer begins with a request to read or write a file, which   also serves to request a connection." -RFC1350
			TFTPwrite reqPak = new TFTPwrite(fileName, dataMode);
			reqPak.send(server, 6973, sock);

			//A transfer is established by sending a request (WRQ or RRQ)
			//and receiving a positive reply, an acknowledgment packet 
			//for write, or the first data packet for read. - RFC1350
			TFTPpacket sendRsp = TFTPpacket.receive(sock);
			int port = sendRsp.getPort(); //siuntimui
			
			//tikrinam koki paketa gavom
			if (sendRsp instanceof TFTPack) {
				TFTPack Rsp = (TFTPack) sendRsp;
				System.out.println("Server ready:\nUploading...");
			} 
			else if (sendRsp instanceof TFTPerror) {
				TFTPerror Rsp = (TFTPerror) sendRsp;
				source.close();
				throw new TftpException(Rsp.message());
			}

			int bytesRead = TFTPpacket.MAXLENGHT;
			
			//Siuntimas
			for (int blkNum = 1; bytesRead == TFTPpacket.MAXLENGHT; blkNum++) {
				
				TFTPdata outPak = new TFTPdata(blkNum, source);
				bytesRead = outPak.getLength(); //kiek baitu gavom?
				outPak.send(server, port, sock); // issiunciam paketa
				
				while (timeoutLimit != 0) {//laukiam tinkamo ACK
					try {
						TFTPpacket ack = TFTPpacket.receive(sock);
						if (!(ack instanceof TFTPack)) {
							break;
						}

						TFTPack a = (TFTPack) ack;
						 // netinkamas portas
						if (port != a.getPort()) {
							continue; // ignoruojame
						}
						
						// gavom ACK i ankstesni paketa, persiusime paketa
						if (a.blockNumber() != blkNum) {
							throw new SocketTimeoutException("Last packet lost, resend packet");
						}
						break;
					} 
					catch (SocketTimeoutException t0) {
						outPak.send(server, port, sock); // persiunciame paskutini paketa
						timeoutLimit--;
					}
				}
				if (timeoutLimit == 0) {
					throw new TftpException("Connection failed");
				}

			}
			source.close();
			sock.close();
			System.out.println("\nUpload finished.");

		} catch (SocketTimeoutException t) {
			System.out.println("No response from sever, please try again");
		} catch (IOException e) {
			System.out.println("IO error, transfer aborted");
		} catch (TftpException e) {
			System.out.println(e.getMessage());
		}
	}

}
