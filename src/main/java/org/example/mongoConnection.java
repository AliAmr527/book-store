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
import org.bson.BsonDocument;
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
        connectionString = new ConnectionString("mongodb+srv://reaper9027:ZYfH3T09JcNGb8MR@cluster0.vreqasy.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0");
        mongoClient = MongoClients.create(connectionString);
        db = mongoClient.getDatabase("bookStore");
        colUsers = db.getCollection("users");
        colBooks = db.getCollection("books");
        colRequests = db.getCollection("requests");
    }

    public String[] msg(String flag,String code,String msg){
        return new String[]{flag,code,msg};
    }
    
    public String[] getUserNameAndName(Document user){
        //this for name
        String s = user.toJson().split(",")[1];
        String s2 = s.split(": \"")[1];
        String s3 = s2.split("\"")[0];

        //this for username
        String s4 = user.toJson().split(",")[2];
        String s5 = s4.split(": \"")[1];
        String s6 = s5.split("\"")[0];

        return new String[]{"true",s3,s6};
    }

    public String[] login(String username, String password) {
        Document doc = colUsers.find(eq("username", username)).first();
        if (doc == null){
            return msg("false","404","username not found");
        }
        
        DBObject condition1 = new BasicDBObject("username", username).append("password", password);
        BasicDBList search = new BasicDBList();
        search.add(condition1);
        Bson projectionFields = Projections.fields(Projections.include("username","name"));
        DBObject query = new BasicDBObject("$and", search);
        Document user = colUsers.find((Bson) query).projection(projectionFields).first();

        if(user == null){
            return msg("false","401","wrong password");
        }

        return getUserNameAndName(user);
    }

    public String[] register(String name, String username, String password) {
        Document doc = colUsers.find(eq("username", username)).first();
        if (doc != null){
            return msg("false","409","duplicate username");
        }
        Document sampleDoc = new Document().append("name",name).append("username",username).append("password",password);
        colUsers.insertOne(sampleDoc);
        Bson projectionFields = Projections.fields(Projections.include("username","name"));
        Document user = colUsers.find(eq("username", username)).projection(projectionFields).first();
        assert user != null;

        return getUserNameAndName(user);
    }
    //title author genre price quantity list of clients
    public String[] addBook(String title, String author, String genre, int price,int quantity,String owner){
        Document doc = colBooks.find(eq("title", title)).first();
        
        if (doc != null){
            return msg("false","409","duplicate book title");
        }
        Document sampleDoc = new Document().append("title",title).append("author",author).append("genre",genre)
                .append("price",price).append("quantity",quantity).append("owner",owner);
        colBooks.insertOne(sampleDoc);
        
        return msg("true","200","book added successfully!");
    }

    public String[] removeBook(String title){
        Bson query = eq("title",title);
        DeleteResult result = colBooks.deleteOne(query);
        System.out.println(result);
        if(result.getDeletedCount() == 0){
            return msg("false","404","couldn't find book");
        }
        return msg("true","200","book deleted successfully");
    }

    public String[] viewMyBooks(String userName){
        MongoCursor<Document> cursor = colBooks.find(eq("owner",userName)).iterator();
        Bson query = eq("owner",userName);
        long matchedCount = colBooks.countDocuments(query);
        int count = 0;
        String [] res = new String[(int) matchedCount];
        while (cursor.hasNext()) {
            res[count] = cursor.next().toJson();
            count++;
        }
        if(res.length==0){
            return msg("false","404","no books found");
        }
        return res;
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