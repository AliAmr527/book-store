package org.example;

import org.bson.Document;

public class Main {
    public static void main(String[] args) {
        mongoConnection db = new mongoConnection();
        System.out.println(db.register("ahmed","1233333221","123"));
    }
}
