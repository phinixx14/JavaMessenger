
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.HashMap;

public class Server {

	private static HashMap<String, Connection> connections = new HashMap<String, Connection>();
	
	private Server(){
		new Thread(new connectionListener()).start();
		new Thread(new messageListener()).start();
		new Thread(new connectionChecker()).start();
	}
	
	public static void main(String[] args) {
		System.out.println("Welcome to Server!\n");
		new Server();
	}
		
	protected class connectionListener implements Runnable{

		@Override
		public void run() {
			while(true){
        		
        		try(ServerSocket ss = openSocket();){
					System.out.println("Waiting to accept connection...");
					
					Connection conn = new Connection(ss.accept());
					
					new Thread(new connectionEstablisher(conn)).start();
					
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        	}	
		}
		
		protected class connectionEstablisher implements Runnable{
			
			private Connection conn;
			
			public connectionEstablisher(Connection conn){
				this.conn = conn;
			}		
			
			@Override
			public void run() {
				establishConnection(conn);
			}
			
			private void establishConnection(Connection conn){
				try {
		    		System.out.println("Waiting for username...");
		    		String name = conn.waitForMessage().content;
					
					System.out.println("Handling connection from " + name);
					
					if(name.equals("SERVER") || name.equals("CLIENT")){
						System.out.println("Username \"" + name + "\" is restricted");
						conn.sendMessage(new Message("SERVER", "CLIENT", "  ERROR: Username is restricted"));
						conn.close();
					}
					if(connections.putIfAbsent(name, conn) != null){
						System.out.println("Username \"" + name + "\" is already in use");
						conn.sendMessage(new Message("SERVER", "CLIENT", "  ERROR: Username is already in use"));
						conn.close();
					}
					else{
						conn.username = name;
						connections.putIfAbsent(conn.username, conn);
						System.out.println("Sending client the \"connected\" message");
						conn.sendMessage(new Message("SERVER", "CLIENT", "Connected!"));
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		private ServerSocket openSocket() throws IOException{
			return new ServerSocket(22334);
		}
	}
		
	protected class messageListener implements Runnable{

		@Override
		public void run() {
			while(true){
        		for(int i = 0; i < connections.size(); i++){
        			Connection conn = (Connection) connections.values().toArray()[i];
        			
        			try{
        				
        				if(conn.hasMessage()){
	        				Message msg = conn.readMessage();
	        												
							System.out.println("Server received \"" + msg.content + "\" FROM:" + msg.sender + " TO:" + msg.recipient);
							if(msg.recipient.equals("SERVER") && msg.sender.equals("CLIENT")){
								switch(msg.content){
									case "GOODBYE":{
										Server.removeConnection(conn.username);
										break;
									}
								}
							}
							else{
								Connection outbound = null;
								try{
									outbound = connections.get(msg.recipient);
									if(outbound == null){
										conn.sendMessage(new Message(msg.recipient, "CLIENT","ERROR: Recipient is not available"));
									}
									else{
										outbound.sendMessage(msg);
									}
									
								} catch(SocketException e){
									System.out.println("Lost connection from " + msg.recipient);
									Server.removeConnection(outbound.username);
									continue;
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
        				}
					
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
        		}
        		try{
        			//TODO remove
        			Thread.sleep(10);
        		}catch(InterruptedException e){
        			e.printStackTrace();
        		}
        	}
		}
	}
		
	protected class connectionChecker implements Runnable{
		@Override
		public void run(){
			while(true){
				for(Connection conn : connections.values()){
					try {
						System.out.println("checking " + conn.username);
						conn.sendMessage(new Message("SERVER", "CLIENT", ""));
						System.out.println(conn.username + " is alive");
					} catch(SocketException e){
						Server.removeConnection(conn.username);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	}
		}
	}
	
	private static void removeConnection(String user){
		System.out.println("removing " + user);
		connections.remove(user);
	}

}
