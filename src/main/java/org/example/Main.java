package org.example;

public class Main {
    public static void main(String[] args) {
        mongoConnection db = new mongoConnection();
        System.out.println(db.login("ahmed","123"));
    }
}
