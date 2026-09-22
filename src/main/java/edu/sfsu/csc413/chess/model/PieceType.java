package edu.sfsu.csc413.chess.model;

public enum PieceType {
    PAWN('P'),
    KNIGHT('N'),
    BISHOP('B'),
    ROOK('R'),
    QUEEN('Q'),
    KING('K');

   private final char symbol;

    private PieceType(char symbol){
        this.symbol = symbol;
    }

    public char symbol(){
        return symbol;
    }

    static PieceType fromSymbol(char letter){
        char uletter = Character.toUpperCase(letter);
        return switch( uletter ){
            case 'P' -> PieceType.PAWN;
            case 'N' -> PieceType.KNIGHT;
            case 'B' -> PieceType.BISHOP;
            case 'R' -> PieceType.ROOK;
            case 'Q' -> PieceType.QUEEN;
            case 'K' -> PieceType.KING;
            default -> throw new IllegalArgumentException("The letter entered names no piece");
        };
    }
}
