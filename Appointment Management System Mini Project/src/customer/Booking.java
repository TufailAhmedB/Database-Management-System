package customer;

public class Booking {
    private String company;
    private String service;
    private String day;
    private String slot;
    private String name;
    private String phone;
    private String email;
    private String notes;

    public Booking(String company, String service, String day, String slot,
                   String name, String phone, String email, String notes) {
        this.company = company;
        this.service = service;
        this.day = day;
        this.slot = slot;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.notes = notes;
    }

    public String getCompany() { return company; }
    public String getService() { return service; }
    public String getDay() { return day; }
    public String getSlot() { return slot; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getNotes() { return notes; }
}
