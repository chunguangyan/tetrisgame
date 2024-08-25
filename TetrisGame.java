import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Random;

public class TetrisGame extends JPanel implements ActionListener {

    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 22;
    private final Timer timer;
    private boolean isFallingFinished = false;
    private boolean isStarted = false;
    private boolean isPaused = false;
    private int numLinesRemoved = 0;
    private int score = 0; // 添加得分系统
    private int curX = 0;
    private int curY = 0;
    private Shape curPiece;
    private final Shape.Tetrominoes[] board;
    private JButton pauseButton; // 定义暂停按钮

    public TetrisGame() {
        setFocusable(true);
        curPiece = new Shape();
        timer = new Timer(400, this);
        timer.start();
        board = new Shape.Tetrominoes[BOARD_WIDTH * BOARD_HEIGHT];
        clearBoard();
        addKeyListener(new TAdapter());

        // 初始化暂停按钮
        pauseButton = new JButton("Pause");
        pauseButton.addActionListener(e -> togglePause());
    }

    public void start() {
        if (isPaused) {
            return;
        }

        isStarted = true;
        isFallingFinished = false;
        numLinesRemoved = 0;
        score = 0; // 重置得分
        clearBoard();

        newPiece();
        timer.start();
    }

    private void togglePause() {
        if (isPaused) {
            pauseButton.setText("Pause");
        } else {
            pauseButton.setText("Resume");
        }
        pause();
    }

    private void pause() {
        if (!isStarted) {
            return;
        }

        isPaused = !isPaused;
        if (isPaused) {
            timer.stop();
            repaint();
        } else {
            timer.start();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
    }

    private void doDrawing(Graphics g) {
        Dimension size = getSize();
        int boardTop = (int) size.getHeight() - BOARD_HEIGHT * squareHeight();

        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                Shape.Tetrominoes shape = shapeAt(j, BOARD_HEIGHT - i - 1);
                if (shape != Shape.Tetrominoes.NoShape) {
                    drawSquare(g, j * squareWidth(), boardTop + i * squareHeight(), shape);
                }
            }
        }

        if (curPiece.getShape() != Shape.Tetrominoes.NoShape) {
            for (int i = 0; i < 4; i++) {
                int x = curX + curPiece.x(i);
                int y = curY - curPiece.y(i);
                drawSquare(g, x * squareWidth(), boardTop + (BOARD_HEIGHT - y - 1) * squareHeight(), curPiece.getShape());
            }
        }

        // 绘制得分
        g.setColor(Color.WHITE);
        g.drawString("Score: " + score, 10, 20);
    }

    private void dropDown() {
        int newY = curY;
        while (newY > 0) {
            if (!tryMove(curPiece, curX, newY - 1)) {
                break;
            }
            newY--;
        }
        pieceDropped();
    }

    private void oneLineDown() {
        if (!tryMove(curPiece, curX, curY - 1)) {
            pieceDropped();
        }
    }

    private int squareWidth() {
        return (int) getSize().getWidth() / BOARD_WIDTH;
    }

    private int squareHeight() {
        return (int) getSize().getHeight() / BOARD_HEIGHT;
    }

    private Shape.Tetrominoes shapeAt(int x, int y) {
        return board[(y * BOARD_WIDTH) + x];
    }

    private void pieceDropped() {
        for (int i = 0; i < 4; i++) {
            int x = curX + curPiece.x(i);
            int y = curY - curPiece.y(i);
            board[(y * BOARD_WIDTH) + x] = curPiece.getShape();
        }

        removeFullLines();

        if (!isFallingFinished) {
            newPiece();
        }
    }

    private void newPiece() {
        curPiece.setRandomShape();
        curX = BOARD_WIDTH / 2 + 1;
        curY = BOARD_HEIGHT - 1 + curPiece.minY();

        if (!tryMove(curPiece, curX, curY)) {
            curPiece.setShape(Shape.Tetrominoes.NoShape);
            timer.stop();
            isStarted = false;
            JOptionPane.showMessageDialog(this, "Game Over! Your score: " + score);
        }
    }

    private boolean tryMove(Shape newPiece, int newX, int newY) {
        for (int i = 0; i < 4; i++) {
            int x = newX + newPiece.x(i);
            int y = newY - newPiece.y(i);
            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) {
                return false;
            }
            if (shapeAt(x, y) != Shape.Tetrominoes.NoShape) {
                return false;
            }
        }

        curPiece = newPiece;
        curX = newX;
        curY = newY;
        repaint();

        return true;
    }

    private void removeFullLines() {
        int numFullLines = 0;

        for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {
            boolean lineIsFull = true;

            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (shapeAt(j, i) == Shape.Tetrominoes.NoShape) {
                    lineIsFull = false;
                    break;
                }
            }

            if (lineIsFull) {
                numFullLines++;
                for (int k = i; k < BOARD_HEIGHT - 1; k++) {
                    for (int j = 0; j < BOARD_WIDTH; j++) {
                        board[(k * BOARD_WIDTH) + j] = shapeAt(j, k + 1);
                    }
                }
            }
        }

        if (numFullLines > 0) {
            numLinesRemoved += numFullLines;
            score += numFullLines * 100; // 每行加 100 分
            isFallingFinished = true;
            curPiece.setShape(Shape.Tetrominoes.NoShape);
            repaint();
        }
    }

    private void clearBoard() {
        for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; i++) {
            board[i] = Shape.Tetrominoes.NoShape;
        }
    }

    private void drawSquare(Graphics g, int x, int y, Shape.Tetrominoes shape) {
        Color[] colors = {new Color(0, 0, 0), new Color(204, 102, 102), new Color(102, 204, 102), new Color(102, 102, 204),
                new Color(204, 204, 102), new Color(204, 102, 204), new Color(102, 204, 204), new Color(218, 170, 0)};

        Color color = colors[shape.ordinal()];

        g.setColor(color);
        g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);

        g.setColor(color.brighter());
        g.drawLine(x, y + squareHeight() - 1, x, y);
        g.drawLine(x, y, x + squareWidth() - 1, y);

        g.setColor(color.darker());
        g.drawLine(x + 1, y + squareHeight() - 1, x + squareWidth() - 1, y + squareHeight() - 1);
        g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1, x + squareWidth() - 1, y + 1);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isFallingFinished) {
            isFallingFinished = false;
            newPiece();
        } else {
            oneLineDown();
        }
    }

    class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (!isStarted || curPiece.getShape() == Shape.Tetrominoes.NoShape) {
                return;
            }

            int keycode = e.getKeyCode();

            if (keycode == 'p' || keycode == 'P') {
                togglePause();
                return;
            }

            if (isPaused) {
                return;
            }

            switch (keycode) {
                case KeyEvent.VK_LEFT -> tryMove(curPiece, curX - 1, curY);
                case KeyEvent.VK_RIGHT -> tryMove(curPiece, curX + 1, curY);
                case KeyEvent.VK_DOWN -> oneLineDown();
                case KeyEvent.VK_UP -> tryMove(curPiece.rotateRight(), curX, curY);
                case KeyEvent.VK_SPACE -> dropDown();
            }
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tetris");
        TetrisGame game = new TetrisGame();
        frame.add(game, BorderLayout.CENTER);

        // 添加暂停按钮到南部
        frame.add(game.pauseButton, BorderLayout.SOUTH);

        frame.setBounds(100, 100, 400, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        game.start();
    }

    public static class Shape {

        public enum Tetrominoes { NoShape, ZShape, SShape, LineShape, TShape, SquareShape, LShape, MirroredLShape }

        private Tetrominoes pieceShape;
        private final int[][] coords;
        private final int[][][] coordsTable;

        public Shape() {
            coords = new int[4][2];
            coordsTable = new int[][][]{
                    {{0, 0}, {0, 0}, {0, 0}, {0, 0}},
                    {{0, -1}, {0, 0}, {-1, 0}, {-1, 1}},
                    {{0, -1}, {0, 0}, {1, 0}, {1, 1}},
                    {{0, -1}, {0, 0}, {0, 1}, {0, 2}},
                    {{-1, 0}, {0, 0}, {1, 0}, {0, 1}},
                    {{0, 0}, {1, 0}, {0, 1}, {1, 1}},
                    {{-1, -1}, {0, -1}, {0, 0}, {0, 1}},
                    {{1, -1}, {0, -1}, {0, 0}, {0, 1}}
            };
            setShape(Tetrominoes.NoShape);
        }

        public void setShape(Tetrominoes shape) {
            for (int i = 0; i < 4; i++) {
                System.arraycopy(coordsTable[shape.ordinal()][i], 0, coords[i], 0, 2);
            }
            pieceShape = shape;
        }

        public int x(int index) {
            return coords[index][0];
        }

        public int y(int index) {
            return coords[index][1];
        }

        public Tetrominoes getShape() {
            return pieceShape;
        }

        public void setRandomShape() {
            Random r = new Random();
            int x = Math.abs(r.nextInt()) % 7 + 1;
            Tetrominoes[] values = Tetrominoes.values();
            setShape(values[x]);
        }

        public int minX() {
            int m = coords[0][0];
            for (int i = 0; i < 4; i++) {
                m = Math.min(m, coords[i][0]);
            }
            return m;
        }

        public int minY() {
            int m = coords[0][1];
            for (int i = 0; i < 4; i++) {
                m = Math.min(m, coords[i][1]);
            }
            return m;
        }

        public Shape rotateLeft() {
            if (pieceShape == Tetrominoes.SquareShape) {
                return this;
            }

            Shape result = new Shape();
            result.pieceShape = pieceShape;

            for (int i = 0; i < 4; ++i) {
                result.coords[i][0] = y(i);
                result.coords[i][1] = -x(i);
            }
            return result;
        }

        public Shape rotateRight() {
            if (pieceShape == Tetrominoes.SquareShape) {
                return this;
            }

            Shape result = new Shape();
            result.pieceShape = pieceShape;

            for (int i = 0; i < 4; ++i) {
                result.coords[i][0] = -y(i);
                result.coords[i][1] = x(i);
            }
            return result;
        }
    }
}


