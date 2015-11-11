package gui.models;

import java.io.IOException;
import java.util.Observable;

import javax.mail.Message;
import javax.mail.MessagingException;

import communication.IMAPClient;

/**
 * MessagesModel
 * Describes the model for a list of messages
 */
public class MessagesModel extends Observable {
	
	private IMAPClient client;
	private Message[] messages;
	
	/**
	 * Creates a new MessagesModel object
	 * 
	 * @param client The underlying IMAP client
	 * @throws MessagingException
	 */
	public MessagesModel(IMAPClient client) throws MessagingException {
		super();
		this.client = client;
		reset();
	}

	/**
	 * Gets all messages from the inbox
	 * @return
	 */
	public Message[] getMessages() {
		return messages;
	}
	
	/**
	 * Gets a specific message
	 * 
	 * @param i Index of message
	 * @return
	 */
	public Message getMessage(int i) {
		return messages[i];
	}
	
	/**
	 * Gets all flags of a given message
	 * 
	 * @param message The message
	 * @return A string of flags
	 * @throws MessagingException
	 */
	public String getFlags(Message message) throws MessagingException {
		return IMAPClient.getFlags(message);
	}
	
	/**
	 * Performs a search against a given term
	 * 
	 * @param term The search term
	 * @throws MessagingException
	 * @throws IOException
	 */
	public void search(String term) throws MessagingException, IOException {		
		messages = this.client.search(term, messages);
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Refreshes the email list
	 * 
	 * @throws MessagingException
	 */
	public void reset() throws MessagingException {
		messages = this.client.getMessages("inbox");
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Adds/Removes a flag according to some keywords in the message body
	 * 
	 * @param keyword
	 * @param flagName The flag
	 * @param set True to add flag and false to remove
	 * @throws MessagingException
	 */
	public void setFlag(String keyword, String flagName, boolean set) throws MessagingException {
		messages = this.client.setFlag(keyword, flagName, messages, set);
		setChanged();
		notifyObservers();
	}
}
