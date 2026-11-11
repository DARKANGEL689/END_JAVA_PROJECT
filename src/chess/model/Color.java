package chess.model;

public enum Color {
    WHITE("White"),
    BLACK("Black");

    private final String displayName;

    Color(String displayName) {
        this.displayName = displayName;
    }

    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

