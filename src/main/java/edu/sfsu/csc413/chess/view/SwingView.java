package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
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
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.Optional;

/**
 * Displays the game in a Swing window and turns clicks into {@link PlayerAction}s.
 *
 * <p>This view is event-driven: {@link #start(GameController)} builds the window
 * and returns. It never calls {@code controller.runLoop()}. Every click is
 * translated into a {@link PlayerAction} and handed to the controller; the view
 * only reads the game and never changes it. Legal moves come from
 * {@link Game#legalMoves()} and check / checkmate / stalemate from
 * {@link Game#status()}.
 */
public class SwingView implements BoardView {

    private static final java.awt.Color LIGHT_SQUARE = new java.awt.Color(0xF0D9B5);
    private static final java.awt.Color DARK_SQUARE = new java.awt.Color(0xB58863);
    private static final java.awt.Color SELECTED = new java.awt.Color(0xF6F669);
    private static final java.awt.Color TARGET = new java.awt.Color(0x7FC97F);

    private final SquareButton[][] squares = new SquareButton[8][8];
    private final JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel messageLabel = new JLabel(" ", SwingConstants.CENTER);

    private JFrame frame;
    private GameController controller;
    private Game game;
    private String selected;

    @Override
    public void render(Game game) {
        this.game = game;
        SwingUtilities.invokeLater(this::refresh);
    }

    @Override
    public void showMessage(String message) {
        SwingUtilities.invokeLater(() -> messageLabel.setText(message.isBlank() ? " " : message));
    }

    /** Builds the window and returns at once; clicks reach the controller later. */
    @Override
    public void start(GameController controller) {
        this.controller = controller;
        SwingUtilities.invokeLater(this::buildWindow);
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
        JPanel boardPanel = new JPanel(new GridLayout(8, 8));
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                String square = "" + (char) ('a' + col) + (8 - row);
                SquareButton button = new SquareButton((row + col) % 2 == 0 ? LIGHT_SQUARE : DARK_SQUARE);
                button.addActionListener(e -> onSquareClicked(square));
                squares[row][col] = button;
                boardPanel.add(button);
            }
        }
        boardPanel.setPreferredSize(new Dimension(560, 560));

        JButton undo = new JButton("Undo");
        undo.addActionListener(e -> {
            selected = null;
            messageLabel.setText(" ");
            controller.handle(new PlayerAction.Undo());
        });

        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD, 16f));
        JPanel top = new JPanel(new GridLayout(2, 1));
        top.add(statusLabel);
        top.add(messageLabel);
        top.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        JPanel bottom = new JPanel();
        bottom.add(undo);

        frame = new JFrame("Chess");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(top, BorderLayout.NORTH);
        frame.add(boardPanel, BorderLayout.CENTER);
        frame.add(bottom, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        refresh();
    }

    private void onSquareClicked(String square) {
        if (game == null || game.isGameOver()) {
            return;
        }
        messageLabel.setText(" ");
        if (selected == null) {
            if (hasMoveFrom(square)) {
                selected = square;
            }
        } else if (selected.equals(square)) {
            selected = null;
        } else if (hasMoveFrom(square) && !hasMove(selected, square)) {
            selected = square;
        } else {
            String from = selected;
            selected = null;
            controller.handle(new PlayerAction.Move(from + square + (isPromotion(from, square) ? "q" : "")));
        }
        refresh();
    }

    private boolean hasMoveFrom(String square) {
        Position from = Position.parse(square);
        for (Move move : game.legalMoves()) {
            if (move.from().equals(from)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasMove(String fromSquare, String toSquare) {
        return findMove(fromSquare, toSquare) != null;
    }

    private boolean isPromotion(String fromSquare, String toSquare) {
        Move move = findMove(fromSquare, toSquare);
        return move != null && move.isPromotion();
    }

    private Move findMove(String fromSquare, String toSquare) {
        Position from = Position.parse(fromSquare);
        Position to = Position.parse(toSquare);
        for (Move move : game.legalMoves()) {
            if (move.from().equals(from) && move.to().equals(to)) {
                return move;
            }
        }
        return null;
    }

    private void refresh() {
        if (game == null || frame == null) {
            return;
        }
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                String square = "" + (char) ('a' + col) + (8 - row);
                SquareButton button = squares[row][col];
                button.setPiece(pieceAt(square));
                if (square.equals(selected)) {
                    button.setHighlight(SELECTED);
                } else if (selected != null && hasMove(selected, square)) {
                    button.setHighlight(TARGET);
                } else {
                    button.setHighlight(null);
                }
            }
        }
        statusLabel.setText(game.sideToMove() + " to move  |  " + game.status());
        frame.repaint();
    }

    private Piece pieceAt(String square) {
        Object found = game.board().pieceAt(Position.parse(square));
        if (found instanceof Optional<?> optional) {
            found = optional.orElse(null);
        }
        return (Piece) found;
    }

    /** One board square: a coloured background with a high-contrast disc for the piece. */
    private static final class SquareButton extends JButton {

        private final java.awt.Color base;
        private java.awt.Color highlight;
        private Piece piece;

        SquareButton(java.awt.Color base) {
            this.base = base;
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
        }

        void setPiece(Piece piece) {
            this.piece = piece;
        }

        void setHighlight(java.awt.Color highlight) {
            this.highlight = highlight;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(highlight != null ? highlight : base);
            g2.fillRect(0, 0, getWidth(), getHeight());

            if (piece != null) {
                boolean white = piece.color() == edu.sfsu.csc413.chess.model.Color.WHITE;
                java.awt.Color fill = white ? java.awt.Color.WHITE : new java.awt.Color(0x1E1E1E);
                java.awt.Color edge = white ? new java.awt.Color(0x1E1E1E) : java.awt.Color.WHITE;
                int size = Math.min(getWidth(), getHeight()) * 3 / 4;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                g2.setColor(fill);
                g2.fillOval(x, y, size, size);
                g2.setColor(edge);
                g2.setStroke(new BasicStroke(3f));
                g2.drawOval(x, y, size, size);

                String letter = String.valueOf(Character.toUpperCase(piece.type().symbol()));
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, size / 2));
                FontMetrics metrics = g2.getFontMetrics();
                g2.setColor(edge);
                g2.drawString(letter,
                        (getWidth() - metrics.stringWidth(letter)) / 2,
                        (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent());
            }
            g2.dispose();
        }
    }
}