package communication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder.FetchProfileItem;
import javax.mail.search.SearchTerm;

import utils.Utils;

import com.sun.mail.imap.IMAPFolder;

/**
 * IMAPClient Defines the actions to retrieve messages and set flags
 */
public class IMAPClient {

	private Session session;
	private Store store;
	private IMAPFolder folder;

	/**
	 * Creates a new IMAP client
	 * 
	 * @param storeProtocol The protocol used for the store
	 * @param host The IMAP host
	 * @param auth The authenticator
	 * @throws MessagingException
	 */
	public IMAPClient(String storeProtocol, String host,
			SimpleAuthenticator auth) throws MessagingException {

		// Set properties
		Properties props = System.getProperties();
		props.setProperty("mail.imap.port", "993");
		props.setProperty("mail.imap.starttls.enable", "true");

		// Get session and store
		session = Session.getInstance(props, auth);
		store = session.getStore(storeProtocol);
		store.connect(host, null, null);

	}
	
	/**
	 * Performs a search against multiple keywords
	 * 
	 * @param keywords The keywords
	 * @param original The original set of messages
	 * @param searchHeaders Whether to search headers
	 * @return
	 * @throws MessagingException
	 */
	private Message[] multipleKeywordSearch(final String[] keywords, Message[] original, final boolean searchHeaders) throws MessagingException {
		
		// Create search criteria
		SearchTerm term = new SearchTerm() {
			public boolean match(Message message) {

				try {

					// Search headers if necessary
					if (searchHeaders) {
						Enumeration headers = message.getAllHeaders();

						while (headers.hasMoreElements()) {
							Header h = (Header) headers.nextElement();

							if (Utils.contains(h.getValue(), keywords)) {
								return true;
							}
						}
					}

					// Search content
					String contentType = message.getContentType();
					if ((contentType.contains("TEXT/HTML") || contentType.contains("TEXT/PLAIN"))) {
						if (Utils.contains(message.getContent().toString(), keywords)) {
							return true;
						}
					} else {
						Multipart multipart = (Multipart) message.getContent();

						// Loop through multipart message
						for (int i = 0; i < multipart.getCount(); i++) {

							// Get body and content type
							BodyPart bodyPart = multipart.getBodyPart(i);
							String bodyPartContentType = bodyPart.getContentType();

							// Check if the part contains the specified text
							if ((bodyPartContentType.contains("TEXT/HTML")
									|| bodyPartContentType.contains("TEXT/PLAIN"))
									&& Utils.contains(bodyPart.getContent().toString(), keywords)) { // (bodyPart.getContent() + "").contains(text)
								return true;
							}
						}
					}
				}
				catch (MessagingException ex) {
					ex.printStackTrace();
				}
				catch (IOException ex) {
					ex.printStackTrace();
				}

				return false;
			}

		};

		// Fetch messages
		Message[] messages = folder.search(term, original);

		FetchProfile fp = new FetchProfile();
		fp.add(FetchProfile.Item.ENVELOPE);
		fp.add(FetchProfileItem.FLAGS);
		fp.add(FetchProfileItem.CONTENT_INFO);

		folder.fetch(messages, fp);

		return messages;
	}

	/**
	 * Get all messages from a folder
	 * 
	 * @param folderName The folder name
	 * @return An array of Message objects
	 * @throws MessagingException
	 */
	public Message[] getMessages(String folderName) throws MessagingException {

		// Get folder if doesn't exist
		if (folder == null)
			folder = (IMAPFolder) store.getFolder(folderName);

		// Open folder is not already
		if (!folder.isOpen())
			folder.open(Folder.READ_WRITE);

		// Fetch messages
		Message[] messages = folder.getMessages();

		FetchProfile fp = new FetchProfile();
		fp.add(FetchProfile.Item.ENVELOPE);
		fp.add(FetchProfileItem.FLAGS);
		fp.add(FetchProfileItem.CONTENT_INFO);

		folder.fetch(messages, fp);

		return messages;
	}

	/**
	 * Performs a search in the current folder
	 * 
	 * @param text The text to search against
	 * @param original The original set of messages
	 * @return An array of Message objects that match the given text
	 * @throws MessagingException
	 */
	public Message[] search(String text, Message[] original) throws MessagingException {
		return search(text, original, true);
	}

	/**
	 * Performs a search in the current folder
	 * 
	 * @param text The text to search against
	 * @param original The original set of messages
	 * @param searchHeaders Include headers in the search
	 * @return An array of Message objects that match the given text
	 * @throws MessagingException
	 */
	public Message[] search(final String text, Message[] original, final boolean searchHeaders) throws MessagingException {
		// Run multiple search on one keyword (text)
		return multipleKeywordSearch(new String[] {text}, original, searchHeaders);
	}

	/**
	 * Sets a flag based on some keywords
	 * 
	 * @param keyword The keyword
	 * @param flagName The flag to apply
	 * @param original The original set of messages
	 * @param set Whether to add or remove the flag
	 * @return An array of Message objects (updated)
	 * @throws MessagingException
	 */
	public Message[] setFlag(String keyword, String flagName, Message[] original, boolean set) throws MessagingException {

		// Sanitize input
		flagName = flagName.trim();
		keyword = keyword.trim();

		Flags flag = new Flags(flagName);

		// Perform set/remove for all messages
		if (keyword.equals("*")) {
			folder.setFlags(original, flag, set);
			return original;
		}

		// Split keywords
		String[] keywordsArr = keyword.split(",");

		// Search matching messages (just check body)
		Message[] messages = multipleKeywordSearch(keywordsArr, original, false);
		
		// Set/Remove flag
		folder.setFlags(messages, flag, set);

		return original;

	}

	/**
	 * Get flags from a message
	 * 
	 * @param message The message
	 * @return A comma-separated string listing all flags
	 * @throws MessagingException
	 */
	public static String getFlags(Message message) throws MessagingException {

		ArrayList<String> flags = new ArrayList<String>();

		// System flags
		if (message.isSet(Flags.Flag.DELETED)) {
			flags.add("Deleted");
		}

		if (message.isSet(Flags.Flag.ANSWERED)) {
			flags.add("Answered");
		}

		if (message.isSet(Flags.Flag.DRAFT)) {
			flags.add("Draft");
		}

		if (message.isSet(Flags.Flag.FLAGGED)) {
			flags.add("Marked");
		}

		if (message.isSet(Flags.Flag.RECENT)) {
			flags.add("Recent");
		}

		if (message.isSet(Flags.Flag.SEEN)) {
			flags.add("Read");
		} else {
			flags.add("Not read");
		}

		// User flags
		String[] userFlags = message.getFlags().getUserFlags();
		for (String flag : userFlags) {
			flags.add(flag);
		}

		return Utils.join(", ", flags);

	}

	/**
	 * Closes the folder and store
	 * 
	 * @throws MessagingException
	 */
	public void close() throws MessagingException {

		if (folder != null && folder.isOpen()) {
			folder.close(true);
		}
		if (store != null) {
			store.close();
		}

	}
}
