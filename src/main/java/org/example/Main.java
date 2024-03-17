package org.example;

import org.bson.Document;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        mongoConnection db = new mongoConnection();
        System.out.println(Arrays.toString(db.login("1233333221", "12")));
    }
}
