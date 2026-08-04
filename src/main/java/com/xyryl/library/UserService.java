package com.xyryl.library;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import java.util.ArrayList;

public class UserService {
    private final MongoCollection<Document> userCollection;

    public UserService() {
        MongoDatabase db = MongoConfig.getDatabase();
        this.userCollection = db.getCollection("users");
    }

    
    public void addUser(String name, String email, String role) {
        
        Document user = new Document("name", name)
                .append("email", email)
                .append("role", role)
                .append("borrowedBooks", new ArrayList<Document>());

        userCollection.insertOne(user);
        System.out.println("User added successfully as " + role + "!");
    }

    
    public Document findUserByEmail(String email) {
        return userCollection.find(Filters.eq("email", email)).first();
    }

   
    public void listAllUsers() {
        for (Document user : userCollection.find()) {
            System.out.println(user.toJson());
        }
    }

    
    public void listUsersByRole(String role) {
        for (Document user : userCollection.find(Filters.eq("role", role))) {
            System.out.println(user.toJson());
        }
    }

    
    public void deleteUser(String email) {
        userCollection.deleteOne(Filters.eq("email", email));
        System.out.println("User deleted!");
    }
}