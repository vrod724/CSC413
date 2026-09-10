package edu.sfsu.csc413.chess.model;

public record Position (int file, int rank){
    public static Position parse(String algebraic) {
        int parsedfile = (int)algebraic.charAt(0) - 97;
        int parsedrank = (int)algebraic.charAt(1) - 49;

        return new Position(parsedfile, parsedrank);
        // throw new UnsupportedOperationException("M0b: your turn");
    }

    public Position offsetOrNull(int fileDelta, int rankDelta) {
        if (!isOnBoard(file + fileDelta, rank + rankDelta)) {
            return null;
        } return new Position(file + fileDelta, rank + rankDelta);

        //throw new UnsupportedOperationException("M0b: your turn");
    }

    /** Files and ranks both run 0..7. */
    public static final int BOARD_SIZE = 8;

    /** True when these raw coordinates name a real square. */
    public static boolean isOnBoard(int file, int rank) {
        return file >= 0 && file < BOARD_SIZE && rank >= 0 && rank < BOARD_SIZE;
    }

    public Position {
        if (!isOnBoard(file, rank)) {
            throw new IllegalArgumentException(
                    "Position off board: file=" + file + ", rank=" + rank);
        }
    }

    @Override
    public String toString() {
        return "" + (char) ('a' + file) + (char) ('1' + rank);
    }

}