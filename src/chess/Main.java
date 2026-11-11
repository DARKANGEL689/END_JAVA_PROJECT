package chess;

import chess.io.GameLogger;
import chess.ui.ChessFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            GameLogger.logGlobalError(thread, throwable);
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                        null,
                        "An unexpected error occurred in thread [" + thread.getName() + "]:\n" + throwable.getMessage(),
                        "Fatal System Error",
                        JOptionPane.ERROR_MESSAGE
                );
            });
        });

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception exception) {
                GameLogger.logStatic("LOOK_AND_FEEL_SETUP", exception);
            }

            ChessFrame frame = new ChessFrame();
            frame.setVisible(true);
        });
    }
}

