package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.model.Color;
import edu.sfsu.csc413.chess.model.Move;
import edu.sfsu.csc413.chess.model.Position;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

/**
 * A Swing window: an 8x8 board you play by clicking a piece and then its
 * destination. It is event-driven, so {@link #start} builds the window and
 * returns; it never calls {@code controller.runLoop()}.
 *
 * <p>The view holds no chess rules. Which pieces can be picked up and where
 * they can go comes from {@link Game#legalMoves()}; check, checkmate and
 * stalemate come from {@link Game#status()}. Every action goes to the
 * controller as a {@link PlayerAction}.
 */
public class SwingView implements BoardView {

    private static final java.awt.Color LIGHT = new java.awt.Color(0xF0D9B5);
    private static final java.awt.Color DARK = new java.awt.Color(0xB58863);
    private static final java.awt.Color SELECTED = new java.awt.Color(0x6FA8DC);
    private static final java.awt.Color TARGET = new java.awt.Color(0x7CB342);
    private static final int SQUARE = 72;

    private GameController controller;
    private Game game;               // last position handed to render()
    private Position selected;       // square the player clicked first, or null
    private String selectedName;     // its notation, e.g. "e2"

    private JFrame frame;
    private BoardPanel boardPanel;
    private JLabel turnLabel;
    private JLabel messageLabel;

    // ---------------------------------------------------------------- BoardView

    @Override
    public void start(GameController controller) {
        this.controller = controller;
        SwingUtilities.invokeLater(this::buildWindow);
    }

    @Override
    public void render(Game game) {
        onEdt(() -> {
            this.game = game;
            this.selected = null;
            if (turnLabel != null) {
                turnLabel.setText(game.sideToMove() + " to move  |  " + describe(game));
                boardPanel.repaint();
            }
        });
    }

    @Override
    public void showMessage(String message) {
        onEdt(() -> {
            if (messageLabel != null) {
                messageLabel.setText(message);
            }
        });
    }

    @Override
    public void close() {
        onEdt(() -> {
            if (frame != null) {
                frame.dispose();
            }
        });
    }

    // ------------------------------------------------------------------ window

    private void buildWindow() {
        frame = new JFrame("Chess");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        boardPanel = new BoardPanel();
        turnLabel = new JLabel(" ", JLabel.CENTER);
        turnLabel.setFont(turnLabel.getFont().deriveFont(Font.BOLD, 16f));
        messageLabel = new JLabel(" ", JLabel.CENTER);

        JButton undo = new JButton("Undo");
        undo.addActionListener(e -> {
            selected = null;
            controller.handle(new PlayerAction.Undo());
        });

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        top.add(turnLabel, BorderLayout.CENTER);
        top.add(undo, BorderLayout.EAST);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(4, 6, 6, 6));
        bottom.add(messageLabel, BorderLayout.CENTER);

        frame.add(top, BorderLayout.NORTH);
        frame.add(boardPanel, BorderLayout.CENTER);
        frame.add(bottom, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        if (game != null) {          // render() may have arrived before the window
            render(game);
        }
    }

    // ------------------------------------------------------------------- input

    private void squareClicked(int file, int rank) {
        if (game == null) {
            return;
        }
        Position clicked = at(file, rank);

        if (selected == null) {
            if (hasMoveFrom(clicked)) {
                selected = clicked;
                selectedName = name(file, rank);
            }
        } else if (selected.equals(clicked)) {
            selected = null;
        } else if (hasMoveFrom(clicked) && !isTarget(selected, clicked)) {
            selected = clicked;      // switched to another of your own pieces
            selectedName = name(file, rank);
        } else {
            sendMove(selected, clicked, selectedName + name(file, rank));
            selected = null;
        }
        boardPanel.repaint();
    }

    private void sendMove(Position from, Position to, String notation) {
        for (Move move : game.legalMoves()) {
            if (move.from().equals(from) && move.to().equals(to) && move.isPromotion()) {
                // Promotion: take the view's choice (queen by default).
                notation += Character.toLowerCase(promotionChoice(game).symbol());
                break;
            }
        }
        controller.handle(new PlayerAction.Move(notation));
    }

    // ----------------------------------------------- reading the game (no rules)

    private boolean hasMoveFrom(Position from) {
        for (Move move : game.legalMoves()) {
            if (move.from().equals(from)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTarget(Position from, Position to) {
        for (Move move : game.legalMoves()) {
            if (move.from().equals(from) && move.to().equals(to)) {
                return true;
            }
        }
        return false;
    }

    private Set<Position> targetsFrom(Position from) {
        Set<Position> targets = new HashSet<>();
        for (Move move : game.legalMoves()) {
            if (move.from().equals(from)) {
                targets.add(move.to());
            }
        }
        return targets;
    }

    /** Status text straight from the game; only the wording is the view's. */
    private static String describe(Game game) {
        String status = game.status().name();
        return switch (status) {
            case "CHECK" -> "Check!";
            case "CHECKMATE" -> "Checkmate! " + game.sideToMove().opposite() + " wins";
            case "STALEMATE" -> "Stalemate - draw";
            default -> "Playing";
        };
    }

    private static Position at(int file, int rank) {
        return Position.parse(name(file, rank));
    }

    private static String name(int file, int rank) {
        return "" + (char) ('a' + file) + (rank + 1);
    }

    // ------------------------------------------------------------ drawing

    private final class BoardPanel extends JPanel {

        private static final String WHITE_GLYPHS = "\u2654\u2655\u2656\u2657\u2658\u2659";
        private static final String BLACK_GLYPHS = "\u265A\u265B\u265C\u265D\u265E\u265F";

        BoardPanel() {
            setPreferredSize(new Dimension(SQUARE * 8, SQUARE * 8));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int file = e.getX() / SQUARE;
                    int rank = 7 - e.getY() / SQUARE;
                    if (file >= 0 && file < 8 && rank >= 0 && rank < 8) {
                        squareClicked(file, rank);
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g0) {
            super.paintComponent(g0);
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setFont(new Font("SansSerif", Font.PLAIN, (int) (SQUARE * 0.8)));
            FontMetrics fm = g.getFontMetrics();

            Set<Position> targets = (game != null && selected != null)
                    ? targetsFrom(selected) : Set.of();

            for (int rank = 7; rank >= 0; rank--) {
                for (int file = 0; file < 8; file++) {
                    int x = file * SQUARE;
                    int y = (7 - rank) * SQUARE;
                    Position pos = at(file, rank);

                    g.setColor((file + rank) % 2 == 0 ? DARK : LIGHT);
                    g.fillRect(x, y, SQUARE, SQUARE);
                    if (pos.equals(selected)) {
                        g.setColor(SELECTED);
                        g.fillRect(x, y, SQUARE, SQUARE);
                    } else if (targets.contains(pos)) {
                        g.setColor(TARGET);
                        g.fillOval(x + SQUARE / 3, y + SQUARE / 3, SQUARE / 3, SQUARE / 3);
                    }

                    if (game != null) {
                        drawPiece(g, fm, pos, x, y);
                    }
                }
            }
        }

        private void drawPiece(Graphics2D g, FontMetrics fm, Position pos, int x, int y) {
            // ASSUMPTION: Board.pieceAt(Position) returns the Piece on a square,
            // or null when empty; Piece exposes color() and type().
            // Adjust these three calls if the model's names differ.
            var piece = game.board().pieceAt(pos);
            if (piece == null) {
                return;
            }
            boolean white = piece.color() == Color.WHITE;
            char letter = Character.toUpperCase(piece.type().symbol());
            int idx = "KQRBNP".indexOf(letter);

            // Always the solid glyph; a dark outline on white pieces and a light
            // outline on black pieces keeps both readable on both square colours.
            String text = (idx >= 0 && g.getFont().canDisplay(BLACK_GLYPHS.charAt(idx)))
                    ? String.valueOf(BLACK_GLYPHS.charAt(idx))
                    : String.valueOf(letter);
            int tx = x + (SQUARE - fm.stringWidth(text)) / 2;
            int ty = y + (SQUARE + fm.getAscent() - fm.getDescent()) / 2;

            g.setColor(white ? java.awt.Color.BLACK : java.awt.Color.WHITE);
            for (int dx = -2; dx <= 2; dx += 2) {
                for (int dy = -2; dy <= 2; dy += 2) {
                    if (dx != 0 || dy != 0) {
                        g.drawString(text, tx + dx, ty + dy);
                    }
                }
            }
            g.setColor(white ? java.awt.Color.WHITE : java.awt.Color.BLACK);
            g.drawString(text, tx, ty);
        }
    }

    // ----------------------------------------------------------------- helpers

    private static void onEdt(Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }
}