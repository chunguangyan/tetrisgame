package org.example;
import controller.GameController;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameController());
    }
}