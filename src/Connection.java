
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Connection implements AutoCloseable{
	Socket soc;
	ObjectOutputStream out;
	ObjectInputStream in;
	InputStream rawIn;
	String username;
	
	public Connection(Socket s) throws IOException{
		this.soc = s;
		System.out.println("ConnGotSocket");
		this.out = new ObjectOutputStream(soc.getOutputStream());
		System.out.println("ConnGotOut");
		this.rawIn = soc.getInputStream();
		this.in = new ObjectInputStream(rawIn);
		System.out.println("ConnGotIn");
	}
	
	public void sendMessage(Message msg) throws IOException{
		System.out.println("TO: " + msg.recipient + " FROM: " + msg.sender + " MSG: " + msg.content);
		this.out.writeObject(msg);
	}
	
	public boolean hasMessage() throws IOException{
		return this.rawIn.available() > 0;
	}
	
	public Message readMessage() throws IOException{
		if(this.hasMessage()){
			try {				
				return (Message)this.in.readObject();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public Message waitForMessage() throws IOException, InterruptedException{
		while(!this.hasMessage()){
			System.out.println("Waiting for message...");
			Thread.sleep(1000);
		}
		return this.readMessage();
	}
	
	@Override
	public void close() throws IOException {
		this.in.close();
		this.rawIn.close();
		this.out.close();
		this.soc.close();
	}
}
