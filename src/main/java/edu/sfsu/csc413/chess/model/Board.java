package edu.sfsu.csc413.chess.model;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class Board {
    private final Piece[][] squares = new Piece[Position.BOARD_SIZE][Position.BOARD_SIZE];

    public Board() {

    }                              // empty

    public Piece pieceAt(Position position){
        Piece pieceat = squares[position.file()][position.rank()];
        if(pieceat != null){
            return pieceat;
        } else return null;
    }              // null if the square is empty

    public boolean isEmpty(Position position){
        return squares[position.file()][position.rank()] == null;
    }
    public void place(Position position, Piece piece) {
        squares[position.file()][position.rank()] = piece;
    }   // replaces; null clears

    public List<Position> positionsOf(Color color){
        int colorcount = 0;
        List<Position> positions = new ArrayList<>();

        for (int file = 0; file < Position.BOARD_SIZE; file++) {
            for (int rank = 0; rank < Position.BOARD_SIZE; rank++) {
                if(squares[file][rank] != null) {
                    if (squares[file][rank].color() == color) {
                        colorcount++;
                        positions.add(new Position(file, rank));
                    }
                }
            }
        }
        return positions;
    }// every square holding that color

    @Override public String toString(){
        StringBuilder text = new StringBuilder();
        int space = 0;
        for (int rank = Position.BOARD_SIZE - 1; rank >= 0; rank--) { // rank 8 first
            for (int file = 0; file < Position.BOARD_SIZE; file++) {
                Position check = new Position(file, rank);
                 if (!(isEmpty(check)) ) {
                     if(file == 0) {
                         text.append(pieceAt(check).toString());
                     } else {
                         text.append(String.valueOf(space));
                         text.append(pieceAt(check).toString());
                         space = 0;
                     }
                } else {
                     space++;
                 }
                 if(space == 8){
                     text.append("8");
                 }
                 if(file == 7 && isEmpty(check) && space != 8){
                     text.append(String.valueOf(space));
                 }
            }
            space = 0;

            // ... this rank's squares, file a to h: a letter per piece,
            //     a digit for each run of empties
            if (rank > 0) {
                text.append('/');
            }
        }
        return text.toString();
    }
}
