import javax.swing.JFrame;


public class ConvoFrame extends JFrame {
	
	private static final long serialVersionUID = -2505300527406657839L;
	
	public Conversation conversation;
	
	public ConvoFrame(String recipient, Client client){
		this.conversation = new Conversation(recipient, client);
		this.setTitle("Convo");
	    this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    
	
	    //Add contents to the window.
	    this.add(conversation);
	
	    //Display the window.
	    this.pack();
	    this.setVisible(true);
	}
	
	@Override
	public void dispose(){
		conversation.client.conversations.remove(conversation);
		super.dispose();
	}

}
