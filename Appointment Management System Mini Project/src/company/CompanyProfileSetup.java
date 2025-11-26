package company;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javafx.stage.Stage;


public class CompanyProfileSetup {

    private TextField ownerNameField, companyNameField, taglineField, phoneField, emailField, websiteField;
    private TextArea descriptionArea, addressArea;
    private TextField startTimeField, endTimeField;
    private ComboBox<String> businessTypeCombo;
    private Set<String> offDaysSet = new HashSet<>();
    private VBox offDaysVBox;
    private ImageView logoPreview;
    private Button submitBtn, browseLogoBtn;
    private int companyId;

    public static Scene createScene(Stage stage, int compId) {
        
        CompanyProfileSetup app = new CompanyProfileSetup(compId);
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(app.createFormVBox());
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color:transparent;");
        root.setCenter(scrollPane);

        return new Scene(root, 800, 800);
    }

    private VBox createFormVBox() {
        VBox formVBox = new VBox(15);
        formVBox.setPadding(new Insets(20));

        ownerNameField = createStyledTextField("e.g., John Doe");
        formVBox.getChildren().add(createLabeledVBox("Owner Name:", ownerNameField));

        companyNameField = createStyledTextField("e.g., QuickFix Services");
        formVBox.getChildren().add(createLabeledVBox("Company Name:", companyNameField));

        businessTypeCombo = createBusinessTypeCombo();
        formVBox.getChildren().add(createLabeledVBox("Business Type:", businessTypeCombo));

        taglineField = createStyledTextField("e.g., Reliable home services");
        formVBox.getChildren().add(createLabeledVBox("Tagline:", taglineField));

        descriptionArea = createStyledTextArea("Detailed description...");
        formVBox.getChildren().add(createLabeledVBox("Description:", descriptionArea));

        // Logo upload
        VBox logoBox = new VBox(5);
        Label logoLabel = new Label("Company Logo:");
        browseLogoBtn = createStyledButton("Browse", e -> browseLogo());
        logoPreview = new ImageView();
        logoPreview.setFitWidth(250);
        logoPreview.setFitHeight(120);
        logoPreview.setPreserveRatio(true);
        logoPreview.setVisible(false);
        HBox logoHBox = new HBox(10, browseLogoBtn, logoPreview);
        logoHBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.getChildren().addAll(logoLabel, logoHBox);
        formVBox.getChildren().add(logoBox);

        startTimeField = createStyledTextField("09:00");
        endTimeField = createStyledTextField("18:00");
        HBox timeHBox = new HBox(10, new Label("Working Hours:"), startTimeField, new Label("to"), endTimeField);
        formVBox.getChildren().add(timeHBox);

        offDaysVBox = createOffDaysGrid();
        formVBox.getChildren().add(new VBox(5, new Label("Weekly Off Days:"), offDaysVBox));

        phoneField = createStyledTextField("+1 (123) 456-7890");
        formVBox.getChildren().add(createLabeledVBox("Phone:", phoneField));

        emailField = createStyledTextField("info@company.com");
        formVBox.getChildren().add(createLabeledVBox("Email:", emailField));

        websiteField = createStyledTextField("https://www.company.com");
        formVBox.getChildren().add(createLabeledVBox("Website:", websiteField));

        addressArea = createStyledTextArea("123 Main St, City, State 12345");
        formVBox.getChildren().add(createLabeledVBox("Address:", addressArea));

        submitBtn = createStyledButton("Save Company Profile", e -> saveProfile());
submitBtn.getStyleClass().add("save-button");

HBox buttonContainer = new HBox(submitBtn);
buttonContainer.setAlignment(Pos.CENTER);
buttonContainer.setPadding(new Insets(20, 0, 20, 0));

formVBox.getChildren().add(buttonContainer);


        return formVBox;
    }

    private VBox createLabeledVBox(String text, Control field) {
        Label label = new Label(text);
        VBox vbox = new VBox(5, label, field);
        field.setMaxWidth(Double.MAX_VALUE);
        return vbox;
    }

    private TextField createStyledTextField(String placeholder) {
        TextField field = new TextField();
        field.setPromptText(placeholder);
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    private TextArea createStyledTextArea(String placeholder) {
        TextArea area = new TextArea();
        area.setPromptText(placeholder);
        area.setWrapText(true);
        return area;
    }

    private ComboBox<String> createBusinessTypeCombo() {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll("", "Salon", "Clinic", "Mechanic", "Tutor", "Other");
        combo.setMaxWidth(Double.MAX_VALUE);
        return combo;
    }

    private Button createStyledButton(String text, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(text);
        btn.setOnAction(handler);
        return btn;
    }

    private VBox createOffDaysGrid() {
        VBox grid = new VBox(5);
        HBox row = new HBox(10);
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String day : days) {
            ToggleButton btn = new ToggleButton(day);
            btn.setPrefSize(70, 50);
            btn.setOnAction(e -> {
                if (btn.isSelected()) offDaysSet.add(day);
                else offDaysSet.remove(day);
            });
            row.getChildren().add(btn);
        }
        grid.getChildren().add(row);
        return grid;
    }

    private void browseLogo() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(null);
        if (file != null) {
            Image image = new Image(file.toURI().toString());
            logoPreview.setImage(image);
            logoPreview.setVisible(true);
        }
    }

    public CompanyProfileSetup(int companyId) {
    this.companyId = companyId;
}

    private void saveProfile() {
    CompanyProfile cp = new CompanyProfile();
    cp.setCompanyId(companyId); // Must exist in 'companies' table
    cp.setOwnerName(ownerNameField.getText());
    cp.setCompanyName(companyNameField.getText());
    cp.setBusinessType(businessTypeCombo.getValue());
    cp.setTagline(taglineField.getText());
    cp.setDescription(descriptionArea.getText());
    cp.setLogoPath(logoPreview.getImage() != null ? logoPreview.getImage().getUrl() : null);
    cp.setStartTime(startTimeField.getText());
    cp.setEndTime(endTimeField.getText());
    cp.setOffDays(String.join(",", offDaysSet));
    cp.setPhone(phoneField.getText());
    cp.setEmail(emailField.getText());
    cp.setWebsite(websiteField.getText());
    cp.setAddress(addressArea.getText());

    CompanyProfileDAO dao = new CompanyProfileDAO();

    if (dao.saveCompanyProfile(cp)) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText("Company profile saved successfully!");
        alert.showAndWait();

        Stage stage = (Stage) submitBtn.getScene().getWindow();
        stage.setScene(ServiceSessionManager.createScene(stage, companyId));
        stage.setTitle("Service & Session Setup");
    } else {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setContentText("Failed to save profile!");
        alert.show();
    }
}

}
