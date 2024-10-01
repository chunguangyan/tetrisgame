package controller;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import data.GameData;
import data.Shape;
import view.GameView;

public class GameControllerTest {
    private GameManager gameManager;
    private GameController gameController;
    private GameData gameData;

    @Before
    public void setUp() {
        gameManager = new GameManager();
        gameController = gameManager.getGameController();
        gameData = gameManager.getGameData();
    }

    @Test
    public void testStartGame() {
        gameController.startGame();
        assertFalse(gameData.isGameOver());
        assertNotNull(gameData.getCurrentShape());
    }

    @Test
    public void testMoveLeft() {
        int initialX = gameData.getCurrentX();
        gameController.moveLeft();
        assertEquals(initialX - 1, gameData.getCurrentX());
    }

    @Test
    public void testMoveRight() {
        int initialX = gameData.getCurrentX();
        gameController.moveRight();
        assertEquals(initialX + 1, gameData.getCurrentX());
    }

    @Test
    public void testRotate() {
        Shape initialShape = gameData.getCurrentShape();
        gameController.rotate();
        assertNotEquals(initialShape, gameData.getCurrentShape());
    }

    @Test
    public void testMoveDown() {
        int initialY = gameData.getCurrentY();
        gameController.moveDown();
        assertTrue(gameData.getCurrentY() > initialY);
    }

    @Test
    public void testLockShapeAndContinue() {

        while (gameData.canMove(gameData.getCurrentX(), gameData.getCurrentY() + 1, gameData.getCurrentShape())) {
            gameController.moveDown();
        }
        int initialY = gameData.getCurrentY();
        gameController.moveDown(); // 这应该触发 lockShapeAndContinue
        assertNotEquals(initialY, gameData.getCurrentY());
        assertNotNull(gameData.getCurrentShape());
    }

    @Test
    public void testGameOver() {

        for (int i = 1; i < gameData.getGrid().length; i++) {
            for (int j = 0; j < gameData.getGrid()[0].length; j++) {
                gameData.getGrid()[i][j] = 1;
            }
        }
        gameController.moveDown();
        assertTrue(gameData.isGameOver());
    }

    @Test
    public void testPauseAndResume() {
        gameController.startGame();
        gameController.pauseGame();

        gameController.resumeGame();

    }

    @Test
    public void testClearLines() {

        for (int j = 0; j < gameData.getGrid()[0].length; j++) {
            gameData.getGrid()[gameData.getGrid().length - 1][j] = 1;
        }
        int initialScore = gameData.getScore();
        gameController.moveDown(); // 这应该触发清除行
        assertTrue(gameData.getScore() > initialScore);
    }

    @Test
    public void testUpdateScore() {
        int initialScore = gameData.getScore();
        for (int j = 0; j < gameData.getGrid()[0].length; j++) {
            gameData.getGrid()[gameData.getGrid().length - 1][j] = 1;
        }
        gameController.moveDown();
        assertTrue(gameData.getScore() > initialScore);
    }
}
