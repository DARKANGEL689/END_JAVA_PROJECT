package chess.ui;

import chess.model.Color;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class StatusBar extends JPanel {
    private static final long serialVersionUID = 1L;

    private final JLabel turnLabel;
    private final JLabel statusMessageLabel;

    public StatusBar() {
        super(new BorderLayout(10, 0));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, java.awt.Color.GRAY),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        setPreferredSize(new Dimension(800, 36));

        turnLabel = new JLabel("Turn: White");
        turnLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        statusMessageLabel = new JLabel("Ready. Select a piece to move.");
        statusMessageLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        statusMessageLabel.setForeground(new java.awt.Color(40, 40, 40));

        add(turnLabel, BorderLayout.WEST);
        add(statusMessageLabel, BorderLayout.CENTER);
    }

    public void updateTurn(Color color, boolean inCheck, boolean isGameOver, String gameResult) {
        if (isGameOver) {
            turnLabel.setText("Game Over");
            turnLabel.setForeground(java.awt.Color.RED);
            statusMessageLabel.setText(gameResult);
            statusMessageLabel.setForeground(java.awt.Color.RED);
            return;
        }

        turnLabel.setForeground(java.awt.Color.BLACK);
        if (inCheck) {
            turnLabel.setText("Turn: " + color.getDisplayName() + " (IN CHECK!)");
            turnLabel.setForeground(new java.awt.Color(180, 0, 0));
        } else {
            turnLabel.setText("Turn: " + color.getDisplayName());
        }
    }

    public void showNormalMessage(String message) {
        statusMessageLabel.setForeground(new java.awt.Color(20, 100, 20));
        statusMessageLabel.setText(message);
    }

    public void showErrorMessage(String errorCode, String message) {
        statusMessageLabel.setForeground(new java.awt.Color(200, 0, 0));
        String codePrefix = (errorCode != null && !errorCode.isEmpty()) ? "[" + errorCode + "] " : "";
        statusMessageLabel.setText(codePrefix + message);
    }

    public void clearMessage() {
        statusMessageLabel.setForeground(new java.awt.Color(40, 40, 40));
        statusMessageLabel.setText("");
    }
}

