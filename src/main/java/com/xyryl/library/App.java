package com.xyryl.library;

public class App {
    public static void main(String[] args) {
        LibraryService libraryService = new LibraryService();

        // Test borrowing
        libraryService.borrowBook("juan@student.com", "978-0134685991");

        // Test returning
        libraryService.returnBook("juan@student.com", "978-0134685991");

        MongoConfig.closeConnection();
    }
}