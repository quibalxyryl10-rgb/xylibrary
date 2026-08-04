package com.xyryl.library;

import org.bson.Document;
import com.google.api.client.auth.oauth2.Credential;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.bson.Document;

public class MainApp extends Application {

    private BookService bookService = new BookService();
    private UserService userService = new UserService();
    private LibraryService libraryService = new LibraryService();

    private final String buttonStyle = "-fx-background-color: #4a90d9; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px; -fx-background-radius: 8px;";
    private final String smallButtonStyle = "-fx-background-color: #4a90d9; -fx-text-fill: white; -fx-padding: 8px; -fx-background-radius: 6px;";
    private final String bgStyle = "-fx-background-color: #1e1e2e;";
    private final String fieldStyle = "-fx-background-color: #2a2a3a; -fx-text-fill: white; -fx-prompt-text-fill: #999999; -fx-background-radius: 5px; -fx-padding: 6px;";

    private String loggedInName = "";
    private String loggedInEmail = "";

    @Override
    public void start(Stage primaryStage) {
        showLoginScreen(primaryStage);
    }

    private void showLoginScreen(Stage primaryStage) {
        Label title = new Label("Xylibrary");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Library Management System");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #aaaaaa;");

        Button googleLoginBtn = new Button("Sign in with Google");
        googleLoginBtn.setStyle(buttonStyle);

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: lightgreen;");

        googleLoginBtn.setOnAction(e -> {
            statusLabel.setStyle("-fx-text-fill: #cccccc;");
            statusLabel.setText("Opening browser for sign-in...");
            new Thread(() -> {
                try {
                    Credential credential = GoogleAuthService.getCredential();
                    String[] userInfo = GoogleAuthService.getUserInfo(credential);
                    loggedInName = userInfo[0];
                    loggedInEmail = userInfo[1];

                    Document existingUser = userService.findUserByEmail(loggedInEmail);
                    if (existingUser == null) {
                        userService.addUser(loggedInName, loggedInEmail, "Student");
                    }

                    javafx.application.Platform.runLater(() -> {
                        showDashboard(primaryStage);
                    });
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> {
                        statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
                        statusLabel.setText("Login failed: " + ex.getMessage());
                    });
                }
            }).start();
        });

        VBox layout = new VBox(15, title, subtitle, googleLoginBtn, statusLabel);
        layout.setPadding(new Insets(40));
        layout.setStyle(bgStyle + " -fx-alignment: center;");

        Scene scene = new Scene(layout, 400, 300);
        primaryStage.setTitle("Xylibrary - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showDashboard(Stage primaryStage) {
        Label title = new Label("Xylibrary - Welcome, " + loggedInName + "!");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label emailLabel = new Label(loggedInEmail);
        emailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");

        Button addBookBtn = new Button("Add Book");
        Button viewBooksBtn = new Button("View Books");
        Button addUserBtn = new Button("Add User");
        Button borrowBtn = new Button("Borrow Book");
        Button returnBtn = new Button("Return Book");

        for (Button b : new Button[]{addBookBtn, viewBooksBtn, addUserBtn, borrowBtn, returnBtn}) {
            b.setStyle(buttonStyle);
        }

        addBookBtn.setOnAction(e -> openAddBookForm());
        viewBooksBtn.setOnAction(e -> openViewBooksWindow());
        addUserBtn.setOnAction(e -> openAddUserForm());
        borrowBtn.setOnAction(e -> openBorrowForm());
        returnBtn.setOnAction(e -> openReturnForm());

        VBox layout = new VBox(12, title, emailLabel, addBookBtn, viewBooksBtn, addUserBtn, borrowBtn, returnBtn);
        layout.setPadding(new Insets(30));
        layout.setStyle(bgStyle + " -fx-alignment: center;");

        Scene scene = new Scene(layout, 400, 450);
        primaryStage.setTitle("Xylibrary");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Label statusLabel() {
        Label l = new Label();
        l.setStyle("-fx-text-fill: lightgreen;");
        return l;
    }

    private void styleField(TextField f) {
        f.setStyle(fieldStyle);
    }

    private void openAddBookForm() {
        Stage formStage = new Stage();
        formStage.setTitle("Add Book");

        Label heading = new Label("Add a New Book");
        heading.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        TextField titleField = new TextField();
        titleField.setPromptText("Title");
        TextField authorField = new TextField();
        authorField.setPromptText("Author");
        TextField isbnField = new TextField();
        isbnField.setPromptText("ISBN");
        TextField categoryField = new TextField();
        categoryField.setPromptText("Category");
        TextField copiesField = new TextField();
        copiesField.setPromptText("Total Copies");
        for (TextField f : new TextField[]{titleField, authorField, isbnField, categoryField, copiesField}) {
            styleField(f);
        }

        Button saveBtn = new Button("Save Book");
        saveBtn.setStyle(smallButtonStyle);
        Label statusLabel = statusLabel();

        saveBtn.setOnAction(e -> {
            try {
                bookService.addBook(
                        titleField.getText(),
                        authorField.getText(),
                        isbnField.getText(),
                        categoryField.getText(),
                        Integer.parseInt(copiesField.getText())
                );
                statusLabel.setStyle("-fx-text-fill: lightgreen;");
                statusLabel.setText("Book added successfully!");
                titleField.clear(); authorField.clear(); isbnField.clear();
                categoryField.clear(); copiesField.clear();
            } catch (Exception ex) {
                statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });

        VBox formLayout = new VBox(10, heading, titleField, authorField, isbnField, categoryField, copiesField, saveBtn, statusLabel);
        formLayout.setPadding(new Insets(20));
        formLayout.setStyle(bgStyle);

        formStage.setScene(new Scene(formLayout, 300, 380));
        formStage.show();
    }

    private void openViewBooksWindow() {
        Stage viewStage = new Stage();
        viewStage.setTitle("All Books");

        Label heading = new Label("All Books");
        heading.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        TextArea display = new TextArea();
        display.setEditable(false);
        display.setStyle("-fx-control-inner-background: #2a2a3a; -fx-text-fill: white; -fx-background-color: #2a2a3a;");

        StringBuilder sb = new StringBuilder();
        for (Document book : bookService.getAllBooksRaw()) {
            sb.append("Title: ").append(book.getString("title")).append("\n");
            sb.append("Author: ").append(book.getString("author")).append("\n");
            sb.append("ISBN: ").append(book.getString("isbn")).append("\n");
            sb.append("Category: ").append(book.getString("category")).append("\n");
            sb.append("Available: ").append(book.getInteger("availableCopies"))
              .append("/").append(book.getInteger("totalCopies")).append("\n");
            sb.append("------------------------\n");
        }
        display.setText(sb.length() == 0 ? "No books yet." : sb.toString());

        VBox layout = new VBox(10, heading, display);
        layout.setPadding(new Insets(15));
        layout.setStyle(bgStyle);

        viewStage.setScene(new Scene(layout, 400, 420));
        viewStage.show();
    }

    private void openAddUserForm() {
        Stage formStage = new Stage();
        formStage.setTitle("Add User");

        Label heading = new Label("Add a New User");
        heading.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        styleField(nameField);
        styleField(emailField);

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Student", "Visitor", "Employee");
        roleBox.setPromptText("Select Role");
        roleBox.setStyle(fieldStyle);
        roleBox.setMaxWidth(Double.MAX_VALUE);

        Button saveBtn = new Button("Save User");
        saveBtn.setStyle(smallButtonStyle);
        Label statusLabel = statusLabel();

        saveBtn.setOnAction(e -> {
            try {
                String role = roleBox.getValue();
                if (role == null) throw new Exception("Please select a role");
                userService.addUser(nameField.getText(), emailField.getText(), role);
                statusLabel.setStyle("-fx-text-fill: lightgreen;");
                statusLabel.setText("User added as " + role + "!");
                nameField.clear(); emailField.clear(); roleBox.setValue(null);
            } catch (Exception ex) {
                statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });

        VBox formLayout = new VBox(10, heading, nameField, emailField, roleBox, saveBtn, statusLabel);
        formLayout.setPadding(new Insets(20));
        formLayout.setStyle(bgStyle);

        formStage.setScene(new Scene(formLayout, 300, 320));
        formStage.show();
    }

    private void openBorrowForm() {
        Stage formStage = new Stage();
        formStage.setTitle("Borrow Book");

        Label heading = new Label("Borrow a Book");
        heading.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        TextField emailField = new TextField();
        emailField.setPromptText("User Email");
        TextField isbnField = new TextField();
        isbnField.setPromptText("Book ISBN");
        styleField(emailField);
        styleField(isbnField);

        Button borrowBtn = new Button("Borrow");
        borrowBtn.setStyle(smallButtonStyle);
        Label statusLabel = statusLabel();

        borrowBtn.setOnAction(e -> {
            try {
                libraryService.borrowBook(emailField.getText(), isbnField.getText());
                statusLabel.setStyle("-fx-text-fill: lightgreen;");
                statusLabel.setText("Borrow request processed. Check console for details.");
            } catch (Exception ex) {
                statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });

        VBox formLayout = new VBox(10, heading, emailField, isbnField, borrowBtn, statusLabel);
        formLayout.setPadding(new Insets(20));
        formLayout.setStyle(bgStyle);

        formStage.setScene(new Scene(formLayout, 300, 270));
        formStage.show();
    }

    private void openReturnForm() {
        Stage formStage = new Stage();
        formStage.setTitle("Return Book");

        Label heading = new Label("Return a Book");
        heading.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        TextField emailField = new TextField();
        emailField.setPromptText("User Email");
        TextField isbnField = new TextField();
        isbnField.setPromptText("Book ISBN");
        styleField(emailField);
        styleField(isbnField);

        Button returnBtn = new Button("Return");
        returnBtn.setStyle(smallButtonStyle);
        Label statusLabel = statusLabel();

        returnBtn.setOnAction(e -> {
            try {
                libraryService.returnBook(emailField.getText(), isbnField.getText());
                statusLabel.setStyle("-fx-text-fill: lightgreen;");
                statusLabel.setText("Return request processed. Check console for details.");
            } catch (Exception ex) {
                statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });

        VBox formLayout = new VBox(10, heading, emailField, isbnField, returnBtn, statusLabel);
        formLayout.setPadding(new Insets(20));
        formLayout.setStyle(bgStyle);

        formStage.setScene(new Scene(formLayout, 300, 270));
        formStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}