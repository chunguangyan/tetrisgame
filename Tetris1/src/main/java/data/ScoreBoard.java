package data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class ScoreBoard {
    private static final String SCORE_FILE = "highscores.json";
    private static final int MAX_SCORES = 10;
    private List<Score> highScores;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ScoreBoard() {
        highScores = new ArrayList<>();
        loadScores();
    }

    public void addScore(String name, int score) {
        highScores.add(new Score(name, score));
        Collections.sort(highScores);
        if (highScores.size() > MAX_SCORES) {
            highScores = highScores.subList(0, MAX_SCORES);
        }
        saveScores();
    }

    public List<Score> getHighScores() {
        return new ArrayList<>(highScores);
    }

    private void loadScores() {
        File file = new File(SCORE_FILE);
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                Type listType = new TypeToken<ArrayList<Score>>(){}.getType();
                highScores = gson.fromJson(reader, listType);
            } catch (IOException e) {
                System.err.println("Error loading scores: " + e.getMessage());
                highScores = new ArrayList<>();
            }
        }
    }

    private void saveScores() {
        try (Writer writer = new FileWriter(SCORE_FILE)) {
            gson.toJson(highScores, writer);
        } catch (IOException e) {
            System.err.println("Error saving scores: " + e.getMessage());
        }
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
            return Integer.compare(other.score, this.score); // 降序排列
        }
    }
}