import java.net.*;
import java.io.*;
import java.util.*;

class TFTPserverRRQ extends Thread {

	protected DatagramSocket sock;
	protected InetAddress host;
	protected int port;
	protected FileInputStream source;
	protected TFTPpacket req;
	protected int timeoutLimit = 5;
	protected String fileName;

	public TFTPserverRRQ(TFTPread request) throws TftpException {
		try {
			req = request;
			sock = new DatagramSocket(); //atsitiktinis portas
			sock.setSoTimeout(1000);
			
			fileName = request.fileName();
			host = request.getAddress();
			port = request.getPort();
			
			File srcFile = new File("../"+fileName); //tevine direktorija
			
			//patikrinam sukurta faila
			if (srcFile.exists() && srcFile.isFile() && srcFile.canRead()) {
				source = new FileInputStream(srcFile);
				this.start(); //nauja gija
			} else
				throw new TftpException("Something went wrong.");

		} 
		catch (Exception e) {
			TFTPerror ePak = new TFTPerror(1, e.getMessage()); // error 1
			try {
				ePak.send(host, port, sock);
			} 
			catch (Exception f) {
			}
			System.out.println("Client start failed:  " + e.getMessage());
		}
	}
	//jei viskas gerai, atidarom nauja gija failo siuntimui
	public void run() {
		int bytesRead = TFTPpacket.MAXLENGHT;
	
		if (req instanceof TFTPread) {
			try {
				for (int blkNum = 1; bytesRead == TFTPpacket.MAXLENGHT; blkNum++) {
					
					TFTPdata outPak = new TFTPdata(blkNum, source);
					bytesRead = outPak.getLength();
					outPak.send(host, port, sock);
					
					//laukiam tinkamo ACK
					while (timeoutLimit!=0) { 
						try {
							TFTPpacket ack = TFTPpacket.receive(sock);
							if (!(ack instanceof TFTPack)){
								throw new Exception("Client failed");
							}
							TFTPack a = (TFTPack) ack;
							
							if(a.blockNumber()!=blkNum){ //gal praradom paskutini paketa?
								throw new SocketTimeoutException("Last packet lost, resend packet");}
							break;
						} 
						catch (SocketTimeoutException t) {//persiunciame paskutini paketa
							System.out.println("Resend blk " + blkNum);
							timeoutLimit--;
							outPak.send(host, port, sock);
						}
					}
					if(timeoutLimit==0){
						throw new Exception("Connection failed");
					}
				}
				System.out.println("Transfer completed.");
			} 
			catch (Exception e) {
				TFTPerror ePak = new TFTPerror(1, e.getMessage()); //error 1
				try {
					ePak.send(host, port, sock);
				} 
				catch (Exception f) {
				}
				System.out.println("Client failed:  " + e.getMessage());
			}
		}
	}
}