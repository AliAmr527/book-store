package org.example;

import java.io.*;
import java.net.*;
import java.util.*;

class Client {
    public static void main(String[] args) {
        // establish a connection by providing host and port
        try (Socket socket = new Socket("localhost", 1234)) {
            // writing to server
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            // reading from server
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // object of scanner class
            Scanner sc = new Scanner(System.in);
            String line = null;
            while (!"exit".equalsIgnoreCase(line)) {
                //Gathering Server reply
                String input, sent = "";
                input = in.readLine();
                if (input.equals("shutDown")) break;
                while (!input.equals("x")) {
                    sent += input + "\n";
                    input = in.readLine();
                }
                // displaying server reply
                System.out.print(sent);
                // reading from user
                line = sc.nextLine();
                // sending the user input to server
                out.println(line);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}