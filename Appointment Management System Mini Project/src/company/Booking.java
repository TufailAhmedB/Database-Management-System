package company;

public class Booking {
    private int bookingId;
    private String customerName;
    private String serviceName;
    private String bookingDate;
    private String timeSlot;

    public Booking(int bookingId, String customerName, String serviceName, String bookingDate, String timeSlot) {
        this.bookingId = bookingId;
        this.customerName = customerName;
        this.serviceName = serviceName;
        this.bookingDate = bookingDate;
        this.timeSlot = timeSlot;
    }

    public int getBookingId() { return bookingId; }
    public String getCustomerName() { return customerName; }
    public String getServiceName() { return serviceName; }
    public String getBookingDate() { return bookingDate; }
    public String getTimeSlot() { return timeSlot; }
}
