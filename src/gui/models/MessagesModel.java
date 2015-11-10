package gui.models;

import java.io.IOException;
import java.util.Observable;

import javax.mail.Message;
import javax.mail.MessagingException;

import communication.IMAPClient;

public class MessagesModel extends Observable {
	
	private IMAPClient client;
	private Message[] messages;
			
	public MessagesModel(IMAPClient client) throws MessagingException {
		super();
		this.client = client;
		reset();
	}

	public Message[] getMessages() {
		return messages;
	}
	
	public Message getMessage(int i) {
		return messages[i];
	}
	
	public String getFlags(Message message) throws MessagingException {
		return IMAPClient.getFlags(message);
	}
	
	public void search(String term) throws MessagingException, IOException {		
		messages = this.client.search(term, messages);
		setChanged();
		notifyObservers();
	}
	
	public void reset() throws MessagingException {
		messages = this.client.getMessages("inbox");
		setChanged();
		notifyObservers();
	}
	
	public void setFlag(String keyword, String flagName, boolean set) throws MessagingException {
		messages = this.client.setFlag(keyword, flagName, messages, set);
		setChanged();
		notifyObservers();
	}
}
