package chess.engine;

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
import chess.model.Move;
import chess.model.Position;
import chess.pieces.Bishop;
import chess.pieces.Knight;
import chess.pieces.Piece;
import chess.pieces.PieceType;
import chess.pieces.Queen;
import chess.pieces.Rook;

public class MoveValidator {
    private final CheckDetector checkDetector;

    public MoveValidator(CheckDetector checkDetector) {
        if (checkDetector == null) {
            throw new IllegalArgumentException("CheckDetector reference cannot be null.");
        }
        this.checkDetector = checkDetector;
    }

    public CheckDetector getCheckDetector() {
        return checkDetector;
    }

    public Move validate(GameState gameState, Position start, Position end, PieceType promotionChoice) throws ChessException {
        if (gameState == null) {
            throw new IllegalArgumentException("GameState cannot be null.");
        }
        if (start == null || end == null) {
            throw new IllegalArgumentException("Start and destination positions cannot be null.");
        }

        if (gameState.isGameOver()) {
            throw new GameOverException("Game is over (" + gameState.getGameResult() + "). No further moves are permitted.");
        }

        Board board = gameState.getBoard();
        Piece movingPiece = board.getPiece(start);

        if (movingPiece == null) {
            throw new NoPieceSelectedException("No piece exists at square " + start.toAlgebraic() + ".");
        }

        if (movingPiece.getColor() != gameState.getCurrentTurn()) {
            throw new WrongTurnException("It is " + gameState.getCurrentTurn().getDisplayName() + "'s turn, but "
                    + start.toAlgebraic() + " has a " + movingPiece.getColor().getDisplayName() + " piece.");
        }

        if (start.equals(end)) {
            throw new IllegalMoveException("Destination square cannot be identical to starting square: " + start.toAlgebraic() + ".");
        }

        boolean isCastlingMove = false;
        boolean isEnPassantMove = false;
        Position enPassantCapturePosition = null;

        if (movingPiece.getType() == PieceType.KING && Math.abs(end.getCol() - start.getCol()) == 2 && start.getRow() == end.getRow()) {
            validateCastling(gameState, start, end);
            isCastlingMove = true;
        } else {
            Piece targetPiece = board.getPiece(end);
            if (targetPiece != null && targetPiece.getColor() == movingPiece.getColor()) {
                throw new OwnPieceCaptureException("Cannot capture friendly piece at " + end.toAlgebraic() + " (" + targetPiece.toString() + ").");
            }
        }

        Piece targetPiece = board.getPiece(end);

        if (!isCastlingMove && movingPiece.getType() == PieceType.PAWN) {
            int direction = (movingPiece.getColor() == Color.WHITE) ? -1 : 1;
            int rowDiff = end.getRow() - start.getRow();
            int colDiff = Math.abs(end.getCol() - start.getCol());

            if (colDiff == 0) {
                if (rowDiff == direction) {
                    if (targetPiece != null) {
                        throw new PathBlockedException("Pawn cannot advance forward into occupied square " + end.toAlgebraic() + ".");
                    }
                } else if (rowDiff == 2 * direction && !movingPiece.hasMoved()) {
                    if (targetPiece != null) {
                        throw new PathBlockedException("Pawn cannot advance forward into occupied square " + end.toAlgebraic() + ".");
                    }
                    Position intermediate = new Position(start.getRow() + direction, start.getCol());
                    if (!board.getSquare(intermediate).isEmpty()) {
                        throw new PathBlockedException("Pawn path is blocked at " + intermediate.toAlgebraic() + ".");
                    }
                } else {
                    throw new IllegalMoveException("Illegal pawn forward move distance from " + start.toAlgebraic() + " to " + end.toAlgebraic() + ".");
                }
            } else if (colDiff == 1 && rowDiff == direction) {
                if (targetPiece == null) {
                    if (end.equals(gameState.getEnPassantTarget())) {
                        isEnPassantMove = true;
                        enPassantCapturePosition = new Position(start.getRow(), end.getCol());
                    } else {
                        throw new IllegalMoveException("Pawn cannot move diagonally to " + end.toAlgebraic() + " without an enemy piece to capture.");
                    }
                }
            } else {
                throw new IllegalMoveException("Invalid pawn trajectory from " + start.toAlgebraic() + " to " + end.toAlgebraic() + ".");
            }
        } else {
            if (!movingPiece.isValidPieceMovement(start, end)) {
                throw new IllegalMoveException(movingPiece.getColor().getDisplayName() + " " + movingPiece.getType().name()
                        + " cannot move from " + start.toAlgebraic() + " to " + end.toAlgebraic() + ".");
            }

            if (movingPiece.getType() != PieceType.KNIGHT) {
                if (!checkDetector.isPathClear(board, start, end)) {
                    Position obstaclePosition = findFirstObstacle(board, start, end);
                    String obstacleDetail = obstaclePosition != null ? " at square " + obstaclePosition.toAlgebraic() : "";
                    throw new PathBlockedException("Path to " + end.toAlgebraic() + " is obstructed by another piece" + obstacleDetail + ".");
                }
            }
        }

        Piece promotionPiece = null;
        int promotionRank = (movingPiece.getColor() == Color.WHITE) ? 0 : 7;
        if (movingPiece.getType() == PieceType.PAWN && end.getRow() == promotionRank) {
            if (promotionChoice == null) {
                promotionPiece = new Queen(movingPiece.getColor());
            } else {
                promotionPiece = createPromotionPiece(movingPiece.getColor(), promotionChoice);
            }
        } else if (promotionChoice != null && movingPiece.getType() != PieceType.PAWN) {
            throw new InvalidPromotionException("Only pawns reaching the final rank can be promoted.");
        }

        verifyKingSafetyAfterMove(gameState, start, end, movingPiece, targetPiece, promotionPiece, isCastlingMove, isEnPassantMove, enPassantCapturePosition);

        return new Move(start, end, movingPiece, targetPiece, promotionPiece, isCastlingMove, isEnPassantMove, enPassantCapturePosition);
    }

    private void validateCastling(GameState gameState, Position start, Position end) throws InvalidCastlingException {
        Board board = gameState.getBoard();
        Piece king = null;
        try {
            king = board.getPiece(start);
        } catch (OutOfBoardException exception) {
            throw new InvalidCastlingException("Castling coordinates are out of board bounds.");
        }

        if (king == null || king.getType() != PieceType.KING) {
            throw new InvalidCastlingException("Castling requires a King on the start square.");
        }

        if (king.hasMoved()) {
            throw new InvalidCastlingException("Castling is not permitted because the King has already moved.");
        }

        Color color = king.getColor();
        if (checkDetector.isKingInCheck(board, color)) {
            throw new InvalidCastlingException("Castling is not permitted while the King is in check.");
        }

        boolean isKingside = end.getCol() > start.getCol();
        int rookCol = isKingside ? 7 : 0;
        Position rookPosition = null;
        try {
            rookPosition = new Position(start.getRow(), rookCol);
        } catch (OutOfBoardException exception) {
            throw new InvalidCastlingException("Invalid rook position calculation.");
        }

        Piece rook = null;
        try {
            rook = board.getPiece(rookPosition);
        } catch (OutOfBoardException exception) {
            throw new InvalidCastlingException("Failed to access rook square.");
        }

        if (rook == null || rook.getType() != PieceType.ROOK || rook.getColor() != color) {
            throw new InvalidCastlingException("Castling rook is missing from " + rookPosition.toAlgebraic() + ".");
        }

        if (rook.hasMoved()) {
            throw new InvalidCastlingException("Castling is not permitted because the chosen Rook has already moved.");
        }

        int step = isKingside ? 1 : -1;
        int checkCol = start.getCol() + step;
        while (checkCol != rookCol) {
            try {
                Position intermediate = new Position(start.getRow(), checkCol);
                if (!board.getSquare(intermediate).isEmpty()) {
                    throw new InvalidCastlingException("Cannot castle: square " + intermediate.toAlgebraic() + " between King and Rook is occupied.");
                }
            } catch (OutOfBoardException exception) {
                throw new InvalidCastlingException("Error inspecting squares between King and Rook.");
            }
            checkCol += step;
        }

        Position passThroughPosition = null;
        try {
            passThroughPosition = new Position(start.getRow(), start.getCol() + step);
        } catch (OutOfBoardException exception) {
            throw new InvalidCastlingException("Error determining castling transit square.");
        }

        if (checkDetector.isSquareAttacked(board, passThroughPosition, color.opposite())) {
            throw new InvalidCastlingException("Cannot castle: King would cross through attacked square " + passThroughPosition.toAlgebraic() + ".");
        }

        if (checkDetector.isSquareAttacked(board, end, color.opposite())) {
            throw new InvalidCastlingException("Cannot castle: destination square " + end.toAlgebraic() + " is under attack.");
        }
    }

    private void verifyKingSafetyAfterMove(GameState gameState, Position start, Position end, Piece movingPiece,
                                          Piece targetPiece, Piece promotionPiece, boolean isCastling,
                                          boolean isEnPassant, Position enPassantCapturePosition) throws KingInCheckException {
        Board simulationBoard = gameState.getBoard().copy();
        try {
            Piece pieceToPlace = (promotionPiece != null) ? promotionPiece.copy() : movingPiece.copy();
            simulationBoard.setPiece(end, pieceToPlace);
            simulationBoard.getSquare(start).clear();

            if (isEnPassant && enPassantCapturePosition != null) {
                simulationBoard.getSquare(enPassantCapturePosition).clear();
            }

            if (isCastling) {
                boolean isKingside = end.getCol() > start.getCol();
                int originalRookCol = isKingside ? 7 : 0;
                int finalRookCol = isKingside ? 5 : 3;
                Position originalRookPos = new Position(start.getRow(), originalRookCol);
                Position finalRookPos = new Position(start.getRow(), finalRookCol);
                Piece rookPiece = simulationBoard.getPiece(originalRookPos);
                simulationBoard.setPiece(finalRookPos, rookPiece);
                simulationBoard.getSquare(originalRookPos).clear();
            }

            if (checkDetector.isKingInCheck(simulationBoard, movingPiece.getColor())) {
                throw new KingInCheckException("Move leaves or places " + movingPiece.getColor().getDisplayName() + " King in check.");
            }
        } catch (OutOfBoardException exception) {
            throw new KingInCheckException("Simulation failed due to boundary error: " + exception.getMessage());
        }
    }

    private Position findFirstObstacle(Board board, Position start, Position target) throws OutOfBoardException {
        int rowStep = Integer.compare(target.getRow(), start.getRow());
        int colStep = Integer.compare(target.getCol(), start.getCol());

        int currentRow = start.getRow() + rowStep;
        int currentCol = start.getCol() + colStep;

        while (currentRow != target.getRow() || currentCol != target.getCol()) {
            if (!board.getSquare(currentRow, currentCol).isEmpty()) {
                return board.getSquare(currentRow, currentCol).getPosition();
            }
            currentRow += rowStep;
            currentCol += colStep;
        }

        return null;
    }

    private Piece createPromotionPiece(Color color, PieceType promotionType) throws InvalidPromotionException {
        if (promotionType == PieceType.QUEEN) {
            return new Queen(color);
        } else if (promotionType == PieceType.ROOK) {
            return new Rook(color);
        } else if (promotionType == PieceType.BISHOP) {
            return new Bishop(color);
        } else if (promotionType == PieceType.KNIGHT) {
            return new Knight(color);
        } else {
            throw new InvalidPromotionException("Cannot promote pawn to " + promotionType.name() + ". Allowed: QUEEN, ROOK, BISHOP, KNIGHT.");
        }
    }

    public boolean hasAnyLegalMoves(GameState gameState, Color color) {
        Board board = gameState.getBoard();
        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromCol = 0; fromCol < 8; fromCol++) {
                try {
                    Piece piece = board.getSquare(fromRow, fromCol).getPiece();
                    if (piece == null || piece.getColor() != color) {
                        continue;
                    }
                    Position startPos = board.getSquare(fromRow, fromCol).getPosition();

                    for (int toRow = 0; toRow < 8; toRow++) {
                        for (int toCol = 0; toCol < 8; toCol++) {
                            try {
                                Position endPos = board.getSquare(toRow, toCol).getPosition();
                                validate(gameState, startPos, endPos, PieceType.QUEEN);
                                return true;
                            } catch (OwnPieceCaptureException | PathBlockedException exception) {
                            } catch (InvalidCastlingException | InvalidPromotionException exception) {
                            } catch (KingInCheckException | IllegalMoveException exception) {
                            } catch (ChessException exception) {
                            }
                        }
                    }
                } catch (OutOfBoardException exception) {
                }
            }
        }
        return false;
    }
}
