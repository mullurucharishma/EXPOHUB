package DAO;
import POJO.Payment;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class PaymentDAO {
    private Connection conn;
    public PaymentDAO(Connection conn) {
        this.conn = conn;
    }
    public List<Payment> getPaymentsByUserId(int userId) {
        List<Payment> payments = new ArrayList<>();
        String query = "SELECT * FROM payments WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                double amount = rs.getDouble("amount");
                Date paymentDate = rs.getDate("payment_date");
                String paymentMethod = rs.getString("payment_method");
                payments.add(new Payment(id, userId, amount, paymentDate, paymentMethod));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return payments;
    }
    public boolean processPayment(int userId, double amount, String paymentMethod) {
        String sql = "INSERT INTO payments (user_id, amount, payment_method) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, paymentMethod);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // Return true if at least one row was inserted
        } catch (SQLException e) {
            e.printStackTrace(); // Log the exception for debugging
            return false; // Return false if there was an error
        }
    }
    private boolean isVenueAvailable(int venueId, Date bookingDate) {
        String query = "SELECT COUNT(*) FROM booking WHERE venue_id = ? AND booking_date = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, venueId);
            stmt.setDate(2, bookingDate);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // If count is 0, venue is available
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean createBooking(int eventId, int userId, int venueId, String foodPreference,
                                 String decorationPreference, String username, Date bookingDate,
                                 Time bookingTime, double totalPrice, String paymentStatus) {
        try {
            conn.setAutoCommit(false);
            if (!isVenueAvailable(venueId, bookingDate)) {
                System.out.println("Venue is not available on this date.");
                return false;
            }
            String sql = "INSERT INTO booking (event_id, user_id, venue_id, food_preference, " +
                    "decoration_preference, username, booking_date, booking_time, total_price, " +
                    " payment_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, eventId);
                pstmt.setInt(2, userId);
                pstmt.setInt(3, venueId);
                pstmt.setString(4, foodPreference);
                pstmt.setString(5, decorationPreference);
                pstmt.setString(6, username);
                pstmt.setDate(7, bookingDate);
                pstmt.setTime(8, bookingTime);
                pstmt.setDouble(9, totalPrice);
                pstmt.setString(10, paymentStatus); // Set payment status
                int rowsAffected = pstmt.executeUpdate();
                conn.commit(); // Commit transaction if successful
                return rowsAffected > 0; // Return true if a row was inserted
            } catch (SQLException e) {
                conn.rollback(); // Rollback transaction on error
                e.printStackTrace();
                return false; // Indicate failure
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    public double getVenuePrice(int venueId) {
        double venuePrice = 0.0;
        String query = "SELECT price FROM venues WHERE venue_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, venueId); //
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                venuePrice = rs.getDouble("price");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return venuePrice;
    }
}


