package company;

import db.DBHelper;
import java.security.MessageDigest;
import java.sql.*;

public class CompanyAuthDAO {

    // Hash password using SHA-256
    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String hexValue = Integer.toHexString(0xff & b);
                if (hexValue.length() == 1) hex.append('0');
                hex.append(hexValue);
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Sign Up (Insert Company)
    public static boolean signup(String username, String email, String password) {
        String sql = "INSERT INTO companies (username, email, password_hash) VALUES (?, ?, ?)";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, hashPassword(password));

            stmt.executeUpdate();
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            return false; // Username or email already exists
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Login → return company_id or -1 if fail
    public static int login(String username, String password) {
        String sql = "SELECT company_id, password_hash FROM companies WHERE username = ?";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                String enteredHash = hashPassword(password);

                if (storedHash.equals(enteredHash)) {
                    return rs.getInt("company_id");
                } else {
                    return -1; // Wrong password
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // no user
    }

    // Check if profile exists
    public static boolean hasProfile(int companyId) {
        String sql = "SELECT company_id FROM company_profiles WHERE company_id = ?";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, companyId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
