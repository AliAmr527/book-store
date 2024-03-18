package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

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
        String userName;
        DbMethods db;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
            db = new DbMethods();
        }

        public void run() {
            try {
                // get the output stream of client
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                // get the input stream of client
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String choice;
                out.println("Welcome to the Library!");
                label:
                while (true) {
                    out.println("1) Log in");
                    out.println("2) Sign up");
                    out.println("3) Exit");
                    out.println("Enter Your Choice: ");
                    out.println("x");
                    choice = in.readLine();
                    switch (choice) {
                        case "1":
                            logIn();
                            break;
                        case "2":
                            signUp();
                            break;
                        case "3":
                            out.println("shutDown");
                            break label;
                        default:
                            out.println("Wrong Input, Please Try Again");
                            break;
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
                userName = ans[2];
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
                userName = ans[2];
                if (ans[0].equals("admin")) adminMenu();
                else menu();
            } else {
                out.println("Error: " + ans[1] + " " + ans[2]);
            }
        }

        private void menu() throws IOException {
            String choice;
            label:
            while (true) {
                out.println("--- P E R S O N A L ---");
                out.println("0) Log out");
                out.println("1) Add a book");
                out.println("2) Remove a book");
                out.println("3) Check Requests");
                out.println("4) Request history");
                out.println("");
                out.println("--- L I B R A R Y ---");
                out.println("5) View Library");
                out.println("6) Search for a book");
                out.println("Enter Your Choice: ");
                out.println("x");
                choice = in.readLine();
                switch (choice) {
                    case "0":
                        break label;
                    case "1":
                        addBook();
                        break;
                    case "2":
                        removeBook();
                        break;
                    case "3":
                        checkRequests();
                        break;
                    case "4":
                        requestHistory();
                        break;
                    case "5":
                        viewLibrary();
                        break;
                    case "6":
                        searchBook();
                        break;

                    default:
                        out.println("Wrong Input, Please Try Again");
                        break;
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
            String[] ans = db.addBook(title, author, genre, price, quantity, userName);
            if (ans[0].equals("true")) out.println(ans[2]);
            else out.println("Error: " + ans[1] + " " + ans[2]);
        }

        private void removeBook() throws IOException {
            String[] books = db.viewMyBooks(userName);
            int bookId;
            if (books.length == 0) out.println("No books to view!");
            else {
                for (int i = 0; i < books.length; i++) {
                    out.println(i + 1 + ") " + books[i]);
                }
                out.println("Choose a book to remove or 0 (Zero) to go back: ");
                out.println("x");
                bookId = Integer.parseInt(in.readLine());
                if (bookId == 0) return;
                String[] ans = db.removeBook(books[bookId - 1]);
                if (ans[0].equals("true")) out.println(ans[2]);
                else out.println("Error: " + ans[1] + " " + ans[2]);
            }
        }

        private void viewLibrary() throws IOException {
            String[] books = db.viewBooks();
            int bookId;
            if (books.length == 0) out.println("No books to view!");
            else {
                for (int i = 0; i < books.length; i++) {
                    out.println(i + 1 + ") " + books[i]);
                }
                out.println("Choose a book to view or 0 (Zero) to go back:  ");
                out.println("x");
                bookId = Integer.parseInt(in.readLine());
                if (bookId == 0) return;
                viewBook(books[bookId - 1]);
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
                String[] ans = db.submitRequest(bookDetail[0], userName);
                if (ans[0].equals("true")) out.println(ans[2]);
                else out.println("Error: " + ans[1] + " " + ans[2]);
            }
        }

        private void searchBook() throws IOException {
            String choice;
            out.println("Choose the search criteria");
            out.println("1) Title ");
            out.println("2) Author ");
            out.println("3) Genre ");
            out.println("Choice: ");
            out.println("x");
            choice = in.readLine();
            switch (choice) {
                case "1":
                    searchByTitle();
                    break;
                case "2":
                    searchByAuthor();
                    break;
                case "3":
                    searchByGenre();
                    break;
                default:
                    out.println("Wrong Input!");
                    break;
            }
        }

        private void searchByTitle() throws IOException {
            String choice;
            out.println("Enter the title you want to search for");
            out.println("Response: ");
            out.println("x");
            choice = in.readLine();
            String[][] advBooks = db.bookByTitle(choice);
            if (advBooks.length == 0) out.println("No books with that title!");
            else {
                viewBook2d(advBooks);
            }
        }

        private void searchByAuthor() throws IOException {
            String choice;
            out.println("Enter the Author you want to search for");
            out.println("Response: ");
            out.println("x");
            choice = in.readLine();
            String[][] advBooks = db.bookByAuthor(choice);
            if (advBooks.length == 0) out.println("No books with that Author!");
            else {
                viewBook2d(advBooks);
            }

        }

        private void searchByGenre() throws IOException {
            String choice;
            out.println("Enter the Genre you want to search for");
            out.println("Response: ");
            out.println("x");
            choice = in.readLine();
            String[][] advBooks = db.bookByGenre(choice);
            if (advBooks.length == 0) out.println("No books with that Genre!");
            else {
                viewBook2d(advBooks);
            }

        }

        private void viewBook2d(String[][] advBooks) throws IOException {
            int bookId;
            for (int i = 0; i < advBooks.length; i++) {
                out.println(i + 1 + ") " + advBooks[i][0]);
                out.println("   Author: " + advBooks[i][1]);
                out.println("   Genre: " + advBooks[i][2]);
                out.println("   Price: " + advBooks[i][3]);
                out.println("   Quantity: " + advBooks[i][4]);
                out.println("   Book Owner: " + advBooks[i][5]);
            }
            out.println("Choose a book to request or 0 (Zero) to go back:  ");
            out.println("x");
            bookId = Integer.parseInt(in.readLine());
            if (bookId == 0) return;
            String[] ans = db.submitRequest(advBooks[bookId - 1][0], userName);
            if (ans[0].equals("true")) out.println(ans[2]);
            else out.println("Error: " + ans[1] + " " + ans[2]);
        }

        private void checkRequests() throws IOException {
            int requestId, choice;
            String[] ans;
            String[][] requests = db.viewMyRequests(userName);
            for (int i = 0; i < requests.length; i++) {
                out.println(i + 1 + ") " + requests[i][1]);
                out.println("   Borrower: " + requests[i][2]);
            }
            out.println("Choose the request you want to handle or 0 (Zero) to go back:  ");
            out.println("x");
            requestId = Integer.parseInt(in.readLine());
            if (requestId == 0) return;
            out.println("1) Approve request");
            out.println("2) Deny request");
            out.println("x");
            choice = Integer.parseInt(in.readLine());
            if (choice == 1) ans = db.modifyRequest("accept", requests[requestId - 1][0]);
            else ans = db.modifyRequest("deny", requests[requestId - 1][0]);
            if (ans[0].equals("true")) out.println(ans[2]);
            else out.println("Error: " + ans[1] + " " + ans[2]);
        }

        private void requestHistory() throws IOException {
            //TODO: CHANGE FOR THE CORRECT METHOD FROM DB
//            String[][] requests = db.requestHistory();
//            for (String[] request : requests) {
//                out.println(request[0] + 1 + ") " + request[1]);
//                out.println("   Borrower: " + request[2]);
//                out.println("   Status: " + request[3]);
//            }

        }

        private void adminMenu() throws IOException {
            String choice;
            label:
            while (true) {
                out.println("--- A D M I N ---");
                out.println("0) Log out");
                out.println("1) View Library Stats");
                out.println("Enter Your Choice: ");
                out.println("x");
                choice = in.readLine();
                switch (choice) {
                    case "0":
                        break label;
                    case "1":
                        libraryStats();
                        break;
                    default:
                        out.println("Wrong Input, Please Try Again");
                        break;
                }
            }
        }

        private void libraryStats() throws IOException {
            //TODO: CHANGE FOR THE CORRECT METHOD FROM DB
//            String[] stats = db.getStats();
//            for (int i = 0; i < stats.length; i++) {
//                out.println("Current borrowed books: " + stats[0]);
//                out.println("Available books: " + stats[1]);
//                out.println("Requests: " + stats[2]);
//            }
        }
    }
}
