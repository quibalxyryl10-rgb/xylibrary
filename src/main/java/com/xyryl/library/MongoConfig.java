package com.xyryl.library;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoConfig {
    private static final String CONNECTION_STRING = "mongodb+srv://quibalxyryl9_db_user:bjuF3G7LwkCkIcMg@cluster0.ogiq4lq.mongodb.net/?appName=Cluster0";
    private static final String DATABASE_NAME = "LibraryDB";
    private static MongoClient mongoClient = null;

    public static MongoDatabase getDatabase() {
        if (mongoClient == null) {
            mongoClient = MongoClients.create(CONNECTION_STRING);
        }
        return mongoClient.getDatabase(DATABASE_NAME);
    }

    public static void closeConnection() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}