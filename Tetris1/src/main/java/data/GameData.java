package data;

public class GameData {
    private int[][] grid;
    private Shape currentShape;
    private Shape nextShape;
    private int currentX;
    private int currentY;
    private int score;
    private int level;
    private int linesCleared;
    private boolean isGameOver;

    public GameData(int rows, int cols) {
        grid = new int[rows][cols];
        score = 0;
        level = 1;
        linesCleared = 0;
        isGameOver = false;
        nextShape = new Shape();
        newShape();
    }

    public void newShape() {
        currentShape = nextShape;
        nextShape = new Shape();
        currentX = grid[0].length / 2 - currentShape.getWidth() / 2;
        currentY = 0;
        if (!canMove(currentX, currentY, currentShape)) {
            isGameOver = true;
        }
    }

    public boolean canMove(int newX, int newY, Shape shape) {
        for (int i = 0; i < shape.getHeight(); i++) {
            for (int j = 0; j < shape.getWidth(); j++) {
                if (shape.getShape()[i][j] != 0) {
                    int x = newX + j;
                    int y = newY + i;
                    if (x < 0 || x >= grid[0].length || y >= grid.length || (y >= 0 && grid[y][x] != 0)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // Getters and setters
    public int[][] getGrid() { return grid; }
    public Shape getCurrentShape() { return currentShape; }
    public Shape getNextShape() { return nextShape; }
    public int getCurrentX() { return currentX; }
    public int getCurrentY() { return currentY; }
    public int getScore() { return score; }
    public int getLevel() { return level; }
    public int getLinesCleared() { return linesCleared; }
    public boolean isGameOver() { return isGameOver; }
    public void setCurrentX(int x) { currentX = x; }
    public void setCurrentY(int y) { currentY = y; }
    public void setScore(int score) { this.score = score; }
    public void setLevel(int level) { this.level = level; }
    public void setLinesCleared(int lines) { this.linesCleared = lines; }
    public void setCurrentShape(Shape shape) { this.currentShape = shape; }
}