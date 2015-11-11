package gui;

import exceptions.ClientException;
import gui.models.MessagesModel;
import gui.views.CustomTextField;
import gui.views.MessagesView;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import communication.IMAPClient;
import communication.SMTPClient;
import communication.SimpleAuthenticator;

public class EmailClient {

	// Components
	// ----------
	
	private SMTPClient smtpClient;
	private IMAPClient imapClient;
	
	// Main wrappers
	private JFrame frmEmailClient;
	private JTabbedPane tabbedPane;
	
	// Display emails components
	private JPanel visualiseEmailPnl;
	private JPanel emailsPnl;
	private JPanel searchPnl;
	private JTextField searchTxt;
	private MessagesView messagesView;
	private JEditorPane emailContentTxt;
	private JButton searchBtn;
	private JButton refreshBtn;
	
	// Send message components
	private JPanel newMessagePnl;
	private JTextField toEmailAddressTxt;
	private JTextField subjectTxt;
	private JTextField ccEmailAddressTxt;
	private JEditorPane newMessage;
	private JButton chooseFileBtn;
	private JFileChooser fileChooser;
	private ArrayList<String> files;
	private JLabel toEmailAddressLbl;
	private JLabel subjectLbl;
	private JLabel ccEmailAddressLbl;
	private JLabel newMessageLbl;
	private JButton sendMessageBtn;
	
	// Set flags components
	private JPanel setFlagsPnl;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					EmailClient window = new EmailClient();
					window.frmEmailClient.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public EmailClient() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		// Create main frame
		// -----------------
		frmEmailClient = new JFrame();
		frmEmailClient.setTitle("Email client");
		frmEmailClient.setBounds(100, 100, 875, 540);
		frmEmailClient.addWindowListener(new WindowAdapter() {
	        public void windowClosing(WindowEvent e) {
	        	// Clean up operations (clients are already created)
	        	try {
					imapClient.close();
				} catch (MessagingException ex) {
					// Fail silently (at least in the GUI)
					System.out.println("Error closing IMAP client: " + ex.getMessage());
				}
	        }

	    });
		frmEmailClient.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Authenticate and create clients
		// -------------------------------
		try {
			SimpleAuthenticator auth = new SimpleAuthenticator(frmEmailClient);
			
			smtpClient = new SMTPClient("smtp", "smtp.gmail.com", auth);
			imapClient = new IMAPClient("imaps", "imap.googlemail.com", auth);
		}
		catch (AuthenticationFailedException e) {
			MessageBox.show("Could not log you in.", "Warning");
			System.exit(1);
		}
		catch (MessagingException e) {
			MessageBox.show("An internal error has occured.", "Error");
			System.exit(1);
		}
		
		files = new ArrayList<String>();
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frmEmailClient.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		// =======================
		// === NEW MESSAGE TAB ===
		// =======================
		
		newMessagePnl = new JPanel();
		tabbedPane.addTab("Compose", null, newMessagePnl, null);
		newMessagePnl.setLayout(null);
		
		toEmailAddressLbl = new JLabel("To:");
		toEmailAddressLbl.setBounds(39, 12, 35, 15);
		newMessagePnl.add(toEmailAddressLbl);
		
		toEmailAddressTxt = new JTextField();
		toEmailAddressTxt.setBounds(39, 33, 170, 19);
		newMessagePnl.add(toEmailAddressTxt);
		toEmailAddressTxt.setColumns(10);
		
		subjectLbl = new JLabel("Subject:");
		subjectLbl.setBounds(39, 64, 60, 15);
		newMessagePnl.add(subjectLbl);
		
		subjectTxt = new JTextField();
		subjectTxt.setBounds(39, 91, 170, 19);
		newMessagePnl.add(subjectTxt);
		subjectTxt.setColumns(10);
		
		newMessageLbl = new JLabel("Message:");
		newMessageLbl.setBounds(39, 162, 70, 15);
		newMessagePnl.add(newMessageLbl);
		
		newMessage = new JEditorPane();
		newMessage.setBounds(39, 189, 597, 210);
		newMessagePnl.add(newMessage);
		
		fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(true);
		
		// Send message button
		// -------------------
		sendMessageBtn = new JButton("Send");
		sendMessageBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					smtpClient.sendMessage(toEmailAddressTxt.getText(), ccEmailAddressTxt.getText(), files, subjectTxt.getText(), newMessage.getText());
					MessageBox.show("Message sent successfully", "Success");
					
					// Reset input
					toEmailAddressTxt.setText("");
					subjectTxt.setText("");
					newMessage.setText("");
					files = new ArrayList<String>();
				}
				catch (ClientException ex) {
					MessageBox.show(ex.getMessage(), "Error");
				}
				catch (MessagingException ex) {
					MessageBox.show("An internal error has occured whilst sending your message.", "Error");
				}
			}
		});
		sendMessageBtn.setBounds(39, 422, 97, 25);
		newMessagePnl.add(sendMessageBtn);
		
		ccEmailAddressTxt = new JTextField();
		ccEmailAddressTxt.setBounds(265, 33, 170, 19);
		newMessagePnl.add(ccEmailAddressTxt);
		ccEmailAddressTxt.setColumns(10);
		
		ccEmailAddressLbl = new JLabel("CC:");
		ccEmailAddressLbl.setBounds(265, 12, 54, 15);
		newMessagePnl.add(ccEmailAddressLbl);
		
		chooseFileBtn = new JButton("Choose a file");
		chooseFileBtn.setBounds(265, 64, 150, 25);
		chooseFileBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				fileChooser.showOpenDialog(newMessagePnl);
				
				File[] selectedFiles = fileChooser.getSelectedFiles();
				for (File file : selectedFiles) {
					files.add(file.getAbsolutePath());
				}
			}
		});
		newMessagePnl.add(chooseFileBtn);
		
		// ==========================
		// === DISPLAY EMAILS TAB ===
		// ==========================
		
		visualiseEmailPnl = new JPanel(new BorderLayout(0, 10));
		tabbedPane.addTab("Inbox", null, visualiseEmailPnl, null);
		
		emailsPnl = new JPanel();
		visualiseEmailPnl.add(emailsPnl, BorderLayout.WEST);
		emailsPnl.setLayout(new BorderLayout(0, 0));
		
		emailContentTxt = new JEditorPane();
		emailContentTxt.setEditable(false);
		emailContentTxt.setContentType("text/html");
		visualiseEmailPnl.add(new JScrollPane(emailContentTxt), BorderLayout.CENTER);
		
		searchBtn = new JButton("Search");
		refreshBtn = new JButton("Refresh");
		
		searchPnl = new JPanel();
		emailsPnl.add(searchPnl, BorderLayout.NORTH);
		searchPnl.setLayout(new BorderLayout(0, 0));
		
		searchTxt = new JTextField();
		searchPnl.add(searchTxt);
		searchTxt.setToolTipText("Search...");
		searchTxt.setColumns(10);
		
		searchPnl.add(searchBtn, BorderLayout.EAST);
		searchPnl.add(refreshBtn, BorderLayout.WEST);
		
		setFlagsPnl = new JPanel(new GridLayout(1, 4));
		final CustomTextField keywordsTxt = new CustomTextField("Enter keywords or *");
		final CustomTextField flagTxt = new CustomTextField("Enter flag");
		JButton addFlagBtn = new JButton("Add");
		JButton removeFlagBtn = new JButton("Remove");
		
		setFlagsPnl.add(keywordsTxt);
		setFlagsPnl.add(flagTxt);
		setFlagsPnl.add(addFlagBtn);
		setFlagsPnl.add(removeFlagBtn);
		
		visualiseEmailPnl.add(setFlagsPnl, BorderLayout.NORTH);
		
		try {
			final MessagesModel messagesModel = new MessagesModel(imapClient);
			messagesView = new MessagesView(messagesModel, emailContentTxt);
			emailsPnl.add(new JScrollPane(messagesView), BorderLayout.CENTER);
			
			messagesModel.addObserver(messagesView);
			
			// Refresh button
			// --------------
			refreshBtn.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						messagesModel.reset();
					} catch (MessagingException e1) {
						MessageBox.show("An internal error has occured whilst refreshing the emails.", "Error");
					}
				}
			});
			
			// Search button
			// -------------
			searchBtn.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						messagesModel.search(searchTxt.getText());
					} catch (MessagingException ex) {
						MessageBox.show("An internal error has occured whilst refreshing the emails.", "Error");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});
			
			// Add flag button
			// --------------
			addFlagBtn.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						String keywords = keywordsTxt.getText();
						String flag = flagTxt.getText();
						
						if (keywords.isEmpty() || flag.isEmpty()) {
							MessageBox.show("Please supply a value", "Warning");
							return;
						}
						
						messagesModel.setFlag(keywords, flag, true);
						MessageBox.show("Flag set successfully", "Success");
					} catch (MessagingException ex) {
						MessageBox.show("An internal error has occured whilst setting the flag.", "Error");
					}
				}
			});
			
			// Remove flag button
			// -----------------
			removeFlagBtn.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						String keywords = keywordsTxt.getText();
						String flag = flagTxt.getText();
						
						if (keywords.isEmpty() || flag.isEmpty()) {
							MessageBox.show("Please supply a value", "Warning");
							return;
						}
						
						messagesModel.setFlag(keywords, flag, false);
						MessageBox.show("Flag removed successfully", "Success");
					} catch (MessagingException e1) {
						MessageBox.show("An internal error has occured whilst removing the flag.", "Error");
					}
				}
			});
			
		} catch (MessagingException ex) {
			ex.printStackTrace();
		}
	}
}
