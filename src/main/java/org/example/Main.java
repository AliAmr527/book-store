package org.example;

import org.bson.Document;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        DbMethods db2 = new DbMethods();
//        System.out.println(Arrays.toString(db2.login("ahmedd", "123")));
//        System.out.println(Arrays.toString(db.addBook("to be 2", "ali","horror",123,12,"ahmedd")));
//        System.out.println(Arrays.toString(db.viewMyBooks("ahmedd")));
//        System.out.println(Arrays.toString(db.viewBookDetails("to be")));
//        System.out.println(Arrays.deepToString(db.bookByAuthor("ali")));
        System.out.println(Arrays.deepToString(db2.bookByTitle("to be")));
        String [] userList = {"ali","hassan"};
//        System.out.println(db2.insertTest("to be 3", "ali","horror",123,12,"ahmedd",userList));
//        System.out.println(Arrays.toString(db2.submitRequest("to be", "ahmeddd")));
    }
}
