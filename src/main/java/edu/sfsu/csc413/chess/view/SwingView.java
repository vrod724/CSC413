package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.engine.GameStatus;
import edu.sfsu.csc413.chess.model.Color;
import edu.sfsu.csc413.chess.model.Move;
import edu.sfsu.csc413.chess.model.Piece;
import edu.sfsu.csc413.chess.model.Position;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A window that shows the game and turns clicks into {@link PlayerAction}s.
 *
 * <p>This view is event-driven: {@link #start} builds the window and returns.
 * It never reads the game's rules for itself. The squares a selected piece may
 * move to come from {@link Game#legalMoves()}, and status text comes from
 * {@link Game#status()}. Every action is sent through the controller.
 */
public class SwingView implements BoardView {

    private static final java.awt.Color LIGHT_SQUARE = new java.awt.Color(0xEEEED2);
    private static final java.awt.Color DARK_SQUARE = new java.awt.Color(0x769656);
    private static final java.awt.Color SELECTED_SQUARE = new java.awt.Color(0xF6F669);
    private static final java.awt.Color TARGET_SQUARE = new java.awt.Color(0xBBCB44);
    private static final java.awt.Color WHITE_PIECE = new java.awt.Color(0xFFFFFF);
    private static final java.awt.Color BLACK_PIECE = new java.awt.Color(0x111111);

    private final SquareButton[][] squares = new SquareButton[8][8];
    private final JLabel turnLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel messageLabel = new JLabel(" ", SwingConstants.CENTER);

    private JFrame frame;
    private GameController controller;
    private Game game;
    private String selected;
    private final List<Position> targets = new ArrayList<>();

    @Override
    public void start(GameController controller) {
        this.controller = controller;
        SwingUtilities.invokeLater(this::buildWindow);
    }

    @Override
    public void render(Game game) {
        this.game = game;
        SwingUtilities.invokeLater(() -> refresh(game));
    }

    @Override
    public void showMessage(String message) {
        SwingUtilities.invokeLater(() -> messageLabel.setText(message));
    }

    @Override
    public void close() {
        SwingUtilities.invokeLater(() -> {
            if (frame != null) {
                frame.dispose();
            }
        });
    }

    private void buildWindow() {
        frame = new JFrame("CSC 413 Chess");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel boardPanel = new JPanel(new GridLayout(8, 8));
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int rank = 8 - row;
                char file = (char) ('a' + col);
                String name = "" + file + rank;
                SquareButton square = new SquareButton(name, (row + col) % 2 == 0);
                square.addActionListener(event -> squareClicked(name));
                squares[row][col] = square;
                boardPanel.add(square);
            }
        }
        boardPanel.setPreferredSize(new Dimension(640, 640));

        turnLabel.setFont(turnLabel.getFont().deriveFont(Font.BOLD, 18f));
        messageLabel.setFont(messageLabel.getFont().deriveFont(14f));

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        top.add(turnLabel, BorderLayout.CENTER);

        JButton undo = new JButton("Undo");
        undo.addActionListener(event -> {
            clearSelection();
            controller.handle(new PlayerAction.Undo());
        });

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        bottom.add(messageLabel, BorderLayout.CENTER);
        bottom.add(undo, BorderLayout.EAST);

        frame.add(top, BorderLayout.NORTH);
        frame.add(boardPanel, BorderLayout.CENTER);
        frame.add(bottom, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        if (game != null) {
            refresh(game);
        }
    }

    private void squareClicked(String name) {
        if (game == null || game.isGameOver()) {
            return;
        }
        Position clicked = Position.parse(name);
        boolean startsALegalMove = false;
        for (Move move : game.legalMoves()) {
            if (move.from().equals(clicked)) {
                startsALegalMove = true;
                break;
            }
        }

        if (selected == null) {
            if (startsALegalMove) {
                select(name);
            } else {
                messageLabel.setText("Pick one of your pieces first.");
            }
            return;
        }

        if (name.equals(selected)) {
            clearSelection();
            refresh(game);
            return;
        }

        if (startsALegalMove && !targets.contains(clicked)) {
            select(name);
            return;
        }

        String notation = selected + name + (isPromotion(selected, name) ? "q" : "");
        clearSelection();
        controller.handle(new PlayerAction.Move(notation));
    }

    private boolean isPromotion(String from, String to) {
        Position fromPosition = Position.parse(from);
        Position toPosition = Position.parse(to);
        for (Move move : game.legalMoves()) {
            if (move.from().equals(fromPosition) && move.to().equals(toPosition) && move.isPromotion()) {
                return true;
            }
        }
        return false;
    }

    private void select(String name) {
        selected = name;
        targets.clear();
        Position from = Position.parse(name);
        for (Move move : game.legalMoves()) {
            if (move.from().equals(from)) {
                targets.add(move.to());
            }
        }
        messageLabel.setText(" ");
        refresh(game);
    }

    private void clearSelection() {
        selected = null;
        targets.clear();
    }

    private void refresh(Game game) {
        if (frame == null) {
            return;
        }
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                SquareButton square = squares[row][col];
                Optional<Piece> piece = pieceAt(game, square.name);
                square.piece = piece.orElse(null);
                if (square.name.equals(selected)) {
                    square.highlight = SELECTED_SQUARE;
                } else if (targets.contains(Position.parse(square.name))) {
                    square.highlight = TARGET_SQUARE;
                } else {
                    square.highlight = null;
                }
                square.repaint();
            }
        }
        turnLabel.setText(statusText(game));
    }

    private static Optional<Piece> pieceAt(Game game, String name) {
        return game.board().pieceAt(Position.parse(name));
    }

    private static String statusText(Game game) {
        GameStatus status = game.status();
        Color mover = game.sideToMove();
        if (status == GameStatus.CHECKMATE) {
            return "Checkmate! " + mover.opposite() + " wins.";
        }
        if (status == GameStatus.STALEMATE) {
            return "Stalemate. The game is a draw.";
        }
        if ("CHECK".equals(status.name())) {
            return mover + " to move: CHECK!";
        }
        return mover + " to move";
    }

    private static String glyph(Piece piece) {
        return switch (piece.type().name()) {
            case "KING" -> "\u265A";
            case "QUEEN" -> "\u265B";
            case "ROOK" -> "\u265C";
            case "BISHOP" -> "\u265D";
            case "KNIGHT" -> "\u265E";
            default -> "\u265F";
        };
    }

    /** One square. It paints its own colour and an outlined chess symbol. */
    private static final class SquareButton extends JButton {

        private final String name;
        private final boolean light;
        private Piece piece;
        private java.awt.Color highlight;

        SquareButton(String name, boolean light) {
            this.name = name;
            this.light = light;
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(highlight != null ? highlight : (light ? LIGHT_SQUARE : DARK_SQUARE));
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (piece != null) {
                boolean whitePiece = piece.color() == Color.WHITE;
                Font font = new Font("Dialog", Font.PLAIN, (int) (getHeight() * 0.8));
                GlyphVector vector = font.createGlyphVector(g2.getFontRenderContext(), glyph(piece));
                Rectangle2D bounds = vector.getVisualBounds();
                double x = (getWidth() - bounds.getWidth()) / 2 - bounds.getX();
                double y = (getHeight() - bounds.getHeight()) / 2 - bounds.getY();
                Shape outline = AffineTransform.getTranslateInstance(x, y).createTransformedShape(vector.getOutline());
                g2.setColor(whitePiece ? WHITE_PIECE : BLACK_PIECE);
                g2.fill(outline);
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(whitePiece ? BLACK_PIECE : WHITE_PIECE);
                g2.draw(outline);
            }
            g2.dispose();
        }
    }
}