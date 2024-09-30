package view;

import controller.GameController;

import javax.swing.*;
import java.awt.*;

public class StartScreen extends JFrame {
    private GameController controller;

    public StartScreen(GameController controller) {
        this.controller = controller;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Tetris");
        setSize(300, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 1, 10, 10));

        JButton startButton = new JButton("Start Game");
        JButton settingsButton = new JButton("Game Settings");
        JButton exitButton = new JButton("Exit Game");

        startButton.addActionListener(e -> controller.startGame());
        settingsButton.addActionListener(e -> controller.openSettings());
        exitButton.addActionListener(e -> System.exit(0));

        add(new JLabel("Welcome to Tetris", SwingConstants.CENTER));
        add(startButton);
        add(settingsButton);
        add(exitButton);

        setLocationRelativeTo(null);
        setVisible(true);
    }
}