package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.model.Color;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * Displays the game as text, with the board lines shaded by whose turn it is.
 *
 * <p>When it is white's turn (moves 1, 3, 5, ...) the board is drawn on a white
 * background with black text so the pieces stay readable. When it is black's
 * turn (moves 2, 4, 6, ...) the board is drawn with no ANSI background at all,
 * in the terminal's default colours. Only the board lines are coloured; the
 * status line and prompt are left alone.
 */
public class AnsiView implements BoardView {

    private static final String WHITE_BACKGROUND = "\u001B[47;30m";
    private static final String RESET = "\u001B[0m";

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
        out.println();
        for (String line : renderer.render(game.board(), perspective).split("\\R")) {
            out.println(game.sideToMove() == Color.WHITE ? WHITE_BACKGROUND + line + RESET : line);
        }
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
        while(true) {
            out.print("Your move (e.g. e2e4, 'undo', 'moves', 'quit'): ");
            out.flush();
            if(in.hasNextLine()){
                return PlayerAction.parse(in.hasNextLine() ? in.nextLine() : "quit");
            }

        }
    }
}