package chess.model;

import chess.exceptions.ChessRuntimeException;
import chess.exceptions.CorruptedSaveFileException;
import chess.pieces.Piece;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    private Board board;
    private Color currentTurn;
    private List<Move> moveHistory;
    private List<Piece> whiteCapturedPieces;
    private List<Piece> blackCapturedPieces;
    private Position enPassantTarget;
    private boolean isGameOver;
    private String gameResult;
    private long whiteRemainingMillis;
    private long blackRemainingMillis;

    public GameState() {
        this.board = new Board();
        this.board.setupInitialPosition();
        this.currentTurn = Color.WHITE;
        this.moveHistory = new ArrayList<>();
        this.whiteCapturedPieces = new ArrayList<>();
        this.blackCapturedPieces = new ArrayList<>();
        this.enPassantTarget = null;
        this.isGameOver = false;
        this.gameResult = "";
        this.whiteRemainingMillis = 600000L;
        this.blackRemainingMillis = 600000L;
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        if (board == null) {
            throw new IllegalArgumentException("Board cannot be null in game state.");
        }
        this.board = board;
    }

    public Color getCurrentTurn() {
        return currentTurn;
    }

    public void setCurrentTurn(Color currentTurn) {
        if (currentTurn == null) {
            throw new IllegalArgumentException("Current turn cannot be null in game state.");
        }
        this.currentTurn = currentTurn;
    }

    public List<Move> getMoveHistory() {
        return moveHistory;
    }

    public void setMoveHistory(List<Move> moveHistory) {
        this.moveHistory = moveHistory != null ? moveHistory : new ArrayList<>();
    }

    public List<Piece> getWhiteCapturedPieces() {
        return whiteCapturedPieces;
    }

    public List<Piece> getBlackCapturedPieces() {
        return blackCapturedPieces;
    }

    public Position getEnPassantTarget() {
        return enPassantTarget;
    }

    public void setEnPassantTarget(Position enPassantTarget) {
        this.enPassantTarget = enPassantTarget;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public void setGameOver(boolean isGameOver) {
        this.isGameOver = isGameOver;
    }

    public String getGameResult() {
        return gameResult;
    }

    public void setGameResult(String gameResult) {
        this.gameResult = gameResult != null ? gameResult : "";
    }

    public long getWhiteRemainingMillis() {
        return whiteRemainingMillis;
    }

    public void setWhiteRemainingMillis(long whiteRemainingMillis) {
        this.whiteRemainingMillis = whiteRemainingMillis;
    }

    public long getBlackRemainingMillis() {
        return blackRemainingMillis;
    }

    public void setBlackRemainingMillis(long blackRemainingMillis) {
        this.blackRemainingMillis = blackRemainingMillis;
    }

    public void switchTurn() {
        this.currentTurn = this.currentTurn.opposite();
    }

    public void validateIntegrity() throws CorruptedSaveFileException {
        if (board == null) {
            throw new CorruptedSaveFileException("Save file corruption detected: Board is null.");
        }
        if (currentTurn == null) {
            throw new CorruptedSaveFileException("Save file corruption detected: Current turn is null.");
        }
        if (whiteRemainingMillis < 0 || blackRemainingMillis < 0) {
            throw new CorruptedSaveFileException("Save file corruption detected: Timer has negative values.");
        }

        try {
            board.findKing(Color.WHITE);
            board.findKing(Color.BLACK);
        } catch (ChessRuntimeException exception) {
            throw new CorruptedSaveFileException("Save file corruption detected: " + exception.getMessage(), exception);
        }
    }

    public GameState copy() {
        GameState copyState = new GameState();
        copyState.board = this.board.copy();
        copyState.currentTurn = this.currentTurn;
        copyState.moveHistory = new ArrayList<>(this.moveHistory);
        copyState.whiteCapturedPieces = new ArrayList<>();
        for (Piece piece : this.whiteCapturedPieces) {
            copyState.whiteCapturedPieces.add(piece.copy());
        }
        copyState.blackCapturedPieces = new ArrayList<>();
        for (Piece piece : this.blackCapturedPieces) {
            copyState.blackCapturedPieces.add(piece.copy());
        }
        copyState.enPassantTarget = this.enPassantTarget;
        copyState.isGameOver = this.isGameOver;
        copyState.gameResult = this.gameResult;
        copyState.whiteRemainingMillis = this.whiteRemainingMillis;
        copyState.blackRemainingMillis = this.blackRemainingMillis;
        return copyState;
    }
}

