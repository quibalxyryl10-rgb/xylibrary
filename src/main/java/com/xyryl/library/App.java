package com.xyryl.library;

public class App {
    public static void main(String[] args) {
        UserService userService = new UserService();

        
        userService.addUser("Juan Dela Cruz", "juan@student.com", "Student");
        userService.addUser("Maria Santos", "maria@visitor.com", "Visitor");
        userService.addUser("Pedro Reyes", "pedro@employee.com", "Employee");

        
        userService.listAllUsers();

        MongoConfig.closeConnection();
    }
}