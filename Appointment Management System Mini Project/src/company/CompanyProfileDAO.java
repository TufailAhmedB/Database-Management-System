package company;

import db.DBHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CompanyProfileDAO {

    public boolean saveCompanyProfile(CompanyProfile cp) {
        String sql = "INSERT INTO company_profiles (company_id, owner_name, company_name, business_type, tagline, "
                   + "description, logo_path, start_time, end_time, off_days, phone, email, website, address) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, cp.getCompanyId());
            ps.setString(2, cp.getOwnerName());
            ps.setString(3, cp.getCompanyName());
            ps.setString(4, cp.getBusinessType());
            ps.setString(5, cp.getTagline());
            ps.setString(6, cp.getDescription());
            ps.setString(7, cp.getLogoPath());
            ps.setString(8, cp.getStartTime());
            ps.setString(9, cp.getEndTime());
            ps.setString(10, cp.getOffDays());
            ps.setString(11, cp.getPhone());
            ps.setString(12, cp.getEmail());
            ps.setString(13, cp.getWebsite());
            ps.setString(14, cp.getAddress());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error saving company profile: " + e.getMessage());
            return false;
        }
    }
}
