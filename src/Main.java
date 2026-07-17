import gui.MainWindow;

import javax.swing.*;

/**
 * Application entry point. Run this class to launch the Smart Library
 * Circulation & Automation System GUI.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // fall back to default look and feel
            }
            new MainWindow().setVisible(true);
        });
    }
}
