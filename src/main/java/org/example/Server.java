package org.example;

import java.io.*;
import java.net.*;

class Server {
    public static void main(String[] args) {
        ServerSocket server = null;
        try {
            server = new ServerSocket(1234);
            server.setReuseAddress(true);
            // running infinite loop for getting client requests
            while (true) {
                // socket object to receive incoming client requests
                Socket client = server.accept();
                // Displaying that new client is connected to server
                System.out.println("New client connected" + client.getInetAddress().getHostAddress());
                // create a new thread object
                ClientHandler clientSock = new ClientHandler(client);
                // This thread will handle the client separately
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
        private final Socket clientSocket;
        PrintWriter out = null;
        BufferedReader in = null;
        String userId;
        mongoConnection db;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            db = new mongoConnection();
        }

        public void run() {
            try {
                // get the output stream of client
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                // get the input stream of client
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String choice;
                out.println("Welcome to the Library!");
                while (true) {
                    out.println("1) Log in");
                    out.println("2) Sign up");
                    out.println("3) Exit");
                    out.println("Enter Your Choice: ");
                    out.println("x");
                    choice = in.readLine();
                    if (choice.equals("1")) {
                        logIn();
                    } else if (choice.equals("2")) {
                        signUp();
                    } else if (choice.equals("3")) {
                        out.println("shutDown");
                        break;
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

        private void signUp() throws IOException {
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
                menu();
            } else {
                out.println("Error: " + ans[1] + " " + ans[2]);
            }

        }

        private void logIn() throws IOException {
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
                menu();
            } else {
                out.println("Error: " + ans[1] + " " + ans[2]);
            }
        }

        private void menu() throws IOException {
            String choice;
            while (true) {
                out.println("0) Log out");
                out.println("1) Add a book");
                out.println("2) Remove a book");
                out.println("3) View Library");
                out.println("Enter Your Choice: ");
                out.println("x");
                choice = in.readLine();
                if (choice.equals("1")) {
                    addBook();
                } else if (choice.equals("2")) {
                    removeBook();
                } else if (choice.equals("3")) {
                    viewLibrary();
                } else if (choice.equals("0")) {
                    break;
                } else {
                    out.println("Wrong Input, Please Try Again");
                }
            }
        }


        private void addBook() throws IOException {
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
                out.println(ans[2]);
            else
                out.println("Error: " + ans[1] + " " + ans[2]);
        }

        private void removeBook() throws IOException {
            String[] books = db.viewMyBooks(userId);
            int bookid;
            if (books.length == 0)
                out.println("No books to view!");
            else {
                for (int i = 0; i < books.length; i++) {
                    out.println(i + 1 + ") " + books[i]);
                }
                out.println("Choose a book to remove or 0 (Zero) to go back: ");
                out.println("x");
                bookid = Integer.parseInt(in.readLine());
                if (bookid == 0) return;
                String[] ans = db.removeBook(books[bookid - 1]);
                if (ans[0].equals("true"))
                    out.println(ans[2]);
                else
                    out.println("Error: " + ans[1] + " " + ans[2]);
            }
        }

        private void viewLibrary() throws IOException {
            String[] books = db.viewBooks();
            int bookid;
            if (books.length == 0)
                out.println("No books to view!");
            else {
                for (int i = 0; i < books.length; i++) {
                    out.println(i + 1 + ") " + books[i]);
                }
                out.println("Choose a book to view or 0 (Zero) to go back:  ");
                out.println("x");
                bookid = Integer.parseInt(in.readLine());
                if (bookid == 0) return;
                viewBook(books[bookid - 1]);
            }
        }

        private void viewBook(String bookName) throws IOException {
            String[] bookDetail = db.viewBookDetails(bookName);
            String choice;
            out.println("Title: " + bookDetail[0]);
            out.println("Author: " + bookDetail[1]);
            out.println("Genre: " + bookDetail[2]);
            out.println("Price: " + bookDetail[3]);
            out.println("Quantity: " + bookDetail[4]);
            out.println("Book Owner: " + bookDetail[5]);
            out.println("Do you want to Request " + bookDetail[0] + "? (enter 'yes' or 'no')");
            out.println("Your Answer: ");
            out.println("x");
            choice = in.readLine();
            if (choice.equals("yes")) {
                //TODO: REQUEST TO BORROW BOOK HERE
                out.println("Book requested!");
            }
        }
    }


}
