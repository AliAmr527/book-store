package org.example;

import org.bson.Document;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        mongoConnection db = new mongoConnection();
//        System.out.println(Arrays.toString(db.addBook("to be 2", "ali","horror",123,12,"ahmedd")));
        System.out.println(Arrays.toString(db.viewMyBooks("ahmedd")));
    }
}
