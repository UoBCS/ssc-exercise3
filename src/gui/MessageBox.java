package gui;

import javax.swing.JOptionPane;

public class MessageBox {
	public static void show(String infoMessage, String titleBar) {
        JOptionPane.showMessageDialog(null, infoMessage, titleBar, JOptionPane.INFORMATION_MESSAGE);
    }
}