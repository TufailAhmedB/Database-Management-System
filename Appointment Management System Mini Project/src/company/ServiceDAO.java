package company;

import db.DBHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceDAO {

    /** Saves a service and returns the generated service_id */
    public static int saveService(ServiceSessionManager.ServiceConfig service, int companyId) throws SQLException {

        String insertService = "INSERT INTO services (service_name, num_customers, description, company_id, staff_id) VALUES (?, ?, ?, ?, ?)";
        String insertServiceDay = "INSERT INTO service_days (service_id, day_abbr) VALUES (?, ?)";
        String insertTimeSlot = "INSERT INTO time_slots (service_day_id, start_time, end_time) VALUES (?, ?, ?)";

        Connection conn = null;
        PreparedStatement psService = null;
        PreparedStatement psDay = null;
        PreparedStatement psSlot = null;
        ResultSet rs = null;

        try {
            conn = DBHelper.getConnection();
            conn.setAutoCommit(false);

            // ✅ Step 1: Find/Create staff properly
            int staffId = findOrCreateStaff(conn, companyId, service.firstName(), service.lastName(), service.role());

            // ✅ Step 2: Insert service
            psService = conn.prepareStatement(insertService, PreparedStatement.RETURN_GENERATED_KEYS);
            psService.setString(1, service.serviceName());
            psService.setInt(2, service.numCustomers());
            psService.setString(3, service.description());
            psService.setInt(4, companyId);          
            psService.setInt(5, staffId);
            psService.executeUpdate();

            rs = psService.getGeneratedKeys();
            int serviceId = -1;
            if (rs.next()) serviceId = rs.getInt(1);
            else throw new SQLException("Failed to retrieve service_id");

            // ✅ Step 3: Insert service days & slots
            psDay = conn.prepareStatement(insertServiceDay, PreparedStatement.RETURN_GENERATED_KEYS);
            psSlot = conn.prepareStatement(insertTimeSlot);

            for (Map.Entry<String, List<ServiceSessionManager.TimeSlot>> entry : service.serviceDays().entrySet()) {
                psDay.setInt(1, serviceId);
                psDay.setString(2, entry.getKey());
                psDay.executeUpdate();

                try (ResultSet rsDay = psDay.getGeneratedKeys()) {
                    if (rsDay.next()) {
                        int serviceDayId = rsDay.getInt(1);

                        for (ServiceSessionManager.TimeSlot slot : entry.getValue()) {
                            psSlot.setInt(1, serviceDayId);
                            psSlot.setString(2, slot.start());
                            psSlot.setString(3, slot.end());
                            psSlot.addBatch();
                        }
                        psSlot.executeBatch();
                    }
                }
            }

            conn.commit();
            System.out.println("✅ Service saved: " + service.serviceName());
            return serviceId;

        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            DBHelper.close(conn, psService, rs);
            DBHelper.close(null, psDay, null);
            DBHelper.close(null, psSlot, null);
        }
    }

    // ✅ Helper function to ensure staff is created if not exists
    private static int findOrCreateStaff(Connection conn, int companyId, String first, String last, String role) throws SQLException {
        String find = "SELECT staff_id FROM staff WHERE first_name = ? AND last_name = ? AND role = ?";
        String insert = "INSERT INTO staff (company_id, first_name, last_name, role) VALUES (?, ?, ?, ?)";

        try (PreparedStatement psFind = conn.prepareStatement(find)) {
            psFind.setString(1, first);
            psFind.setString(2, last);
            psFind.setString(3, role);

            try (ResultSet rs = psFind.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }

        try (PreparedStatement psInsert = conn.prepareStatement(insert, PreparedStatement.RETURN_GENERATED_KEYS)) {
            psInsert.setInt(1, companyId);
            psInsert.setString(2, first);
            psInsert.setString(3, last);
            psInsert.setString(4, role);
            psInsert.executeUpdate();

            try (ResultSet rs = psInsert.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }

        throw new SQLException("Failed to create staff");
    }
    // ServiceDAO.java (Add this to your existing class)

/** Fetches all active services for a given company, including staff and all time slots. */
public static List<ServiceSessionManager.ServiceConfig> getActiveServicesByCompanyId(int companyId) throws SQLException {
    List<ServiceSessionManager.ServiceConfig> activeServices = new ArrayList<>();
    
    // Query joins services and staff tables
    String sql = "SELECT " +
                 "s.service_id, s.service_name, s.num_customers, s.description, s.is_active, " +
                 "st.first_name, st.last_name, st.role " +
                 "FROM services s " +
                 "JOIN staff st ON s.staff_id = st.staff_id " +
                 "WHERE s.company_id = ?"; // Assuming 'is_active' column

    Connection conn = null;
    try {
        conn = DBHelper.getConnection();
        conn.setAutoCommit(false); // Use transaction scope for safety and efficiency
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, companyId);
            try (ResultSet rs = ps.executeQuery()) {
                
                while (rs.next()) {
                    int serviceId = rs.getInt("service_id");
                    boolean isActive = rs.getBoolean("is_active");
                    // Call the helper to fetch the complex nested Map data
                    Map<String, List<ServiceSessionManager.TimeSlot>> serviceDays = fetchTimeSlotsForService(serviceId, conn);

                    // Reconstruct the full ServiceConfig object
                    ServiceSessionManager.ServiceConfig service = new ServiceSessionManager.ServiceConfig(
                        serviceId,
                        rs.getString("service_name"),
                        rs.getInt("num_customers"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("role"),
                        rs.getString("description"),
                        isActive,
                        serviceDays
                    );
                    activeServices.add(service);
                }
            }
        }
        conn.commit();
        return activeServices;
    } catch (SQLException e) {
        if (conn != null) conn.rollback();
        throw e;
    } finally {
        DBHelper.close(conn, null, null);
    }
}
// ServiceDAO.java (Add this private helper method)

/** Fetches all time slots for a single service ID. */
private static Map<String, List<ServiceSessionManager.TimeSlot>> fetchTimeSlotsForService(int serviceId, Connection conn) throws SQLException {
    // Map: day_abbr -> List of TimeSlot objects
    Map<String, List<ServiceSessionManager.TimeSlot>> serviceDays = new HashMap<>();

    // Query joins service_days and time_slots tables
    String sql = "SELECT sd.day_abbr, ts.start_time, ts.end_time " +
                 "FROM service_days sd " +
                 "JOIN time_slots ts ON sd.service_day_id = ts.service_day_id " +
                 "WHERE sd.service_id = ?";
    
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, serviceId);
        try (ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                String dayAbbr = rs.getString("day_abbr");
                
                ServiceSessionManager.TimeSlot slot = new ServiceSessionManager.TimeSlot(
                    rs.getString("start_time"),
                    rs.getString("end_time")
                );
                
                // Group the slots by day abbreviation
                serviceDays.computeIfAbsent(dayAbbr, k -> new ArrayList<>()).add(slot);
            }
        }
    }
    return serviceDays;
}

// ServiceDAO.java

/**
 * Updates an existing service configuration in the database.
 * This operation is wrapped in a transaction (Update, Delete, Insert).
 */
public static void updateService(
    ServiceSessionManager.ServiceConfig updatedService, 
    int serviceId, 
    int companyId) throws SQLException 
{
    // SQL for main service update
    String updateServiceSql = "UPDATE services SET service_name = ?, num_customers = ?, description = ?, staff_id = ? WHERE service_id = ? AND company_id = ?";
    
    // SQL for clearing old days/slots (Assuming time_slots references service_days)
    String deleteTimeSlotsSql = "DELETE FROM time_slots WHERE service_day_id IN (SELECT service_day_id FROM service_days WHERE service_id = ?)";
    String deleteServiceDaysSql = "DELETE FROM service_days WHERE service_id = ?";
    
    // SQL for inserting new days/slots (re-using logic from saveService)
    String insertServiceDay = "INSERT INTO service_days (service_id, day_abbr) VALUES (?, ?)";
    String insertTimeSlot = "INSERT INTO time_slots (service_day_id, start_time, end_time) VALUES (?, ?, ?)";

    Connection conn = null;

    try {
        conn = DBHelper.getConnection();
        conn.setAutoCommit(false); // Start transaction

        // 1. Find or create staff for the potentially new provider details
        // Re-using the existing helper method
        int staffId = findOrCreateStaff(conn, companyId, updatedService.firstName(), updatedService.lastName(), updatedService.role());

        // 2. Update main service details
        try (PreparedStatement psUpdate = conn.prepareStatement(updateServiceSql)) {
            psUpdate.setString(1, updatedService.serviceName());
            psUpdate.setInt(2, updatedService.numCustomers());
            psUpdate.setString(3, updatedService.description());
            psUpdate.setInt(4, staffId); 
            psUpdate.setInt(5, serviceId); // Key identifier for the row to update
            psUpdate.setInt(6, companyId);
            
            if (psUpdate.executeUpdate() == 0) {
                throw new SQLException("Update failed: Service ID " + serviceId + " not found or company ID mismatch.");
            }
        }

        // 3. Delete old time slots and service days (critical to clear existing schedule)
        try (PreparedStatement psDeleteSlots = conn.prepareStatement(deleteTimeSlotsSql)) {
            psDeleteSlots.setInt(1, serviceId);
            psDeleteSlots.executeUpdate();
        }
        
        try (PreparedStatement psDeleteDays = conn.prepareStatement(deleteServiceDaysSql)) {
            psDeleteDays.setInt(1, serviceId);
            psDeleteDays.executeUpdate();
        }

        // 4. Insert the new service days and time slots
        try (PreparedStatement psDay = conn.prepareStatement(insertServiceDay, PreparedStatement.RETURN_GENERATED_KEYS);
             PreparedStatement psSlot = conn.prepareStatement(insertTimeSlot)) 
        {
            for (Map.Entry<String, List<ServiceSessionManager.TimeSlot>> entry : updatedService.serviceDays().entrySet()) {
                String dayAbbr = entry.getKey();
                List<ServiceSessionManager.TimeSlot> slots = entry.getValue();

                // Insert into service_days
                psDay.setInt(1, serviceId);
                psDay.setString(2, dayAbbr);
                psDay.executeUpdate();

                // Get the generated service_day_id
                try (ResultSet rsDay = psDay.getGeneratedKeys()) {
                    if (rsDay.next()) {
                        int serviceDayId = rsDay.getInt(1);

                        // Insert into time_slots
                        for (ServiceSessionManager.TimeSlot slot : slots) {
                            psSlot.setInt(1, serviceDayId);
                            psSlot.setString(2, slot.start());
                            psSlot.setString(3, slot.end());
                            psSlot.addBatch(); // Batch insertion for efficiency
                        }
                    }
                }
            }
            psSlot.executeBatch(); // Execute all batched inserts
        }

        conn.commit(); // Commit transaction

    } catch (SQLException e) {
        if (conn != null) {
            conn.rollback(); // Rollback transaction on error
        }
        throw e;
    } finally {
        DBHelper.close(conn, null, null); // Close connection
    }
}

// ServiceDAO.java

/**
 * Disables a service by setting its is_active flag to 0 (false).
 */
public static void disableService(int serviceId, int companyId) throws SQLException {
    // Assuming 'is_active' column exists in the 'services' table.
    String sql = "UPDATE services SET is_active = 0 WHERE service_id = ? AND company_id = ?";
    
    Connection conn = null;
    try {
        conn = DBHelper.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, serviceId);
            ps.setInt(2, companyId);
            
            // Execute the update
            ps.executeUpdate();
        }
    } finally {
        DBHelper.close(conn, null, null);
    }
}

// ServiceDAO.java

/**
 * Permanently deletes a service and all its associated data (time slots, days).
 * This operation is wrapped in a transaction (Delete slots, Delete days, Delete service).
 */
public static void deleteService(int serviceId, int companyId) throws SQLException {
    // SQL for clearing old days/slots (in case time_slots references service_days)
    String deleteTimeSlotsSql = "DELETE FROM time_slots WHERE service_day_id IN (SELECT service_day_id FROM service_days WHERE service_id = ?)";
    String deleteServiceDaysSql = "DELETE FROM service_days WHERE service_id = ?";
    String deleteServiceSql = "DELETE FROM services WHERE service_id = ? AND company_id = ?";

    Connection conn = null;

    try {
        conn = DBHelper.getConnection();
        conn.setAutoCommit(false); // Start transaction

        // 1. Delete Time Slots
        try (PreparedStatement psDeleteSlots = conn.prepareStatement(deleteTimeSlotsSql)) {
            psDeleteSlots.setInt(1, serviceId);
            psDeleteSlots.executeUpdate();
        }
        
        // 2. Delete Service Days
        try (PreparedStatement psDeleteDays = conn.prepareStatement(deleteServiceDaysSql)) {
            psDeleteDays.setInt(1, serviceId);
            psDeleteDays.executeUpdate();
        }

        // 3. Delete Main Service
        try (PreparedStatement psDeleteService = conn.prepareStatement(deleteServiceSql)) {
            psDeleteService.setInt(1, serviceId);
            psDeleteService.setInt(2, companyId);
            if (psDeleteService.executeUpdate() == 0) {
                throw new SQLException("Delete failed: Service ID " + serviceId + " not found or company ID mismatch.");
            }
        }

        conn.commit(); // Commit transaction

    } catch (SQLException e) {
        if (conn != null) {
            conn.rollback(); // Rollback transaction on error
        }
        throw e;
    } finally {
        DBHelper.close(conn, null, null); // Close connection
    }
}

/**
 * Reactivates a service by setting its is_active flag to 1 (true).
 */
public static void reactivateService(int serviceId, int companyId) throws SQLException {
    String sql = "UPDATE services SET is_active = 1 WHERE service_id = ? AND company_id = ?";
    
    Connection conn = null;
    try {
        conn = DBHelper.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, serviceId);
            ps.setInt(2, companyId);
            ps.executeUpdate();
        }
    } finally {
        DBHelper.close(conn, null, null);
    }
}
}
