import java.net.*;
import java.io.*;
import java.util.*;

class TFTPserverWRQ extends Thread {

	protected DatagramSocket sock;
	protected InetAddress host;
	protected int port;
	protected FileOutputStream outFile;
	protected TFTPpacket req;
	protected int timeoutLimit = 5;
	protected File saveFile;
	protected String fileName;

	public TFTPserverWRQ(TFTPwrite request) throws TftpException {
		try {
			req = request;
			sock = new DatagramSocket(); //atsitiktinis portas
			sock.setSoTimeout(1000);

			host = request.getAddress();
			port = request.getPort();
			fileName = request.fileName();

			saveFile = new File("../"+fileName); //tevine direktorija
			//patikrinam sukurta faila
			if (!saveFile.exists()) {
				outFile = new FileOutputStream(saveFile);
				TFTPack a = new TFTPack(0); //ACK pradedam nuo 0 "Since the positive response to a write request is an acknowledgment packet, in this special case the block number will be zero." -RFC1350
				a.send(host, port, sock);
				this.start(); //nauja gija
			} 
			else
				throw new TftpException("File exists");

		} 
		catch (Exception e) {
			TFTPerror ePak = new TFTPerror(1, e.getMessage()); // error 1
			try {
				ePak.send(host, port, sock);
			} 
			catch (Exception f) {
			}
			System.out.println("Client start failed:" + e.getMessage());
		}
	}
	//jei viskas gerai, atidarom nauja gija failo gavimui
	public void run() {
		if (req instanceof TFTPwrite) {
			try {
				for (int blkNum = 1, bytesOut = 512; bytesOut == 512; blkNum++) {
					while (timeoutLimit != 0) {
						try {
							TFTPpacket inPak = TFTPpacket.receive(sock); 
							
							//tikrinam koki paketa gavom
							if (inPak instanceof TFTPerror) {
								TFTPerror p = (TFTPerror) inPak;
								throw new TftpException(p.message());
							} 
							else if (inPak instanceof TFTPdata) {
								TFTPdata p = (TFTPdata) inPak;
	
								// tikrinam bloku numerius
								if (p.blockNumber() != blkNum) {
									throw new SocketTimeoutException();
								}
								//irasom i faila ir siunciam ACK
								bytesOut = p.write(outFile);
								TFTPack a = new TFTPack(blkNum);
								a.send(host, port, sock);
								break;
							}
						}
						//persiunciame ACK					
						catch (SocketTimeoutException t2) {
							TFTPack a = new TFTPack(blkNum - 1);
							a.send(host, port, sock);
							timeoutLimit--;
						}
					}
					if(timeoutLimit==0){
						throw new Exception("Connection failed");
					}
				}
				System.out.println("Transfer completed." );		
			} 
			catch (Exception e) {
				TFTPerror ePak = new TFTPerror(1, e.getMessage());
				try {
					ePak.send(host, port, sock);
				} 
				catch (Exception f) {
				}
				System.out.println("Client failed:  " + e.getMessage());
				saveFile.delete();
			}
		}
	}
}