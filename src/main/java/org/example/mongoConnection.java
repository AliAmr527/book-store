package org.example;

import static com.mongodb.client.model.Filters.eq;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.util.concurrent.TimeUnit.SECONDS;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.ConnectionString;
import com.mongodb.DBObject;
import com.mongodb.client.*;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class mongoConnection {
    static ConnectionString connectionString;
    static MongoClient mongoClient;
    static MongoDatabase db;
    MongoCollection<Document> colUsers;
    MongoCollection<Document> colBooks;
    MongoCollection<Document> colRequests;

    public mongoConnection() {
        ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger("org.mongodb.driver").setLevel(Level.OFF);
        connectionString = new ConnectionString("mongodb://localhost:27017/bookStore");
        mongoClient = MongoClients.create(connectionString);
        db = mongoClient.getDatabase("bookStore");
        colUsers = db.getCollection("users");
        colBooks = db.getCollection("books");
        colRequests = db.getCollection("requests");
    }

    public String login(String email, String password) {

        DBObject condition1 = new BasicDBObject("email", email).append("password", password);
        BasicDBList search = new BasicDBList();
        search.add(condition1);
        Bson projectionFields = Projections.fields(Projections.include("_id"));
        DBObject query = new BasicDBObject("$and", search);
        Document doc = colUsers.find((Bson) query).projection(projectionFields).first();

        if (doc != null) {
            return doc.toJson();
        } else {
            return null;
        }
    }

    public boolean register(String name, String email, String password) {
        Document doc = colUsers.find(eq("email", email)).first();
        if (doc != null){
            return false;
        }
        Document sampleDoc = new Document().append("name",name).append("email",email).append("password",password);
        colUsers.insertOne(sampleDoc);
        return true;
    }
    //title author genre price quantity list of clients
    public boolean addBook(String title, String author, String genre, int price,int quantity){
        Document doc = colUsers.find(eq("title", title)).first();
        if (doc != null){
            return false;
        }
        Document sampleDoc = new Document().append("title",title).append("author",author).append("genre",genre)
                .append("price",price).append("quantity",quantity);
        colBooks.insertOne(sampleDoc);
        return true;
    }

    public boolean removeBook(String title){
        Bson query = eq("title",title);
        DeleteResult result = colBooks.deleteOne(query);
        return true;
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