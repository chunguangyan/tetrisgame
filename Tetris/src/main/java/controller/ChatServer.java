package controller;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {
    private static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        System.out.println("Chat server started on port 3030...");
        ServerSocket serverSocket = new ServerSocket(3030);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("New client connected...");
            ClientHandler clientHandler = new ClientHandler(clientSocket);
            clients.add(clientHandler);
            clientHandler.start();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                username = in.readLine();
                System.out.println("User '" + username + "' connected.");
                broadcastMessage("SERVER", username + " has joined the chat!");
                sendUserList();

                String message;
                while ((message = in.readLine()) != null) {
                    broadcastMessage(username, message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clients.remove(this);
                broadcastMessage("SERVER", username + " has left the chat.");
            }
        }

        private void broadcastMessage(String user, String message) {
            for (ClientHandler client : clients) {
                client.out.println(user + ": " + message);
            }
        }

        private void sendUserList() {
            out.println("Connected users:");
            for (ClientHandler client : clients) {
                out.println(client.username);
            }
        }
    }
}

