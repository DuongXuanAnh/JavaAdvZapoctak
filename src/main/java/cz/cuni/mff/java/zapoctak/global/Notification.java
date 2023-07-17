package cz.cuni.mff.java.zapoctak.global;
import javax.swing.JOptionPane;
public class Notification {
    /**
     * Displays an error message in a JOptionPane dialog.
     * @param message The message to display.
     */
    public static void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(null, message, message, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Displays a success message in a JOptionPane dialog.
     * @param message The message to display.
     */
    public static void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(null, message, message, JOptionPane.INFORMATION_MESSAGE);
    }
}
