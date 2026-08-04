package com.xyryl.library;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import java.util.Date;

public class LibraryService {
    private final MongoCollection<Document> userCollection;
    private final MongoCollection<Document> bookCollection;

    public LibraryService() {
        MongoDatabase db = MongoConfig.getDatabase();
        this.userCollection = db.getCollection("users");
        this.bookCollection = db.getCollection("books");
    }

    // Borrow a book
    public void borrowBook(String userEmail, String isbn) {
        // 1. Check if book is available
        Document book = bookCollection.find(Filters.eq("isbn", isbn)).first();
        if (book == null) {
            System.out.println("Error: Book not found!");
            return;
        }
        if (book.getInteger("availableCopies") <= 0) {
            System.out.println("Error: No copies available!");
            return;
        }

        // 2. Check if user exists
        Document user = userCollection.find(Filters.eq("email", userEmail)).first();
        if (user == null) {
            System.out.println("Error: User not found!");
            return;
        }

        // 3. Create transaction log entry
        Document transactionLog = new Document("bookId", book.getObjectId("_id"))
                .append("title", book.getString("title"))
                .append("borrowDate", new Date())
                .append("returned", false);

        // 4. Push to user's borrowedBooks list
        userCollection.updateOne(
                Filters.eq("email", userEmail),
                Updates.push("borrowedBooks", transactionLog)
        );

        // 5. Decrement available copies
        bookCollection.updateOne(
                Filters.eq("isbn", isbn),
                Updates.inc("availableCopies", -1)
        );

        System.out.println(user.getString("name") + " borrowed \"" + book.getString("title") + "\" successfully!");
    }

    // Return a book
    public void returnBook(String userEmail, String isbn) {
        Document book = bookCollection.find(Filters.eq("isbn", isbn)).first();
        if (book == null) {
            System.out.println("Error: Book not found!");
            return;
        }

        // Increment available copies back
        bookCollection.updateOne(
                Filters.eq("isbn", isbn),
                Updates.inc("availableCopies", 1)
        );

        System.out.println("Book \"" + book.getString("title") + "\" returned successfully!");
    }
}