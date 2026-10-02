package edu.sfsu.csc413.chess.model;

import java.util.List;
import java.util.ArrayList;

public abstract class Piece {
    private final Color color;
    private final PieceType type;

    protected Piece(Color color, PieceType type) {
        this.color  = color;
        this.type = type;
    }     // was public

    public abstract List<Move> pseudoLegalMoves(Board board, Position from);
    public boolean attacks(Board board, Position from, Position target) {
        for (Move move : pseudoLegalMoves(board, from)) {
            if (move.to().equals(target)) {
                return true;
            }
        }
        return false;
    } // default: "can I move there?"

    protected List<Move> slidingMoves(Board board, Position from, int[][] directions){
        ArrayList<Move> moves = new ArrayList<Move>();
        for( int[] slides : directions){ // d short for directions
            Position check = from.offsetOrNull(slides[0], slides[1]);
            while (check != null) {
                Piece checkpiece = board.pieceAt(check);
                Piece frompiece = board.pieceAt(from);
                boolean promoted = (frompiece.type() == PieceType.PAWN ) && ( (from.file() == 7 && frompiece.color() == Color.WHITE) || (from.file() == 0 && frompiece.color() == Color.BLACK) );
                PieceType promoteto = null;
                if(promoted){
                    promoteto = PieceType.QUEEN;
                }

                if (checkpiece == null) {
                    moves.add(new Move(from, check, frompiece, null, promoteto));
                } else {
                    if (checkpiece.color() != frompiece.color()){
                        moves.add(new Move(from, check, frompiece, checkpiece, promoteto));
                    }
                    break;
                }
                check = check.offsetOrNull(slides[0],slides[1]);
            }

        }
        return moves;
    }

    protected List<Move> steppingMoves(Board board, Position from, int[][] offsets){
        ArrayList<Move> moves = new ArrayList<Move>();
        for (int[] steps : offsets) {
            Position check = from.offsetOrNull(steps[0], steps[1]);
            Piece frompiece = board.pieceAt(from);

            if (check == null) {
                continue;
            }
            Piece checkpiece = board.pieceAt(check);
            if (checkpiece == null) { // quiet move, no piece taken
                moves.add(new Move(from, check, frompiece, null, null));
            } else if (checkpiece.color() != this.color) { // captures a piece
                moves.add(new Move(from, check, frompiece, checkpiece, null));
            }
        }
        return moves;
    }

    /*public Piece(Color color, PieceType type) {
        this.color = color;
        this.type = type;
    }*/

    public Color color() { return this.color; }
    public PieceType type() { return this.type; }

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
