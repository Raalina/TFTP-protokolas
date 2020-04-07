import java.net.*;
import java.io.*;
import java.util.*;

public class TFTPServer {

	public static void main(String argv[]) {
		try {
			DatagramSocket sock = new DatagramSocket(6973);
			System.out.println("Server Ready.  Port:  " + sock.getLocalPort());

			// Laukiam RRQ arba WRQ
			while (true) {
				TFTPpacket in = TFTPpacket.receive(sock);
				// RRQ
				if (in instanceof TFTPread) {
					TFTPserverRRQ r = new TFTPserverRRQ((TFTPread) in);
				}
				// WRQ
				else if (in instanceof TFTPwrite) {
					TFTPserverWRQ w = new TFTPserverWRQ((TFTPwrite) in);
				}
			}
		} 
		catch (SocketException e) {
			System.out.println("Server terminated(SocketException) " + e.getMessage());
		} 
		catch (TftpException e) {
			System.out.println("Server terminated(TftpException)" + e.getMessage());
		} 
		catch (IOException e) {
			System.out.println("Server terminated(IOException)" + e.getMessage());
		}
	}
}