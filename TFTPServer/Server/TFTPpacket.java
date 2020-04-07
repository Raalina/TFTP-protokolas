import java.net.*;
import java.io.*;
import java.util.*;

class TftpException extends Exception {
	public TftpException() {
		super();
	}
	public TftpException(String s) {
		super(s);
	}
}

///////////GENERAL//////////////
public class TFTPpacket {

	  public static int TFTPPORT = 69;
	  public static int MAXLENGHT = 516;
	  public static int MAXDATA = 512;

	  //op kodai
	  protected static final short TFTPRRQ = 1;
	  protected static final short TFTPWRQ = 2;
	  protected static final short TFTPDATA = 3;
	  protected static final short TFTPACK = 4;
	  protected static final short TFTPERROR = 5;

	  protected static final int OPOFFSET=0;
	  protected static final int FILEOFFSET=2;
	  protected static final int BLOCKOFFSET=2;
	  protected static final int DATAOFFSET=4;
	  protected static final int NUMOFFSET=2;
	  protected static final int MSGOFFSET=4;

	  //zinute
	  protected byte [] message;
	  protected int length;

	  //adresas
	  protected InetAddress host;
	  protected int port;

	  public TFTPpacket() {
		message=new byte[MAXLENGHT]; 
		length=MAXLENGHT; 
	  } 

	// gauname paketa ir pakeiciame i reikiama formata
	public static TFTPpacket receive(DatagramSocket sock) throws IOException {
	  
		TFTPpacket in = new TFTPpacket(), retPak = new TFTPpacket();
		
		//gauti duomenys perkeliami i in.message
		DatagramPacket inPak = new DatagramPacket(in.message,in.length);
		sock.receive(inPak); 
		
		//Tikrinam OP koda ir perkeliam duomenis i tinkama paketa.
		switch (in.get(0)) {
		  case TFTPRRQ:
			  retPak=new TFTPread();
			break;
		  case TFTPWRQ:
			  retPak=new TFTPwrite();
			break;
		  case TFTPDATA:
			  retPak=new TFTPdata();
			break;
		  case TFTPACK:
			  retPak=new TFTPack();
			break;
		  case TFTPERROR:
			  retPak=new TFTPerror();
			break;
		}
		retPak.message=in.message;
		retPak.length=inPak.getLength();
		retPak.host=inPak.getAddress();
		retPak.port=inPak.getPort();

		return retPak;
	}
  
	  //paketo issiuntimas
	  public void send(InetAddress ip, int port, DatagramSocket s) throws IOException {
		s.send(new DatagramPacket(message,length,ip,port));
	  }

	  public InetAddress getAddress() {
		return host;
	  }

	  public int getPort() {
		return port;
	  }

	  public int getLength() {
		return length;
	  }

	  //op koda, bloko numeri ir error koda i message. 
	  protected void put(int at, short value) {
		message[at++] = (byte)(value >>> 8);  // 1 bitas
		message[at] = (byte)(value % 256);    // paskutinis bitas
	  }

	  @SuppressWarnings("deprecation")
	  //filename ir mode i message
	  protected void put(int at, String value, byte del) {
		value.getBytes(0, value.length(), message, at);
		message[at + value.length()] = del;
	  }

	  protected int get(int at) {
		return (message[at] & 0xff) << 8 | message[at+1] & 0xff;
	  }

	  protected String get (int at, byte del) {
		StringBuffer result = new StringBuffer();
		while (message[at] != del) result.append((char)message[at++]);
		return result.toString();
	  }
}

///////////////////DATA///////////////////////////
final class TFTPdata extends TFTPpacket {

	protected TFTPdata() {}
	public TFTPdata(int blockNumber, FileInputStream in) throws IOException {
		this.message = new byte[MAXLENGHT];
		this.put(OPOFFSET, TFTPDATA);
		this.put(BLOCKOFFSET, (short) blockNumber);
		length = in.read(message, DATAOFFSET, MAXDATA) + 4;
	}
	
	public int blockNumber() {
		return this.get(BLOCKOFFSET);
	}

	public int write(FileOutputStream out) throws IOException {
		out.write(message, DATAOFFSET, length - 4);

		return (length - 4);
	}
}

////////////////////ERROR//////////////////////////
class TFTPerror extends TFTPpacket {

	protected TFTPerror() {}
	public TFTPerror(int number, String message) {
		length = 4 + message.length() + 1;
		this.message = new byte[length];
		put(OPOFFSET, TFTPERROR);
		put(NUMOFFSET, (short) number);
		put(MSGOFFSET, message, (byte) 0);
	}

	public int number() {
		return this.get(NUMOFFSET);
	}
	
	public String message() {
		return this.get(MSGOFFSET, (byte) 0);
	}
}

/////////////////////ACK////////////////////////////
final class TFTPack extends TFTPpacket {

	protected TFTPack() {}
	public TFTPack(int blockNumber) {
		length = 4;
		this.message = new byte[length];
		put(OPOFFSET, TFTPACK);
		put(BLOCKOFFSET, (short) blockNumber);
	}

	public int blockNumber() {
		return this.get(BLOCKOFFSET);
	}
}


////////////////////READ/////////////////////////////
final class TFTPread extends TFTPpacket {

	protected TFTPread() {}
	public TFTPread(String filename, String dataMode) {
		length=2+filename.length()+1+dataMode.length()+1;
		message = new byte[length];
		put(OPOFFSET,TFTPRRQ);
		put(FILEOFFSET,filename,(byte)0);
		put(FILEOFFSET+filename.length()+1,dataMode,(byte)0);
	}

	public String fileName() {
	  return this.get(FILEOFFSET,(byte)0);
	}

	public String requestType() {
	  String fname = fileName();
	  return this.get(FILEOFFSET+fname.length()+1,(byte)0);
	}
}

/////////////////////WRITE/////////////////////////////////
final class TFTPwrite extends TFTPpacket {

	protected TFTPwrite() {}
	public TFTPwrite(String filename, String dataMode) {
		length=2+filename.length()+1+dataMode.length()+1;
		message = new byte[length];
		put(OPOFFSET,TFTPWRQ);
		put(FILEOFFSET,filename,(byte)0);
		put(FILEOFFSET+filename.length()+1,dataMode,(byte)0);
	}

	public String fileName() {
		return this.get(FILEOFFSET,(byte)0);
	}

	public String requestType() {
		String fname = fileName();
		return this.get(FILEOFFSET+fname.length()+1,(byte)0);
	}
}