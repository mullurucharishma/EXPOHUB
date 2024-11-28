package DAO;
import POJO.Booking;
import POJO.Event;
import POJO.User;
import POJO.Venue;




import java.sql.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Time;
public class UserDAO {
    private Connection conn;
    public UserDAO(Connection conn) {
        this.conn = conn;
    }
    public boolean createUser(String username, String email, String password, String phone) {
        if (phone.length() != 10 || !phone.matches("\\d+")) {
            System.out.println("Invalid phone number. It must contain exactly 10 digits.");
            return false;
        }
        String passwordPattern = "^(?=.*[A-Z])(?=.*[!@#$%^&*()_+=-])(?=.*[0-9]).{8,}$";
        if (!password.matches(passwordPattern)) {
            System.out.println("Invalid password. It must be at least 8 characters long, contain one uppercase letter, one special character, and one digit.");
            return false;
        }
        String emailPattern = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
        if (!email.matches(emailPattern)) {
            System.out.println("Invalid email address.");
            return false;
        }
        String sql = "INSERT INTO Users (username, email, password, phone) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, password);
            stmt.setString(4, phone);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("user already exists");;
        }
        return false;
    }

    public User validateUser(String email, String password) {
        String sql = "SELECT * FROM Users WHERE email = ? AND password = ?";
        User user = null;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setString(2, password); // Use hashed passwords in real app
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user = new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("email"), rs.getString("password"), rs.getString("phone"), rs.getTimestamp("created_at"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }
    public boolean validatePassword(int userId, String enteredPassword) {
        String sql = "SELECT password FROM users WHERE user_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                return storedPassword.equals(enteredPassword);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public List<Event> getExistingEvents() {
        List<Event> events = new ArrayList<>();
        String query = "SELECT event_id, event_name FROM events";
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                int eventId = rs.getInt("event_id");
                String eventName = rs.getString("event_name");
                events.add(new Event(eventId, eventName)); // Create an Event object
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return events;
    }
    public List<Venue> getVenuesByEvent(int eventChoice) {
        List<Venue> venues = new ArrayList<>();
        String query = "SELECT venue_id, venue_name, location, capacity, price FROM venues";
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                int venueId = rs.getInt("venue_id");
                String venueName = rs.getString("venue_name");
                String location = rs.getString("location");
                int capacity = rs.getInt("capacity");
                double price = rs.getDouble("price");
                Venue venue = new Venue(venueId, venueName, location, capacity, price);
                venues.add(venue);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return venues;
    }
    public boolean isVenueAvailable(int venueId, Date booking_date) {
        String query = "SELECT COUNT(*) FROM booking WHERE venue_id = ? AND booking_date = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, venueId);
            stmt.setDate(2, booking_date);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public List<Booking> getBookedEvents(int userId) {
        List<Booking> bookedEvents = new ArrayList<>();
        String query = "SELECT b.booking_id, e.event_name, v.venue_name, b.booking_date, b.booking_time, b.total_price, b.food_preference, b.decoration_preference, b.username, b.booked_at, b.payment_status " +
                "FROM booking b " +
                "JOIN events e ON b.event_id = e.event_id " +
                "JOIN venues v ON b.venue_id = v.venue_id " +
                "WHERE b.user_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int bookingId = rs.getInt("booking_id");
                String eventName = rs.getString("event_name");
                String venueName = rs.getString("venue_name");
                Date bookingDate = rs.getDate("booking_date");
                Time bookingTime = rs.getTime("booking_time");
                double price = rs.getDouble("total_price");
                String foodPreference = rs.getString("food_preference");
                String decorationPreference = rs.getString("decoration_preference");
                String username = rs.getString("username");
                Timestamp bookedAt = rs.getTimestamp("booked_at");
                String paymentStatus = rs.getString("payment_status");

                Booking booking = new Booking(bookingId, eventName, venueName, bookingDate, bookingTime,
                        price, foodPreference, decorationPreference, username, bookedAt, paymentStatus);
                bookedEvents.add(booking);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookedEvents;
    }
}
