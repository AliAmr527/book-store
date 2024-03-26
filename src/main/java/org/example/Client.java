package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

class Client {
    Socket socket;
    PrintWriter writer;
    BufferedReader reader;
    boolean isRunning = true;

    public Client(Socket socket) {
        try {
            this.socket = socket;
            this.writer = new PrintWriter(socket.getOutputStream(), true);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            closeEverything();
        }
    }

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 1234);
        Client client = new Client(socket);
        client.sendMessage();
    }

    public void sendMessage() {
        listenForMessage();
        String line;
        Scanner sc = new Scanner(System.in);
        while (isRunning) {
            line = sc.nextLine();
            writer.println(line);
        }
    }

    public void listenForMessage() {
        new Thread(() -> {
            String msgFromServer;
            while (socket.isConnected()) {
                try {
                    msgFromServer = reader.readLine();
                    if (msgFromServer.equals("shutDown")) {
                        isRunning = false;
                        closeEverything();
                        break;
                    }
                    System.out.println(msgFromServer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void closeEverything() {
        try {
            if (writer != null) {
                writer.close();
            }
            if (reader != null) {
                reader.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}