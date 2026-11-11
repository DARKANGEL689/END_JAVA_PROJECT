package chess.engine;

import chess.exceptions.ChessRuntimeException;
import chess.exceptions.OutOfBoardException;
import chess.model.Board;
import chess.model.Color;
import chess.model.Position;
import chess.pieces.Piece;
import chess.pieces.PieceType;

public class CheckDetector {

    public boolean isKingInCheck(Board board, Color kingColor) throws ChessRuntimeException {
        if (board == null) {
            throw new IllegalArgumentException("Board cannot be null when checking king status.");
        }
        if (kingColor == null) {
            throw new IllegalArgumentException("King color cannot be null.");
        }

        Position kingPosition = board.findKing(kingColor);
        return isSquareAttacked(board, kingPosition, kingColor.opposite());
    }

    public boolean isSquareAttacked(Board board, Position targetSquare, Color attackerColor) {
        if (board == null || targetSquare == null || attackerColor == null) {
            throw new IllegalArgumentException("Arguments to isSquareAttacked must not be null.");
        }

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                try {
                    Piece piece = board.getSquare(row, col).getPiece();
                    if (piece == null || piece.getColor() != attackerColor) {
                        continue;
                    }

                    Position piecePosition = board.getSquare(row, col).getPosition();
                    if (canPieceAttackSquare(board, piece, piecePosition, targetSquare)) {
                        return true;
                    }
                } catch (OutOfBoardException exception) {
                    throw new ChessRuntimeException("Unexpected coordinates while scanning board for check.", exception);
                }
            }
        }

        return false;
    }

    private boolean canPieceAttackSquare(Board board, Piece attacker, Position start, Position target) throws OutOfBoardException {
        int rowDifference = target.getRow() - start.getRow();
        int colDifference = target.getCol() - start.getCol();

        if (attacker.getType() == PieceType.PAWN) {
            int attackRowDirection = (attacker.getColor() == Color.WHITE) ? -1 : 1;
            return rowDifference == attackRowDirection && Math.abs(colDifference) == 1;
        }

        if (attacker.getType() == PieceType.KNIGHT) {
            int absRow = Math.abs(rowDifference);
            int absCol = Math.abs(colDifference);
            return (absRow == 1 && absCol == 2) || (absRow == 2 && absCol == 1);
        }

        if (attacker.getType() == PieceType.KING) {
            return Math.abs(rowDifference) <= 1 && Math.abs(colDifference) <= 1 && (rowDifference != 0 || colDifference != 0);
        }

        if (attacker.isValidPieceMovement(start, target)) {
            return isPathClear(board, start, target);
        }

        return false;
    }

    public boolean isPathClear(Board board, Position start, Position target) throws OutOfBoardException {
        int rowStep = Integer.compare(target.getRow(), start.getRow());
        int colStep = Integer.compare(target.getCol(), start.getCol());

        int currentRow = start.getRow() + rowStep;
        int currentCol = start.getCol() + colStep;

        while (currentRow != target.getRow() || currentCol != target.getCol()) {
            if (!board.getSquare(currentRow, currentCol).isEmpty()) {
                return false;
            }
            currentRow += rowStep;
            currentCol += colStep;
        }

        return true;
    }
}

