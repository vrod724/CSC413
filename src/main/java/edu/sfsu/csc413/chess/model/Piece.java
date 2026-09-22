package edu.sfsu.csc413.chess.model;

public class Piece {
    private final Color color;
    private final PieceType type;

    public Piece(Color color, PieceType type) {
        this.color = color;
        this.type = type;
    }

    public Color color() { return color; }
    public PieceType type() { return type; }

   public char symbol() {
        boolean white = this.color == Color.WHITE;
        return switch (this.type()) {
            case KING -> white ? 'K' : 'k';
            case QUEEN -> white ? 'Q' : 'q';
            case ROOK -> white ? 'R' : 'r';
            case BISHOP -> white ? 'B' : 'b';
            case KNIGHT -> white ? 'N' : 'n';
            case PAWN -> white ? 'P' : 'p';
        };
    }

    @Override
    public String toString() {
        return String.valueOf(symbol());
    }
}
