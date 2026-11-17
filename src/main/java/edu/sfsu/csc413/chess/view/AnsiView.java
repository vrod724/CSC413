package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.model.Color;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * A console view that paints the board in the colour of the side to move.
 *
 * <p>White to move draws the board in bright white; black to move draws it in
 * black. Black text would vanish on a black terminal, so it is drawn on a white
 * background. Like {@link ConsoleView}, it makes no rules decisions: it draws
 * what the {@link Game} reports and passes along what the player typed.
 */
public class AnsiView implements BoardView {

    private static final String RESET = "\u001B[0m";
    private static final String WHITE_BOARD = "\u001B[97m";
    private static final String BLACK_BOARD = "\u001B[30;47m";

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
        String boardColor = game.sideToMove() == Color.WHITE ? WHITE_BOARD : BLACK_BOARD;
        out.println();
        out.println(boardColor + renderer.render(game.board(), perspective) + RESET);
        out.println();
        out.println(game.sideToMove() + " to move  |  " + game.status());
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