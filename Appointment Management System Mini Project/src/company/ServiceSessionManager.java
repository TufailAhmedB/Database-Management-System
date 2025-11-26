package company;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.scene.Node;
import java.util.stream.Collectors;

import javafx.scene.layout.Region;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.SQLException;

/**
 * ServiceSessionManager as a Scene-provider (no Application).
 * Use: primaryStage.setScene(ServiceSessionManager.createScene());
 */
public class ServiceSessionManager {

    // --- Data Models ---
    public static record TimeSlot(String start, String end) {}
    
    // MODIFIED: Replaced providerName with firstName, lastName, and role
    public static record ServiceConfig(int serviceId, String serviceName, int numCustomers, String firstName, String lastName, String role, String description, boolean isActive, Map<String, List<TimeSlot>> serviceDays) {}

    public List<ServiceConfig> sessions;

    // --- Global State ---
    private final Map<String, List<TimeSlot>> serviceDaysConfig = new HashMap<>();
    private final ObservableList<ServiceConfig> activeServices = FXCollections.observableArrayList();
    private String currentSelectedDay = null;
    private int currentSelectedSlotIndex = -1;

    // UI Elements
    private VBox formContainer; // holds form inputs
    private VBox dayConfigSection;
    private VBox activeServicesPage;
    private VBox bookingsPage;
    private VBox newServicePage;
    private VBox editServicePage;
    private VBox appContent;
    private Spinner<Integer> maxSessionsSpinner;
    private FlowPane sessionButtonsContainer;
    private TextField startTimeField;
    private TextField endTimeField;
    private VBox sessionsSection; // holds rendered cards
    private int companyId;

    private TextField serviceNameField;
    private TextField numCustomersField;
    private TextField firstNameField;
    private TextField lastNameField;
    private TextField roleField;
    private TextArea descriptionArea;
    private Button saveButton;
    // Helper to prevent recursive updates when programmatically setting text
    private boolean suppressUpdate = false;

    private final String[] dayAbbrs = {"sun", "mon", "tue", "wed", "thu", "fri", "sat"};
    private final Map<String, String> dayFullNames = Map.of(
            "sun", "Sunday", "mon", "Monday", "tue", "Tuesday", "wed", "Wednesday",
            "thu", "Thursday", "fri", "Friday", "sat", "Saturday"
    );

    /** Public Scene provider */
    public static Scene createScene(Stage stage, int companyId) {
        ServiceSessionManager app = new ServiceSessionManager();
        app.initState(); // initialize maps, sample data
        app.companyId = companyId;
        // NEW: Log companyId on init
System.out.println("=== INIT: ServiceSessionManager loaded with companyId = " + companyId + " ===");
        BorderPane root = new BorderPane();

        // ---- Top Bar with Logout ----
HBox topBar = new HBox(10);
topBar.setPadding(new Insets(10));
topBar.setAlignment(Pos.CENTER_LEFT);

Label titleLabel = new Label("Welcome, Company");
Button logoutBtn = new Button("Logout");

// Spacer to push logout button right
Region spacer = new Region();
HBox.setHgrow(spacer, Priority.ALWAYS);

topBar.getChildren().addAll(titleLabel, spacer, logoutBtn);

// Set top bar in borderpane
root.setTop(topBar);

// Logout action
logoutBtn.setOnAction(e -> {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Logout");
    alert.setHeaderText(null);
    alert.setContentText("Are you sure you want to logout?");
    alert.showAndWait().ifPresent(response -> {
        if (response == ButtonType.OK) {
            stage.setScene(LoginPage.createScene(stage)); // your login screen method
        }
    });
});

        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f3f4f6;");

        VBox mainContent = new VBox(20);
        mainContent.setPadding(new Insets(20));
        mainContent.setStyle("-fx-background-color: white; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 0);");

        // Navigation Bar
HBox menuBar = new HBox(20);
menuBar.setAlignment(Pos.CENTER_LEFT);
menuBar.setPadding(new Insets(10, 0, 10, 0));

Button btnActiveService = new Button("Active Services");
Button btnBookings = new Button("Bookings");
Button btnAddService = new Button("New Service");
Button btnEditService = new Button("Edit Service");

app.styleNavButton(btnActiveService);
app.styleNavButton(btnBookings);
app.styleNavButton(btnAddService);
app.styleNavButton(btnEditService);

// Default selected button
app.setSelected(btnActiveService);
app.unset(btnBookings);
app.unset(btnAddService);
app.unset(btnEditService);

menuBar.getChildren().addAll(btnActiveService, btnBookings, btnAddService, btnEditService);

        app.appContent = new VBox(20);
        app.appContent.setPadding(new Insets(20));
        app.appContent.setStyle("-fx-background-color: white; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 0);");

        // Active Services page
        // 1. Fetch data from the database
List<ServiceConfig> services = loadActiveServices(companyId);

// 2. Update the application's data model
app.sessions = services;

// 3. Create the fully rendered VBox using the DB data
VBox sessionsView = app.createSessionsSection(app.sessions);

// 4. Set the application's UI fields to reference the rendered view
app.sessionsSection = sessionsView;
app.activeServicesPage = sessionsView;
app.activeServicesPage.setPadding(new Insets(10)); // Keep the padding// loads active service cards

        // Bookings Page (placeholder)
        Label bookingsTitle = new Label("Bookings");
        bookingsTitle.setFont(Font.font("Inter", FontWeight.BOLD, 20));
        app.bookingsPage = new VBox(10, bookingsTitle, new Label("All Bookings will show here"));
        app.bookingsPage.setPadding(new Insets(10));

        // New Service Page = your form
        app.newServicePage = app.formContainer = app.createFormContainer();
        app.newServicePage.setPadding(new Insets(10));

        // Edit Service Page (placeholder for now)
        Label editTitle = new Label("Edit Services");
        editTitle.setFont(Font.font("Inter", FontWeight.BOLD, 20));
        app.editServicePage = new VBox(10, editTitle, new Label("Select a service to edit"));
        app.editServicePage.setPadding(new Insets(10));

        // Default View -> Active Services
        app.appContent.getChildren().setAll(app.sessionsSection);

        // ServiceSessionManager.java (Inside createScene method)

btnActiveService.setOnAction(e -> {
    // 1. Unset the styles of other buttons
    app.unset(btnBookings);
    app.unset(btnAddService);
    app.unset(btnEditService); // If you have this button
    app.setSelected(btnActiveService);
    app.appContent.getChildren().setAll(app.sessionsSection);
});

        // Navigation actions
        btnBookings.setOnAction(e -> {
    app.unset(btnActiveService);
    app.unset(btnAddService);
    app.unset(btnEditService);
    app.setSelected(btnBookings);

    // NEW: Log companyId
    System.out.println("=== DEBUG: Bookings query for companyId = " + companyId + " ===");

    Label bookingsHeader = new Label("Bookings Section");
    bookingsHeader.setFont(Font.font("Inter", FontWeight.BOLD, 20));
    TableView<Booking> table = new TableView<>();
    
    // ServiceSessionManager.java (Around line 180 equivalent)

    TableColumn<Booking, Integer> colId = new TableColumn<>("ID"); 
    colId.setPrefWidth(50);
    colId.setCellValueFactory(new PropertyValueFactory<>("bookingId"));

    TableColumn<Booking, String> colCust = new TableColumn<>("Customer");
    colCust.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getCustomerName()));

    TableColumn<Booking, String> colService = new TableColumn<>("Service");
    colService.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getServiceName()));

    TableColumn<Booking, String> colDate = new TableColumn<>("Date");
    colDate.setCellValueFactory(data -> 
        new javafx.beans.property.SimpleStringProperty(data.getValue().getBookingDate()));

    TableColumn<Booking, String> colSlot = new TableColumn<>("Slot");
    colSlot.setCellValueFactory(data ->
        new javafx.beans.property.SimpleStringProperty(data.getValue().getTimeSlot()));

        // ServiceSessionManager.java: inside btnBookings.setOnAction

// Set the resize policy to ensure columns use all available space
table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

// Calculate the total number of columns (5 in your case: ID, Customer, Service, Date, Slot)

// Give the 'ID' column a fixed, small percentage
colId.setMaxWidth(1f * Integer.MAX_VALUE * 0.05); // e.g., 5% of width

// Give the main content columns proportional shares of the remaining 95%
colCust.setMaxWidth(1f * Integer.MAX_VALUE * 0.25); // e.g., 25% of width
colService.setMaxWidth(1f * Integer.MAX_VALUE * 0.35); // e.g., 35% of width
colDate.setMaxWidth(1f * Integer.MAX_VALUE * 0.15); // e.g., 15% of width
colSlot.setMaxWidth(1f * Integer.MAX_VALUE * 0.20); // e.g., 20% of width

// Total percentage should be 100% (5 + 25 + 35 + 15 + 20 = 100)
    table.getColumns().addAll(colId, colCust, colService, colDate, colSlot);

    VBox.setVgrow(table, Priority.ALWAYS);

    VBox box = new VBox(10);
    box.setMaxWidth(Double.MAX_VALUE);
    // Fetch bookings
    try {
        BookingDAO dao = new BookingDAO();
        List<Booking> bookings = dao.getBookingsByCompanyId(companyId);
        table.getItems().addAll(bookings);
        
        // NEW: Log size
        System.out.println("=== DEBUG: Fetched " + bookings.size() + " bookings ===");
        
    } catch (Exception ex) {
        ex.printStackTrace();
        box.getChildren().add(new Label("Error loading bookings: " + ex.getMessage()));
    }
    box.getChildren().addAll(bookingsHeader, table);
    app.appContent.getChildren().setAll(box);
});

        btnAddService.setOnAction(e -> {
            app.unset(btnActiveService);
            app.unset(btnBookings);
            app.unset(btnEditService);
            app.setSelected(btnAddService);

            app.appContent.getChildren().setAll(app.formContainer);
        });

        btnEditService.setOnAction(e -> {
            app.unset(btnActiveService);
            app.unset(btnBookings);
            app.unset(btnAddService);
            app.setSelected(btnEditService);

            Label editHeader = new Label("Edit Service Section");
            editHeader.setFont(Font.font("Inter", FontWeight.BOLD, 20));
            VBox editSelectionPage = app.createEditServiceSelectionPage();
            app.appContent.getChildren().setAll(editSelectionPage);
        });

        ScrollPane scrollPane = new ScrollPane(new VBox(menuBar, app.appContent));
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f3f4f6;");

        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 800, 700);
        return scene;
    }

    private void styleNavButton(Button btn) {
    btn.setFont(Font.font("Inter", 14));
    btn.setPrefHeight(35);
    btn.setMinHeight(35);
    btn.setMaxHeight(35);
    btn.setPrefWidth(140);
    btn.setStyle(
        "-fx-background-color: transparent;" +
        "-fx-text-fill: #374151;" +
        "-fx-background-radius: 6;" +
        "-fx-padding: 8 16;" +
        "-fx-border-color: #d1d5db;" +   /* light border */
        "-fx-border-width: 1;" +
        "-fx-border-radius: 6;"
    );

    btn.setOnMouseEntered(e -> {
        if (!btn.getStyleClass().contains("nav-selected")) {
btn.setStyle(
                "-fx-background-color: #E5E7EB;" +
                "-fx-text-fill: #111827;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 8 16;" +
                "-fx-border-color: #9ca3af;" +  /* darker border on hover */
                "-fx-border-width: 1;" +
                "-fx-border-radius: 6;"
            );        }
    });

    btn.setOnMouseExited(e -> {
        if (!btn.getStyleClass().contains("nav-selected")) {
btn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #374151;" +
                "-fx-background-radius: 6;" +
                "-fx-padding: 8 16;" +
                "-fx-border-color: #d1d5db;" +  /* return original border */
                "-fx-border-width: 1;" +
                "-fx-border-radius: 6;"
            );        }
    });
}

    private void setSelected(Button btn) {
    btn.getStyleClass().add("nav-selected");
    btn.setPrefHeight(35);
    btn.setMinHeight(35);
    btn.setStyle(
        "-fx-background-color: #2563eb;" +
        "-fx-text-fill: white;" +
        "-fx-font-weight: bold;" +
        "-fx-background-radius: 6;" +
        "-fx-padding: 8 16;"
    );
}

    private void unset(Button btn) {
    btn.getStyleClass().remove("nav-selected");
    btn.setStyle(
        "-fx-background-color: transparent;" +
        "-fx-text-fill: #374151;" +
        "-fx-font-weight: normal;" +
        "-fx-background-radius: 6;" +
        "-fx-padding: 8 16;" +
        "-fx-border-width: 1;" +
        "-fx-border-color: #d1d5db;" +   /* visible border when normal */
        "-fx-border-radius: 6;"
    );
}

    /** Initialize empty lists for each day */
    private void initState() {
        for (String day : dayAbbrs) {
            serviceDaysConfig.put(day, new ArrayList<>());
        }
    }

    /** Creates the toggle button and header for the form. */
    private HBox createFormToggleHeader() {
        Button toggleButton = new Button("+");
        toggleButton.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: #10b981; -fx-text-fill: white; -fx-pref-width: 40px; -fx-pref-height: 40px; -fx-background-radius: 20px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 0);");
        Label headerLabel = new Label("Add New Service Configuration");
        headerLabel.setFont(Font.font("Inter", FontWeight.SEMI_BOLD, 16));
        headerLabel.setStyle("-fx-text-fill: #4b5563;");

        HBox toggleHeader = new HBox(15, toggleButton, headerLabel);
        toggleHeader.setAlignment(Pos.CENTER_LEFT);
        toggleHeader.setPadding(new Insets(0, 0, 15, 0));
        toggleHeader.setStyle("-fx-border-color: #d1d5db; -fx-border-width: 0 0 1px 0;");

        toggleButton.setOnAction(e -> toggleForm(toggleButton));
        return toggleHeader;
    }

    /** Creates the full form container with all inputs and configuration panels. */
    private VBox createFormContainer() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(15));
        container.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-border-width: 1px;");

        // Main Inputs
        GridPane mainInputs = new GridPane();
        mainInputs.setHgap(10);
        mainInputs.setVgap(10);

        TextField serviceNameField = new TextField();
        serviceNameField.setPromptText("e.g., Personal Training, Tutoring");
        TextField numCustomersField = new TextField();
        numCustomersField.setPromptText("Enter number");
        numCustomersField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                numCustomersField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        
        // MODIFIED: Replaced providerNameField with three new fields
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("e.g., Jane");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("e.g., Doe");
        TextField roleField = new TextField();
        roleField.setPromptText("e.g., Certified Trainer");
        
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("A brief description of the service.");
        descriptionArea.setPrefRowCount(3);

        mainInputs.add(new Label("Service Name:"), 0, 0);
        mainInputs.add(serviceNameField, 1, 0);
        mainInputs.add(new Label("No. of Customers:"), 0, 1);
        mainInputs.add(numCustomersField, 1, 1);
        
        // MODIFIED: New rows for staff details
        mainInputs.add(new Label("Provider First Name:"), 0, 2);
        mainInputs.add(firstNameField, 1, 2);
        mainInputs.add(new Label("Provider Last Name:"), 0, 3);
        mainInputs.add(lastNameField, 1, 3);
        mainInputs.add(new Label("Provider Role:"), 0, 4);
        mainInputs.add(roleField, 1, 4);

        // Days of Operation
        VBox daysSection = new VBox(5);
        Separator separator = new Separator();
        HBox dayButtons = new HBox(5);
        dayButtons.setPadding(new Insets(10, 0, 0, 0));

        for (String day : dayAbbrs) {
            ToggleButton dayBtn = new ToggleButton(dayFullNames.get(day).substring(0, 3));
            dayBtn.setId("day-" + day);
            dayBtn.getStyleClass().add("day-toggle");
            dayBtn.setOnAction(e -> toggleDay(dayBtn, day));
            dayButtons.getChildren().add(dayBtn);
        }
        daysSection.getChildren().addAll(separator, dayButtons);

        // Day-specific configuration panel
        dayConfigSection = createDayConfigurationPanel();
        dayConfigSection.setVisible(false);
        dayConfigSection.setManaged(false);

        VBox descriptionBox = new VBox(5, new Label("Description:"), descriptionArea);
        descriptionBox.setPadding(new Insets(10, 0, 0, 0));
        descriptionBox.setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 1px 0 0 0;");

        Button saveButton = new Button("Save New Service");
        saveButton.getStyleClass().add("save-button");
        
        // MODIFIED: Updated processInput call with new fields
        saveButton.setOnAction(e -> processInput(serviceNameField, numCustomersField, firstNameField, lastNameField, roleField, descriptionArea));

        container.getChildren().addAll(mainInputs, daysSection, dayConfigSection, descriptionBox, saveButton);
        return container;
    }

    /** Creates the panel for session time configuration. */
    private VBox createDayConfigurationPanel() {
        VBox configPanel = new VBox(10);
        configPanel.setPadding(new Insets(15));
        configPanel.setStyle("-fx-background-color: white; -fx-border-color: #93c5fd; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-border-width: 2px;");

        Label title = new Label("Configure Sessions");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 14));
        title.setStyle("-fx-text-fill: #1d4ed8;");

        Label maxSessionsLabel = new Label("Max Sessions:");
        maxSessionsSpinner = new Spinner<>(1, 10, 1);
        maxSessionsSpinner.setEditable(true);
        maxSessionsSpinner.setPrefWidth(80);
        HBox maxSessionsBox = new HBox(10, maxSessionsLabel, maxSessionsSpinner);
        maxSessionsBox.setAlignment(Pos.CENTER_LEFT);

        sessionButtonsContainer = new FlowPane(5, 5);
        sessionButtonsContainer.setPadding(new Insets(10, 0, 0, 0));
        sessionButtonsContainer.setStyle("-fx-border-color: #e5e7eb; -fx-border-width: 1px 0 0 0; -fx-padding: 10 0 0 0;");

        startTimeField = new TextField();
        startTimeField.setPromptText("HH:MM (e.g., 09:00)");
        endTimeField = new TextField();
        endTimeField.setPromptText("HH:MM (e.g., 10:00)");

        Label startTimeLabel = new Label("Start Time:");
        Label endTimeLabel = new Label("End Time:");

        VBox startTimeBox = new VBox(5, startTimeLabel, startTimeField);
        VBox endTimeBox = new VBox(5, endTimeLabel, endTimeField);
        HBox timeInputs = new HBox(15, startTimeBox, endTimeBox);
        timeInputs.setPadding(new Insets(10, 0, 0, 0));

        // Use suppressUpdate to prevent recursion
        startTimeField.textProperty().addListener((obs, oldV, newV) -> {
            if (!suppressUpdate) updateTimeForSelectedSlot(false);
        });
        endTimeField.textProperty().addListener((obs, oldV, newV) -> {
            if (!suppressUpdate) updateTimeForSelectedSlot(false);
        });

        maxSessionsSpinner.valueProperty().addListener((obs, oldV, newV) -> renderSessionSelectors(newV.intValue()));

        configPanel.getChildren().addAll(title, maxSessionsBox, new Label("Select a Slot to Configure Its Time:"), sessionButtonsContainer, timeInputs);
        return configPanel;
    }

    /** Toggles the visibility of the main form. */
    private void toggleForm(Button toggleButton) {
        boolean isVisible = formContainer.isVisible();
        formContainer.setVisible(!isVisible);
        formContainer.setManaged(!isVisible);

        if (isVisible) {
            toggleButton.setText("+");
            toggleButton.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: #10b981; -fx-text-fill: white; -fx-pref-width: 40px; -fx-pref-height: 40px; -fx-background-radius: 20px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 0);");
            resetForm();
        } else {
            toggleButton.setText("X");
            toggleButton.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: #ef4444; -fx-text-fill: white; -fx-pref-width: 40px; -fx-pref-height: 40px; -fx-background-radius: 20px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 0);");
        }
    }

    /** Toggles a day's selection state and updates the configuration context. */
    private void toggleDay(ToggleButton dayBtn, String day) {
        if (currentSelectedDay != null) {
            updateTimeForSelectedSlot(true);
        }

        if (dayBtn.isSelected()) {
            currentSelectedDay = day;
            dayBtn.getStyleClass().add("day-selected");
        } else {
            dayBtn.getStyleClass().remove("day-selected");
        }

        boolean anyDaySelected = Arrays.stream(dayAbbrs)
                .anyMatch(d -> {
                    Node node = dayBtn.getParent().lookup("#day-" + d);
                    return node instanceof ToggleButton && ((ToggleButton) node).isSelected();
                });

        if (!anyDaySelected) {
            dayConfigSection.setVisible(false);
            dayConfigSection.setManaged(false);
            currentSelectedDay = null;
            return;
        }

        if (!dayBtn.isSelected() && day.equals(currentSelectedDay)) {
            currentSelectedDay = Arrays.stream(dayAbbrs)
                    .filter(d -> {
                        Node node = dayBtn.getParent().lookup("#day-" + d);
                        return node instanceof ToggleButton && ((ToggleButton) node).isSelected();
                    })
                    .findFirst()
                    .orElse(null);
        }

        if (currentSelectedDay != null) {
            dayConfigSection.setVisible(true);
            dayConfigSection.setManaged(true);

            List<TimeSlot> slotsForDay = serviceDaysConfig.get(currentSelectedDay);
            maxSessionsSpinner.getValueFactory().setValue(slotsForDay.size() > 0 ? slotsForDay.size() : 1);
            currentSelectedSlotIndex = slotsForDay.size() > 0 ? 0 : -1;
            renderSessionSelectors(maxSessionsSpinner.getValue());
        }
    }

    /** Renders session slot buttons based on spinner value. */
    private void renderSessionSelectors(int max) {
        if (currentSelectedDay == null) return;

        List<TimeSlot> sourceSlots = serviceDaysConfig.get(currentSelectedDay);
        List<TimeSlot> newSlots = new ArrayList<>();

        for (int i = 0; i < max; i++) {
            if (i < sourceSlots.size()) newSlots.add(sourceSlots.get(i));
            else newSlots.add(new TimeSlot("", ""));
        }
        serviceDaysConfig.put(currentSelectedDay, newSlots);

        if (currentSelectedSlotIndex >= newSlots.size()) {
            currentSelectedSlotIndex = newSlots.size() > 0 ? 0 : -1;
        }
        if (newSlots.size() > 0) {
            currentSelectedSlotIndex = currentSelectedSlotIndex >= 0 ? currentSelectedSlotIndex : 0;
        } else {
            currentSelectedSlotIndex = -1;
        }

        sessionButtonsContainer.getChildren().clear();
        for (int i = 0; i < newSlots.size(); i++) {
            TimeSlot slot = newSlots.get(i);
            Button slotBtn = new Button(slot.start().isEmpty() ? "Empty Slot" : slot.start() + " - " + slot.end());
            slotBtn.setId("session-slot-" + i);
            slotBtn.getStyleClass().add("session-slot-button");

            final int index = i;
            slotBtn.setOnAction(e -> toggleSessionSlot(index));
            sessionButtonsContainer.getChildren().add(slotBtn);
        }

        if (currentSelectedSlotIndex != -1) toggleSessionSlot(currentSelectedSlotIndex);
        else syncTimeInputsWithSelection();

        updateButtonTimesDisplay();
    }

    /** Toggle session slot selection, save previous slot and load new slot times. */
    private void toggleSessionSlot(int newIndex) {
        if (currentSelectedDay == null || newIndex == currentSelectedSlotIndex) return;

        if (currentSelectedSlotIndex != -1) {
            updateTimeForSelectedSlot(true);
            Button oldButton = (Button) sessionButtonsContainer.lookup("#session-slot-" + currentSelectedSlotIndex);
            if (oldButton != null) oldButton.getStyleClass().remove("session-selected");
        }

        currentSelectedSlotIndex = newIndex;
        Button newButton = (Button) sessionButtonsContainer.lookup("#session-slot-" + currentSelectedSlotIndex);
        if (newButton != null) newButton.getStyleClass().add("session-selected");

        syncTimeInputsWithSelection();
    }

    /** Saves current time inputs back into selected slot. */
    private void updateTimeForSelectedSlot(boolean silent) {
        if (currentSelectedDay == null || currentSelectedSlotIndex == -1) return;

        List<TimeSlot> slots = serviceDaysConfig.get(currentSelectedDay);
        String newStart = startTimeField.getText().trim();
        String newEnd = endTimeField.getText().trim();

        if (currentSelectedSlotIndex < slots.size()) {
            TimeSlot updatedSlot = new TimeSlot(newStart, newEnd);            slots.set(currentSelectedSlotIndex, updatedSlot);
        }

        if (!silent) updateButtonTimesDisplay();
    }

    /** Update button labels to include start time. */
    private void updateButtonTimesDisplay() {
        if (currentSelectedDay == null) return;
        List<TimeSlot> slots = serviceDaysConfig.get(currentSelectedDay);
        for (int i = 0; i < slots.size(); i++) {
            TimeSlot slot = slots.get(i);
            Button button = (Button) sessionButtonsContainer.lookup("#session-slot-" + i);
            if (button != null) {
                button.setText(slot.start().isEmpty() ? "Empty Slot" : slot.start() + " - " + slot.end());
            }
        }
    }

    /** Loads time inputs from the currently selected slot. Uses suppressUpdate to avoid recursion. */
    private void syncTimeInputsWithSelection() {
        if (currentSelectedDay == null || currentSelectedSlotIndex == -1) {
            suppressUpdate = true;
            startTimeField.setText("");
            endTimeField.setText("");
            suppressUpdate = false;
            return;
        }

        List<TimeSlot> slots = serviceDaysConfig.get(currentSelectedDay);
        if (currentSelectedSlotIndex < slots.size()) {
            TimeSlot slot = slots.get(currentSelectedSlotIndex);
            suppressUpdate = true;
            startTimeField.setText(slot.start());
            endTimeField.setText(slot.end());
            suppressUpdate = false;
        }
    }

    /** Validates and saves new service configuration. */
    // MODIFIED: Updated method signature to accept new staff fields
    private void processInput(TextField serviceNameField, TextField numCustomersField, TextField firstNameField, TextField lastNameField, TextField roleField, TextArea descriptionArea) {
        if (currentSelectedDay != null) updateTimeForSelectedSlot(true);

        String serviceName = serviceNameField.getText().trim();
        String numCustomersText = numCustomersField.getText().trim();
        
        // MODIFIED: Get new staff fields
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String role = roleField.getText().trim();
        
        String description = descriptionArea.getText().trim();

        // MODIFIED: Check all new staff fields for emptiness
        if (serviceName.isEmpty() || numCustomersText.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || role.isEmpty() || description.isEmpty()) {
            showAlert("Validation Error", "Please fill in all required fields (Service Name, Customers, First Name, Last Name, Role, Description).");
            return;
        }

        int numCustomers;
        try {
            numCustomers = Integer.parseInt(numCustomersText);
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "Number of Customers must be a valid number.");
            return;
        }

        Map<String, List<TimeSlot>> finalServiceDays = new HashMap<>();
        int totalDefinedSlots = 0;

        for (String day : dayAbbrs) {
            ToggleButton dayBtn = (ToggleButton) formContainer.lookup("#day-" + day);
            if (dayBtn != null && dayBtn.isSelected()) {
                List<TimeSlot> slotsForDay = serviceDaysConfig.get(day);
                List<TimeSlot> definedSlots = slotsForDay.stream()
                        .filter(slot -> !slot.start().isEmpty() && !slot.end().isEmpty())
                        .collect(Collectors.toList());
                if (!definedSlots.isEmpty()) {
                    finalServiceDays.put(day, definedSlots);
                    totalDefinedSlots += definedSlots.size();
                }
            }
        }

        if (totalDefinedSlots == 0) {
            showAlert("Validation Error", "Please configure at least one session time for your selected days.");
            return;
        }

        // MODIFIED: Instantiate ServiceConfig with new staff fields
        ServiceConfig newSession = new ServiceConfig(0, serviceName, numCustomers, firstName, lastName, role, description, true, finalServiceDays);

        try {
    int serviceId = ServiceDAO.saveService(newSession, this.companyId);
    System.out.println("✅ Saved to DB with ID: " + serviceId);

    Alert success = new Alert(Alert.AlertType.INFORMATION);
    success.setTitle("Success");
    success.setHeaderText(null);
    success.setContentText("Service saved successfully to the database!");
    reloadActiveServicesPage();
    success.showAndWait();

} catch (SQLException e) {
    e.printStackTrace();
    showAlert("Database Error", "Failed to save service to database: " + e.getMessage());
    return;
}


        activeServices.add(0, newSession);
        renderSessionCard(newSession);

        // Hide and reset form: find toggle button in parent header
        VBox parentVBox = (VBox) formContainer.getParent();
        HBox toggleHeader = (HBox) parentVBox.getChildren().get(1);
        Button toggleButton = (Button) toggleHeader.getChildren().get(0);
        toggleForm(toggleButton);
    }

    /** Renders a single saved session card. */
    private VBox renderSessionCard(ServiceConfig session) {
        VBox card = new VBox(5);
        card.getStyleClass().add("session-card");

        HBox header = new HBox(10);
        header.setAlignment(Pos.TOP_LEFT);

        VBox summary = new VBox(2);
        Label title = new Label(session.serviceName());
        title.getStyleClass().add("session-title");
        title.setFont(Font.font("Inter", FontWeight.BOLD, 16));

        // MODIFIED: Display full provider name (First Name + Last Name) and Role
        String providerDisplay = session.firstName() + " " + session.lastName() + 
                                 (session.role().isEmpty() ? "" : " (" + session.role() + ")");
        Label provider = new Label("Provider: " + providerDisplay);
        provider.getStyleClass().add("session-provider");
        provider.setFont(Font.font("Inter", FontWeight.SEMI_BOLD, 14)); 
        provider.setStyle("-fx-text-fill: #4b5563;");

        Label descriptionLabel = new Label(session.description());
        descriptionLabel.setWrapText(true); // This method works on Label!
        descriptionLabel.setFont(Font.font("Inter", FontWeight.NORMAL, 13)); 
        descriptionLabel.setStyle("-fx-text-fill: #374151;");

        summary.getChildren().addAll(title, descriptionLabel, provider);
        summary.setPrefWidth(400);

        Button toggleBtn = new Button("▶");
        toggleBtn.getStyleClass().add("detail-toggle");

        TextArea detailsArea = new TextArea();
        detailsArea.setText(formatSessionDetails(session));
        detailsArea.setEditable(false);
        detailsArea.setWrapText(true);
        detailsArea.setPrefHeight(150);
        detailsArea.setVisible(false);
        detailsArea.setManaged(false);
        detailsArea.getStyleClass().add("details-area");

        toggleBtn.setOnAction(e -> {
            boolean isVisible = detailsArea.isVisible();
            detailsArea.setVisible(!isVisible);
            detailsArea.setManaged(!isVisible);
            toggleBtn.setText(isVisible ? "▶" : "▼");
        });

        header.getChildren().addAll(summary, new Region(), toggleBtn);
        HBox.setHgrow(summary, Priority.ALWAYS);

        Label timestamp = new Label(LocalDateTime.now().format(DateTimeFormatter.ofPattern("M/d/yy, h:mm:ss a")));
        timestamp.getStyleClass().add("session-timestamp");
        HBox timestampBox = new HBox(timestamp);
        timestampBox.setAlignment(Pos.BOTTOM_RIGHT);

        card.getChildren().addAll(header, detailsArea, timestampBox);
        return card;
    }

    /** Formats session details text. */
    private String formatSessionDetails(ServiceConfig session) {
        StringBuilder sb = new StringBuilder();
        
        // MODIFIED: Display new staff fields
        sb.append("Provider: ").append(session.firstName()).append(" ").append(session.lastName());
        sb.append(" (Role: ").append(session.role()).append(")\n");
        
        sb.append("Customers: ").append(session.numCustomers()).append("\n");

        List<String> activeDays = session.serviceDays().keySet().stream()
                .map(dayFullNames::get)
                .collect(Collectors.toList());

        sb.append("Days Active: ").append(String.join(", ", activeDays)).append("\n");
        sb.append("--- Session Details ---\n");

        for (String dayAbbr : dayAbbrs) {
            if (session.serviceDays().containsKey(dayAbbr)) {
                String dayName = dayFullNames.get(dayAbbr);
                List<TimeSlot> slots = session.serviceDays().get(dayAbbr);

                sb.append("- ").append(dayName).append(" (").append(slots.size()).append(" Sessions):\n");
                for (TimeSlot slot : slots) {
                sb.append(" - ").append(slot.start()).append(" - ").append(slot.end()).append("\n");                }
            }
        }
        return sb.toString().trim();
    }

    /** Resets form fields and internal state. */
    private void resetForm() {
        GridPane mainInputs = (GridPane) formContainer.getChildren().get(0);
        
        // MODIFIED: Reset all five main text fields
        ((TextField) mainInputs.getChildren().get(1)).setText(""); // Service Name
        ((TextField) mainInputs.getChildren().get(3)).setText(""); // Num Customers
        ((TextField) mainInputs.getChildren().get(5)).setText(""); // First Name
        ((TextField) mainInputs.getChildren().get(7)).setText(""); // Last Name
        ((TextField) mainInputs.getChildren().get(9)).setText(""); // Role
        
        VBox descriptionBox = (VBox) formContainer.getChildren().get(3);
        ((TextArea) descriptionBox.getChildren().get(1)).setText("");

        VBox daysSection = (VBox) formContainer.getChildren().get(1);
        HBox dayButtons = (HBox) daysSection.getChildren().get(1);
        dayButtons.getChildren().stream()
                .filter(node -> node instanceof ToggleButton)
                .map(node -> (ToggleButton) node)
                .forEach(btn -> {
                    btn.setSelected(false);
                    btn.getStyleClass().remove("day-selected");
                });

        for (String day : dayAbbrs) {
            serviceDaysConfig.put(day, new ArrayList<>());
        }
        currentSelectedDay = null;
        currentSelectedSlotIndex = -1;
        dayConfigSection.setVisible(false);
        dayConfigSection.setManaged(false);
        if (maxSessionsSpinner != null) maxSessionsSpinner.getValueFactory().setValue(1);
    }

    /** Simple error alert. */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ServiceSessionManager.java (Add this new helper method)

/**
 * Creates the VBox containing all rendered ServiceCards for the given list of services.
 * This replaces the rendering behavior previously scattered across loadSampleSessions 
 * and renderSessionCard.
 */
public VBox createSessionsSection(List<ServiceConfig> services) {
    // 1. FILTERING STEP: Create a new list containing only active services
    List<ServiceConfig> activeServices = services.stream()
        .filter(ServiceConfig::isActive) // Keeps only services where isActive is true
        .collect(Collectors.toList());

    VBox sessionsContainer = new VBox(20); // 20 is spacing between cards
    sessionsContainer.setPadding(new Insets(10));

    // Check if there are any active services after filtering
    if (activeServices.isEmpty()) {
        Label noServices = new Label("No active services are currently configured.");
        noServices.setFont(Font.font("Inter", FontWeight.BOLD, 18));
        sessionsContainer.getChildren().add(noServices);
        return sessionsContainer;
    }

    // 2. ITERATE: Loop through the FILTERED list in reverse order
    for (int i = activeServices.size() - 1; i >= 0; i--) {
        ServiceConfig service = activeServices.get(i);
        
        // Call your existing card rendering method
        VBox card = renderSessionCard(service); 
        sessionsContainer.getChildren().add(card); 
    }
    
    return sessionsContainer;
}
    // ServiceSessionManager.java (Add this static method)

/** Loads active services from the DB using the ServiceDAO. */
private static List<ServiceConfig> loadActiveServices(int companyId) {
    try {
        // CALL YOUR NEW DAO METHOD HERE
        return ServiceDAO.getActiveServicesByCompanyId(companyId); 
    } catch (SQLException e) {
        // If the DB call fails (e.g., connection error), log it and return an empty list
        System.err.println("Failed to load services from DB: " + e.getMessage());
        e.printStackTrace();
        return Collections.emptyList();
    }
}

// ServiceSessionManager.java

/**
 * Fetches the latest service data from the database and updates the UI.
 * This should be called after adding, editing, or deleting a service.
 */
public void reloadActiveServicesPage() {
    // 1. Fetch the latest data from the database
    List<ServiceConfig> latestServices = loadActiveServices(this.companyId);

    // 2. Update the application's main state (sessions)
    this.sessions = latestServices;

    // 3. Create a brand new, fully rendered VBox of service cards
    VBox newSessionsSection = createSessionsSection(this.sessions);

    // 4. Update the global UI reference
    // This is vital: it ensures that when you click 'Active Services', 
    // you see the latest data.
    this.sessionsSection = newSessionsSection; 
    
    // Optional: If you are currently viewing the Active Services page, 
    // update the actual scene content immediately.
    // Assuming 'appContent' is the container where sections are displayed.
    // If you don't have access to 'appContent' here, you must ensure the 
    // button action that switches to 'Active Services' uses this.sessionsSection.
    // (This part depends on your exact navigation setup, but this.sessionsSection 
    // is the key element being updated).
}

/**
 * Creates the VBox for the 'Edit Services' page, showing a list of services
 * with 'Edit' buttons.
 */
private VBox createEditServiceSelectionPage() {
    VBox selectionContainer = new VBox(15);
    selectionContainer.setPadding(new Insets(10));
    Label editHeader = new Label("Select a Service to Edit");
    editHeader.setFont(Font.font("Inter", FontWeight.BOLD, 20));
    selectionContainer.getChildren().add(editHeader);

    // Assuming 'this.sessions' is a class member holding List<ServiceConfig>
    if (this.sessions == null || this.sessions.isEmpty()) {
        selectionContainer.getChildren().add(new Label("No active services to edit."));
        return selectionContainer;
    }

    for (ServiceConfig service : this.sessions) {
        HBox serviceRow = new HBox(10);
        serviceRow.setAlignment(Pos.CENTER_LEFT);
        serviceRow.setPadding(new Insets(10));
        serviceRow.setStyle("-fx-border-color: #d1d5db; -fx-border-width: 0 0 1px 0;");
        HBox.setHgrow(serviceRow, Priority.ALWAYS);

        Label serviceName = new Label(service.serviceName());
        serviceName.setFont(Font.font("Inter", FontWeight.BOLD, 16));
        serviceName.setMinWidth(200);

        Label provider = new Label("Provider: " + service.firstName() + " " + service.lastName());
        provider.setFont(Font.font("Inter", FontWeight.SEMI_BOLD, 14));
        provider.setStyle("-fx-text-fill: #6b7280;");        
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("edit-button"); 

        // CRITICAL ACTION: Switches to the form and loads data
        editBtn.setOnAction(e -> {
            // 1. Switch the view to the form container
            if (this.appContent != null) {
                this.appContent.getChildren().setAll(this.formContainer);
            }
            // 2. Load the data into the now visible form
            loadEditForm(service);
        }); 

        Button disableReactivateBtn;

if (service.isActive()) {
    disableReactivateBtn = new Button("Disable");
    disableReactivateBtn.setStyle("-fx-background-color: #f3f4f6; -fx-text-fill: #1f2937;"); 
    disableReactivateBtn.setOnAction(e -> disableService(service)); 
} else {
    disableReactivateBtn = new Button("Reactivate");
    // Use a prominent color to highlight that it's inactive/needs attention
    disableReactivateBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white;"); 
    disableReactivateBtn.setOnAction(e -> reactivateService(service)); 
}
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("delete-button"); 
        deleteBtn.setStyle("-fx-background-color: #dc2626; -fx-text-fill: white;");
        
        // Action: Call the delete method
        deleteBtn.setOnAction(e -> deleteService(service));

        serviceRow.getChildren().addAll(serviceName, provider, spacer, deleteBtn, disableReactivateBtn, editBtn);
        selectionContainer.getChildren().add(serviceRow);
    }

    return selectionContainer;
}

/**
 * Loads the existing form with the data of the specified ServiceConfig
 * and switches the save button action to updateService.
 */
private void loadEditForm(ServiceConfig serviceToEdit) {
    // 1. Reset all temporary state and form fields
    resetForm(); 

    // --- Retrieve form fields via lookup on the class member 'formContainer' ---
    // This is necessary because the fields are local variables in createFormContainer.
    GridPane mainInputs = (GridPane) formContainer.getChildren().get(0);
    VBox descriptionBox = (VBox) formContainer.getChildren().get(3);
    Button saveButton = (Button) formContainer.getChildren().get(4);
    VBox daysSection = (VBox) formContainer.getChildren().get(1);

    // Get the actual field references (index based on your createFormContainer structure)
    TextField serviceNameField = (TextField) mainInputs.getChildren().get(1);
    TextField numCustomersField = (TextField) mainInputs.getChildren().get(3);
    TextField firstNameField = (TextField) mainInputs.getChildren().get(5);
    TextField lastNameField = (TextField) mainInputs.getChildren().get(7);
    TextField roleField = (TextField) mainInputs.getChildren().get(9);
    TextArea descriptionArea = (TextArea) descriptionBox.getChildren().get(1);
    
    // 2. Populate Form Fields
    serviceNameField.setText(serviceToEdit.serviceName());
    numCustomersField.setText(String.valueOf(serviceToEdit.numCustomers()));
    firstNameField.setText(serviceToEdit.firstName());
    lastNameField.setText(serviceToEdit.lastName());
    roleField.setText(serviceToEdit.role());
    descriptionArea.setText(serviceToEdit.description());

    // 3. Set Day Toggles and Slots (Using internal state: serviceDaysConfig)
    HBox dayButtons = (HBox) daysSection.getChildren().get(1);
    String firstSelectedDay = null;
    
    // Load config data into temporary map and select days
    for (String day : dayAbbrs) {
        ToggleButton dayBtn = (ToggleButton) dayButtons.lookup("#day-" + day);
        if (serviceToEdit.serviceDays().containsKey(day)) {
            dayBtn.setSelected(true);
            dayBtn.getStyleClass().add("day-selected");
            List<TimeSlot> slots = serviceToEdit.serviceDays().get(day);
            serviceDaysConfig.put(day, new ArrayList<>(slots));
            if (firstSelectedDay == null) firstSelectedDay = day;
        } else {
            dayBtn.setSelected(false);
            dayBtn.getStyleClass().remove("day-selected");
            serviceDaysConfig.put(day, new ArrayList<>());
        }
    }

    // Load day config panel for the first selected day (Assuming dayConfigSection is a class member)
    if (firstSelectedDay != null) {
        currentSelectedDay = firstSelectedDay;
        dayConfigSection.setVisible(true);
        dayConfigSection.setManaged(true);
        List<TimeSlot> slotsForDay = serviceDaysConfig.get(currentSelectedDay);
        // Assuming maxSessionsSpinner is a class member
        maxSessionsSpinner.getValueFactory().setValue(slotsForDay.size() > 0 ? slotsForDay.size() : 1);
        currentSelectedSlotIndex = slotsForDay.size() > 0 ? 0 : -1;
        renderSessionSelectors(maxSessionsSpinner.getValue());
    } else {
        dayConfigSection.setVisible(false);
        dayConfigSection.setManaged(false);
    }

    // 4. Change Save Button Action and Text to UPDATE
    saveButton.setText("Update Service Configuration");
    saveButton.setOnAction(null);

    // Wire the update logic, passing the original service and all fields
    saveButton.setOnAction(e -> updateService(serviceToEdit, serviceNameField, numCustomersField, firstNameField, lastNameField, roleField, descriptionArea, saveButton));
}

/** Validates input, updates the database, and resets the form state. */
private void updateService(ServiceConfig originalService, TextField serviceNameField, TextField numCustomersField, TextField firstNameField, TextField lastNameField, TextField roleField, TextArea descriptionArea, Button saveButton) {
    if (currentSelectedDay != null) updateTimeForSelectedSlot(true);

    // 1. Retrieve data
    String serviceName = serviceNameField.getText().trim();
    String numCustomersText = numCustomersField.getText().trim();
    String firstName = firstNameField.getText().trim();
    String lastName = lastNameField.getText().trim();
    String role = roleField.getText().trim();
    String description = descriptionArea.getText().trim();

    // 2. Validation (Same as processInput)
    if (serviceName.isEmpty() || numCustomersText.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || role.isEmpty() || description.isEmpty()) {
        showAlert("Validation Error", "Please fill in all required fields.");
        return;
    }
    int numCustomers;
    try {
        numCustomers = Integer.parseInt(numCustomersText);
    } catch (NumberFormatException e) {
        showAlert("Validation Error", "Number of Customers must be a valid number.");
        return;
    }

    // 3. Build the updated days map
    Map<String, List<TimeSlot>> finalServiceDays = new HashMap<>();
    int totalDefinedSlots = 0;
    
    // Retrieve day buttons from the formContainer structure
    VBox daysSection = (VBox) formContainer.getChildren().get(1);
    HBox dayButtons = (HBox) daysSection.getChildren().get(1);

    for (String day : dayAbbrs) {
        ToggleButton dayBtn = (ToggleButton) dayButtons.lookup("#day-" + day);
        if (dayBtn != null && dayBtn.isSelected()) {
            List<TimeSlot> slotsForDay = serviceDaysConfig.get(day);
            List<TimeSlot> definedSlots = slotsForDay.stream()
                    .filter(slot -> !slot.start().isEmpty() && !slot.end().isEmpty())
                    .collect(Collectors.toList());
            if (!definedSlots.isEmpty()) {
                finalServiceDays.put(day, definedSlots);
                totalDefinedSlots += definedSlots.size();
            }
        }
    }

    if (totalDefinedSlots == 0) {
        showAlert("Validation Error", "Please configure at least one session time for your selected days.");
        return;
    }

    // 4. Create the UPDATED ServiceConfig
    ServiceConfig updatedSession = new ServiceConfig(originalService.serviceId, serviceName, numCustomers, firstName, lastName, role, description, originalService.isActive(), finalServiceDays);

    try {
        // !!! ASSUMPTION: ServiceDAO.updateService(ServiceConfig updated, int originalId, int companyId) is implemented
        // Since we don't know the ID, we rely on the DAO to find it using originalService
        ServiceDAO.updateService(updatedSession, originalService.serviceId(), this.companyId); 

        // 5. Success actions
        reloadActiveServicesPage(); // Refreshes the active services list

        showAlert("Success", "Service updated successfully!");

    } catch (Exception ex) {
        ex.printStackTrace();
        showAlert("Database Error", "Failed to update service: " + ex.getMessage());
        return;
    }
    
    // 6. Reset form state and button actions
    resetForm();
    saveButton.setText("Save New Service");
    // Restore the original 'Save New Service' action
    saveButton.setOnAction(e -> processInput(serviceNameField, numCustomersField, firstNameField, lastNameField, roleField, descriptionArea));

    // Hide and reset form (toggle it closed)
    VBox parentVBox = (VBox) formContainer.getParent();
    HBox toggleHeader = (HBox) parentVBox.getChildren().get(1); 
    Button toggleButton = (Button) toggleHeader.getChildren().get(0);
    toggleForm(toggleButton);
}

// ServiceSessionManager.java

/**
 * Handles confirmation and logic to disable a service.
 */
private void disableService(ServiceConfig service) {
    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
    confirmation.setTitle("Confirm Service Deactivation");
    confirmation.setHeaderText("Disable Service: " + service.serviceName());
    confirmation.setContentText("Are you sure you want to disable this service? It will no longer appear in the active list.");

    Optional<ButtonType> result = confirmation.showAndWait();

    if (result.isPresent() && result.get() == ButtonType.OK) {
        try {
            // Call the new DAO method
            ServiceDAO.disableService(service.serviceId(), this.companyId); 
            
            showAlert("Success", "Service '" + service.serviceName() + "' has been successfully disabled.");

            // Reload the UI to reflect the change
            reloadActiveServicesPage(); 

            // Re-render the selection page to show the updated list
            VBox newEditSelectionPage = createEditServiceSelectionPage();
            
            // Assuming appContent is the container where sections are displayed
            if (this.appContent != null) {
                this.appContent.getChildren().setAll(newEditSelectionPage);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to disable service: " + e.getMessage());
        }
    }
}

// ServiceSessionManager.java

/**
 * Handles confirmation and logic to permanently delete a service.
 */
private void deleteService(ServiceConfig service) {
    Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
    confirmation.setTitle("Confirm Service DELETION");
    confirmation.setHeaderText("PERMANENTLY Delete Service: " + service.serviceName());
    confirmation.setContentText("WARNING: This action is irreversible. All service data and associated time slot configurations will be permanently removed. Continue?");

    Optional<ButtonType> result = confirmation.showAndWait();

    if (result.isPresent() && result.get() == ButtonType.OK) {
        try {
            // Call the new DAO method
            ServiceDAO.deleteService(service.serviceId(), this.companyId); 
            
            showAlert("Success", "Service '" + service.serviceName() + "' has been PERMANENTLY deleted.");

            // Reload the UI to reflect the change
            reloadActiveServicesPage(); 

            // Re-render the selection page to show the updated list
            VBox newEditSelectionPage = createEditServiceSelectionPage();
            
            // Switch the view back to the updated selection page
            if (this.appContent != null) {
                this.appContent.getChildren().setAll(newEditSelectionPage);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to delete service: " + e.getMessage());
        }
    }
}

private void reactivateService(ServiceConfig service) {
    try {
        ServiceDAO.reactivateService(service.serviceId(), this.companyId); 
        showAlert("Success", "Service '" + service.serviceName() + "' has been successfully REACTIVATED.");
        
        // Refresh the page
        reloadActiveServicesPage(); 
        VBox newEditSelectionPage = createEditServiceSelectionPage();
        if (this.appContent != null) {
            this.appContent.getChildren().setAll(newEditSelectionPage);
        }
    } catch (SQLException e) {
        e.printStackTrace();
        showAlert("Database Error", "Failed to reactivate service: " + e.getMessage());
    }
}
}