package company;

import db.DBHelper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public List<Booking> getBookingsByCompanyId(int companyId) {
        List<Booking> bookings = new ArrayList<>();

        String sql = "SELECT booking_id, customer_name, service_name, booking_date, slot_time " +
                     "FROM bookings WHERE company_id = ?";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, companyId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Booking booking = new Booking(
                        rs.getInt("booking_id"),
                        rs.getString("customer_name"),
                        rs.getString("service_name"),
                        rs.getString("booking_date"),
                        rs.getString("slot_time")
                );
                bookings.add(booking);
            }
            // Log for terminal
            System.out.println("=== DAO DEBUG: Fetched " + bookings.size() + " bookings for companyId=" + companyId + " ===");
            if (bookings.isEmpty()) {
                System.out.println("=== DAO: No matches – verify DB data for this ID ===");
            } else {
                // Bonus: Print first row for sanity
                System.out.println("=== DAO Sample: " + bookings.get(0).getCustomerName() + " on " + bookings.get(0).getBookingDate() + " ===");
            }

        } catch (SQLException e) {
            System.err.println("=== DAO SQL FAIL: " + e.getMessage() + " (companyId=" + companyId + ") ===");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("=== DAO GENERAL FAIL: " + e.getMessage() + " ===");
            e.printStackTrace();
        }

        return bookings;
    }
}