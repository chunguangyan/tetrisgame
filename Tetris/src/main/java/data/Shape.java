package data;

import java.awt.Color;
import java.util.Random;

public class Shape {
    private int[][] shape;
    private Color color;
    private int shapeId;
    private static final Random RANDOM = new Random();

    private static final int[][][] SHAPES = {
            {{1, 1, 1, 1}},         // I
            {{1, 1}, {1, 1}},       // O
            {{1, 1, 1}, {0, 1, 0}}, // T
            {{1, 1, 1}, {1, 0, 0}}, // L
            {{1, 1, 1}, {0, 0, 1}}, // J
            {{0, 1, 1}, {1, 1, 0}}, // S
            {{1, 1, 0}, {0, 1, 1}}  // Z
    };

    private static final Color[] COLORS = {
            Color.CYAN, Color.YELLOW, Color.MAGENTA, Color.ORANGE,
            Color.BLUE, Color.GREEN, Color.RED
    };

    public Shape() {
        int index = RANDOM.nextInt(SHAPES.length);
        this.shape = SHAPES[index];
        this.color = COLORS[index];
        this.shapeId = index;
    }

    public int[][] getShape() { return shape; }
    public Color getColor() { return color; }
    public int getWidth() { return shape[0].length; }
    public int getHeight() { return shape.length; }
    public int getShapeId() { return shapeId; }

    public Shape getRotatedShape() {
        int[][] rotated = new int[getWidth()][getHeight()];
        for (int i = 0; i < getHeight(); i++) {
            for (int j = 0; j < getWidth(); j++) {
                rotated[j][getHeight() - 1 - i] = shape[i][j];
            }
        }
        Shape newShape = new Shape();
        newShape.shape = rotated;
        newShape.color = this.color;
        newShape.shapeId = this.shapeId;
        return newShape;
    }
}