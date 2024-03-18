package org.example;
import com.mongodb.ConnectionString;
import com.mongodb.client.*;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

public class mongoConnection {
    static ConnectionString connectionString;
    static MongoClient mongoClient;
    static MongoDatabase db;

    public static MongoDatabase getDb() {
        return db;
    }

    public mongoConnection() {
        ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger("org.mongodb.driver").setLevel(Level.OFF);
        connectionString = new ConnectionString("mongodb+srv://reaper9027:ZYfH3T09JcNGb8MR@cluster0.vreqasy.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0");
        mongoClient = MongoClients.create(connectionString);
        db = mongoClient.getDatabase("bookStore");
    }
}