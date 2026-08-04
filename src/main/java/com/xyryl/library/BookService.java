package com.xyryl.library;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

public class BookService {
    private final MongoCollection<Document> bookCollection;

    public BookService() {
        MongoDatabase db = MongoConfig.getDatabase();
        this.bookCollection = db.getCollection("books");
    }

    // Create: Add a new book
    public void addBook(String title, String author, String isbn, String category, int totalCopies) {
        Document book = new Document("title", title)
                .append("author", author)
                .append("isbn", isbn)
                .append("category", category)
                .append("totalCopies", totalCopies)
                .append("availableCopies", totalCopies);

        bookCollection.insertOne(book);
        System.out.println("Book added successfully!");
    }

    // Read: Find a book by ISBN
    public Document findBookByIsbn(String isbn) {
        return bookCollection.find(Filters.eq("isbn", isbn)).first();
    }

    // Read: Get all books (prints to console)
    public void listAllBooks() {
        for (Document book : bookCollection.find()) {
            System.out.println(book.toJson());
        }
    }

    // Read: Get all books as raw documents (used by the UI)
    public Iterable<Document> getAllBooksRaw() {
        return bookCollection.find();
    }

    // Update: Modify available stock copies (add/subtract from current value)
    public void updateAvailableCopies(String isbn, int countChange) {
        bookCollection.updateOne(
                Filters.eq("isbn", isbn),
                Updates.inc("availableCopies", countChange)
        );
        System.out.println("Stock updated!");
    }

    // Update: Set an exact new value for available copies (used by the UI's Update button)
    public void setAvailableCopies(String isbn, int newAvailable) {
        bookCollection.updateOne(
                Filters.eq("isbn", isbn),
                Updates.set("availableCopies", newAvailable)
        );
        System.out.println("Copies updated!");
    }

    // Delete: Remove a book
    public void deleteBook(String isbn) {
        bookCollection.deleteOne(Filters.eq("isbn", isbn));
        System.out.println("Book deleted!");
    }
}