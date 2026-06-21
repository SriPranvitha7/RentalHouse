package com.housefinder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PropertyDAO {

    // ── Save a new property to the database ──────────────────
    public boolean addProperty(Property p) {
        String sql = "INSERT INTO properties "
                   + "(owner, phone, location, address, rent, type, lat, lng) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, p.getOwner());
            stmt.setString(2, p.getPhone());
            stmt.setString(3, p.getLocation());
            stmt.setString(4, p.getAddress());
            stmt.setInt   (5, p.getRent());
            stmt.setString(6, p.getType());
            stmt.setDouble(7, p.getLat());
            stmt.setDouble(8, p.getLng());

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.out.println("Error adding property: " + e.getMessage());
            return false;
        }
    }

    // ── Get all properties from the database ─────────────────
    public List<Property> getAllProperties() {
        List<Property> list = new ArrayList<>();
        String sql = "SELECT * FROM properties ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Property(
                    rs.getInt   ("id"),
                    rs.getString("owner"),
                    rs.getString("phone"),
                    rs.getString("location"),
                    rs.getString("address"),
                    rs.getInt   ("rent"),
                    rs.getString("type"),
                    rs.getDouble("lat"),
                    rs.getDouble("lng")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Error fetching properties: " + e.getMessage());
        }

        return list;
    }

    // ── Search properties by location, type, rent ────────────
    public List<Property> searchProperties(String location, String type, int minRent, int maxRent) {
        List<Property> list = new ArrayList<>();

        String sql = "SELECT * FROM properties WHERE "
                   + "LOWER(location) LIKE ? AND "
                   + "(? = '' OR type = ?) AND "
                   + "rent BETWEEN ? AND ? "
                   + "ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + location.toLowerCase() + "%");
            stmt.setString(2, type);
            stmt.setString(3, type);
            stmt.setInt   (4, minRent);
            stmt.setInt   (5, maxRent);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new Property(
                    rs.getInt   ("id"),
                    rs.getString("owner"),
                    rs.getString("phone"),
                    rs.getString("location"),
                    rs.getString("address"),
                    rs.getInt   ("rent"),
                    rs.getString("type"),
                    rs.getDouble("lat"),
                    rs.getDouble("lng")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Error searching properties: " + e.getMessage());
        }

        return list;
    }
}