package org.example;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.io.*;
import java.net.*;
import java.util.Scanner;

// Server class
class Server {
    public static void main(String[] args) {
        ServerSocket server = null;
        try {
            // server is listening on port 1234
            server = new ServerSocket(1234);
            server.setReuseAddress(true);
            // running infinite loop for getting
            // client request
            while (true) {

                // socket object to receive incoming client
                // requests
                Socket client = server.accept();
                // Displaying that new client is connected
                // to server
                System.out.println("New client connected"
                        + client.getInetAddress()
                        .getHostAddress());
                // create a new thread object
                ClientHandler clientSock = new ClientHandler(client);
                // This thread will handle the client
                // separately
                new Thread(clientSock).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // ClientHandler class
    private static class ClientHandler implements Runnable {
        mongoConnection db;
        private final Socket clientSocket;
        boolean closeTerm = true;
        boolean isLoggedIn = false;
        String userId;
        // Constructor
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            db = new mongoConnection();
        }
        
        public void run() {
            PrintWriter out = null;
            BufferedReader in = null;
            try {
                // get the output stream of client
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                // get the input stream of client
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String choice;
                String email, password, name, line;
                while (closeTerm){
                    out.println("1) Log in 2) Sign up 3) Exit Choice: ");
                    choice = in.readLine();
                    out.println(choice);
                    int temp = Integer.parseInt(choice);
                    if(choice == "1"){
                        logIn(out, in);
                    } else if (choice == "2") {
                        signUp(out, in);
                    }else if (choice == "3") {
                        closeTerm = false;
                    }else {
                        out.println("Wrong Input, Please Try Again");
                    }
                }
//                while ((line = in.readLine()) != null) {
//                    // writing the received message from
//                    // client
//                    System.out.printf("Sent from the client: %s\n",line);
//                    out.println(line);
//                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                        clientSocket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private static void signUp(PrintWriter out, BufferedReader in) throws IOException {
            String name;
            String email;
            String password;
            out.println("Please Enter Name, Email and Password \n Name: ");
            name = in.readLine();
            out.println("Email: ");
            email = in.readLine();
            out.println("Password: ");
            password = in.readLine();
        }

        private void logIn(PrintWriter out, BufferedReader in) throws IOException {
            String password;
            String email;
            while (!isLoggedIn){
                out.println("Please Enter Email and Password \n Email: ");
                email = in.readLine();
                out.println("Password: ");
                password = in.readLine();
                String user = db.login(email,password);
                if (user != null){
                    out.println("Welcome User");
                    //TODO: SHOW MENU.
                }
                else{
                    out.println("No User Found With these Credintials");
                }
            }

        }
    }
}
