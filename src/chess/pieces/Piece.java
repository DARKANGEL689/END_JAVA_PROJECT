package chess.pieces;

import chess.model.Color;
import chess.model.Position;
import java.io.Serializable;
import java.util.Objects;

public abstract class Piece implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Color color;
    private final PieceType type;
    private boolean hasMoved;

    public Piece(Color color, PieceType type) {
        if (color == null) {
            throw new IllegalArgumentException("Piece color must not be null.");
        }
        if (type == null) {
            throw new IllegalArgumentException("Piece type must not be null.");
        }
        this.color = color;
        this.type = type;
        this.hasMoved = false;
    }

    public Color getColor() {
        return color;
    }

    public PieceType getType() {
        return type;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    public String getSymbol() {
        return type.getSymbol(color);
    }

    public abstract boolean isValidPieceMovement(Position start, Position end);

    public abstract Piece copy();

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Piece other = (Piece) obj;
        return hasMoved == other.hasMoved && color == other.color && type == other.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type, hasMoved);
    }

    @Override
    public String toString() {
        return color.getDisplayName() + " " + type.name();
    }
}

