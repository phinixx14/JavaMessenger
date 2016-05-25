import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;


public class Conversation extends JPanel implements ActionListener{

	private static final long serialVersionUID = -4100470593117593481L;
	Client client;
	Connection conn;
	String recipient;
	JTextField textField;
	JTextPane textPane;
	StyledDocument doc;
	Style style;
	
	public Conversation(String recipient, Client client){
		super(new GridBagLayout());  
		this.client = client;
		this.conn = client.conn;
		this.recipient = recipient;
		
		textField = new JTextField(20);
        textField.addActionListener(this);
 
        textPane = new JTextPane();
        doc = textPane.getStyledDocument();
        style = textPane.addStyle("textStyle", null);
        
        
        textPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(400,200));
        //Add Components to this panel.
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
 
        c.fill = GridBagConstraints.HORIZONTAL;
        add(textField, c);
 
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 5.0;
        c.weighty = 1.0;
        add(scrollPane, c);
	}
	
	public void displayMessage(Message msg){
		if(msg.recipient.equals("CLIENT")){
			StyleConstants.setForeground(style, Color.RED);
			
			try{
				this.doc.insertString(doc.getLength(), "    " + msg.content + "\n", style);
			} catch(BadLocationException e){
				e.printStackTrace();
			}
		}
		
		else{
			if(msg.sender.equals(client.username)){
				StyleConstants.setForeground(style, Color.BLUE);
			}
			else{
				StyleConstants.setForeground(style, Color.GREEN);
			}
			
			try{
				this.doc.insertString(doc.getLength(), msg.sender + ": ", style);
			} catch(BadLocationException e){
				e.printStackTrace();
			}
			
			StyleConstants.setForeground(style, Color.BLACK);
			
			try{
				this.doc.insertString(doc.getLength(), msg.content + "\n", style);
			} catch(BadLocationException e){
				e.printStackTrace();
			}
		}
		this.textPane.selectAll();
        this.textPane.setCaretPosition(textPane.getDocument().getLength());
	}
		
	@Override
	public void actionPerformed(ActionEvent evt) {
		String text = textField.getText();
		Message msg = new Message(this.client.username, recipient, text);
		displayMessage(msg);
		try {
			conn.sendMessage(msg);
			textField.setText("");
		} catch(IOException e){
			e.printStackTrace();
		}
        
	}

}
