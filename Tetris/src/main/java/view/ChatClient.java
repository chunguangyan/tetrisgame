package view;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChatClient {
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;
    private JFrame frame = new JFrame("Chat Client");
    private JTextField ipField = new JTextField(15);
    private JTextField usernameField = new JTextField(15);
    private JTextArea messageArea = new JTextArea(20, 40);
    private JTextField messageField = new JTextField(40);
    private JButton sendButton = new JButton("Send");
    private JButton connectButton = new JButton("Connect");

    public static void main(String[] args) {
        new ChatClient().startClient();
    }

    private void startClient() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        JPanel topPanel = new JPanel(new GridLayout(3, 2));
        topPanel.add(new JLabel("IP Address (default: localhost):"));
        topPanel.add(ipField);
        topPanel.add(new JLabel("Username:"));
        topPanel.add(usernameField);
        topPanel.add(new JLabel(""));
        topPanel.add(connectButton);
        messageArea.setEditable(false);
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(messageArea), BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(messageField);
        bottomPanel.add(sendButton);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        sendButton.setEnabled(false);

        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(messageField.getText());
                messageField.setText("");
            }
        });

        messageField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(messageField.getText());
                messageField.setText("");
            }
        });

        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                if (username.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "You must provide a username.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                String ipAddress = ipField.getText().isEmpty() ? "localhost" : ipField.getText();
                try {
                    socket = new Socket(ipAddress, 3030);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(socket.getOutputStream(), true);
                    out.println(username);
                    sendButton.setEnabled(true);
                    connectButton.setEnabled(false);
                    new Thread(new IncomingReader()).start();
                } catch (IOException ex) {
                    messageArea.append("Failed to connect to server. Please check the IP address or try again.\n");
                }
            }
        });

        frame.pack();
        frame.setVisible(true);
    }

    private class IncomingReader implements Runnable {
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    messageArea.append(message + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
