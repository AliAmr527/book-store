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

    private static class ClientHandler implements Runnable {
        mongoConnection db;
        private final Socket clientSocket;
        boolean closeTerm = true;
        boolean isLoggedIn = false;
        String userId;

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
                while (closeTerm) {
                    out.println("1) Log in");
                    out.println("2) Sign up");
                    out.println("3) Exit");
                    out.println("Enter Your Choice: ");
                    out.println("x");
                    choice = in.readLine();
                    if (choice.equals("1")) {
                        logIn(out, in);
                    } else if (choice.equals("2")) {
                        signUp(out, in);
                    } else if (choice.equals("3")) {
                        closeTerm = false;
                    } else {
                        out.println("Wrong Input, Please Try Again");
                    }
                }
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

        private void signUp(PrintWriter out, BufferedReader in) throws IOException {
            String name, username, password;
            out.println("Please enter your details to sign up");
            out.println("Name:");
            out.println("x");
            name = in.readLine();
            out.println("Username:");
            out.println("x");
            username = in.readLine();
            out.println("Password:");
            out.println("x");
            password = in.readLine();
            String[] ans = db.register(name, username, password);
            if (!ans[0].equals("false")) {
                out.println("Signed up Successfully!!!");
                out.println("Hello " + ans[1]);
                userId = ans[2];
                menu(out, in);
            } else {
                out.println("Error: " + ans[1] + " " + ans[2]);
            }

        }

        private void logIn(PrintWriter out, BufferedReader in) throws IOException {
            String password;
            String username;
            out.println("Please Enter Username and Password");
            out.println("Username: ");
            out.println("x");
            username = in.readLine();
            out.println("Password: ");
            out.println("x");
            password = in.readLine();
            String[] ans = db.login(username, password);
            if (ans[0].equals("true")) {
                out.println("Signed in successfully!");
                out.println("Hello " + ans[1]);
                userId = ans[2];
                menu(out, in);
            } else {
                out.println("Error: " + ans[1] + " " + ans[2]);
            }
        }

        private void menu(PrintWriter out, BufferedReader in) throws IOException {
            String choice;
            while (true) {
                out.println("1) Add a book");
                out.println("2) Remove a book");
                out.println("z) option z");
                out.println("x) Log out");
                out.println("Enter Your Choice: ");
                out.println("x");
                choice = in.readLine();
                if (choice.equals("1")) {
                    addBook(out, in);
                } else if (choice.equals("2")) {
                    removeBook(out, in);
                } else if (choice.equals("x")) {
                    break;
                } else {
                    out.println("Wrong Input, Please Try Again");
                }
            }
        }

        //(String title, String author, String genre, int price,int quantity,String owner){
        private void addBook(PrintWriter out, BufferedReader in) throws IOException {
            String title, author, genre;
            int price, quantity;
            out.println("Please enter book details");
            out.println("Book title: ");
            out.println("x");
            title = in.readLine();
            out.println("Book author: ");
            out.println("x");
            author = in.readLine();
            out.println("Book genre: ");
            out.println("x");
            genre = in.readLine();
            out.println("Book price: ");
            out.println("x");
            price = Integer.parseInt(in.readLine());
            out.println("Book quantity: ");
            out.println("x");
            quantity = Integer.parseInt(in.readLine());
            String[] ans = db.addBook(title, author, genre, price, quantity, userId);
            if (ans[0].equals("true"))
                out.println(ans[2] + " added successfully!");
            else
                out.println("Error: " + ans[1] + " " + ans[2]);
        }

        private void removeBook(PrintWriter out, BufferedReader in) {

        }
    }
}
