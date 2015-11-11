package gui;

import javax.swing.JOptionPane;

/**
 * Displays a simple dialog message
 *
 */
public class MessageBox {
	
	/**
	 * Displays the message in a dialog box
	 * @param infoMessage The message
	 * @param titleBar The title bar text
	 */
	public static void show(String infoMessage, String titleBar) {
        JOptionPane.showMessageDialog(null, infoMessage, titleBar, JOptionPane.INFORMATION_MESSAGE);
    }
}