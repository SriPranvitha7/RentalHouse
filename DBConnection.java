package com.housefinder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // ── Change these if your setup is different ──────────────
    private static final String URL      = "jdbc:mysql://localhost:3306/housefinder";
    private static final String USER     = "root";
    private static final String PASSWORD = "your_mysql_password_here"; // ← put your password
    // ─────────────────────────────────────────────────────────

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found. Check lib folder.", e);
        }
    }
}