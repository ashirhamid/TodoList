package edu.oel.tasknest;

import java.awt.Font;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Starts the TaskNest desktop application.
 * @author OEL Student Team (add team names before submission)
 */
public final class TaskNestApp {
    private TaskNestApp() { }

    /** Starts the application on Swing's event dispatch thread.
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Swing's cross-platform look and feel remains available as a fallback.
        }
        UIManager.put("Button.font", new Font("Palatino Linotype", Font.PLAIN, 14));
        UIManager.put("Label.font", new Font("Palatino Linotype", Font.PLAIN, 14));
        UIManager.put("TextField.font", new Font("Palatino Linotype", Font.PLAIN, 14));
        UIManager.put("ComboBox.font", new Font("Palatino Linotype", Font.PLAIN, 14));
        UIManager.put("TextArea.font", new Font("Palatino Linotype", Font.PLAIN, 14));
        SwingUtilities.invokeLater(() -> new TaskManagerPanel().showWindow());
    }
}
