// MainApp.java - Single-window scene switching (stable Java features only)
package main;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import company.LoginPage;
import customer.AppointmentBooking;

public class MainApp extends Application {

    private Stage primaryStage;
    private Scene mainScene;
    private VBox heroContent;
    private static MainApp instance;

    @Override
public void start(Stage stage) {
    primaryStage = stage;
    primaryStage.setTitle("Appointment Management and Booking System");

    // Root BorderPane for easy content switching
    BorderPane root = new BorderPane();
    root.setStyle("-fx-background-color: #f8f9fa;");

    // Header (fixed at top)
    VBox header = createHeader();
    root.setTop(header);

    // Initial Hero Content (center)
    heroContent = createHeroContent();
    root.setCenter(heroContent);

    mainScene = new Scene(root, 800, 800);  // Fixed size
    primaryStage.setScene(mainScene);
    
    // Lock size to 800x700 (prevent resize)
    primaryStage.setMinWidth(800);
    primaryStage.setMinHeight(800);
    primaryStage.setMaxWidth(1600);
    primaryStage.setMaxHeight(800);
    primaryStage.show();
}

    private VBox createHeader() {
        Label headerLabel = new Label("Appointment Management and Booking System");
        headerLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        headerLabel.setTextFill(Color.WHITE);
        headerLabel.setAlignment(Pos.CENTER);
        VBox headerVBox = new VBox(headerLabel);  // Renamed to avoid conflicts
        headerVBox.setAlignment(Pos.CENTER);
        headerVBox.setBackground(new Background(new BackgroundFill(
            new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#667eea")), new Stop(1, Color.web("#764ba2"))),
            new CornerRadii(0), Insets.EMPTY)));
        headerVBox.setPadding(new Insets(15, 0, 15, 0));
        headerVBox.setPrefWidth(800);
        headerVBox.setFillWidth(true);
        return headerVBox;
    }

    private VBox createHeroContent() {
        Label titleLabel = new Label("Welcome to Appointment Management");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.web("#4a5568"));

        Label descLabel = new Label("Streamline your scheduling with our intuitive system. Whether you're allocating resources or booking directly, we've got you covered for efficient healthcare appointments.");
        descLabel.setFont(Font.font("Segoe UI", 14));
        descLabel.setTextFill(Color.web("#718096"));
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(600);
        descLabel.setAlignment(Pos.CENTER);

        // In createHeroContent() method:
Button registerBtn = new Button("Register");
registerBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
styleButton(registerBtn, "#4CAF50", "#45a049");
registerBtn.setOnAction(e -> {
    Scene loginScene = LoginPage.createLoginScene(primaryStage);
    primaryStage.setScene(loginScene);
});

Button bookBtn = new Button("Book Appointment");
bookBtn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
styleButton(bookBtn, "#2196F3", "#1976D2");
bookBtn.setOnAction(e -> switchToApp(() -> AppointmentBooking.createScene(), "Appointment Booking"));

        VBox buttonContainer = new VBox(15, registerBtn, bookBtn);
        buttonContainer.setAlignment(Pos.CENTER);

        VBox hero = new VBox(20, titleLabel, descLabel, buttonContainer);
        hero.setAlignment(Pos.CENTER);
        hero.setPadding(new Insets(40, 20, 40, 20));
        hero.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 4);");
        hero.setPrefWidth(800);
        hero.setFillWidth(true);
        return hero;
    }

    private void styleButton(Button btn, String baseColor, String hoverColor) {
        btn.setStyle("-fx-background-color: linear-gradient(to bottom right, " + baseColor + ", " + hoverColor + "); -fx-text-fill: white; -fx-padding: 15 30; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 4);");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: linear-gradient(to bottom right, " + hoverColor + ", " + baseColor + "); -fx-text-fill: white; -fx-padding: 15 30; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 15, 0, 0, 6); -fx-translate-y: -2;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: linear-gradient(to bottom right, " + baseColor + ", " + hoverColor + "); -fx-text-fill: white; -fx-padding: 15 30; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 4);"));
    }

    private void switchToApp(java.util.function.Supplier<Scene> sceneSupplier, String title) {
    try {
        Scene newScene = sceneSupplier.get();
        if (newScene != null) {
            // No setWidth/setHeight—enforced in constructors
            primaryStage.setScene(newScene);
            primaryStage.setTitle(title);
            addBackButton(newScene.getRoot());
        }
    } catch (Exception ex) {
        ex.printStackTrace();
        Label error = new Label("Failed to load: " + ex.getMessage());
        error.setStyle("-fx-text-fill: red;");
        ((BorderPane) mainScene.getRoot()).setCenter(error);
    }
}

    private void addBackButton(javafx.scene.Parent root) {
    Button backBtn = new Button("← Back to Main");
    backBtn.setOnAction(e -> {
        primaryStage.setScene(mainScene);
        primaryStage.setTitle("Appointment Management and Booking System");
    });
    backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #0078ff; -fx-font-size: 14;");

    VBox backContainer = new VBox(backBtn);
    backContainer.setPadding(new Insets(10));
    backContainer.setAlignment(Pos.CENTER_LEFT);

    if (root instanceof BorderPane bp) {
        bp.setTop(backContainer);
    } else if (root instanceof VBox vb) {
        vb.getChildren().add(0, backContainer);  // Prepend to top of VBox
    }
    // Add more layouts if needed (e.g., StackPane)
}
    public MainApp() {
    instance = this;
}

    public static MainApp getInstance() {
    return instance;
}

public Scene getMainScene() {
    return mainScene;
}

    public static void main(String[] args) {
        launch(args);
    }
}