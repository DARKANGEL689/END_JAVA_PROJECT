package chess.pieces;

import chess.exceptions.InvalidPromotionException;
import chess.model.Color;

public enum PieceType {
    KING("\u2654", "\u265A", "K"),
    QUEEN("\u2655", "\u265B", "Q"),
    ROOK("\u2656", "\u265C", "R"),
    BISHOP("\u2657", "\u265D", "B"),
    KNIGHT("\u2658", "\u265E", "N"),
    PAWN("\u2659", "\u265F", "");

    private final String whiteSymbol;
    private final String blackSymbol;
    private final String notationSymbol;

    PieceType(String whiteSymbol, String blackSymbol, String notationSymbol) {
        this.whiteSymbol = whiteSymbol;
        this.blackSymbol = blackSymbol;
        this.notationSymbol = notationSymbol;
    }

    public String getSymbol(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("Color cannot be null when fetching piece symbol.");
        }
        return color == Color.WHITE ? whiteSymbol : blackSymbol;
    }

    public String getNotationSymbol() {
        return notationSymbol;
    }

    public static PieceType parsePromotionType(String text) throws InvalidPromotionException {
        if (text == null || text.trim().isEmpty()) {
            throw new InvalidPromotionException("Promotion piece type cannot be empty.");
        }

        String normalized = text.trim().toUpperCase();
        if (normalized.equals("Q") || normalized.equals("QUEEN")) {
            return QUEEN;
        } else if (normalized.equals("R") || normalized.equals("ROOK")) {
            return ROOK;
        } else if (normalized.equals("B") || normalized.equals("BISHOP")) {
            return BISHOP;
        } else if (normalized.equals("N") || normalized.equals("KNIGHT")) {
            return KNIGHT;
        } else if (normalized.equals("K") || normalized.equals("KING")) {
            throw new InvalidPromotionException("Pawns cannot be promoted to a King.");
        } else if (normalized.equals("P") || normalized.equals("PAWN")) {
            throw new InvalidPromotionException("Pawns cannot be promoted to another Pawn.");
        } else {
            throw new InvalidPromotionException("Unrecognized promotion piece: " + text);
        }
    }
}

