package customer;

import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class AppointmentBooking {

    private ComboBox<String> companyCombo;
    private ComboBox<String> serviceCombo;
    private ComboBox<String> dayCombo;
    private FlowPane slotsPane;
    private VBox slotsSection, formVBox, serviceDetailsBox, companyDetailsBox;
    private Button submitBtn;
    private String selectedSlotTime;
    private AppointmentDAO appointmentDAO = new AppointmentDAO();

    // Service fields
    Label lblServiceName, lblProvider, lblCustomers, lblDescription;

    // Company fields
    Label lblOwner, lblCompany, lblBusinessType, lblPhone,
          lblEmail, lblWebsite, lblWorkTime, lblOffDays, lblAddress;

    public static Scene createScene() {
        AppointmentBooking app = new AppointmentBooking();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color:#f9fafb;");

        VBox header = new VBox(8);
        header.setAlignment(Pos.CENTER);

        Label title = new Label("Book Your Appointment");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 26));

        Label sub = new Label("Select company → service → day → slot → fill details");

        header.getChildren().addAll(title, sub);
        root.setTop(header);

        VBox mainContent = new VBox(25);
        mainContent.setPadding(new Insets(20));

        // Company selection
        VBox companySelectBox = app.createCompanySelectBox();
        mainContent.getChildren().add(companySelectBox);

        // Service selection
        VBox serviceSelectBox = app.createServiceSelectSection();
        serviceSelectBox.setVisible(true);
        mainContent.getChildren().add(serviceSelectBox);

        app.serviceDetailsBox = app.createServiceDetailsBox();
        app.serviceDetailsBox.setVisible(false);
        mainContent.getChildren().add(app.serviceDetailsBox);

        app.slotsSection = app.createSlotSection();
        app.slotsSection.setVisible(false);
        mainContent.getChildren().add(app.slotsSection);

        app.formVBox = app.createFormSection();
        app.formVBox.setVisible(false);
        mainContent.getChildren().add(app.formVBox);

        app.companyDetailsBox = app.createCompanyDetailsBox();
        app.companyDetailsBox.setVisible(false);
        mainContent.getChildren().add(app.companyDetailsBox);

        ScrollPane scroll = new ScrollPane(mainContent);
        scroll.setFitToWidth(true);

        root.setCenter(scroll);

        Scene scene = new Scene(root, 900, 950);

        app.loadDummyData();

        return scene;
    }

    // ---------------- Company Section -------------------

    private VBox createCompanySelectBox() {
        VBox box = new VBox(10);

        companyCombo = new ComboBox<>();
        companyCombo.setPromptText("Select Company");
        companyCombo.setPrefWidth(Double.MAX_VALUE);
        companyCombo.setOnAction(e -> onCompanySelected());

        box.getChildren().addAll(new Label("Company:"), companyCombo);
        return box;
    }

    private void onCompanySelected() {
        String company = companyCombo.getValue();

        // Show service selection
        serviceCombo.setDisable(false);
        serviceCombo.setValue(null);

        serviceCombo.getParent().setVisible(true);

        System.out.println("Company Selected: " + company);
        loadServicesForCompany(company);
    }

    // ---------------- Service Section -------------------

    private VBox createServiceSelectSection() {
        VBox box = new VBox(10);

        serviceCombo = new ComboBox<>();
        serviceCombo.setPromptText("Select Service");
        serviceCombo.setPrefWidth(Double.MAX_VALUE);
        serviceCombo.setDisable(true);
        serviceCombo.setOnAction(e -> onServiceSelected());

        dayCombo = new ComboBox<>();
        dayCombo.setPromptText("Select Day");
        dayCombo.setPrefWidth(Double.MAX_VALUE);
        dayCombo.setOnAction(e -> onDaySelected());

        box.getChildren().addAll(new Label("Service:"), serviceCombo, new Label("Day:"), dayCombo);
        return box;
    }

    private void onServiceSelected() {
    String service = serviceCombo.getValue();

    dayCombo.setValue(null); 
    dayCombo.setDisable(false);
    slotsSection.setVisible(false);
    formVBox.setVisible(false);

    customer.Service serviceDetails = appointmentDAO.getServiceDetails(service);
    if (serviceDetails != null) {
        lblServiceName.setText("Service: " + serviceDetails.getServiceName());
        lblProvider.setText("Provider: " + serviceDetails.getProviderName());
        lblCustomers.setText("Max Customers: " + serviceDetails.getMaxCustomers());
        lblDescription.setText("Description: " + serviceDetails.getDescription());
    }

    serviceDetailsBox.setVisible(true);

    ObservableList<String> availableDays = FXCollections.observableArrayList();
    String[] allPossibleDays = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}; // Use all days for a comprehensive check

    for (String day : allPossibleDays) {
        // A day is "available" if the service offers at least one slot 
        // AND that slot is not yet fully booked.
        for (String slot : appointmentDAO.getSlots(service, day)) {
            if (appointmentDAO.isSlotAvailable(service, day, slot)) {
                availableDays.add(day);
                break; // Found one available slot, so the day is available, move to the next day
            }
        }
    }

    dayCombo.setItems(availableDays);
    
    // Optional: Disable dayCombo if no days are available
    if (availableDays.isEmpty()) {
        dayCombo.setPromptText("No available days for this service");
        dayCombo.setDisable(true);
    }
}


    // ---------------- Service Details -------------------

    private VBox createServiceDetailsBox() {
        VBox box = new VBox(6);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-border-color:#ccc; -fx-padding:10; -fx-background-color:white;");

        lblServiceName = new Label("Service: ");
        lblProvider = new Label("Provider: ");
        lblCustomers = new Label("Max Customers: ");
        lblDescription = new Label("Description: ");

        box.getChildren().addAll(
            new Label("Service Details:"),
            lblServiceName, lblProvider, lblCustomers, lblDescription
        );
        return box;
    }

    // ---------------- Slots -------------------

    private VBox createSlotSection() {
        VBox box = new VBox(10);
        Label label = new Label("Available Slots:");

        slotsPane = new FlowPane(8,8);
        slotsPane.setPrefWrapLength(400);

        box.getChildren().addAll(label, slotsPane);
        return box;
    }

    private void onDaySelected() {
    slotsPane.getChildren().clear();
    formVBox.setVisible(false);

    String company = companyCombo.getValue();
    String service = serviceCombo.getValue();
    String day = dayCombo.getValue();

    slotsPane.getChildren().clear();
    for(String slot : appointmentDAO.getSlots(service, day)) {
        Button btn = new Button(slot);
        
        // ✅ If slot full, disable + mark
        if (!appointmentDAO.isSlotAvailable(service, day, slot)) {
            btn.setDisable(true);
            btn.setStyle("-fx-background-color:#ffcccc; -fx-text-fill:#333;");
            btn.setText(slot + " (Full)");
        } else {
            btn.setOnAction(e -> selectSlot(slot));
        }
        slotsPane.getChildren().add(btn);
    }
    slotsSection.setVisible(true);
}


    private void selectSlot(String slot) {
    String service = serviceCombo.getValue();
    String day = dayCombo.getValue();

    selectedSlotTime = slot;
    formVBox.setVisible(true);
    companyDetailsBox.setVisible(true);
    submitBtn.setDisable(false);

    String company = companyCombo.getValue();
    Company companyData = appointmentDAO.getCompanyDetails(company);

    lblOwner.setText("Owner: " + companyData.getOwnerName());
    lblCompany.setText("Company: " + companyData.getCompanyName());
    lblBusinessType.setText("Business Type: " + companyData.getBusinessType());
    lblPhone.setText("Phone: " + companyData.getPhone());
    lblEmail.setText("Email: " + companyData.getEmail());
    lblWebsite.setText("Website: " + companyData.getWebsite());
    lblWorkTime.setText("Working Time: " + companyData.getWorkingTime());
    lblOffDays.setText("Off Days: " + companyData.getOffDays());
    lblAddress.setText("Address: " + companyData.getAddress());
}


    // --------------- Customer Form -------------------

    private VBox createFormSection() {
        VBox box = new VBox(12);
        box.setPadding(new Insets(10));

        GridPane form = new GridPane();
        form.setHgap(10); form.setVgap(12);

        TextField name = new TextField(); name.setPromptText("Full Name");
        TextField email = new TextField(); email.setPromptText("Email");
        TextField phone = new TextField(); phone.setPromptText("Phone");
        TextArea notes = new TextArea(); notes.setPromptText("Notes");

        form.add(new Label("Name:"), 0, 0); form.add(name, 1, 0);
        form.add(new Label("Email:"), 0, 1); form.add(email, 1, 1);
        form.add(new Label("Phone:"), 0, 2); form.add(phone, 1, 2);
        form.add(new Label("Notes:"), 0, 3); form.add(notes, 1, 3);

        submitBtn = new Button("Confirm Appointment");
        submitBtn.setDisable(true);
        
        submitBtn.setOnAction(e -> {
            String company = companyCombo.getValue();
            String service = serviceCombo.getValue();
            String day = dayCombo.getValue();
            String slot = selectedSlotTime;

            TextField nameField = (TextField) ((GridPane) formVBox.getChildren().get(0)).getChildren().get(1);
            TextField emailField = (TextField) ((GridPane) formVBox.getChildren().get(0)).getChildren().get(3);
            TextField phoneField = (TextField) ((GridPane) formVBox.getChildren().get(0)).getChildren().get(5);
            TextArea notesArea = (TextArea) ((GridPane) formVBox.getChildren().get(0)).getChildren().get(7);

            if (!appointmentDAO.isSlotAvailable(service, day, slot)) {
                new Alert(Alert.AlertType.WARNING, "❌ Slot is already full! Please choose another slot.").show();
                return;
            }

            Booking booking = new Booking(
                company, service, day, slot,
                nameField.getText(),
                phoneField.getText(),
                emailField.getText(),
                notesArea.getText()
            );

            int companyId = appointmentDAO.getCompanyId(company);
            boolean saved = appointmentDAO.saveBooking(booking, companyId);

            if(saved) {
                new Alert(Alert.AlertType.INFORMATION, "✅ Appointment Saved Successfully!").show();
                onDaySelected(); 
                formVBox.setVisible(false);                 companyDetailsBox.setVisible(false); // Hide the company details
                selectedSlotTime = null;
            } else {
                new Alert(Alert.AlertType.ERROR, "❌ Failed to save booking!").show();
            }
        });

        box.getChildren().addAll(form, submitBtn);
        return box;
    }

    // -------------- Company Details Section -----------------

    private VBox createCompanyDetailsBox() {
        VBox box = new VBox(6);
        box.setPadding(new Insets(12));
        box.setStyle("-fx-border-color:#ccc; -fx-background-color:white;");

        lblOwner = new Label("Owner: ");
        lblCompany = new Label("Company: ");
        lblBusinessType = new Label("Business Type: ");
        lblPhone = new Label("Phone: ");
        lblEmail = new Label("Email: ");
        lblWebsite = new Label("Website: ");
        lblWorkTime = new Label("Working Time: ");
        lblOffDays = new Label("Off Days: ");
        lblAddress = new Label("Address: ");

        box.getChildren().addAll(
            new Label("Company Information:"),
            lblOwner, lblCompany, lblBusinessType,
            lblPhone, lblEmail, lblWebsite,
            lblWorkTime, lblOffDays, lblAddress
        );
        return box;
    }

    // -------------- Dummy Data Loader -----------------

    private void loadDummyData() {
    companyCombo.setItems(appointmentDAO.getCompanies());
}

    private void loadServicesForCompany(String company) {
    serviceCombo.setItems(appointmentDAO.getServicesByCompany(company));
}

}