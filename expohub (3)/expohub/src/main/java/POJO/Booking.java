package POJO;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public class Booking {
    private int bookingId;
    private String eventName;
    private String venueName;
    private Date bookingDate;
    private Time bookingTime;
    private double price;
    private String foodPreference;
    private String decorationPreference;
    private String username;
    private Timestamp bookedAt;
    private String paymentStatus;

    public Booking(int bookingId, String eventName, String venueName, Date bookingDate, Time bookingTime, double price, String foodPreference, String decorationPreference, String username, Timestamp bookedAt,String paymentStatus) {
        this.bookingId = bookingId;
        this.eventName = eventName;
        this.venueName = venueName;
        this.bookingDate = bookingDate;
        this.bookingTime = bookingTime;
        this.price = price;
        this.foodPreference = foodPreference;
        this.decorationPreference = decorationPreference;
        this.username = username;
        this.bookedAt = bookedAt;
        this.paymentStatus = paymentStatus;
    }

    public int getBookingId() {
        return bookingId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getVenueName() {
        return venueName;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public Time getBookingTime() {
        return bookingTime;
    }

    public double getPrice() {
        return price;
    }

    public String getFoodPreference() {
        return foodPreference;
    }

    public String getDecorationPreference() {
        return decorationPreference;
    }

    public String getUsername() {
        return username;
    }

    public Timestamp getBookedAt() {
        return bookedAt; // Getter for bookedAt
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}

