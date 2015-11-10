package communication;

import gui.MessageBox;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class SimpleAuthenticator extends Authenticator {

	private Frame frame;
	private String username;
	private String password;

	public SimpleAuthenticator(Frame f) {
		this.frame = f;
	}

	protected PasswordAuthentication getPasswordAuthentication() {
		
		JComponent d = new JComponent() {};

		GridBagLayout gb = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		d.setLayout(gb);
		c.insets = new Insets(2, 2, 2, 2);

		c.gridwidth = 1;
		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		d.add(constrain(new JLabel("Username:"), gb, c));

		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		JTextField username = new JTextField("nadiaeddb@gmail.com", 20);
		d.add(constrain(username, gb, c));

		c.gridwidth = 1;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		c.weightx = 0.0;
		d.add(constrain(new JLabel("Password:"), gb, c));

		c.anchor = GridBagConstraints.EAST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		JPasswordField password = new JPasswordField("", 20);
		password.requestFocus();
		d.add(constrain(password, gb, c));
		
		// Prompt the login message box
		while (true) {
			int result = JOptionPane.showConfirmDialog(frame, d, "Login",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (result == JOptionPane.OK_OPTION) {
				this.username = username.getText();
				this.password = password.getText();
				
				// Test the connection
				try {
					testConnection(this.username, this.password);
					break;
				} catch (AuthenticationFailedException e) {
					MessageBox.show("Authentication failure. Check your email or/and password.", "Error");
				} catch (MessagingException e) {
					MessageBox.show("Communication error.", "Error");
				}
			}
			else
				return null;
		}
		
		return new PasswordAuthentication(this.username, this.password);
	}

	private void testConnection(String username, String password) throws MessagingException, AuthenticationFailedException {
		int port = 587;
		String host = "smtp.gmail.com";

		Properties props = new Properties();
	    props.put("mail.smtp.starttls.enable","true");
	    props.put("mail.smtp.auth", "true");
	    
	    Session session = Session.getInstance(props, null);
	    Transport transport = session.getTransport("smtp");
	    transport.connect(host, port, username, password);
	    transport.close();
	}
	
	private Component constrain(Component cmp, GridBagLayout gb, GridBagConstraints c) {
		gb.setConstraints(cmp, c);
		return (cmp);
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
}