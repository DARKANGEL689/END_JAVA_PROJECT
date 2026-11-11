package chess.engine;

import chess.exceptions.ChessException;
import chess.exceptions.OutOfBoardException;
import chess.io.GameLogger;
import chess.model.Board;
import chess.model.Color;
import chess.model.GameState;
import chess.model.Move;
import chess.model.Position;
import chess.pieces.Piece;
import chess.pieces.PieceType;

public class GameRules {
    private final MoveValidator moveValidator;
    private final CheckDetector checkDetector;

    public GameRules(MoveValidator moveValidator) {
        if (moveValidator == null) {
            throw new IllegalArgumentException("MoveValidator cannot be null.");
        }
        this.moveValidator = moveValidator;
        this.checkDetector = moveValidator.getCheckDetector();
    }

    public MoveValidator getMoveValidator() {
        return moveValidator;
    }

    public CheckDetector getCheckDetector() {
        return checkDetector;
    }

    public Move executeMove(GameState gameState, Position start, Position end, PieceType promotion) throws ChessException {
        try {
            Move validatedMove = moveValidator.validate(gameState, start, end, promotion);
            applyMoveToState(gameState, validatedMove);
            updateGameStateStatus(gameState);
            return validatedMove;
        } catch (ChessException chessException) {
            GameLogger.logStatic("MOVE_EXECUTION_RETHROWN", chessException);
            throw chessException;
        }
    }

    private void applyMoveToState(GameState gameState, Move move) throws OutOfBoardException {
        Board board = gameState.getBoard();
        Position start = move.getStart();
        Position end = move.getEnd();
        Piece movingPiece = move.getMovedPiece();

        Piece pieceToPlace = (move.getPromotionPiece() != null) ? move.getPromotionPiece() : movingPiece;
        pieceToPlace.setMoved(true);

        board.setPiece(end, pieceToPlace);
        board.getSquare(start).clear();

        if (move.getCapturedPiece() != null) {
            if (move.getCapturedPiece().getColor() == Color.WHITE) {
                gameState.getWhiteCapturedPieces().add(move.getCapturedPiece());
            } else {
                gameState.getBlackCapturedPieces().add(move.getCapturedPiece());
            }
        }

        if (move.isEnPassant() && move.getEnPassantCapturePosition() != null) {
            Piece capturedPawn = board.getPiece(move.getEnPassantCapturePosition());
            board.getSquare(move.getEnPassantCapturePosition()).clear();
            if (capturedPawn != null) {
                if (capturedPawn.getColor() == Color.WHITE) {
                    gameState.getWhiteCapturedPieces().add(capturedPawn);
                } else {
                    gameState.getBlackCapturedPieces().add(capturedPawn);
                }
            }
        }

        if (move.isCastling()) {
            boolean isKingside = end.getCol() > start.getCol();
            int rookSourceCol = isKingside ? 7 : 0;
            int rookDestCol = isKingside ? 5 : 3;

            Position rookSourcePos = new Position(start.getRow(), rookSourceCol);
            Position rookDestPos = new Position(start.getRow(), rookDestCol);

            Piece rook = board.getPiece(rookSourcePos);
            rook.setMoved(true);
            board.setPiece(rookDestPos, rook);
            board.getSquare(rookSourcePos).clear();
        }

        if (movingPiece.getType() == PieceType.PAWN && Math.abs(end.getRow() - start.getRow()) == 2) {
            int middleRow = (start.getRow() + end.getRow()) / 2;
            gameState.setEnPassantTarget(new Position(middleRow, start.getCol()));
        } else {
            gameState.setEnPassantTarget(null);
        }

        gameState.getMoveHistory().add(move);
        gameState.switchTurn();
    }

    public void updateGameStateStatus(GameState gameState) {
        Color activeColor = gameState.getCurrentTurn();
        boolean inCheck = checkDetector.isKingInCheck(gameState.getBoard(), activeColor);
        boolean hasMoves = moveValidator.hasAnyLegalMoves(gameState, activeColor);

        if (!hasMoves) {
            gameState.setGameOver(true);
            if (inCheck) {
                gameState.setGameResult("Checkmate! " + activeColor.opposite().getDisplayName() + " wins.");
            } else {
                gameState.setGameResult("Stalemate! Game drawn.");
            }
        }
    }
}

