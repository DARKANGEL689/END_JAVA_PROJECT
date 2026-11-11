package chess.ui;

import chess.engine.GameController;
import chess.engine.MoveValidator;
import chess.exceptions.ChessException;
import chess.exceptions.GameOverException;
import chess.exceptions.IllegalMoveException;
import chess.exceptions.InvalidCastlingException;
import chess.exceptions.InvalidPromotionException;
import chess.exceptions.KingInCheckException;
import chess.exceptions.NoPieceSelectedException;
import chess.exceptions.OutOfBoardException;
import chess.exceptions.OwnPieceCaptureException;
import chess.exceptions.PathBlockedException;
import chess.exceptions.WrongTurnException;
import chess.model.Board;
import chess.model.Color;
import chess.model.GameState;
import chess.model.Position;
import chess.pieces.Piece;
import chess.pieces.PieceType;
import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

public class BoardPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private static final java.awt.Color LIGHT_SQUARE_COLOR = new java.awt.Color(240, 217, 181);
    private static final java.awt.Color DARK_SQUARE_COLOR = new java.awt.Color(181, 136, 99);
    private static final java.awt.Color SELECTED_COLOR = new java.awt.Color(246, 246, 105, 180);
    private static final java.awt.Color LEGAL_MOVE_COLOR = new java.awt.Color(100, 220, 100, 180);
    private static final java.awt.Color ERROR_COLOR = new java.awt.Color(255, 60, 60, 200);
    private static final java.awt.Color CHECK_COLOR = new java.awt.Color(255, 0, 0, 140);

    private final GameController controller;
    private final ChessFrame frame;
    private Position selectedPosition;
    private final List<Position> legalMovePositions;
    private Position errorFlashPosition;
    private Timer errorFlashTimer;

    public BoardPanel(GameController controller, ChessFrame frame) {
        if (controller == null || frame == null) {
            throw new IllegalArgumentException("Controller and Frame cannot be null.");
        }
        this.controller = controller;
        this.frame = frame;
        this.selectedPosition = null;
        this.legalMovePositions = new ArrayList<>();
        this.errorFlashPosition = null;

        setPreferredSize(new Dimension(560, 560));
        setMinimumSize(new Dimension(400, 400));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                handleBoardClick(event.getX(), event.getY());
            }
        });
    }

    private void handleBoardClick(int mouseX, int mouseY) {
        int squareSize = getSquareSize();
        if (squareSize <= 0) {
            return;
        }

        int col = mouseX / squareSize;
        int row = mouseY / squareSize;

        Position clickedPosition;
        try {
            clickedPosition = new Position(row, col);
        } catch (OutOfBoardException exception) {
            frame.handleException("ERR_OUT_OF_BOARD", exception);
            return;
        }

        GameState gameState = controller.getCurrentState();

        if (selectedPosition == null) {
            try {
                Piece piece = gameState.getBoard().getPiece(clickedPosition);
                if (piece == null) {
                    throw new NoPieceSelectedException("No piece at " + clickedPosition.toAlgebraic() + ". Select a valid piece to move.");
                }
                if (piece.getColor() != gameState.getCurrentTurn()) {
                    throw new WrongTurnException("It is " + gameState.getCurrentTurn().getDisplayName() + "'s turn. You clicked a " + piece.getColor().getDisplayName() + " piece.");
                }

                selectedPosition = clickedPosition;
                calculateLegalMoves(selectedPosition);
                frame.getStatusBar().showNormalMessage("Selected " + piece.toString() + " at " + clickedPosition.toAlgebraic());
                repaint();
            } catch (NoPieceSelectedException | WrongTurnException exception) {
                flashErrorSquare(clickedPosition);
                frame.handleException(exception.getErrorCode(), exception);
            } catch (ChessException exception) {
                flashErrorSquare(clickedPosition);
                frame.handleException(exception.getErrorCode(), exception);
            }
        } else {
            if (clickedPosition.equals(selectedPosition)) {
                selectedPosition = null;
                legalMovePositions.clear();
                frame.getStatusBar().showNormalMessage("Deselected square.");
                repaint();
                return;
            }

            try {
                Piece movingPiece = gameState.getBoard().getPiece(selectedPosition);
                PieceType promotionChoice = null;

                if (movingPiece != null && movingPiece.getType() == PieceType.PAWN) {
                    int promotionRow = (movingPiece.getColor() == Color.WHITE) ? 0 : 7;
                    if (clickedPosition.getRow() == promotionRow && Math.abs(clickedPosition.getRow() - selectedPosition.getRow()) == 1) {
                        promotionChoice = promptPromotionChoice(movingPiece.getColor());
                    }
                }

                controller.makeMove(selectedPosition, clickedPosition, promotionChoice);
                frame.onMoveCompleted();
            } catch (OutOfBoardException exception) {
                flashErrorSquare(clickedPosition);
                frame.handleException("ERR_OUT_OF_BOARD", exception);
            } catch (WrongTurnException exception) {
                flashErrorSquare(clickedPosition);
                frame.handleException("ERR_WRONG_TURN", exception);
            } catch (NoPieceSelectedException exception) {
                frame.handleException("ERR_NO_PIECE_SELECTED", exception);
            } catch (OwnPieceCaptureException exception) {
                flashErrorSquare(clickedPosition);
                frame.handleException("ERR_OWN_PIECE_CAPTURE", exception);
            } catch (PathBlockedException exception) {
                flashErrorSquare(clickedPosition);
                frame.handleException("ERR_PATH_BLOCKED", exception);
            } catch (KingInCheckException exception) {
                flashErrorSquare(clickedPosition);
                frame.handleException("ERR_KING_IN_CHECK", exception);
            } catch (InvalidCastlingException exception) {
                flashErrorSquare(clickedPosition);
                frame.handleException("ERR_INVALID_CASTLING", exception);
            } catch (InvalidPromotionException exception) {
                flashErrorSquare(clickedPosition);
                frame.handleException("ERR_INVALID_PROMOTION", exception);
            } catch (GameOverException exception) {
                frame.handleException("ERR_GAME_OVER", exception);
            } catch (IllegalMoveException exception) {
                flashErrorSquare(clickedPosition);
                frame.handleException("ERR_ILLEGAL_MOVE", exception);
            } catch (ChessException exception) {
                flashErrorSquare(clickedPosition);
                frame.handleException(exception.getErrorCode(), exception);
            } catch (Exception exception) {
                frame.handleException("ERR_UNEXPECTED", exception);
            } finally {
                selectedPosition = null;
                legalMovePositions.clear();
                repaint();
            }
        }
    }

    private void calculateLegalMoves(Position start) {
        legalMovePositions.clear();
        GameState gameState = controller.getCurrentState();
        MoveValidator validator = controller.getGameRules().getMoveValidator();

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                try {
                    Position target = new Position(r, c);
                    validator.validate(gameState, start, target, PieceType.QUEEN);
                    legalMovePositions.add(target);
                } catch (ChessException ignored) {
                }
            }
        }
    }

    private PieceType promptPromotionChoice(Color color) throws InvalidPromotionException {
        Object[] options = {"Queen", "Rook", "Bishop", "Knight"};
        int response = JOptionPane.showOptionDialog(
                this,
                "Choose promotion piece for " + color.getDisplayName() + " pawn:",
                "Pawn Promotion",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (response == 0) {
            return PieceType.QUEEN;
        } else if (response == 1) {
            return PieceType.ROOK;
        } else if (response == 2) {
            return PieceType.BISHOP;
        } else if (response == 3) {
            return PieceType.KNIGHT;
        } else {
            throw new InvalidPromotionException("Pawn promotion selection cancelled.");
        }
    }

    public void flashErrorSquare(Position position) {
        this.errorFlashPosition = position;
        if (errorFlashTimer != null && errorFlashTimer.isRunning()) {
            errorFlashTimer.stop();
        }
        errorFlashTimer = new Timer(650, event -> {
            errorFlashPosition = null;
            repaint();
        });
        errorFlashTimer.setRepeats(false);
        errorFlashTimer.start();
        repaint();
    }

    public void clearSelection() {
        this.selectedPosition = null;
        this.legalMovePositions.clear();
        this.errorFlashPosition = null;
        repaint();
    }

    private int getSquareSize() {
        return Math.min(getWidth(), getHeight()) / 8;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2d = (Graphics2D) graphics;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int squareSize = getSquareSize();
        GameState gameState = controller.getCurrentState();
        Board board = gameState.getBoard();

        Position checkedKingPosition = null;
        if (controller.getGameRules().getCheckDetector().isKingInCheck(board, gameState.getCurrentTurn())) {
            try {
                checkedKingPosition = board.findKing(gameState.getCurrentTurn());
            } catch (Exception ignored) {
            }
        }

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int x = col * squareSize;
                int y = row * squareSize;

                boolean isLight = (row + col) % 2 == 0;
                g2d.setColor(isLight ? LIGHT_SQUARE_COLOR : DARK_SQUARE_COLOR);
                g2d.fillRect(x, y, squareSize, squareSize);

                try {
                    Position pos = new Position(row, col);

                    if (checkedKingPosition != null && checkedKingPosition.equals(pos)) {
                        g2d.setColor(CHECK_COLOR);
                        g2d.fillRect(x, y, squareSize, squareSize);
                    }

                    if (selectedPosition != null && selectedPosition.equals(pos)) {
                        g2d.setColor(SELECTED_COLOR);
                        g2d.fillRect(x, y, squareSize, squareSize);
                    }

                    if (legalMovePositions.contains(pos)) {
                        g2d.setColor(LEGAL_MOVE_COLOR);
                        int circleDiameter = squareSize / 3;
                        int cx = x + (squareSize - circleDiameter) / 2;
                        int cy = y + (squareSize - circleDiameter) / 2;
                        g2d.fillOval(cx, cy, circleDiameter, circleDiameter);
                        g2d.setStroke(new BasicStroke(2));
                        g2d.drawRect(x + 1, y + 1, squareSize - 2, squareSize - 2);
                    }

                    if (errorFlashPosition != null && errorFlashPosition.equals(pos)) {
                        g2d.setColor(ERROR_COLOR);
                        g2d.fillRect(x, y, squareSize, squareSize);
                    }

                    Piece piece = board.getPiece(pos);
                    if (piece != null) {
                        g2d.setFont(new Font("SansSerif", Font.BOLD, (int) (squareSize * 0.72)));
                        g2d.setColor(piece.getColor() == Color.WHITE ? java.awt.Color.WHITE : java.awt.Color.BLACK);
                        String symbol = piece.getSymbol();
                        int textX = x + (squareSize - g2d.getFontMetrics().stringWidth(symbol)) / 2;
                        int textY = y + ((squareSize - g2d.getFontMetrics().getHeight()) / 2) + g2d.getFontMetrics().getAscent();

                        g2d.setColor(new java.awt.Color(0, 0, 0, 160));
                        g2d.drawString(symbol, textX + 1, textY + 1);

                        g2d.setColor(piece.getColor() == Color.WHITE ? new java.awt.Color(255, 255, 255) : new java.awt.Color(20, 20, 20));
                        g2d.drawString(symbol, textX, textY);
                    }
                } catch (OutOfBoardException ignored) {
                }
            }
        }
    }
}

