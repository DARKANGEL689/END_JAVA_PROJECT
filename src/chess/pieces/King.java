package chess.pieces;

import chess.model.Color;
import chess.model.Position;

public class King extends Piece {
    private static final long serialVersionUID = 1L;

    public King(Color color) {
        super(color, PieceType.KING);
    }

    @Override
    public boolean isValidPieceMovement(Position start, Position end) {
        if (start.equals(end)) {
            return false;
        }

        int rowDifference = Math.abs(end.getRow() - start.getRow());
        int colDifference = Math.abs(end.getCol() - start.getCol());

        if (rowDifference <= 1 && colDifference <= 1) {
            return true;
        }

        if (rowDifference == 0 && colDifference == 2 && !hasMoved()) {
            return true;
        }

        return false;
    }

    @Override
    public Piece copy() {
        King clone = new King(getColor());
        clone.setMoved(hasMoved());
        return clone;
    }
}

