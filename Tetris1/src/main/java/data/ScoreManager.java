package data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScoreManager {
    private static final int MAX_HIGHSCORES = 10;
    private List<Score> highScores;

    public ScoreManager() {
        highScores = ConfigManager.loadHighScores();
    }

    public void addScore(String name, int score) {
        highScores.add(new Score(name, score));
        Collections.sort(highScores);
        if (highScores.size() > MAX_HIGHSCORES) {
            highScores = highScores.subList(0, MAX_HIGHSCORES);
        }
        ConfigManager.saveHighScores(highScores);
    }

    public List<Score> getHighScores() {
        return highScores;
    }

    public static class Score implements Comparable<Score> {
        private String name;
        private int score;

        public Score(String name, int score) {
            this.name = name;
            this.score = score;
        }

        public String getName() { return name; }
        public int getScore() { return score; }

        @Override
        public int compareTo(Score other) {
            return Integer.compare(other.score, this.score); // For descending order
        }
    }
}