package communication;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.UIDFolder.FetchProfileItem;
import javax.mail.internet.MimeMessage;
import javax.mail.search.SearchTerm;

import utils.Utils;

import com.sun.mail.imap.IMAPFolder;

public class IMAPClient {
	
	private Session session;
	private Store store;
	private IMAPFolder folder;
	
	public IMAPClient(String storeProtocol, String host, SimpleAuthenticator auth) throws MessagingException {
		
		// Set properties
		Properties props = System.getProperties();
		props.setProperty("mail.imap.port", "993");
		props.setProperty("mail.imap.starttls.enable", "true");
		
		// Get session and store
		session = Session.getInstance(props, auth);
		session.setDebug(true);
		store = session.getStore(storeProtocol);
		store.connect(host, null, null);
		
	}
	
	public Message[] getMessages(String folderName) throws MessagingException {
		
		// Get folder if doesn't exist
		if (folder == null)
			folder = (IMAPFolder)store.getFolder(folderName);
		
		// Open folder is not already
		if (!folder.isOpen())
			folder.open(Folder.READ_WRITE);
		
		// Fetch messages
		Message[] messages = folder.getMessages();
		
		FetchProfile fp = new FetchProfile();
	    fp.add(FetchProfile.Item.ENVELOPE);
	    fp.add(FetchProfileItem.FLAGS);
	    fp.add(FetchProfileItem.CONTENT_INFO);

	    //fp.add("X-mailer");
	    folder.fetch(messages, fp);
		
		return messages;
	}
	
	public Message[] search(String text, Message[] original) throws MessagingException {
		return search(text, original, true);
	}
	
	public Message[] search(final String text, Message[] original, final boolean searchSubject) throws MessagingException {
		
		// Create search criteria
		SearchTerm term = new SearchTerm() {
		    public boolean match(Message message) {
		    	
		        try {
		        	String contentType = message.getContentType();
		        	
		        	// Search subject
		        	if (searchSubject) {
			        	String subject = message.getSubject();
			            if (subject != null && subject.contains(text)) {
			                return true;
			            }
		        	}
		            
		        	// Search content
		            if ((contentType.contains("TEXT/HTML") || contentType.contains("TEXT/PLAIN"))) {
		            	if ((message.getContent() + "").contains(text))
		            		return true;
		            }
		            else {
						Multipart multipart = (Multipart) message.getContent();
						
						// Loop through multipart message
						for (int i = 0; i < multipart.getCount(); i++) {
							
							// Get body and content type
							BodyPart bodyPart = multipart.getBodyPart(i);
							String bodyPartContentType = bodyPart.getContentType();
							
							// Check if the part contains the specified text
							if ((bodyPartContentType.contains("TEXT/HTML") || bodyPartContentType.contains("TEXT/PLAIN")) && (bodyPart.getContent() + "").contains(text)) {
								return true;
							}
						}
					}
		        } catch (MessagingException ex) {
		            ex.printStackTrace();
		        } catch (IOException ex) {
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
	
	public Message[] setFlag(String keyword, String flagName, Message[] original) throws MessagingException {
		
		// Search matching messages (just check body)
	    Message[] messages = search(keyword, original, false);
	    
	    // Set flag
		Flags flag = new Flags(flagName);
		folder.setFlags(messages, flag, true);
		
		return messages;
		
	}
	
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
		}
		
		Flags fg = message.getFlags();
		System.out.println(fg.contains("SPAM"));
		
		// User flags
		if (message.isSet(Flags.Flag.USER)) {
			System.out.println("yeah");
			
			String[] userFlags = message.getFlags().getUserFlags();
			for (int i = 0; i < userFlags.length; i++) {
				flags.add(userFlags[i]);
			}
		}
		
		return Utils.join(", ", flags); //String.join(", ", flags);
	}
}
