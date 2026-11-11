package chess.engine;

import chess.exceptions.ChessException;
import chess.exceptions.CorruptedSaveFileException;
import chess.exceptions.InvalidMoveFormatException;
import chess.io.GameLoader;
import chess.io.GameSaver;
import chess.model.Color;
import chess.model.GameState;
import chess.model.Move;
import chess.model.Position;
import chess.pieces.PieceType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class GameController {
    private GameState currentState;
    private final Deque<GameState> stateHistory;
    private final GameRules gameRules;

    public GameController() {
        this.currentState = new GameState();
        this.stateHistory = new ArrayDeque<>();
        CheckDetector checkDetector = new CheckDetector();
        MoveValidator moveValidator = new MoveValidator(checkDetector);
        this.gameRules = new GameRules(moveValidator);
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public GameRules getGameRules() {
        return gameRules;
    }

    public void newGame() {
        stateHistory.clear();
        currentState = new GameState();
    }

    public Move makeMove(Position start, Position end) throws ChessException {
        return makeMove(start, end, null);
    }

    public Move makeMove(Position start, Position end, PieceType promotion) throws ChessException {
        if (currentState.isGameOver()) {
            throw new IllegalStateException("Cannot make move: game is already finished.");
        }

        GameState previousSnapshot = currentState.copy();
        Move executedMove = gameRules.executeMove(currentState, start, end, promotion);
        stateHistory.push(previousSnapshot);
        return executedMove;
    }

    public Move makeMoveFromAlgebraic(String textInput) throws ChessException {
        if (textInput == null || textInput.trim().isEmpty()) {
            throw new InvalidMoveFormatException("Move command cannot be empty. Expected format: 'e2 e4' or 'e7 e8 Q'.");
        }

        String sanitized = textInput.trim().replace("-", " ");
        Position start;
        Position end;
        PieceType promotionChoice = null;

        try (Scanner scanner = new Scanner(sanitized)) {
            String startSquareToken;
            String endSquareToken;

            try {
                startSquareToken = scanner.next();
                endSquareToken = scanner.next();
            } catch (InputMismatchException inputMismatchException) {
                throw new InvalidMoveFormatException("Input token mismatch while parsing coordinates.", inputMismatchException);
            } catch (NoSuchElementException noSuchElementException) {
                throw new InvalidMoveFormatException("Input must contain at least start and end squares (e.g., 'e2 e4').", noSuchElementException);
            }

            start = Position.fromAlgebraic(startSquareToken);
            end = Position.fromAlgebraic(endSquareToken);

            if (scanner.hasNext()) {
                String promotionToken = scanner.next();
                promotionChoice = PieceType.parsePromotionType(promotionToken);
            }
        }

        return makeMove(start, end, promotionChoice);
    }

    public void undoMove() throws IllegalStateException {
        if (stateHistory.isEmpty()) {
            throw new IllegalStateException("Cannot undo move: no previous moves recorded in history.");
        }
        currentState = stateHistory.pop();
    }

    public void saveGame(File targetFile) throws CorruptedSaveFileException, IOException {
        GameSaver.saveGame(currentState, targetFile);
    }

    public void loadGame(File sourceFile) throws CorruptedSaveFileException, FileNotFoundException {
        GameState loadedState = GameLoader.loadGame(sourceFile);
        stateHistory.clear();
        currentState = loadedState;
        gameRules.updateGameStateStatus(currentState);
    }

    public boolean tickTimer() {
        if (currentState.isGameOver()) {
            return false;
        }

        if (currentState.getCurrentTurn() == Color.WHITE) {
            long remaining = currentState.getWhiteRemainingMillis() - 1000L;
            currentState.setWhiteRemainingMillis(Math.max(0L, remaining));
            if (remaining <= 0) {
                currentState.setGameOver(true);
                currentState.setGameResult("White ran out of time! Black wins.");
                return true;
            }
        } else {
            long remaining = currentState.getBlackRemainingMillis() - 1000L;
            currentState.setBlackRemainingMillis(Math.max(0L, remaining));
            if (remaining <= 0) {
                currentState.setGameOver(true);
                currentState.setGameResult("Black ran out of time! White wins.");
                return true;
            }
        }

        return false;
    }
}
