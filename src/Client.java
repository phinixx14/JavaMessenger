
import java.awt.Dialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Client extends JPanel implements ActionListener{
	public HashMap<String, Conversation> conversations = new HashMap<String, Conversation>();
	public String username;
	JTextField textField;
	public Connection conn;
	
	public static void main(String args[]){
		JFrame frame = new JFrame("Chat Client");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Client c = new Client();
		
		if(c.username == null || c.conn == null){
			return;
		}
		
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(WindowEvent windowEvent) {
		        try {
					c.conn.sendMessage(new Message("CLIENT","SERVER","GOODBYE"));
					c.conn.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	System.exit(0);
		    }
		});
		
		frame.add(c);
		frame.pack();
		frame.setVisible(true);
		
		while(true){ 
			if(c.conn != null){
				Message msg;
				
					try{
						if(c.conn.hasMessage()){
							msg = c.conn.readMessage();
							if(msg.sender.equals("SERVER") && msg.content.equals("")){
								//do nothing
							}
							else{
								if(c.conversations.containsKey(msg.sender)){
									c.conversations.get(msg.sender).displayMessage(msg);
								}
								else{
									c.startConversation(msg.sender).displayMessage(msg);
								}
							}
						}
						
						//TODO remove
						Thread.sleep(10);
						
					} catch (IOException e) {
						if(e.getLocalizedMessage().equals("Stream closed.")){
							System.out.println("Stream is closed");
							break;
						}
						else{
							e.printStackTrace();
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
			}
		}
	}
	
	public Client(){
		super(new GridBagLayout());  
		Message msg = new Message("","","");
		
		do{
			username = getUsername();
			if(username == null){
				return;
			}
			try{
				conn = new Connection(openSocket());
			
				conn.sendMessage(new Message("CLIENT", "SERVER", username));
				System.out.println("sentUsername");
				
				msg = conn.waitForMessage();
				System.out.println(msg.content);
				
			} catch(ConnectException e){
				System.out.println("Server is unavailable");
				return;
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
		}while(msg.content.equals("  ERROR: Username is already in use"));
		
		JLabel label = new JLabel("Enter user to Message");
		textField = new JTextField(20);
        textField.addActionListener(this);
        
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5,5,5,5);
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(label, c);
        
        c.fill = GridBagConstraints.BOTH;
        add(textField, c);
	}
	
	private static Socket openSocket() throws UnknownHostException, IOException{
		return new Socket(InetAddress.getByName("127.0.0.1"), 22334);
	}
		
	private static String getUsername(){
		String name;
		do{
			name = JOptionPane.showInputDialog("Enter Username");
			if(name == null){
				return null;
			}
		}while(name.equals(""));
		return name;
	}
	
	private Conversation startConversation(String user){
		ConvoFrame cFrame = new ConvoFrame(user, this);
		conversations.put(user, cFrame.conversation);
		return cFrame.conversation;
    }
	
	@Override
	public void actionPerformed(ActionEvent evt) {
		String user = textField.getText();
		startConversation(user);
		textField.setText("");
	}
}
