package edu.sfsu.csc413.chess;


import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.factory.BoardFactory;
import edu.sfsu.csc413.chess.model.Board;
import edu.sfsu.csc413.chess.model.Move;
import edu.sfsu.csc413.chess.view.AnsiView;
import edu.sfsu.csc413.chess.view.BoardView;
import edu.sfsu.csc413.chess.view.ConsoleView;
import edu.sfsu.csc413.chess.view.EmojiView;
import edu.sfsu.csc413.chess.view.GameController;
import edu.sfsu.csc413.chess.view.SwingView;

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
        Game game = new Game();
        BoardView view = chooseView(args);
        GameController controller = new GameController(game, view);
        controller.start();
    }

    private static BoardView chooseView(String[] args) {
        String name = args.length > 0 ? args[0].toLowerCase() : "console";
        return switch (name) {
            case "emoji" -> new EmojiView();
            case "swing" -> new SwingView();
            case "ansi" -> new AnsiView();
            default -> new ConsoleView();
        };
    }

    private Main() {

    }
}