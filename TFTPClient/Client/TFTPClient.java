import java.net.InetAddress;
import java.net.UnknownHostException;

class UseException extends Exception {
	public UseException() {
		super();
	}

	public UseException(String s) {
		super(s);
	}
}

public class TFTPClient {
	public static void main(String argv[]) throws TftpException, UseException {
		String host = "";
		String fileName = "";
		String mode="octet"; //default, nebent nurodome kitaip
		String type="";
		try {
			
			if (argv.length == 0)
				throw new UseException("Usage: \n TFTPClient [host] [R/W] [filename] \n TFTPClient [host] [R/W] [filename] [octet/netascii]" );
			
			//octet, nes kitaip nenurodeme
			if(argv.length == 3){
				host =argv[0];
			    type = argv[argv.length - 2];
			    fileName = argv[argv.length - 1];}
				
			//netascii arba octet "The mail mode is obsolete and should not be implemented or used."-RFC1350 
			else if(argv.length == 4){
				host = argv[0];
				mode =argv[argv.length-1];
				type = argv[argv.length - 3];
				fileName = argv[argv.length - 2];
			}
			else throw new UseException("Unrecognised command. \n Usage: \n TFTPClient [host] [R/W] [filename] \n TFTPClient [host] [R/W] [filename] [octet/netascii]");
			
			
			InetAddress server = InetAddress.getByName(host);
			
			//RRQ
			if(type.matches("R")){
				TFTPclientRRQ r = new TFTPclientRRQ(server, fileName, mode);}
			//WRQ
			else if(type.matches("W")){
				TFTPclientWRQ w = new TFTPclientWRQ(server, fileName, mode);
			}
			else{throw new UseException("Unrecognised command. \n Usage: \n TFTPClient [host] [R/W] [filename] \n TFTPClient [host] [R/W] [filename] [octet/netascii]");}
			
		} catch (UnknownHostException e) {
			System.out.println("Unknown host " + host);
		} catch (UseException e) {
			System.out.println(e.getMessage());
		}
	}
}