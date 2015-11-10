package gui.views;

import gui.MessageBox;
import gui.models.MessagesModel;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.swing.JEditorPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

public class MessagesView extends JTable implements Observer {

	MessagesModel model;
	
	public MessagesView(MessagesModel model, JEditorPane content) {
		super();
        this.model = model;
        
		try {
			Object[][] displayedMsgs = displayMessages(model.getMessages());
			String[] columnNames = new String[] { "From", "Subject", "Status" };
			DefaultTableModel dtm = new DefaultTableModel(displayedMsgs, columnNames);
			
			setModel(dtm);
		} catch (MessagingException ex) {
			ex.printStackTrace();
		}
		
		getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int index = getSelectedRow();
				if (index == -1)
					return;
					
				Message message = model.getMessage(index);
				
				// Display content
				try {
					String contentType = message.getContentType();
					
					if (contentType.contains("TEXT/HTML") || contentType.contains("TEXT/PLAIN")) {
						content.setText(message.getContent() + "");
					}
					else {
						Multipart multipart = (Multipart) message.getContent();
						boolean foundText = false;
						
						for (int i = 0; i < multipart.getCount() && !foundText; i++) {
							BodyPart bodyPart = multipart.getBodyPart(i);
							String bodyPartContentType = bodyPart.getContentType();
							
							if (bodyPartContentType.contains("TEXT/HTML") || bodyPartContentType.contains("TEXT/PLAIN")) {
								content.setText(bodyPart.getContent() + "");
								foundText = true;
							}
						}
						
						if (!foundText) {
							content.setText("Cannot display content. There is no text part in the message.");
						}
					}
					
				} catch (MessagingException ex) {
					MessageBox.show("Error in displaying the message.", "Error");
				} catch (IOException ex) {
					MessageBox.show("Error in getting the content of the message.", "Error");
				}
			}
		});
	}
	
	private Object[][] displayMessages(Message[] messages) throws MessagingException {
		int max = messages.length, index = 0;
		Object[][] displayedMessages = new Object[max][3];
		
		for (Message message : messages) {
			
			// Get address
			Address[] addresses = message.getFrom();
			String from = "";
			
			for (Address address : addresses) {
				from += address + ", ";
			}
			
			if (from != null && from.length() >= 3) {
				from = from.substring(0, from.length() - 2);
			}
			
			System.out.println(model.getFlags(message));
			
			// Fill data
			displayedMessages[index][0] = from;
			displayedMessages[index][1] = message.getSubject();
			displayedMessages[index][2] = model.getFlags(message);
			
			index++;
		}
		
		return displayedMessages;
	}
	
	@Override
	public void update(Observable o, Object arg) {
		try {
			
			// Reset table
			DefaultTableModel previousModel = (DefaultTableModel)getModel();
			previousModel.setRowCount(0);
			
			// Populate table
			Object[][] displayedMsgs = displayMessages(model.getMessages());
			String[] columnNames = new String[] { "From", "Subject", "Status" };
			DefaultTableModel dtm = new DefaultTableModel(displayedMsgs, columnNames);
			
			setModel(dtm);
			
		} catch (MessagingException ex) {
			MessageBox.show("Error in updating the email table.", "Error");
		}
	}
	
}
