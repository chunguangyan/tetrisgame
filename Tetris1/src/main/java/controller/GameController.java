package controller;

import com.google.gson.Gson;
import data.GameData;
import data.Shape;
import data.ScoreManager;
import data.ConfigManager;
import data.PureGame;
import data.OpMove;
import view.GameView;
import view.StartScreen;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class GameController implements ActionListener {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 3000;

    private GameData gameData;
    private GameView gameView;
    private StartScreen startScreen;
    private Timer timer;
    private ScoreManager scoreManager;
    private ConfigManager.GameConfig gameConfig;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Gson gson;

    public GameController() {
        this.scoreManager = new ScoreManager();
        this.gameConfig = ConfigManager.loadConfig();
        this.startScreen = new StartScreen(this);
        this.gson = new Gson();
        connectToServer();
    }

    private void connectToServer() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Connected to TetrisServer");
        } catch (IOException e) {
            System.err.println("Failed to connect to TetrisServer: " + e.getMessage());
        }
    }

    public void startGame() {
        if (gameView != null) {
            gameView.dispose();
        }
        gameData = new GameData(gameConfig.gridRows, gameConfig.gridCols);
        gameData.setLevel(gameConfig.initialLevel);
        gameView = new GameView(this);
        startScreen.setVisible(false);
        int initialDelay = 1000 - (gameConfig.initialLevel - 1) * 50;
        timer = new Timer(Math.max(100, initialDelay), this);
        timer.start();
    }

    public void openSettings() {
        JTextField levelField = new JTextField(String.valueOf(gameConfig.initialLevel));
        JTextField rowsField = new JTextField(String.valueOf(gameConfig.gridRows));
        JTextField colsField = new JTextField(String.valueOf(gameConfig.gridCols));
        JCheckBox soundCheckBox = new JCheckBox("Enable Sound", gameConfig.soundEnabled);

        JPanel panel = new JPanel();
        panel.add(new JLabel("Initial Level:"));
        panel.add(levelField);
        panel.add(new JLabel("Grid Rows:"));
        panel.add(rowsField);
        panel.add(new JLabel("Grid Columns:"));
        panel.add(colsField);
        panel.add(soundCheckBox);

        int result = JOptionPane.showConfirmDialog(null, panel, "Game Settings",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                gameConfig.initialLevel = Integer.parseInt(levelField.getText());
                gameConfig.gridRows = Integer.parseInt(rowsField.getText());
                gameConfig.gridCols = Integer.parseInt(colsField.getText());
                gameConfig.soundEnabled = soundCheckBox.isSelected();
                ConfigManager.saveConfig(gameConfig);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid input. Please enter numbers for level, rows, and columns.");
            }
        }
    }

    public void endGame() {
        if (timer != null) {
            timer.stop();
        }
        if (gameView != null) {
            gameView.dispose();
        }
        startScreen.setVisible(true);
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }

    public void moveLeft() {
        if (gameData.canMove(gameData.getCurrentX() - 1, gameData.getCurrentY(), gameData.getCurrentShape())) {
            gameData.setCurrentX(gameData.getCurrentX() - 1);
            updateView();
            sendGameStateAndGetMove();
        }
    }

    public void moveRight() {
        if (gameData.canMove(gameData.getCurrentX() + 1, gameData.getCurrentY(), gameData.getCurrentShape())) {
            gameData.setCurrentX(gameData.getCurrentX() + 1);
            updateView();
            sendGameStateAndGetMove();
        }
    }

    public void rotate() {
        Shape rotated = gameData.getCurrentShape().getRotatedShape();
        if (gameData.canMove(gameData.getCurrentX(), gameData.getCurrentY(), rotated)) {
            gameData.setCurrentShape(rotated);
            updateView();
            sendGameStateAndGetMove();
        }
    }

    public void moveDown() {
        if (!moveDownInternal()) {
            lockShapeAndContinue();
        }
        sendGameStateAndGetMove();
    }

    private boolean moveDownInternal() {
        if (gameData.canMove(gameData.getCurrentX(), gameData.getCurrentY() + 1, gameData.getCurrentShape())) {
            gameData.setCurrentY(gameData.getCurrentY() + 1);
            updateView();
            return true;
        }
        return false;
    }

    private void lockShapeAndContinue() {
        lockShape();
        clearLines();
        gameData.newShape();
        if (gameData.isGameOver()) {
            handleGameOver();
        }
        updateView();
    }

    private void handleGameOver() {
        timer.stop();
        String name = JOptionPane.showInputDialog(gameView, "Game Over! Your score: " + gameData.getScore() + "\nEnter your name:");
        if (name != null && !name.trim().isEmpty()) {
            scoreManager.addScore(name, gameData.getScore());
        }
        showHighScores();
        int option = JOptionPane.showConfirmDialog(gameView, "Do you want to play again?", "Game Over", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            startGame();
        } else {
            endGame();
        }
    }

    private void lockShape() {
        Shape currentShape = gameData.getCurrentShape();
        int[][] grid = gameData.getGrid();
        int currentX = gameData.getCurrentX();
        int currentY = gameData.getCurrentY();

        for (int i = 0; i < currentShape.getHeight(); i++) {
            for (int j = 0; j < currentShape.getWidth(); j++) {
                if (currentShape.getShape()[i][j] != 0) {
                    grid[currentY + i][currentX + j] = 1;
                }
            }
        }
    }

    private void clearLines() {
        int[][] grid = gameData.getGrid();
        int linesCleared = 0;

        for (int i = grid.length - 1; i >= 0; i--) {
            if (isLineFull(grid[i])) {
                removeLine(grid, i);
                linesCleared++;
                i++; // Check the same line again
            }
        }

        if (linesCleared > 0) {
            gameData.setScore(gameData.getScore() + calculateScore(linesCleared));
            gameData.setLinesCleared(gameData.getLinesCleared() + linesCleared);
            checkLevelUp();
        }
    }

    private boolean isLineFull(int[] line) {
        for (int cell : line) {
            if (cell == 0) return false;
        }
        return true;
    }

    private void removeLine(int[][] grid, int line) {
        for (int i = line; i > 0; i--) {
            System.arraycopy(grid[i-1], 0, grid[i], 0, grid[i].length);
        }
        java.util.Arrays.fill(grid[0], 0);
    }

    private int calculateScore(int linesCleared) {
        switch (linesCleared) {
            case 1: return 100;
            case 2: return 300;
            case 3: return 500;
            case 4: return 800;
            default: return 0;
        }
    }

    private void checkLevelUp() {
        int newLevel = gameData.getLinesCleared() / 10 + 1;
        if (newLevel > gameData.getLevel()) {
            gameData.setLevel(newLevel);
            int newDelay = Math.max(100, 1000 - (newLevel - 1) * 50);
            timer.setDelay(newDelay);
        }
    }

    public void showHighScores() {
        StringBuilder sb = new StringBuilder("High Scores:\n");
        for (ScoreManager.Score score : scoreManager.getHighScores()) {
            sb.append(score.getName()).append(": ").append(score.getScore()).append("\n");
        }
        JOptionPane.showMessageDialog(gameView, sb.toString());
    }

    private void updateView() {
        gameView.update();
    }

    private void sendGameStateAndGetMove() {
        if (socket == null || socket.isClosed()) {
            return;
        }
        try {
            PureGame pureGame = convertToPureGame();
            String jsonGameState = gson.toJson(pureGame);
            out.println(jsonGameState);

            String response = in.readLine();
            OpMove move = gson.fromJson(response, OpMove.class);
            applyMove(move);
        } catch (IOException e) {
            System.err.println("Error communicating with TetrisServer: " + e.getMessage());
        }
    }

    private PureGame convertToPureGame() {
        PureGame pureGame = new PureGame();
        pureGame.setWidth(gameData.getGrid()[0].length);
        pureGame.setHeight(gameData.getGrid().length);
        pureGame.setCells(gameData.getGrid());
        pureGame.setCurrentShape(gameData.getCurrentShape().getShape());
        pureGame.setNextShape(gameData.getNextShape().getShape());
        return pureGame;
    }

    private void applyMove(OpMove move) {
        // Move the piece to the suggested X position
        while (gameData.getCurrentX() < move.opX() && gameData.canMove(gameData.getCurrentX() + 1, gameData.getCurrentY(), gameData.getCurrentShape())) {
            gameData.setCurrentX(gameData.getCurrentX() + 1);
        }
        while (gameData.getCurrentX() > move.opX() && gameData.canMove(gameData.getCurrentX() - 1, gameData.getCurrentY(), gameData.getCurrentShape())) {
            gameData.setCurrentX(gameData.getCurrentX() - 1);
        }

        // Rotate the piece the suggested number of times
        for (int i = 0; i < move.opRotate(); i++) {
            Shape rotated = gameData.getCurrentShape().getRotatedShape();
            if (gameData.canMove(gameData.getCurrentX(), gameData.getCurrentY(), rotated)) {
                gameData.setCurrentShape(rotated);
            }
        }

        updateView();
    }

    public GameData getGameData() {
        return gameData;
    }

    public ScoreManager getScoreManager() {
        return scoreManager;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        moveDown();
    }
}