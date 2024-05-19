package org.example;

import com.mongodb.ConnectionString;
import com.mongodb.client.*;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.LoggerFactory;

public class MongoConnection {
    static ConnectionString connectionString;
    static MongoClient mongoClient;
    static MongoDatabase db;

    public MongoDatabase getDb() {
        return db;
    }

    public MongoConnection() {
        ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger("org.mongodb.driver").setLevel(Level.OFF);
        connectionString = new ConnectionString("/*put your database link here*/");
        mongoClient = MongoClients.create(connectionString);
        db = mongoClient.getDatabase("bookStore");
    }
}
