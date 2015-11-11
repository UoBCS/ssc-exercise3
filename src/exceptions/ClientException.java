package exceptions;

/**
 * Custom exception for IMAP/SMTP clients
 */
public class ClientException extends Exception {
	public ClientException(String message) {
		super(message);
	}
}
