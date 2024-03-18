package org.example;

import static com.mongodb.client.model.Filters.eq;

import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Pattern;

import static com.mongodb.client.model.Filters.regex;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.DBObject;
import com.mongodb.client.*;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbMethods {
    MongoCollection<Document> colUsers;
    MongoCollection<Document> colBooks;
    MongoCollection<Document> colRequests;
    mongoConnection db;
    public DbMethods() {
        db = new mongoConnection();
        colUsers = db.getDb().getCollection("users");
        colBooks = db.getDb().getCollection("books");
        colRequests = db.getDb().getCollection("requests");
    }

    public Document insertTest(String title, String author, String genre, int price, int quantity, String owner,String[] userList){
        Document sampleDoc = new Document().append("title", title).append("author", author).append("genre", genre)
                .append("price", price).append("quantity", quantity).append("owner", owner).append("userList", Arrays.asList(userList));
        colBooks.insertOne(sampleDoc);
        Document book = colBooks.find(eq("title", title)).first();
        return book;
    }

    public List<Document> getBooks() {
        List<Document> books = new ArrayList<>();
        Bson projectionFields = Projections.fields(Projections.include("username", "name"),Projections.excludeId());
        for (Document bookDoc : colBooks.find()) {
            List<String> userList = (List<String>) bookDoc.get("userList");
            if (userList != null && !userList.isEmpty()) {
                List<Document> users = new ArrayList<>();
                for (String username : userList) {
                    Document user = colUsers.find(new Document("username", username)).projection(projectionFields).first();
                    if (user != null) {
                        users.add(user);
                    }
                }
                bookDoc.put("userList", users);
            }
            books.add(bookDoc);
        }
        return books;
    }

    public String[] msg(String flag, String code, String msg) {
        return new String[]{flag, code, msg};
    }

    public String[] getUserNameAndName(Document user) {
        //this for name
        String s = user.toJson().split(",")[1];
        String s2 = s.split(": \"")[1];
        String s3 = s2.split("\"")[0];

        //this for username
        String s4 = user.toJson().split(",")[2];
        String s5 = s4.split(": \"")[1];
        String s6 = s5.split("\"")[0];

        return new String[]{"true", s3, s6};
    }

    public String[] login(String username, String password) {
        Document doc = colUsers.find(eq("username", username)).first();
        if (doc == null) {
            return msg("false", "404", "username not found");
        }

        DBObject condition1 = new BasicDBObject("username", username).append("password", password);
        BasicDBList search = new BasicDBList();
        search.add(condition1);
        Bson projectionFields = Projections.fields(Projections.include("username", "name"));
        DBObject query = new BasicDBObject("$and", search);
        Document user = colUsers.find((Bson) query).projection(projectionFields).first();

        if (user == null) {
            return msg("false", "401", "wrong password");
        }

        return getUserNameAndName(user);
    }

    public String[] register(String name, String username, String password) {
        Document doc = colUsers.find(eq("username", username)).first();
        if (doc != null) {
            return msg("false", "409", "duplicate username");
        }
        Document sampleDoc = new Document().append("name", name).append("username", username).append("password", password);
        colUsers.insertOne(sampleDoc);
        Bson projectionFields = Projections.fields(Projections.include("username", "name"));
        Document user = colUsers.find(eq("username", username)).projection(projectionFields).first();
        assert user != null;

        return getUserNameAndName(user);
    }

    //title author genre price quantity list of clients
    public String[] addBook(String title, String author, String genre, int price, int quantity, String owner) {
        Document doc = colBooks.find(eq("title", title)).first();

        if (doc != null) {
            return msg("false", "409", "duplicate book title");
        }
        Document sampleDoc = new Document().append("title", title).append("author", author).append("genre", genre)
                .append("price", price).append("quantity", quantity).append("owner", owner).append("userList", Collections.emptyList());
        colBooks.insertOne(sampleDoc);

        return msg("true", "200", "book added successfully!");
    }

    public String[] removeBook(String title) {
        Bson query = eq("title", title);
        DeleteResult result = colBooks.deleteOne(query);
        System.out.println(result);
        if (result.getDeletedCount() == 0) {
            return msg("false", "404", "couldn't find book");
        }
        return msg("true", "200", "book deleted successfully");
    }

    public String[] loopDocuments(MongoCursor<Document> cursor, String[] res) {
        int count = 0;
        while (cursor.hasNext()) {
            String s = cursor.next().toJson().split(": \"")[1];
            String s2 = s.split("\"")[0];
            res[count] = s2;
            count++;
        }
        return res;
    }


    public String[] viewMyBooks(String userName) {
        Bson projectionFields = Projections.fields(Projections.include("title"), Projections.excludeId());
        MongoCursor<Document> cursor = colBooks.find(eq("owner", userName)).projection(projectionFields).iterator();
        Bson query = eq("owner", userName);
        long matchedCount = colBooks.countDocuments(query);
        String[] res = new String[(int) matchedCount];

        return loopDocuments(cursor, res);
    }

    public String[] viewBooks() {
        Bson projectionFields = Projections.fields(Projections.include("title"), Projections.excludeId());
        MongoCursor<Document> cursor = colBooks.find().projection(projectionFields).iterator();
        long matchedCount = colBooks.countDocuments();
        String[] res = new String[(int) matchedCount];

        return loopDocuments(cursor, res);
    }

    public String[] bookDetails(Document doc) {
        //title
        String s = doc.toJson().split(": \"")[1];
        String s2 = s.split("\",")[0];
        //author
        String s3 = doc.toJson().split(": \"")[2];
        String s4 = s3.split("\",")[0];
        //genre
        String s5 = doc.toJson().split(": \"")[3];
        String s6 = s5.split("\",")[0];
        //price
        String s7 = s5.split(": ")[1];
        String s8 = s7.split(",")[0];
        //quantity
        String s9 = s5.split(": ")[2];
        String s10 = s9.split(",")[0];
        //owner
        String s11 = doc.toJson().split(": \"")[4];
        String s12 = s11.split("\",")[0];

        return new String[]{s2, s4, s6, s8, s10, s12};
    }

    public String[] viewBookDetails(String title) {
        Bson projectionFields = Projections.fields(Projections.excludeId());
        Document doc = colBooks.find(eq("title", title)).projection(projectionFields).first();

        assert doc != null;
        return bookDetails(doc);
    }

    private String[][] loopDocuments2D(MongoCursor<Document> cursor, int matchedCount) {
        String[][] res = new String[matchedCount][6];
        int count = 0;

        while (cursor.hasNext()) {
            String[] temp;
            temp = bookDetails(cursor.next());
            for (int i = 0; i <= 5; i++) {
                if (temp[i] != null) {
                    res[count][i] = temp[i];
                }
            }
            count++;
        }
        return res;
    }

    public String[][] bookByTitle(String title) {
        Bson projectionFields = Projections.fields(Projections.excludeId());
        Document query = new Document("title",title);
        String pattern = ".*" + query.getString("title") + ".*";
        MongoCursor<Document> cursor = colBooks.find(regex("title", pattern,"i")).projection(projectionFields).iterator();
        long matchedCount = colBooks.countDocuments(regex("title", pattern,"i"));
        return loopDocuments2D(cursor, (int) matchedCount);
    }

    public String[][] bookByAuthor(String author) {
        Bson projectionFields = Projections.fields(Projections.excludeId());
        Document query = new Document("author",author);
        String pattern = ".*" + query.getString("author") + ".*";
        MongoCursor<Document> cursor = colBooks.find(regex("author", pattern,"i")).projection(projectionFields).iterator();
        long matchedCount = colBooks.countDocuments(regex("author", pattern,"i"));
        return loopDocuments2D(cursor, (int) matchedCount);
    }

    public String[][] bookByGenre(String genre){
        Bson projectionFields = Projections.fields(Projections.excludeId());
        Document query = new Document("genre",genre);
        String pattern = ".*" + query.getString("genre") + ".*";
        MongoCursor<Document> cursor = colBooks.find(regex("genre", pattern,"i")).projection(projectionFields).iterator();
        long matchedCount = colBooks.countDocuments(regex("genre", pattern,"i"));
        return loopDocuments2D(cursor, (int) matchedCount);
    }

    public String[] submitRequest(String title,String borrower){
        Bson projectionFields = Projections.fields(Projections.excludeId());
        Document doc = colBooks.find(eq("title", title)).projection(projectionFields).first();
        //TODO:check if book exists
        //lender
        String s1 = doc.toJson().split(": \"")[4];
        String lender = s1.split("\"}")[0];
        //quantity
        String s2 = doc.toJson().split(": ")[5];
        String quantity = s2.split(",")[0];

        if(Objects.equals(quantity, "0")){
            return msg("false","404","not enough books available");
        }

        DBObject condition1 = new BasicDBObject("username", borrower);
        BasicDBList search = new BasicDBList();
        search.add(condition1);

        DBObject query = new BasicDBObject("$and", search);

        Document checkUsers = colUsers.find((Bson) query).first();

        if(checkUsers == null){
            return msg("false", "404", "lender or borrower not found");
        }

        Document sampleDoc = new Document().append("bookTitle", title).append("lender", lender).append("borrower",borrower).
                append("status","pending");
        colRequests.insertOne(sampleDoc);

        return msg("true","200","book requested successfully");
    }

//        Document sampleDoc = new Document("_id","4").append("name","john smith").append("books", Arrays.asList("book1","book2"));
//        Document[] docs = col.find();
//        col.insertOne(sampleDoc);

//        Bson projectionFields = Projections.fields(Projections.include("name"),
//                Projections.excludeId());

//        Document query = new Document().append("_id","1");

//        Bson updates = Updates.combine(
//                Updates.set("name","andrew samir")
//        );

//        UpdateResult result = col.updateOne(query,updates);

//        if(result.getMatchedCount()==1){
//            System.out.println(result);
//        }

//        Bson query = eq("name","andrew samir");
//
//        DeleteResult result = col.deleteOne(query);

//        MongoCursor<Document> cursor = col.find().iterator();
//        while (cursor.hasNext()) {
//            System.out.println(cursor.next().toJson());
//        }
}
