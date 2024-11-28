package POJO;
import java.sql.Date;
public class Payment {
    private int id;
    private int userId;
    private double amount;
    private Date paymentDate;
    private String paymentMethod;
    public Payment(int id, int userId, double amount, Date paymentDate, String paymentMethod) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
    }

    public int getId() {
        return id;
    }

    public double getTotalAmount() {
        return this.amount;
    }

    public int getPaymentId() {
        return this.id;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }
}
