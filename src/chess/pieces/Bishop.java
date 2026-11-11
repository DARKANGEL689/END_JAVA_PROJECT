package chess.pieces;

import chess.model.Color;
import chess.model.Position;

public class Bishop extends Piece {
    private static final long serialVersionUID = 1L;

    public Bishop(Color color) {
        super(color, PieceType.BISHOP);
    }

    @Override
    public boolean isValidPieceMovement(Position start, Position end) {
        if (start.equals(end)) {
            return false;
        }

        int rowDifference = Math.abs(end.getRow() - start.getRow());
        int colDifference = Math.abs(end.getCol() - start.getCol());

        return rowDifference == colDifference;
    }

    @Override
    public Piece copy() {
        Bishop clone = new Bishop(getColor());
        clone.setMoved(hasMoved());
        return clone;
    }
}

