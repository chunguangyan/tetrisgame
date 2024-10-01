package data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ConfigManager {
    private static final String CONFIG_FILE = "game_config.json";
    private static final String HIGHSCORE_FILE = "highscores.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static class GameConfig {
        public int initialLevel;
        public int gridRows;
        public int gridCols;
        public boolean soundEnabled;

        public GameConfig() {
            // Default values
            this.initialLevel = 1;
            this.gridRows = 20;
            this.gridCols = 10;
            this.soundEnabled = true;
        }
    }

    public static void saveConfig(GameConfig config) {
        try (Writer writer = new FileWriter(CONFIG_FILE)) {
            gson.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static GameConfig loadConfig() {
        try (Reader reader = new FileReader(CONFIG_FILE)) {
            return gson.fromJson(reader, GameConfig.class);
        } catch (IOException e) {
            System.out.println("Config file not found. Creating new config.");
            return new GameConfig();
        }
    }

    public static void saveHighScores(List<ScoreManager.Score> highScores) {
        try (Writer writer = new FileWriter(HIGHSCORE_FILE)) {
            gson.toJson(highScores, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<ScoreManager.Score> loadHighScores() {
        try (Reader reader = new FileReader(HIGHSCORE_FILE)) {
            Type type = new TypeToken<ArrayList<ScoreManager.Score>>(){}.getType();
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            System.out.println("Highscore file not found. Starting with empty list.");
            return new ArrayList<>();
        }
    }
}