
package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.model.Color;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * A console view that draws the board in the colour of the side to move:
 * bright white text when it is White's turn, black text on a white background
 * when it is Black's turn (plain black text would vanish on a dark terminal).
 *
 * <p>Like {@link ConsoleView}, it never decides anything about the rules. It
 * only asks {@link Game} whose turn it is and tints what it draws.
 */
public class AnsiView implements BoardView {

    private static final String RESET = "\u001B[0m";
    private static final String WHITE_SIDE = "\u001B[97m";
    private static final String BLACK_SIDE = "\u001B[30;107m";

    private final TextBoardRenderer renderer =
            new TextBoardRenderer(PieceGlyphs.LETTERS);
    private final PrintStream out;
    private final Scanner in;
    private final Color perspective;

    /** A view reading from standard input, drawn from white's side. */
    public AnsiView() {
        this(System.out, new Scanner(System.in), Color.WHITE);
    }

    /** Injectable streams, so tests can drive the view without a terminal. */
    public AnsiView(PrintStream out, Scanner in, Color perspective) {
        this.out = out;
        this.in = in;
        this.perspective = perspective;
    }

    @Override
    public void render(Game game) {
        String tint = game.sideToMove() == Color.WHITE ? WHITE_SIDE : BLACK_SIDE;
        out.println();
        // Tint line by line so the background colour never bleeds past the text.
        for (String line : renderer.render(game.board(), perspective).split("\\R")) {
            out.println(tint + line + RESET);
        }
        out.println();
        out.println(tint + game.sideToMove() + " to move  |  " + game.status() + RESET);
    }

    @Override
    public void showMessage(String message) {
        out.println(message);
    }

    @Override
    public void start(GameController controller) {
        controller.runLoop();
    }

    @Override
    public PlayerAction nextAction(Game game) {
        out.print("Your move (e.g. e2e4, 'undo', 'moves', 'quit'): ");
        out.flush();
        return PlayerAction.parse(in.hasNextLine() ? in.nextLine() : "quit");
    }
}
 