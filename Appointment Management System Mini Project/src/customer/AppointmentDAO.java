package customer;

import db.DBHelper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AppointmentDAO {

    // 1️⃣ Get all company names
    public ObservableList<String> getCompanies() {
        ObservableList<String> companies = FXCollections.observableArrayList();
        String sql = "SELECT company_name FROM company_profiles";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                companies.add(rs.getString("company_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return companies;
    }

    // 2️⃣ Get services by company
    public ObservableList<String> getServicesByCompany(String company) {
        ObservableList<String> services = FXCollections.observableArrayList();
        String sql = "SELECT s.service_name FROM services s "
                   + "JOIN company_profiles c ON s.company_id = c.company_id "
                   + "WHERE c.company_name = ?";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, company);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                services.add(rs.getString("service_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return services;
    }

    // 3️⃣ Get service details
    public Service getServiceDetails(String service) {
        String sql = "SELECT s.service_name, CONCAT(st.first_name, ' ', st.last_name) AS provider_name, "
                   + "s.num_customers, s.description "
                   + "FROM services s "
                   + "JOIN staff st ON s.staff_id = st.staff_id "
                   + "WHERE s.service_name = ?";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, service);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Service(
                    rs.getString("service_name"),
                    rs.getString("provider_name"),
                    rs.getInt("num_customers"),
                    rs.getString("description")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 4️⃣ Get company details
    public Company getCompanyDetails(String companyName) {
        String sql = "SELECT * FROM company_profiles WHERE company_name = ?";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, companyName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Company(
                    rs.getString("owner_name"),
                    rs.getString("company_name"),
                    rs.getString("business_type"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getString("website"),
                    rs.getString("start_time") + " - " + rs.getString("end_time"),
                    rs.getString("off_days"),
                    rs.getString("address")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 5️⃣ Get time slots by staff and day
    public List<String> getSlots(String serviceName, String day) {
        List<String> slots = new ArrayList<>();

        String sql = "SELECT ts.start_time, ts.end_time FROM time_slots ts "
                   + "JOIN service_days sd ON ts.service_day_id = sd.service_day_id "
                   + "JOIN services s ON sd.service_id = s.service_id "
                   + "JOIN staff st ON s.staff_id = st.staff_id "
                   + "WHERE s.service_name = ? AND sd.day_abbr = ?";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, serviceName);
            stmt.setString(2, day);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                slots.add(rs.getString("start_time") + " - " + rs.getString("end_time"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return slots;
    }

    // 1️⃣ Get company ID by name
    public int getCompanyId(String companyName) {
        String sql = "SELECT company_id FROM company_profiles WHERE company_name = ?";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, companyName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("company_id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // Return -1 or throw an exception if not found
    }

    public boolean saveBooking(Booking b, int company_id) {
        String sql = "INSERT INTO bookings (company_id, company_name, service_name, day, slot_time, customer_name, phone, email, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBHelper.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, company_id);
            stmt.setString(2, b.getCompany());
            stmt.setString(3, b.getService());
            stmt.setString(4, b.getDay());
            stmt.setString(5, b.getSlot());
            stmt.setString(6, b.getName());
            stmt.setString(7, b.getPhone());
            stmt.setString(8, b.getEmail());
            stmt.setString(9, b.getNotes());

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ✅ Check if seats available for a given service, day & slot
    public boolean isSlotAvailable(String service, String day, String slot) {
        String capacitySql = "SELECT num_customers FROM services WHERE service_name = ?";
        String bookingSql = "SELECT COUNT(*) AS total FROM bookings WHERE service_name = ? AND day = ? AND slot_time = ?";

        try (Connection conn = DBHelper.getConnection()) {

            // 1️⃣ Get capacity
            int capacity = 0;
            try (PreparedStatement stmt = conn.prepareStatement(capacitySql)) {
                stmt.setString(1, service);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) capacity = rs.getInt("num_customers");
            }

            // 2️⃣ Count bookings
            int booked = 0;
            try (PreparedStatement stmt2 = conn.prepareStatement(bookingSql)) {
                stmt2.setString(1, service);
                stmt2.setString(2, day);
                stmt2.setString(3, slot);
                ResultSet rs2 = stmt2.executeQuery();
                if (rs2.next()) booked = rs2.getInt("total");
            }

            return booked < capacity; // ✅ True means slot available

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
