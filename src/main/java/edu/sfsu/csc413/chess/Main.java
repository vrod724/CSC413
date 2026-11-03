package edu.sfsu.csc413.chess;


import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.factory.BoardFactory;
import edu.sfsu.csc413.chess.model.Board;
import edu.sfsu.csc413.chess.model.Move;

import java.util.Optional;

/**
 * Entry point.
 *
 * <p>At M0 this does nothing but prove the toolchain works. It grows into the
 * real launcher as the engine appears underneath it.
 */
public final class Main {

    public static void main(String[] args) {
        System.out.println("CSC 413 Chess — environment OK.");
        //BoardFactory.standard();
        Game game = new Game();


        Move move1 = game.findLegalMove("f2f3").orElseThrow(() -> new IllegalStateException("blank"));
        game.play(move1);
        Move move2 = game.findLegalMove("e7e5").orElseThrow(() -> new IllegalStateException("blank"));
        game.play(move2);
        Move move3 = game.findLegalMove("g2g4").orElseThrow(() -> new IllegalStateException("blank"));
        game.play(move3);
        Move move4 = game.findLegalMove("d8h4").orElseThrow(() -> new IllegalStateException("blank"));
        game.play(move4);



        System.out.println(game.legalMoves());
    }

    private Main() {

    }
}
