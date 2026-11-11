package chess.ui;

import chess.engine.GameController;
import chess.exceptions.ChessException;
import chess.exceptions.CorruptedSaveFileException;
import chess.exceptions.IllegalMoveException;
import chess.exceptions.InvalidMoveFormatException;
import chess.exceptions.KingInCheckException;
import chess.exceptions.OutOfBoardException;
import chess.exceptions.PathBlockedException;
import chess.io.GameLogger;
import chess.model.Color;
import chess.model.GameState;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

public class ChessFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    private final GameController controller;
    private final BoardPanel boardPanel;
    private final MoveHistoryPanel moveHistoryPanel;
    private final StatusBar statusBar;
    private final JTextField moveInputField;
    private final Timer gameClockTimer;

    public ChessFrame() {
        super("Java Chess - Exception Handling Architecture");

        this.controller = new GameController();
        this.boardPanel = new BoardPanel(controller, this);
        this.moveHistoryPanel = new MoveHistoryPanel();
        this.statusBar = new StatusBar();
        this.moveInputField = new JTextField(12);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel controlToolBar = createToolBar();
        add(controlToolBar, BorderLayout.NORTH);

        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.add(boardPanel, BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);

        add(moveHistoryPanel, BorderLayout.EAST);

        JPanel bottomContainer = new JPanel(new BorderLayout());
        JPanel textInputPanel = createTextInputPanel();
        bottomContainer.add(textInputPanel, BorderLayout.NORTH);
        bottomContainer.add(statusBar, BorderLayout.SOUTH);
        add(bottomContainer, BorderLayout.SOUTH);

        this.gameClockTimer = new Timer(1000, event -> {
            boolean stateChanged = controller.tickTimer();
            moveHistoryPanel.updateView(controller.getCurrentState());
            if (stateChanged) {
                updateUIState();
            }
        });
        this.gameClockTimer.start();

        updateUIState();
        pack();
        setMinimumSize(new Dimension(860, 680));
        setLocationRelativeTo(null);
    }

    private JPanel createToolBar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));

        JButton newGameButton = new JButton("New Game");
        newGameButton.addActionListener(event -> handleNewGame());

        JButton undoButton = new JButton("Undo Move");
        undoButton.addActionListener(event -> handleUndoMove());

        JButton saveButton = new JButton("Save Game");
        saveButton.addActionListener(event -> handleSaveGame());

        JButton loadButton = new JButton("Load Game");
        loadButton.addActionListener(event -> handleLoadGame());

        toolbar.add(newGameButton);
        toolbar.add(undoButton);
        toolbar.add(saveButton);
        toolbar.add(loadButton);

        return toolbar;
    }

    private JPanel createTextInputPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JLabel inputPromptLabel = new JLabel("Manual Move (e.g., 'e2 e4' or 'e7 e8 Q'):");
        inputPromptLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        moveInputField.setFont(new Font("Monospaced", Font.BOLD, 13));
        moveInputField.addActionListener(event -> handleTextMove());

        JButton executeMoveButton = new JButton("Submit Move");
        executeMoveButton.addActionListener(event -> handleTextMove());

        panel.add(inputPromptLabel);
        panel.add(moveInputField);
        panel.add(executeMoveButton);

        return panel;
    }

    public StatusBar getStatusBar() {
        return statusBar;
    }

    public void onMoveCompleted() {
        updateUIState();
        statusBar.showNormalMessage("Move accepted.");
    }

    public void updateUIState() {
        GameState gameState = controller.getCurrentState();
        boolean inCheck = controller.getGameRules().getCheckDetector().isKingInCheck(gameState.getBoard(), gameState.getCurrentTurn());
        statusBar.updateTurn(gameState.getCurrentTurn(), inCheck, gameState.isGameOver(), gameState.getGameResult());
        moveHistoryPanel.updateView(gameState);
        boardPanel.repaint();
    }

    public void handleException(String errorCode, Exception exception) {
        GameLogger.logStatic("UI_HANDLED_EXCEPTION", exception);
        statusBar.showErrorMessage(errorCode, exception.getMessage());
    }

    private void handleTextMove() {
        String text = moveInputField.getText();
        if (text == null || text.trim().isEmpty()) {
            statusBar.showErrorMessage("ERR_INPUT_EMPTY", "Please enter a move notation (e.g. 'e2 e4').");
            return;
        }

        try {
            controller.makeMoveFromAlgebraic(text);
            moveInputField.setText("");
            onMoveCompleted();
        } catch (InvalidMoveFormatException exception) {
            handleException("ERR_INVALID_MOVE_FORMAT", exception);
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Invalid Move Format", JOptionPane.WARNING_MESSAGE);
        } catch (OutOfBoardException exception) {
            handleException("ERR_OUT_OF_BOARD", exception);
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Coordinate Out Of Bounds", JOptionPane.WARNING_MESSAGE);
        } catch (PathBlockedException exception) {
            handleException("ERR_PATH_BLOCKED", exception);
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Path Blocked", JOptionPane.WARNING_MESSAGE);
        } catch (KingInCheckException exception) {
            handleException("ERR_KING_IN_CHECK", exception);
            JOptionPane.showMessageDialog(this, exception.getMessage(), "King In Check", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalMoveException exception) {
            handleException("ERR_ILLEGAL_MOVE", exception);
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Illegal Move", JOptionPane.WARNING_MESSAGE);
        } catch (ChessException exception) {
            handleException(exception.getErrorCode(), exception);
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Chess Rule Violation", JOptionPane.WARNING_MESSAGE);
        } catch (Exception exception) {
            handleException("ERR_UNEXPECTED", exception);
            JOptionPane.showMessageDialog(this, "An unexpected error occurred: " + exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            boardPanel.clearSelection();
        }
    }

    private void handleNewGame() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to start a new game?", "New Game", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            controller.newGame();
            updateUIState();
            statusBar.showNormalMessage("New game started. White to move.");
        }
    }

    private void handleUndoMove() {
        try {
            controller.undoMove();
            updateUIState();
            statusBar.showNormalMessage("Previous move was undone.");
        } catch (IllegalStateException exception) {
            GameLogger.logStatic("UNDO_OPERATION_FAILED", exception);
            statusBar.showErrorMessage("ERR_ILLEGAL_STATE", exception.getMessage());
            JOptionPane.showMessageDialog(this, exception.getMessage(), "Undo Unavailable", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleSaveGame() {
        JFileChooser fileChooser = new JFileChooser(".");
        fileChooser.setDialogTitle("Save Chess Game");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Chess Save Files (*.ser)", "ser"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (!selectedFile.getName().toLowerCase().endsWith(".ser")) {
                selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + ".ser");
            }

            try {
                controller.saveGame(selectedFile);
                statusBar.showNormalMessage("Game successfully saved to " + selectedFile.getName());
                JOptionPane.showMessageDialog(this, "Game saved successfully!", "Save Game", JOptionPane.INFORMATION_MESSAGE);
            } catch (CorruptedSaveFileException exception) {
                GameLogger.logStatic("SAVE_INTEGRITY_ERROR", exception);
                statusBar.showErrorMessage(exception.getErrorCode(), exception.getMessage());
                JOptionPane.showMessageDialog(this, "Failed to save: " + exception.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
            } catch (IOException exception) {
                GameLogger.logStatic("SAVE_IO_ERROR", exception);
                statusBar.showErrorMessage("ERR_IO", "File output error: " + exception.getMessage());
                JOptionPane.showMessageDialog(this, "I/O error saving game: " + exception.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleLoadGame() {
        JFileChooser fileChooser = new JFileChooser(".");
        fileChooser.setDialogTitle("Load Chess Game");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Chess Save Files (*.ser)", "ser"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            try {
                controller.loadGame(selectedFile);
                updateUIState();
                statusBar.showNormalMessage("Game successfully loaded from " + selectedFile.getName());
                JOptionPane.showMessageDialog(this, "Game loaded successfully!", "Load Game", JOptionPane.INFORMATION_MESSAGE);
            } catch (FileNotFoundException exception) {
                GameLogger.logStatic("LOAD_FILE_NOT_FOUND", exception);
                statusBar.showErrorMessage("ERR_FILE_NOT_FOUND", exception.getMessage());
                JOptionPane.showMessageDialog(this, "File not found: " + exception.getMessage(), "File Missing", JOptionPane.ERROR_MESSAGE);
            } catch (CorruptedSaveFileException exception) {
                GameLogger.logStatic("LOAD_CORRUPTED_FILE", exception);
                String detail = exception.getMessage();
                if (exception.getCause() != null) {
                    detail += "\nRoot Cause: " + exception.getCause().getClass().getSimpleName() + " - " + exception.getCause().getMessage();
                }
                statusBar.showErrorMessage(exception.getErrorCode(), exception.getMessage());
                JOptionPane.showMessageDialog(this, "Corrupted save file:\n" + detail, "Corrupted Save File", JOptionPane.ERROR_MESSAGE);
            } catch (Exception exception) {
                GameLogger.logStatic("LOAD_UNEXPECTED_ERROR", exception);
                statusBar.showErrorMessage("ERR_UNEXPECTED", exception.getMessage());
                JOptionPane.showMessageDialog(this, "Unexpected error loading game: " + exception.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

