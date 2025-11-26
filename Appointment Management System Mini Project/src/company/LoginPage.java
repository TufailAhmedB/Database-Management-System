package company;

import main.MainApp;
import company.CompanyAuthDAO;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Callback;

public class LoginPage {

    private static boolean signupMode = true;
    private static Label titleLabel, subtitleLabel;
    private static Button loginButton;
    private static Label confirmLabel;
    private static PasswordField confirmPasswordField;
    private static Label emailLabel;
    private static TextField emailField;
    private static ToggleButton loginToggle, signupToggle;
    private static TextField usernameField;
    private static PasswordField passwordField;

    public static Scene createLoginScene(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: white;");

        // BACK BUTTON
        Button backBtn = new Button("← Back");
            backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #0078ff; -fx-font-size: 14;");
            backBtn.setOnAction(e -> {
            MainApp mainApp = MainApp.getInstance();
            primaryStage.setScene(mainApp.getMainScene()); 
            primaryStage.show();
        });

// Header (Start of the replacement block)
VBox header = new VBox(20);
header.setAlignment(Pos.CENTER);

titleLabel = new Label("Create Account");
titleLabel.setFont(Font.font("System", FontWeight.BOLD, 24));

subtitleLabel = new Label("Sign up for a new account");
subtitleLabel.setFont(Font.font("System", 14));

header.getChildren().addAll(titleLabel, subtitleLabel);

// FIX: Combine the Back Button and the centered Header titles.
// 1. Put the back button in its own HBox for clean alignment.
HBox backButtonContainer = new HBox(backBtn);
backButtonContainer.setAlignment(Pos.CENTER_LEFT);

// 2. Put both the HBox (back button) and the VBox (titles) into a single VBox for the top section.
VBox topSection = new VBox(10, backButtonContainer, header);
topSection.setAlignment(Pos.TOP_LEFT); // You can keep the outer VBox alignment as TOP_LEFT or TOP_CENTER

// Set the combined VBox as the top of the BorderPane
root.setTop(topSection);
// Header (End of the replacement block)

        // Form Section
        VBox formContainer = new VBox(20);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setPrefWidth(350);
        formContainer.setMaxWidth(350);

        VBox form = new VBox(15);
        form.setPadding(new Insets(30));
        form.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 5; -fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 5;");
        form.setMaxWidth(350);
        form.setAlignment(Pos.CENTER_LEFT);

        // Toggle Buttons
        ToggleGroup toggleGroup = new ToggleGroup();
        signupToggle = new ToggleButton("Sign Up");
        signupToggle.setToggleGroup(toggleGroup);

        loginToggle = new ToggleButton("Login");
        loginToggle.setToggleGroup(toggleGroup);
        signupToggle.setSelected(true);

        HBox toggleBox = new HBox(10, signupToggle, loginToggle);
        toggleBox.setAlignment(Pos.CENTER);

        loginToggle.setOnAction(e -> switchToLogin());
        signupToggle.setOnAction(e -> switchToSignup());

        // Username Field
        Label usernameLabel = new Label("Username");
        usernameLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));

        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setPrefHeight(35);
        usernameField.setMaxWidth(300);
        usernameField.setStyle("-fx-border-color: #ccc; -fx-padding: 8;");

        // Email Field (for signup only)
        emailLabel = new Label("Email");
        emailLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        emailLabel.setVisible(true);

        emailField = new TextField();
        emailField.setPromptText("Enter your email");
        emailField.setPrefHeight(35);
        emailField.setMaxWidth(300);
        emailField.setStyle("-fx-border-color: #ccc; -fx-padding: 8;");
        emailField.setVisible(true);

        // Password Field
        Label passwordLabel = new Label("Password");
        passwordLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));

        passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefHeight(35);
        passwordField.setMaxWidth(300);
        passwordField.setStyle("-fx-border-color: #ccc; -fx-padding: 8;");

        // Confirm Password Field (for signup only)
        confirmLabel = new Label("Confirm Password");
        confirmLabel.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        confirmLabel.setVisible(true);

        confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm your password");
        confirmPasswordField.setPrefHeight(35);
        confirmPasswordField.setMaxWidth(300);
        confirmPasswordField.setStyle("-fx-border-color: #ccc; -fx-padding: 8;");
        confirmPasswordField.setVisible(true);

        // Action Button
        loginButton = new Button("Create Account");
        loginButton.setPrefWidth(300);
        loginButton.setMaxWidth(300);
        loginButton.setPrefHeight(40);
        loginButton.setFont(Font.font("System", FontWeight.BOLD, 14));
        loginButton.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #ccc; -fx-border-width: 1; -fx-background-radius: 5;");
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String email = emailField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            handleAction(username, email, password, confirmPassword, primaryStage);
        });

        // Forgot Password Link
        Hyperlink forgotLink = new Hyperlink("Forgot Password?");
        forgotLink.setAlignment(Pos.CENTER_RIGHT);
        forgotLink.setOnAction(e -> showResetPasswordDialog());

        form.getChildren().addAll(
                toggleBox,
                usernameLabel, usernameField,
                emailLabel, emailField,
                passwordLabel, passwordField,
                confirmLabel, confirmPasswordField,
                loginButton,
                forgotLink
        );

        formContainer.getChildren().add(form);
        root.setCenter(formContainer);

        Scene scene = new Scene(root, 500, 600);
        return scene;
    }

    private static void showResetPasswordDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Enter your username and new password");

        ButtonType submitButton = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(submitButton, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #f5f5f5;");

        Label usernameLbl = new Label("Username:");
        usernameLbl.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        TextField usernameTF = new TextField();
        usernameTF.setPromptText("Enter your username");
        usernameTF.setPrefHeight(35);
        usernameTF.setMaxWidth(250);
        usernameTF.setStyle("-fx-border-color: #ccc; -fx-padding: 8;");

        Label newPassLbl = new Label("New Password:");
        newPassLbl.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        PasswordField newPass = new PasswordField();
        newPass.setPromptText("Enter new password");
        newPass.setPrefHeight(35);
        newPass.setMaxWidth(250);
        newPass.setStyle("-fx-border-color: #ccc; -fx-padding: 8;");

        Label confirmPassLbl = new Label("Confirm New Password:");
        confirmPassLbl.setFont(Font.font("System", FontWeight.MEDIUM, 12));
        PasswordField confirmPass = new PasswordField();
        confirmPass.setPromptText("Confirm new password");
        confirmPass.setPrefHeight(35);
        confirmPass.setMaxWidth(250);
        confirmPass.setStyle("-fx-border-color: #ccc; -fx-padding: 8;");

        content.getChildren().addAll(usernameLbl, usernameTF, newPassLbl, newPass, confirmPassLbl, confirmPass);

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(new Callback<ButtonType, ButtonType>() {
            @Override
            public ButtonType call(ButtonType param) {
                if (param == submitButton) {
                    String user = usernameTF.getText();
                    String pass1 = newPass.getText();
                    String pass2 = confirmPass.getText();
                    if (pass1.isEmpty() || pass2.isEmpty() || user.isEmpty()) {
                        showAlert("Error", "All fields are required.");
                        return ButtonType.CANCEL;
                    }
                    if (!pass1.equals(pass2)) {
                        showAlert("Error", "Passwords do not match.");
                        return ButtonType.CANCEL;
                    }
                    // TODO: Implement actual password reset logic
                    showAlert("Success", "Password reset successfully for " + user + ".");
                    dialog.close();
                    return submitButton;
                }
                return null;
            }
        });

        dialog.showAndWait();
    }

    private static void switchToLogin() {
        signupMode = false;
        loginButton.setText("Sign In");
        titleLabel.setText("Welcome Back");
        subtitleLabel.setText("Please sign in to your account");
        emailLabel.setVisible(false);
        emailField.setVisible(false);
        confirmLabel.setVisible(false);
        confirmPasswordField.setVisible(false);
        // Reset toggle styles to default
        loginToggle.setStyle("");
        signupToggle.setStyle("");

        emailField.clear();
confirmPasswordField.clear();
passwordField.clear();
usernameField.clear();

    }

    private static void switchToSignup() {
        signupMode = true;
        loginButton.setText("Create Account");
        titleLabel.setText("Create Account");
        subtitleLabel.setText("Sign up for a new account");
        emailLabel.setVisible(true);
        emailField.setVisible(true);
        confirmLabel.setVisible(true);
        confirmPasswordField.setVisible(true);
        // Reset toggle styles to default
        loginToggle.setStyle("");
        signupToggle.setStyle("");

        emailField.clear();
confirmPasswordField.clear();
passwordField.clear();
usernameField.clear();

    }

    private static void handleAction(String username, String email, String password, String confirmPassword, Stage stage) {

    if (signupMode) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Signup Failed", "All fields are required.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showAlert("Signup Failed", "Passwords do not match.");
            return;
        }

        boolean created = CompanyAuthDAO.signup(username, email, password);
        if (created) {
            showAlert("Account Created", "Account created! Please login.");
            switchToLogin();
        } else {
            showAlert("Signup Failed", "Username or Email already exists.");
        }

    } else { // LOGIN
        int companyId = CompanyAuthDAO.login(username, password);
        if (companyId == -1) {
            showAlert("Login Failed", "Invalid username or password.");
            return;
        }

        // Redirect
        if (CompanyAuthDAO.hasProfile(companyId)) {
            showAlert("Login Successful", "Welcome back!");
            stage.setScene(ServiceSessionManager.createScene(stage, companyId));
        } else {
            showAlert("First Login", "Complete your company profile.");
            stage.setScene(CompanyProfileSetup.createScene(stage, companyId));
        }
    }
}


    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Alias method so other pages can call LoginPage.createScene(stage)
public static Scene createScene(Stage stage) {
    return createLoginScene(stage);
}

}