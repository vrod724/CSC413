package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.engine.GameStatus;

/**
 * Connects the player's decisions to the game: carry out what was asked, then
 * ask the view to redraw.
 *
 * <p>This is the C in MVC. It is the only class that knows about both the
 * {@link Game} and the {@link BoardView}. It holds no game state of its own:
 * the model owns the position, the view owns the display, and the controller
 * owns only the conversation between them.
 *
 * <p>When you are done, this class mentions no {@code Scanner}, no
 * {@code System.out}, no window, and none of the words "undo", "moves", or
 * "quit". Those belong to the view and to {@link PlayerAction#parse}.
 */
public class GameController {

    private final Game game;
    private final BoardView view;

    public GameController(Game game, BoardView view) {
        this.game = game;
        this.view = view;
    }

    /** The game being played, so a view can read it in order to draw it. */
    public Game game() {
        return game;
    }

    /**
     * Greets the player, draws the opening position, and hands control to the
     * view with {@link BoardView#start}.
     */
    public void start() {
        view.showMessage("Hello, Player!\n");
        view.render(game);
        view.start(this);
    }

    /**
     * The ask-and-handle loop, for views that can wait for input.
     *
     * <p>Asks the view for the next action and handles it, until the player
     * quits or the game is over. A console view calls this from its
     * {@code start}; an event-driven view never calls it.
     */
    public void runLoop() {
       while(!game.isGameOver()){
            PlayerAction action = view.nextAction(game);
            handle(action);
            if(action instanceof PlayerAction.Quit) break;
        }
        switch(game.status()){
            case GameStatus.CHECKMATE:
                view.showMessage("Checkmate. " + game.sideToMove().opposite() + " wins." );
                view.close();
                break;
            case GameStatus.STALEMATE:
                view.showMessage("Stalemate. The game is a draw.");
                view.close();
                break;
        }
        //throw new UnsupportedOperationException();
    }

    /**
     * Carries out one player action and redraws.
     *
     * <p>Every route into the game goes through here: {@link #runLoop()} calls
     * it, and so will a button's click handler in M7. The handout lists what
     * each of the four actions does and what the player is told.
     */
    public void handle(PlayerAction action) {
        switch(action){
            case PlayerAction.Move(String notation):
                var move = game.findLegalMove(notation);
                if(game.findLegalMove(notation).isEmpty() && notation.length() == 4){
                    String choice =  Character.toString(view.promotionChoice(game).symbol()).toLowerCase();
                    move = game.findLegalMove(notation + choice);
                }



                if(move.isPresent()){
                    game.play(move.get());
                } else view.showMessage("Not a legal move: '" + notation + "'.");
                view.render(game);
                break;
            case PlayerAction.Quit():
                view.showMessage("Goodbye.");
                view.close();
                break;
            case PlayerAction.Undo():
                if(!game.history().isEmpty()){
                    game.undoLastMove();
                } else view.showMessage("Nothing to undo.");
                view.render(game);
                break;
            case PlayerAction.ShowMoves():
                view.showMessage( game.legalMoves().size() + " legal moves: ");
                for(Object moves: game.legalMoves()){
                    System.out.print(moves + " ");
                }
                view.render(game);
                break;

        }
    }
}
