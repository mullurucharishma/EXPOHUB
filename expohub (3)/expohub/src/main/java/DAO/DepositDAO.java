package DAO;
import java.sql.*;

import static DB.Connections.getConnection;
public class DepositDAO {
    private Connection conn;
    public DepositDAO(Connection conn) {
        this.conn = conn;
    }
    public double getDepositBalance(int userId) {
        double balance = 0.0;
        String sql = "SELECT SUM(amount) as total_amount FROM deposits WHERE user_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                balance = rs.getDouble("total_amount");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return balance;
    }

    public boolean useDeposit(int userId, double amount) {
        PreparedStatement pstmt = null;
        try {
            // Start a transaction
            conn.setAutoCommit(false); // Ensure auto-commit is off
            double currentBalance = getDepositBalance(userId);
            if (currentBalance < amount) {
                return false; // Not enough balance
            }
            // Deduct amount from balance
            String sql = "UPDATE deposits SET amount = amount - ? WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, userId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on exception
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                // Don't close conn here, it's managed externally
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public boolean addDeposit(int userId, double amount) {
        String sql = "INSERT INTO deposits (user_id, amount) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setDouble(2, amount);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // Return true if deposit was added successfully
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
