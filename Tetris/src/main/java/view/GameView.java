package view;

import controller.GameController;
import data.GameData;
import data.Shape;
import data.ScoreManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

public class GameView extends JFrame {
    private GameController controller;
    private JPanel gamePanel;
    private JPanel sidePanel;
    private JButton highScoreButton;

    private final int CELL_SIZE = 30;
    private final double GAME_PANEL_RATIO = 0.7;

    public GameView(GameController controller) {
        this.controller = controller;
        initializeUI();
    }

    private void initializeUI() {
        // 在新线程中播放背景音乐
        PlayMusic.playMusicInThread("wav/bgMusic.wav", true);  // 确保路径正确

        setTitle("Tetris");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());

        gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGame(g);
            }
        };
        gamePanel.setPreferredSize(new Dimension(300, 600));

        sidePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawSidePanel(g);
            }
        };
        sidePanel.setPreferredSize(new Dimension(200, 600));
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));

        highScoreButton = new JButton("Show High Scores");
        highScoreButton.addActionListener(e -> controller.showHighScores());
        highScoreButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        sidePanel.add(Box.createVerticalGlue());
        sidePanel.add(highScoreButton);
        sidePanel.add(Box.createVerticalGlue());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = GAME_PANEL_RATIO;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(gamePanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0 - GAME_PANEL_RATIO;
        add(sidePanel, gbc);

        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        JMenuItem mainMenuItem = new JMenuItem("Return to Main Menu");
        mainMenuItem.addActionListener(e -> controller.endGame());
        gameMenu.add(mainMenuItem);
        menuBar.add(gameMenu);
        setJMenuBar(menuBar);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        controller.moveLeft();
                        break;
                    case KeyEvent.VK_RIGHT:
                        controller.moveRight();
                        break;
                    case KeyEvent.VK_DOWN:
                        controller.moveDown();
                        break;
                    case KeyEvent.VK_UP:
                        controller.rotate();
                        break;
                }
            }
        });

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adjustLayout();
            }
        });

        setFocusable(true);
        adjustLayout();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void adjustLayout() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int maxHeight = (int) (screenSize.height * 0.9);
        int maxWidth = (int) (screenSize.width * 0.9);

        int rows = controller.getGameData().getGrid().length;
        int cols = controller.getGameData().getGrid()[0].length;

        int gamePanelWidth = cols * CELL_SIZE;
        int gamePanelHeight = rows * CELL_SIZE;
        int totalWidth = (int) (gamePanelWidth / GAME_PANEL_RATIO);

        if (gamePanelHeight > maxHeight || totalWidth > maxWidth) {
            double scale = Math.min((double) maxHeight / gamePanelHeight, (double) maxWidth / totalWidth);
            gamePanelHeight = (int) (gamePanelHeight * scale);
            totalWidth = (int) (totalWidth * scale);
        }

        setSize(totalWidth, gamePanelHeight);
        setLocationRelativeTo(null);
        revalidate();
    }

    private void drawGame(Graphics g) {
        GameData gameData = controller.getGameData();
        int[][] grid = gameData.getGrid();
        int cellSize = Math.min(gamePanel.getWidth() / grid[0].length, gamePanel.getHeight() / grid.length);

        // Draw the grid
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j] != 0) {
                    g.setColor(gameData.getCurrentShape().getColor());
                    g.fillRect(j * cellSize, i * cellSize, cellSize, cellSize);
                }
                g.setColor(Color.GRAY);
                g.drawRect(j * cellSize, i * cellSize, cellSize, cellSize);
            }
        }

        // Draw the current shape
        Shape currentShape = gameData.getCurrentShape();
        int currentX = gameData.getCurrentX();
        int currentY = gameData.getCurrentY();
        g.setColor(currentShape.getColor());
        for (int i = 0; i < currentShape.getHeight(); i++) {
            for (int j = 0; j < currentShape.getWidth(); j++) {
                if (currentShape.getShape()[i][j] != 0) {
                    g.fillRect((currentX + j) * cellSize, (currentY + i) * cellSize, cellSize, cellSize);
                }
            }
        }
    }

    private void drawSidePanel(Graphics g) {
        GameData gameData = controller.getGameData();

        // Draw next shape preview
        g.setColor(Color.BLACK);
        g.drawString("Next Shape:", 10, 30);
        Shape nextShape = gameData.getNextShape();
        int cellSize = 20;
        int startX = 50;
        int startY = 50;
        g.setColor(nextShape.getColor());
        for (int i = 0; i < nextShape.getHeight(); i++) {
            for (int j = 0; j < nextShape.getWidth(); j++) {
                if (nextShape.getShape()[i][j] != 0) {
                    g.fillRect(startX + j * cellSize, startY + i * cellSize, cellSize, cellSize);
                }
            }
        }

        // Draw score, level, and lines
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Score: " + gameData.getScore(), 10, 150);
        g.drawString("Level: " + gameData.getLevel(), 10, 180);
        g.drawString("Lines: " + gameData.getLinesCleared(), 10, 210);

        // Draw current rank
        List<ScoreManager.Score> highScores = controller.getScoreManager().getHighScores();
        int currentRank = highScores.size() + 1;
        for (int i = 0; i < highScores.size(); i++) {
            if (gameData.getScore() > highScores.get(i).getScore()) {
                currentRank = i + 1;
                break;
            }
        }
        g.drawString("Current Rank: " + currentRank, 10, 240);
    }

    public void update() {
        gamePanel.repaint();
        sidePanel.repaint();
    }
}