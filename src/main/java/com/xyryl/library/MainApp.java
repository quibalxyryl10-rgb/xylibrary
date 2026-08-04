package com.xyryl.library;

import com.google.api.client.auth.oauth2.Credential;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.bson.Document;
import java.io.File;

public class MainApp extends Application {

    private BookService bookService = new BookService();
    private UserService userService = new UserService();
    private LibraryService libraryService = new LibraryService();

    private final String bgDark = "-fx-background-color: #1e1e2e;";
    private final String sidebarStyle = "-fx-background-color: #171722;";
    private final String topbarStyle = "-fx-background-color: #4a90d9;";
    private final String buttonStyle = "-fx-background-color: #4a90d9; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10px; -fx-background-radius: 8px;";
    private final String sidebarBtnStyle = "-fx-background-color: transparent; -fx-text-fill: #cccccc; -fx-font-size: 13px; -fx-padding: 10px 15px; -fx-alignment: CENTER_LEFT; -fx-background-radius: 0;";
    private final String sidebarBtnActive = "-fx-background-color: #2a2a3a; -fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 10px 15px; -fx-alignment: CENTER_LEFT; -fx-background-radius: 0;";
    private final String fieldStyle = "-fx-background-color: #2a2a3a; -fx-text-fill: white; -fx-prompt-text-fill: #999999; -fx-background-radius: 5px; -fx-padding: 8px;";
    private final String smallButtonStyle = "-fx-background-color: #4a90d9; -fx-text-fill: white; -fx-padding: 10px 18px; -fx-background-radius: 6px;";
    private final String deleteButtonStyle = "-fx-background-color: #d94a4a; -fx-text-fill: white; -fx-padding: 10px 18px; -fx-background-radius: 6px;";

    private String loggedInName = "";
    private String loggedInEmail = "";
    private BorderPane rootLayout;
    private StackPane contentArea;

    @Override
    public void start(Stage primaryStage) {
        showLoginScreen(primaryStage);
    }

    private void showLoginScreen(Stage primaryStage) {
        Label title = new Label("Xylibrary");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

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

                    javafx.application.Platform.runLater(() -> showDashboard(primaryStage));
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
        layout.setStyle(bgDark + " -fx-alignment: center;");
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout, 420, 320);
        primaryStage.setTitle("Xylibrary - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void showDashboard(Stage primaryStage) {
        rootLayout = new BorderPane();

        Label topTitle = new Label("Xylibrary - Library Management System");
        topTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label topUser = new Label(loggedInName + " (" + loggedInEmail + ")");
        topUser.setStyle("-fx-font-size: 12px; -fx-text-fill: #eeeeee;");
        VBox topTextBox = new VBox(2, topTitle, topUser);

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color: #2a2a3a; -fx-text-fill: white; -fx-padding: 8px 14px; -fx-background-radius: 6px;");
        logoutBtn.setOnAction(e -> {
            try {
                File tokenFile = new File("tokens/StoredCredential");
                if (tokenFile.exists()) tokenFile.delete();
            } catch (Exception ex) {
                System.out.println("Logout cleanup error: " + ex.getMessage());
            }
            loggedInName = "";
            loggedInEmail = "";
            showLoginScreen(primaryStage);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(topTextBox, spacer, logoutBtn);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(15, 20, 15, 20));
        topBar.setStyle(topbarStyle);
        rootLayout.setTop(topBar);

        VBox sidebar = new VBox();
        sidebar.setStyle(sidebarStyle);
        sidebar.setPrefWidth(190);
        sidebar.setPadding(new Insets(15, 0, 0, 0));

        Button navAddBook = new Button("Add Book");
        Button navViewBooks = new Button("View / Manage Books");
        Button navAddUser = new Button("Add User");
        Button navBorrow = new Button("Borrow Book");
        Button navReturn = new Button("Return Book");

        Button[] navButtons = {navAddBook, navViewBooks, navAddUser, navBorrow, navReturn};
        for (Button b : navButtons) {
            b.setStyle(sidebarBtnStyle);
            b.setMaxWidth(Double.MAX_VALUE);
        }

        navAddBook.setOnAction(e -> { highlightNav(navButtons, navAddBook); showAddBookView(); });
        navViewBooks.setOnAction(e -> { highlightNav(navButtons, navViewBooks); showViewBooksView(); });
        navAddUser.setOnAction(e -> { highlightNav(navButtons, navAddUser); showAddUserView(); });
        navBorrow.setOnAction(e -> { highlightNav(navButtons, navBorrow); showBorrowView(); });
        navReturn.setOnAction(e -> { highlightNav(navButtons, navReturn); showReturnView(); });

        sidebar.getChildren().addAll(navButtons);
        rootLayout.setLeft(sidebar);

        contentArea = new StackPane();
        contentArea.setStyle(bgDark);
        contentArea.setAlignment(Pos.TOP_LEFT);
        rootLayout.setCenter(contentArea);

        highlightNav(navButtons, navAddBook);
        showAddBookView();

        Scene scene = new Scene(rootLayout, 800, 550);
        primaryStage.setTitle("Xylibrary");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void highlightNav(Button[] all, Button active) {
        for (Button b : all) {
            b.setStyle(b == active ? sidebarBtnActive : sidebarBtnStyle);
        }
    }

    private Label sectionHeading(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        return l;
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #aaaaaa;");
        return l;
    }

    private Label statusLabel() {
        Label l = new Label();
        l.setStyle("-fx-text-fill: lightgreen;");
        return l;
    }

    private void styleField(TextField f) {
        f.setStyle(fieldStyle);
        f.setPrefWidth(320);
    }

    private void showAddBookView() {
        Label heading = sectionHeading("Add Resource");

        TextField isbnField = new TextField();
        TextField titleField = new TextField();
        TextField authorField = new TextField();
        TextField categoryField = new TextField();
        TextField copiesField = new TextField();
        for (TextField f : new TextField[]{isbnField, titleField, authorField, categoryField, copiesField}) styleField(f);

        Button saveBtn = new Button("Save Resource");
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
                isbnField.clear(); titleField.clear(); authorField.clear();
                categoryField.clear(); copiesField.clear();
            } catch (Exception ex) {
                statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });

        VBox form = new VBox(6,
                fieldLabel("ISBN / Barcode"), isbnField,
                fieldLabel("Title"), titleField,
                fieldLabel("Author"), authorField,
                fieldLabel("Category"), categoryField,
                fieldLabel("Quantity"), copiesField,
                saveBtn, statusLabel
        );
        form.setSpacing(8);

        VBox layout = new VBox(20, heading, form);
        layout.setPadding(new Insets(30));
        layout.setStyle(bgDark);

        contentArea.getChildren().setAll(layout);
    }

    private void showViewBooksView() {
        Label heading = sectionHeading("All Resources");

        TextArea display = new TextArea();
        display.setEditable(false);
        display.setPrefHeight(260);
        display.setStyle("-fx-control-inner-background: #2a2a3a; -fx-text-fill: white; -fx-background-color: #2a2a3a;");

        Runnable refresh = () -> {
            StringBuilder sb = new StringBuilder();
            for (Document book : bookService.getAllBooksRaw()) {
                sb.append("Title: ").append(book.getString("title")).append("   |   ");
                sb.append("Author: ").append(book.getString("author")).append("   |   ");
                sb.append("ISBN: ").append(book.getString("isbn")).append("\n");
                sb.append("Category: ").append(book.getString("category"))
                  .append("   |   Available: ").append(book.getInteger("availableCopies"))
                  .append("/").append(book.getInteger("totalCopies")).append("\n");
                sb.append("------------------------------------------------------\n");
            }
            display.setText(sb.length() == 0 ? "No resources yet." : sb.toString());
        };
        refresh.run();

        Label manageHeading = sectionHeading("Update / Delete Resource");
        manageHeading.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");

        TextField isbnField = new TextField();
        TextField newCopiesField = new TextField();
        styleField(isbnField);
        styleField(newCopiesField);

        Button updateBtn = new Button("Update Copies");
        updateBtn.setStyle(smallButtonStyle);
        Button deleteBtn = new Button("Delete Resource");
        deleteBtn.setStyle(deleteButtonStyle);

        Label statusLabel = statusLabel();

        updateBtn.setOnAction(e -> {
            try {
                bookService.setAvailableCopies(isbnField.getText(), Integer.parseInt(newCopiesField.getText()));
                statusLabel.setStyle("-fx-text-fill: lightgreen;");
                statusLabel.setText("Copies updated!");
                refresh.run();
            } catch (Exception ex) {
                statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });

        deleteBtn.setOnAction(e -> {
            try {
                bookService.deleteBook(isbnField.getText());
                statusLabel.setStyle("-fx-text-fill: lightgreen;");
                statusLabel.setText("Resource deleted!");
                refresh.run();
            } catch (Exception ex) {
                statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });

        HBox actionButtons = new HBox(10, updateBtn, deleteBtn);

        VBox manageForm = new VBox(6,
                fieldLabel("ISBN"), isbnField,
                fieldLabel("New Available Copies (for update)"), newCopiesField,
                actionButtons, statusLabel
        );
        manageForm.setSpacing(8);

        VBox layout = new VBox(20, heading, display, manageHeading, manageForm);
        layout.setPadding(new Insets(30));
        layout.setStyle(bgDark);

        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setStyle(bgDark);
        scrollPane.setFitToWidth(true);

        contentArea.getChildren().setAll(scrollPane);
    }

    private void showAddUserView() {
        Label heading = sectionHeading("Add User");

        TextField nameField = new TextField();
        TextField emailField = new TextField();
        styleField(nameField);
        styleField(emailField);

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Student", "Visitor", "Employee");
        roleBox.setPromptText("Select Role");
        roleBox.setStyle(fieldStyle);
        roleBox.setPrefWidth(320);

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

        VBox form = new VBox(6,
                fieldLabel("Full Name"), nameField,
                fieldLabel("Email"), emailField,
                fieldLabel("Role"), roleBox,
                saveBtn, statusLabel
        );
        form.setSpacing(8);

        VBox layout = new VBox(20, heading, form);
        layout.setPadding(new Insets(30));
        layout.setStyle(bgDark);

        contentArea.getChildren().setAll(layout);
    }

    private void showBorrowView() {
        Label heading = sectionHeading("Borrow a Book");

        TextField emailField = new TextField();
        TextField isbnField = new TextField();
        styleField(emailField);
        styleField(isbnField);

        Button borrowBtn = new Button("Borrow");
        borrowBtn.setStyle(smallButtonStyle);
        Label statusLabel = statusLabel();

        borrowBtn.setOnAction(e -> {
            try {
                libraryService.borrowBook(emailField.getText(), isbnField.getText());
                statusLabel.setStyle("-fx-text-fill: lightgreen;");
                statusLabel.setText("Borrow request processed.");
            } catch (Exception ex) {
                statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });

        VBox form = new VBox(6,
                fieldLabel("User Email"), emailField,
                fieldLabel("Book ISBN"), isbnField,
                borrowBtn, statusLabel
        );
        form.setSpacing(8);

        VBox layout = new VBox(20, heading, form);
        layout.setPadding(new Insets(30));
        layout.setStyle(bgDark);

        contentArea.getChildren().setAll(layout);
    }

    private void showReturnView() {
        Label heading = sectionHeading("Return a Book");

        TextField emailField = new TextField();
        TextField isbnField = new TextField();
        styleField(emailField);
        styleField(isbnField);

        Button returnBtn = new Button("Return");
        returnBtn.setStyle(smallButtonStyle);
        Label statusLabel = statusLabel();

        returnBtn.setOnAction(e -> {
            try {
                libraryService.returnBook(emailField.getText(), isbnField.getText());
                statusLabel.setStyle("-fx-text-fill: lightgreen;");
                statusLabel.setText("Return request processed.");
            } catch (Exception ex) {
                statusLabel.setStyle("-fx-text-fill: #ff6b6b;");
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });

        VBox form = new VBox(6,
                fieldLabel("User Email"), emailField,
                fieldLabel("Book ISBN"), isbnField,
                returnBtn, statusLabel
        );
        form.setSpacing(8);

        VBox layout = new VBox(20, heading, form);
        layout.setPadding(new Insets(30));
        layout.setStyle(bgDark);

        contentArea.getChildren().setAll(layout);
    }

    public static void main(String[] args) {
        launch(args);
    }
}