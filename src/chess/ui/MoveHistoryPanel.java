package chess.ui;

import chess.model.GameState;
import chess.model.Move;
import chess.pieces.Piece;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class MoveHistoryPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final DefaultTableModel tableModel;
    private final JTable moveTable;
    private final JLabel whiteTimerLabel;
    private final JLabel blackTimerLabel;
    private final JLabel whiteCapturedLabel;
    private final JLabel blackCapturedLabel;

    public MoveHistoryPanel() {
        super(new BorderLayout(8, 8));
        setPreferredSize(new Dimension(280, 600));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel timerPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        timerPanel.setBorder(BorderFactory.createTitledBorder("Clocks"));

        blackTimerLabel = new JLabel("Black: 10:00", JLabel.CENTER);
        blackTimerLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        whiteTimerLabel = new JLabel("White: 10:00", JLabel.CENTER);
        whiteTimerLabel.setFont(new Font("Monospaced", Font.BOLD, 18));

        timerPanel.add(blackTimerLabel);
        timerPanel.add(whiteTimerLabel);

        tableModel = new DefaultTableModel(new String[]{"#", "White", "Black"}, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        moveTable = new JTable(tableModel);
        moveTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        moveTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        moveTable.setRowHeight(22);
        moveTable.getColumnModel().getColumn(0).setPreferredWidth(35);
        moveTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        moveTable.getColumnModel().getColumn(2).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(moveTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Move History"));

        JPanel capturedPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        capturedPanel.setBorder(BorderFactory.createTitledBorder("Captured Pieces"));

        whiteCapturedLabel = new JLabel("White captures: ", JLabel.LEFT);
        whiteCapturedLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));

        blackCapturedLabel = new JLabel("Black captures: ", JLabel.LEFT);
        blackCapturedLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));

        capturedPanel.add(whiteCapturedLabel);
        capturedPanel.add(blackCapturedLabel);

        add(timerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(capturedPanel, BorderLayout.SOUTH);
    }

    public void updateView(GameState gameState) {
        if (gameState == null) {
            return;
        }

        whiteTimerLabel.setText("White: " + formatTime(gameState.getWhiteRemainingMillis()));
        blackTimerLabel.setText("Black: " + formatTime(gameState.getBlackRemainingMillis()));

        tableModel.setRowCount(0);
        List<Move> history = gameState.getMoveHistory();
        for (int i = 0; i < history.size(); i += 2) {
            int moveNumber = (i / 2) + 1;
            String whiteMove = history.get(i).toAlgebraicNotation();
            String blackMove = (i + 1 < history.size()) ? history.get(i + 1).toAlgebraicNotation() : "";
            tableModel.addRow(new Object[]{moveNumber, whiteMove, blackMove});
        }

        if (tableModel.getRowCount() > 0) {
            int lastRow = tableModel.getRowCount() - 1;
            moveTable.scrollRectToVisible(moveTable.getCellRect(lastRow, 0, true));
        }

        StringBuilder whiteCapturedBuilder = new StringBuilder("White captured: ");
        for (Piece piece : gameState.getWhiteCapturedPieces()) {
            whiteCapturedBuilder.append(piece.getSymbol()).append(" ");
        }
        whiteCapturedLabel.setText(whiteCapturedBuilder.toString());

        StringBuilder blackCapturedBuilder = new StringBuilder("Black captured: ");
        for (Piece piece : gameState.getBlackCapturedPieces()) {
            blackCapturedBuilder.append(piece.getSymbol()).append(" ");
        }
        blackCapturedLabel.setText(blackCapturedBuilder.toString());
    }

    private String formatTime(long millis) {
        long totalSeconds = millis / 1000L;
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        return String.format("%02d:%02d", minutes, seconds);
    }
}

