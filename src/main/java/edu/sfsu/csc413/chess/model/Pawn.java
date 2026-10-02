package edu.sfsu.csc413.chess.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The pawn — the piece that breaks every rule the others follow.
 *
 * <p>It is the only piece that moves in just one direction, the only one whose
 * capture differs from its move, the only one with a special first move, and
 * the only one that turns into something else. It is worth noticing that all of
 * that awkwardness is contained in this one file. No other class in the engine
 * knows that pawns are strange. That containment is the payoff of polymorphism:
 * the irregular case costs one class, not a special case in every method that
 * touches a piece.
 *
 * <p>En passant is not handled here. Like castling, it depends on the previous
 * move rather than on the current board, so it waits for Week 15 when
 * {@code Game} owns the move history.
 */
public class Pawn extends Piece {

    /**
     * What a pawn may become on reaching the far rank.
     */

    private static final PieceType[] PROMOTION_CHOICES = { PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT };

    public Pawn(Color color) {
        super(color, PieceType.PAWN);
    }



    @Override
    public List<Move> pseudoLegalMoves(Board board, Position from) {
        ArrayList<Move> moves = new ArrayList<Move>();
        int numofmoves = 1;
        boolean first = (from.rank() == 1 && this.color() == Color.WHITE ) || (from.rank() == 6 && this.color() == Color.BLACK );
        if(first) {
            numofmoves++;
        }
        // forward checks
        int color = (this.color() == Color.WHITE) ? 1 : -1;


        for( int rankdelta = 1; rankdelta < (numofmoves + 1 ); rankdelta++){
            int second = 0;
            if(rankdelta == 1){
                second++;
            }
            Position check = from.offsetOrNull(0, rankdelta * color);
            Piece frompiece = board.pieceAt(from);

            if(check == null){
                break;
            }
            Piece checkpiece = board.pieceAt(check);
            if(checkpiece == null){
                boolean promoted = (frompiece.type() == PieceType.PAWN ) && ( (check.rank() == 7 && frompiece.color() == Color.WHITE) || (check.rank() == 0 && frompiece.color() == Color.BLACK) );
                if(promoted){
                    addAllPromotionalChecks(moves,from, check,frompiece, null, PROMOTION_CHOICES);
                } else moves.add(new Move(from, check, frompiece, null, null));
            } else break;
        }

       // diagonal checks
        int[][] deltas = new int[2][2];
        if(this.color() == Color.WHITE){
            deltas[0][0] = -1; deltas[0][1] = 1;
            deltas[1][0] = 1; deltas[1][1] = 1;
        } else{
            deltas[0][0] = -1; deltas[0][1] = -1;
            deltas[1][0] = 1; deltas[1][1] = -1;
        }

        for(int i = 0; i < 2; i++){
            Position check = from.offsetOrNull(deltas[i][0], deltas[i][1]);
            Piece frompiece = board.pieceAt(from);

            if(check == null){
                continue;
            }

            Piece checkpiece = board.pieceAt(check);

            if(checkpiece != null && checkpiece.color() != this.color()){
                boolean promoted = (frompiece.type() == PieceType.PAWN ) && ( (check.rank() == 7 && frompiece.color() == Color.WHITE) || (check.rank() == 0 && frompiece.color() == Color.BLACK) );
                if(promoted){
                    addAllPromotionalChecks(moves,from, check,frompiece, checkpiece, PROMOTION_CHOICES);
                } else moves.add(new Move(from, check, frompiece, checkpiece, null));
            }



        }




       return moves;
    }

    // promotional check
    private void addAllPromotionalChecks (List<Move> moves, Position from, Position to, Piece moved, Piece captured, PieceType[] choices){
        moves.add(new Move(from, to, moved, captured, choices[0]));
        moves.add(new Move(from, to, moved, captured, choices[1]));
        moves.add(new Move(from, to, moved, captured, choices[2]));
        moves.add(new Move(from, to, moved, captured, choices[3]));
    }


    /**
     * A pawn attacks the two squares diagonally ahead of it, whether or not
     * anything stands there.
     *
     * <p>This override exists because the inherited version answers "can this
     * piece move to that square", and for a pawn that is the wrong question.
     * An empty square in front of a pawn is a square the pawn can move to but
     * does <em>not</em> attack — which matters enormously for king safety: a
     * king may not be blocked from a square merely because a pawn could advance
     * onto it, but it certainly may not step onto a square a pawn guards.
     */
    @Override
    public boolean attacks(Board board, Position from, Position target) {
        int color = (this.color() == Color.WHITE) ? 1 : -1;
        for (int side : new int[] { -1, 1 }) {
            Position attack = from.offsetOrNull(side, color);
            if (attack != null && attack.equals(target)) {
                return true;
            }
        }
        return false;
    }
}
