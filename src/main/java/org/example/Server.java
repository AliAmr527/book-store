package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

class Server {
    ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket);
        server.startServer();
    }

    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected Has connected");
                ClientHandler clientHandler = new ClientHandler(socket);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        static Map<String, ClientHandler> activeUsers = new HashMap<String, ClientHandler>();
        Socket socket;
        PrintWriter writer;
        BufferedReader reader;
        String userName;
        DbMethods db;

        public ClientHandler(Socket socket) {
            try {
                this.socket = socket;
                writer = new PrintWriter(socket.getOutputStream(), true);
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                db = new DbMethods();
            } catch (IOException e) {
                closeEverything();
            }
        }

        public void run() {
            try {
                String choice;
                writer.println("Welcome to the Library!");
                label:
                while (true) {
                    writer.println("1) Log in");
                    writer.println("2) Sign up");
                    writer.println("3) Exit");
                    writer.println("Enter Your Choice: ");
                    writer.println("x");
                    choice = reader.readLine();
                    switch (choice) {
                        case "1":
                            logIn();
                            break;
                        case "2":
                            signUp();
                            break;
                        case "3":
                            writer.println("shutDown");
                            break label;
                        default:
                            writer.println("Wrong Input, Please Try Again");
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                closeEverything();
            }

        }
        /*
        public void removeClientHandler() {
            clientHandlers.remove(this);
            //TODO: send the receiver's name
            broadcastMessage("user has left the chat");
        }*/
        public void closeEverything() {
            try {
                //TODO: CHECK THIS
                //removeClientHandler();
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

        private void signUp() throws IOException {
            String name, username, password;
            writer.println("Please enter your details to sign up");
            writer.println("Name:");
            writer.println("x");
            name = reader.readLine();
            writer.println("Username:");
            writer.println("x");
            username = reader.readLine();
            writer.println("Password:");
            writer.println("x");
            password = reader.readLine();
            String[] ans = db.register(name, username, password);
            if (!ans[0].equals("false")) {
                writer.println("Signed up Successfully!!!");
                writer.println("Hello " + ans[1]);
                userName = ans[2];
                activeUsers.put(userName, this);
                menu();
            } else {
                writer.println("Error: " + ans[1] + " " + ans[2]);
            }

        }

        private void logIn() throws IOException {
            String password;
            String username;
            writer.println("Please Enter Username and Password");
            writer.println("Username: ");
            writer.println("x");
            username = reader.readLine();
            writer.println("Password: ");
            writer.println("x");
            password = reader.readLine();
            String[] ans = db.login(username, password);
            if (ans[0].equals("true")) {
                writer.println("Signed in successfully!");
                writer.println("Hello " + ans[1]);
                userName = ans[2];
                if (ans[0].equals("admin")) adminMenu();
                else {
                    activeUsers.put(userName, this);
                    menu();
                }
            } else {
                writer.println("Error: " + ans[1] + " " + ans[2]);
            }
        }

        private void menu() throws IOException {
            String choice;
            label:
            while (true) {
                writer.println("--- P E R S O N A L ---");
                writer.println("0) Log out");
                writer.println("1) Add a book");
                writer.println("2) Remove a book");
                writer.println("3) Check Requests");
                writer.println("4) Request history");
                writer.println("5) Chat With other clients");
                writer.println("");
                writer.println("--- L I B R A R Y ---");
                writer.println("6) View Library");
                writer.println("7) Search for a book");
                writer.println("Enter Your Choice: ");
                writer.println("x");
                choice = reader.readLine();
                switch (choice) {
                    case "0":
                        activeUsers.remove(userName);
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
                        chatList();
                        break;
                    case "6":
                        viewLibrary();
                        break;
                    case "7":
                        searchBook();
                        break;

                    default:
                        writer.println("Wrong Input, Please Try Again");
                        break;
                }
            }
        }


        private void addBook() throws IOException {
            String title, author, genre;
            int price, quantity;
            writer.println("Please enter book details");
            writer.println("Book title: ");
            writer.println("x");
            title = reader.readLine();
            writer.println("Book author: ");
            writer.println("x");
            author = reader.readLine();
            writer.println("Book genre: ");
            writer.println("x");
            genre = reader.readLine();
            writer.println("Book price: ");
            writer.println("x");
            price = Integer.parseInt(reader.readLine());
            writer.println("Book quantity: ");
            writer.println("x");
            quantity = Integer.parseInt(reader.readLine());
            String[] ans = db.addBook(title, author, genre, price, quantity, userName);
            if (ans[0].equals("true")) writer.println(ans[2]);
            else writer.println("Error: " + ans[1] + " " + ans[2]);
        }

        private void removeBook() throws IOException {
            String[] books = db.viewMyBooks(userName);
            int bookId;
            if (books.length == 0) writer.println("No books to view!");
            else {
                for (int i = 0; i < books.length; i++) {
                    writer.println(i + 1 + ") " + books[i]);
                }
                writer.println("Choose a book to remove or 0 (Zero) to go back: ");
                writer.println("x");
                bookId = Integer.parseInt(reader.readLine());
                if (bookId == 0) return;
                String[] ans = db.removeBook(books[bookId - 1]);
                if (ans[0].equals("true")) writer.println(ans[2]);
                else writer.println("Error: " + ans[1] + " " + ans[2]);
            }
        }

        private void viewLibrary() throws IOException {
            String[] books = db.viewBooks();
            int bookId;
            if (books.length == 0) writer.println("No books to view!");
            else {
                for (int i = 0; i < books.length; i++) {
                    writer.println(i + 1 + ") " + books[i]);
                }
                writer.println("Choose a book to view or 0 (Zero) to go back:  ");
                writer.println("x");
                bookId = Integer.parseInt(reader.readLine());
                if (bookId == 0) return;
                viewBook(books[bookId - 1]);
            }
        }

        private void viewBook(String bookName) throws IOException {
            String[] bookDetail = db.viewBookDetails(bookName);
            String choice;
            writer.println("Title: " + bookDetail[0]);
            writer.println("Author: " + bookDetail[1]);
            writer.println("Genre: " + bookDetail[2]);
            writer.println("Price: " + bookDetail[3]);
            writer.println("Quantity: " + bookDetail[4]);
            writer.println("Book Owner: " + bookDetail[5]);
            writer.println("Do you want to Request " + bookDetail[0] + "? (enter 'yes' or 'no')");
            writer.println("Your Answer: ");
            writer.println("x");
            choice = reader.readLine();
            if (choice.equals("yes")) {
                String[] ans = db.submitRequest(bookDetail[0], userName);
                if (ans[0].equals("true")) writer.println(ans[2]);
                else writer.println("Error: " + ans[1] + " " + ans[2]);
            }
        }

        private void searchBook() throws IOException {
            String choice;
            writer.println("Choose the search criteria");
            writer.println("1) Title ");
            writer.println("2) Author ");
            writer.println("3) Genre ");
            writer.println("Choice: ");
            writer.println("x");
            choice = reader.readLine();
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
                    writer.println("Wrong Input!");
                    break;
            }
        }

        private void searchByTitle() throws IOException {
            String choice;
            writer.println("Enter the title you want to search for");
            writer.println("Response: ");
            writer.println("x");
            choice = reader.readLine();
            String[][] advBooks = db.bookByTitle(choice);
            if (advBooks.length == 0) writer.println("No books with that title!");
            else {
                viewBook2d(advBooks);
            }
        }

        private void searchByAuthor() throws IOException {
            String choice;
            writer.println("Enter the Author you want to search for");
            writer.println("Response: ");
            writer.println("x");
            choice = reader.readLine();
            String[][] advBooks = db.bookByAuthor(choice);
            if (advBooks.length == 0) writer.println("No books with that Author!");
            else {
                viewBook2d(advBooks);
            }

        }

        private void searchByGenre() throws IOException {
            String choice;
            writer.println("Enter the Genre you want to search for");
            writer.println("Response: ");
            writer.println("x");
            choice = reader.readLine();
            String[][] advBooks = db.bookByGenre(choice);
            if (advBooks.length == 0) writer.println("No books with that Genre!");
            else {
                viewBook2d(advBooks);
            }

        }

        private void viewBook2d(String[][] advBooks) throws IOException {
            int bookId;
            for (int i = 0; i < advBooks.length; i++) {
                writer.println(i + 1 + ") " + advBooks[i][0]);
                writer.println("   Author: " + advBooks[i][1]);
                writer.println("   Genre: " + advBooks[i][2]);
                writer.println("   Price: " + advBooks[i][3]);
                writer.println("   Quantity: " + advBooks[i][4]);
                writer.println("   Book Owner: " + advBooks[i][5]);
            }
            writer.println("Choose a book to request or 0 (Zero) to go back:  ");
            writer.println("x");
            bookId = Integer.parseInt(reader.readLine());
            if (bookId == 0) return;
            String[] ans = db.submitRequest(advBooks[bookId - 1][0], userName);
            if (ans[0].equals("true")) writer.println(ans[2]);
            else writer.println("Error: " + ans[1] + " " + ans[2]);
        }

        private void checkRequests() throws IOException {
            int requestId, choice;
            String[] ans;
            String[][] requests = db.viewMyRequests(userName);
            for (int i = 0; i < requests.length; i++) {
                writer.println(i + 1 + ") " + requests[i][1]);
                writer.println("   Borrower: " + requests[i][2]);
            }
            writer.println("Choose the request you want to handle or 0 (Zero) to go back:  ");
            writer.println("x");
            requestId = Integer.parseInt(reader.readLine());
            if (requestId == 0) return;
            writer.println("1) Approve request");
            writer.println("2) Deny request");
            writer.println("x");
            choice = Integer.parseInt(reader.readLine());
            if (choice == 1) ans = db.modifyRequest("accept", requests[requestId - 1][0]);
            else ans = db.modifyRequest("deny", requests[requestId - 1][0]);
            if (ans[0].equals("true")) writer.println(ans[2]);
            else writer.println("Error: " + ans[1] + " " + ans[2]);
        }

        private void requestHistory() throws IOException {
            String[][] requests = db.viewMyRequestHistory(userName);
            for (String[] request : requests) {
                writer.println(request[0] + 1 + ") " + request[1]);
                writer.println("   Borrower: " + request[2]);
                writer.println("   Status: " + request[3]);
            }

        }

        private void adminMenu() throws IOException {
            String choice;
            label:
            while (true) {
                writer.println("--- A D M I N ---");
                writer.println("0) Log out");
                writer.println("1) View Library Stats");
                writer.println("Enter Your Choice: ");
                writer.println("x");
                choice = reader.readLine();
                switch (choice) {
                    case "0":
                        break label;
                    case "1":
                        libraryStats();
                        break;
                    default:
                        writer.println("Wrong Input, Please Try Again");
                        break;
                }
            }
        }

        private void libraryStats() throws IOException {
            String[] stats = db.libraryStats();
            for (int i = 0; i < stats.length; i++) {
                writer.println("Current borrowed books: " + stats[0]);
                writer.println("Available books: " + stats[1]);
                writer.println("Requests: " + stats[2]);
            }
        }

//      private void chatList() throws IOException {
//            int userId;
//            String[][] acceptedUsers = db.getAcceptedUsers(userName);
//            if (acceptedUsers.length == 0) {
//                out.println("No books to view!");
//                return;
//            }
//            out.println("Active Users: ");
//            for (int i = 0; i < acceptedUsers.length; i++) {
//                out.println(i + 1 + ") " + acceptedUsers[i][0]);
//                out.println("   Type: " + acceptedUsers[i][0]);
//                out.println("   Book Name: " + acceptedUsers[i][0]);
//            }
//            out.println("Choose User to chat with: ");
//            out.println("x");
//            userId = Integer.parseInt(in.readLine());
//            Socket receiver = activeUsers.get(acceptedUsers[0][userId - 1]);
//            Socket sender = activeUsers.get(userName);
//            if (receiver != null || sender != null) {
//                startChat(receiver, sender);
//            } else {
//                out.println("This user is no longer active!");
//            }
//        }
        private void chatList() throws IOException {
            int counter = 0;
            String userId;
            writer.println("Active Users: ");
            for (String activeU : activeUsers.keySet()) {
                writer.println(counter + 1 + ") " + activeU);
                counter++;
            }
            writer.println("Choose User to chat with: ");
            writer.println("x");
            userId = reader.readLine();
            if (userId.equals("x")) return;
            ClientHandler receiver = activeUsers.get(userId);
            if (receiver != null) {
                startChat(receiver);
            } else {
                writer.println("This user is no longer active!");
            }
        }

        private void startChat(ClientHandler receiver) throws IOException {
            //TODO: HERE WE NEED TO SEND EVERY MESSAGE RECEIVED FROM THE CLIENT AND SEND IT TO THE OTHER CONNECTED USER
            //TODO: ALSO SWITCH TO ASYNC MODE IN THE CLIENT SIDE
            String messageFromClient;
            while (socket.isConnected()) {
                try {
                    messageFromClient = reader.readLine();
                    //TODO: CHECK IF THE TEXT IS THE 'END' TEXT THEN EXIT
                    receiver.writer.println(messageFromClient);
                } catch (IOException e) {
                    closeEverything();
                    break;
                }

            }
        }
    }

}
