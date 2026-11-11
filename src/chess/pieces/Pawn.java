package chess.pieces;

import chess.model.Color;
import chess.model.Position;

public class Pawn extends Piece {
    private static final long serialVersionUID = 1L;

    public Pawn(Color color) {
        super(color, PieceType.PAWN);
    }

    @Override
    public boolean isValidPieceMovement(Position start, Position end) {
        if (start.equals(end)) {
            return false;
        }

        int direction = (getColor() == Color.WHITE) ? -1 : 1;
        int initialRow = (getColor() == Color.WHITE) ? 6 : 1;

        int rowDifference = end.getRow() - start.getRow();
        int colDifference = Math.abs(end.getCol() - start.getCol());

        if (colDifference == 0 && rowDifference == direction) {
            return true;
        }

        if (colDifference == 0 && rowDifference == 2 * direction && !hasMoved() && start.getRow() == initialRow) {
            return true;
        }

        if (colDifference == 1 && rowDifference == direction) {
            return true;
        }

        return false;
    }

    @Override
    public Piece copy() {
        Pawn clone = new Pawn(getColor());
        clone.setMoved(hasMoved());
        return clone;
    }
}

