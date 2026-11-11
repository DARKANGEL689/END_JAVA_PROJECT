package chess.model;

import chess.pieces.Piece;
import java.io.Serializable;
import java.util.Objects;

public class Square implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Position position;
    private Piece piece;

    public Square(Position position) {
        if (position == null) {
            throw new IllegalArgumentException("Square position must not be null.");
        }
        this.position = position;
        this.piece = null;
    }

    public Square(Position position, Piece piece) {
        if (position == null) {
            throw new IllegalArgumentException("Square position must not be null.");
        }
        this.position = position;
        this.piece = piece;
    }

    public Position getPosition() {
        return position;
    }

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public void clear() {
        this.piece = null;
    }

    public boolean isEmpty() {
        return piece == null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Square other = (Square) obj;
        return Objects.equals(position, other.position) && Objects.equals(piece, other.piece);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, piece);
    }

    @Override
    public String toString() {
        return position.toAlgebraic() + (piece == null ? ": Empty" : ": " + piece.toString());
    }
}

