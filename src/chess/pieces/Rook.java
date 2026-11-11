package chess.pieces;

import chess.model.Color;
import chess.model.Position;

public class Rook extends Piece {
    private static final long serialVersionUID = 1L;

    public Rook(Color color) {
        super(color, PieceType.ROOK);
    }

    @Override
    public boolean isValidPieceMovement(Position start, Position end) {
        if (start.equals(end)) {
            return false;
        }

        int rowDifference = Math.abs(end.getRow() - start.getRow());
        int colDifference = Math.abs(end.getCol() - start.getCol());

        return rowDifference == 0 || colDifference == 0;
    }

    @Override
    public Piece copy() {
        Rook clone = new Rook(getColor());
        clone.setMoved(hasMoved());
        return clone;
    }
}

