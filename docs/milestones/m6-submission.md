1. Who decides, who shows. Checkmate is detected in one class and announced in another. Name both, and say which of session 16's three questions puts each one there.
Checkmate is detected in Game.java and announced in GameController.java. "Is it a rule, or the state the rules work on" corresponds to Game.java (movel) and "does it take a request, run the rule, and tell the view?" corresponds to GameController.java (controller).

2. The dependency rule. Which of your classes import edu.sfsu.csc413.chess.view? Which must not, and why?
BoardView, ConsoleView, EmojiView, GameController, PieceGlyphs, PlayerAction, and TextBoardRenderer import edu.sfsu.csc413.chess.view. All of the classes that belong to the model, factory, or engine portions/folders of the project must not import it because of the way dependency in the project works. The view can depend on the model but the model can't depend on the view.

3. Test 2 on your engine. In M7 you add a Swing view. List the files you expect to change. If your list includes GameController or anything in engine, say what in your design makes that necessary.