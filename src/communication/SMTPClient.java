package communication;

import java.util.ArrayList;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import exceptions.ClientException;

public class SMTPClient {
	
	private Session session;
	private String transportProtocol;
	private SimpleAuthenticator auth;
	
	public SMTPClient(String transportProtocol, String host, SimpleAuthenticator auth) {
		
		// Set properties
		Properties props = System.getProperties();
		props.setProperty("mail.smtp.host", host);
		props.setProperty("mail.smtp.auth", "true");
		props.setProperty("mail.smtp.starttls.enable", "true");
		props.setProperty("mail.smtp.port", "587");
		
		this.transportProtocol = transportProtocol;
		this.auth = auth;
		this.session = Session.getInstance(props, auth);
		
	}
	
	public void sendMessage(String to, String cc, ArrayList<String> filenames, String subject, String body) throws MessagingException, ClientException {
		if (to.isEmpty()) {
			throw new ClientException("The receiver field cannot be empty");
		}
		
		Message message = new MimeMessage(session);
		
		message.setFrom(new InternetAddress(auth.getUsername()));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
		if (cc != null && !cc.isEmpty())
			message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
		message.setSubject(subject);
		
		Multipart multipart = new MimeMultipart();
		
		// Text message
		BodyPart messageBodyPart = new MimeBodyPart();
		messageBodyPart.setText(body);
		multipart.addBodyPart(messageBodyPart);
		
		// Attach files
		for (String filename : filenames) {
			messageBodyPart = new MimeBodyPart();
			DataSource source = new FileDataSource(filename);
			messageBodyPart.setDataHandler(new DataHandler(source));
			messageBodyPart.setFileName(filename);
			multipart.addBodyPart(messageBodyPart);
		}
		
		message.setContent(multipart);
		message.saveChanges();
		
		// Send message
		Transport tr = session.getTransport(transportProtocol);	
		tr.connect(auth.getUsername(), auth.getPassword());
		tr.sendMessage(message, message.getAllRecipients());
		tr.close();
	}
	
}
