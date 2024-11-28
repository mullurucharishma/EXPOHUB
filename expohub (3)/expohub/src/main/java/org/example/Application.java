package org.example;

import DAO.DepositDAO;
import DAO.PaymentDAO;
import DAO.UserDAO;
import POJO.Payment;
import POJO.Booking;
import POJO.Event;
import POJO.User;
import POJO.Venue;

import java.sql.Connection;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class Application {
    private final UserDAO userDAO;
    private final PaymentDAO paymentDAO;
    private final DepositDAO depositDAO;
    private final Scanner scanner;

    public Application(Connection conn, Scanner scanner) {
        this.userDAO = new UserDAO(conn);
        this.paymentDAO = new PaymentDAO(conn);
        this.depositDAO = new DepositDAO(conn);
        this.scanner = scanner;
    }

    public void run() {
        int choice;
        while (true) {
            System.out.println("Welcome! Choose an option:");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    registerUser(userDAO, scanner);
                    break;

                case 2:
                    loginUser(userDAO, paymentDAO, depositDAO, scanner);
                    break;

                case 3:
                    System.out.println("Exiting...");
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid choice. Please choose again.");
                    break;
               }
           }
        }

    private static void registerUser(UserDAO userDAO, Scanner scanner) {
        System.out.println("Registration:");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter phone: ");
        String phone = scanner.nextLine();
        boolean created = userDAO.createUser(username, email, password, phone);
        if (created) {
            System.out.println("User created successfully.");
        } else {
            System.out.println("Failed to create user.");
        }
    }
    private static void loginUser(UserDAO userDAO, PaymentDAO paymentDAO, DepositDAO depositDAO, Scanner scanner) {
        System.out.println("Login:");
        System.out.print("Enter email: ");
        String loginEmail = scanner.nextLine();
        System.out.print("Enter password: ");
        String loginPassword = scanner.nextLine();
        User user = userDAO.validateUser(loginEmail, loginPassword);
        if (user != null) {
            System.out.println("Login successful! Welcome, " + user.getUsername());
            handleUserMenu(userDAO, paymentDAO, depositDAO, user, scanner);
        } else {
            System.out.println("Invalid email or password. Please try again.");
        }
    }
    private static void handleUserMenu(UserDAO userDAO, PaymentDAO paymentDAO, DepositDAO depositDAO, User user, Scanner scanner) {
        int choice;
        while (true) {
            System.out.println("Choose an option:");
            System.out.println("1. Book an event");
            System.out.println("2. View booked events");
            System.out.println("3. View my payments");
            System.out.println("4. View my deposit balance");
            System.out.println("5. Deposit money");
            System.out.println("6. Logout");
            System.out.print("Enter your choice: ");

            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine();
                switch (choice) {
                    case 1:
                        handleEventCreation(userDAO, paymentDAO, depositDAO, user, scanner);
                        break;
                    case 2:
                        viewBookedEvents(userDAO, user);
                        break;
                    case 3:
                        viewPayments(paymentDAO, user.getUserId());
                        break;
                    case 4:
                        viewDepositBalance(depositDAO, user.getUserId());
                        break;
                    case 5:
                        depositMoney(depositDAO, user.getUserId(), scanner);
                        break;
                    case 6:
                        System.out.println("Logging out...");
                        return;
                    default:
                        System.out.println("Invalid choice. Please choose again.");
                        break;
                }
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();
            }
        }
    }
    private static void handleEventCreation(UserDAO userDAO, PaymentDAO paymentDAO, DepositDAO depositDAO, User user, Scanner scanner) {
        System.out.println("Select an event:");
        List<Event> existingEvents = userDAO.getExistingEvents();
        for (Event event : existingEvents) {
            System.out.println(event.getEventId() + ". " + event.getEventName());
        }
        int eventChoice = -1;
        while (eventChoice < 1 || eventChoice > existingEvents.size()) {
            System.out.print("Enter your choice (1-" + existingEvents.size() + "): ");
            if (scanner.hasNextInt()) {
                eventChoice = scanner.nextInt();
                scanner.nextLine();
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next();
            }
        }
        int eventId = existingEvents.get(eventChoice - 1).getEventId();
        System.out.println("You selected event ID: " + eventId);

        List<Venue> venues = userDAO.getVenuesByEvent(eventId);
        if (venues.isEmpty()) {
            System.out.println("No venues available for this event type.");
            return;
        }
        System.out.println("Available venues:");
        for (int i = 0; i < venues.size(); i++) {
            Venue venue = venues.get(i);
            System.out.println((i + 1) + ". " + venue.getVenueName() + " - Capacity: " + venue.getCapacity() + ", Price: " + venue.getPrice());
        }
        int venueChoice = -1;
        while (venueChoice < 1 || venueChoice > venues.size()) {
            System.out.print("Select a venue (1-" + venues.size() + "): ");
            if (scanner.hasNextInt()) {
                venueChoice = scanner.nextInt();
                scanner.nextLine();
                // Ensure the choice is valid
                if (venueChoice < 1 || venueChoice > venues.size()) {
                    System.out.println("Invalid choice. Please select a venue within the specified range.");
                }
            } else {
                System.out.println("Invalid input. Please enter a number.");
                scanner.next();
            }
        }

        Venue selectedVenue = venues.get(venueChoice - 1);
        int venueId = selectedVenue.getVenueId();
        double venuePrice = selectedVenue.getPrice();
        int capacity = selectedVenue.getCapacity();
        System.out.println("You selected venue: " + selectedVenue.getVenueName());
        System.out.print("Enter event date (YYYY-MM-DD): ");
        String dateInput = scanner.nextLine();
        Date booking_date;
        try {
            booking_date = Date.valueOf(dateInput); // Convert directly to java.sql.Date
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid date format. Please try again.");
            return;
        }

        System.out.print("Enter event time (HH:MM): ");
        String timeInput = scanner.nextLine();

        while (!timeInput.matches("^(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$")) {
            System.out.println("Invalid time format. Please enter time in HH:MM format (e.g., 08:30).");
            System.out.print("Enter event time (HH:MM): ");
            timeInput = scanner.nextLine(); // Prompt again for time
        }

        System.out.print("Is this time in AM or PM? (Enter 'AM' or 'PM'): ");
        String amPm = scanner.nextLine().toUpperCase();

        if (amPm.equals("PM") && !timeInput.startsWith("12")) {
            timeInput = (Integer.parseInt(timeInput.split(":")[0]) + 12) + ":" + timeInput.split(":")[1];
        } else if (amPm.equals("AM") && timeInput.startsWith("12")) {
            timeInput = "00:" + timeInput.split(":")[1];
        }

        Time booking_time = Time.valueOf(timeInput + ":00"); // Convert directly to java.sql.Time (adding seconds)
        LocalDateTime desiredBookingDateTime = LocalDateTime.of(booking_date.toLocalDate(), booking_time.toLocalTime());
        LocalDateTime currentDateTime = LocalDateTime.now();

        if (desiredBookingDateTime.isBefore(currentDateTime)) {
            System.out.println("The booking date and time must be in the future. Please try again.");
            return;
        }
        if (!userDAO.isVenueAvailable(venueId, booking_date)) {
            System.out.println("Venue is not available on this date and time.");
            return;
        }
        System.out.print("Enter number of attendees (must not exceed " + capacity + "): ");
        int numberOfAttendees = scanner.nextInt();
        scanner.nextLine();
        if (numberOfAttendees > capacity) {
            System.out.println("The number of attendees exceeds the venue capacity. Please try again.");
            return;
        }
        System.out.print("Do you want food customization? (yes/no): ");
        String foodCustomizationChoice = scanner.nextLine().toLowerCase();
        try {
            if (!foodCustomizationChoice.equals("yes") && !foodCustomizationChoice.equals("no")) {

            }
        }catch (Exception e){
            System.out.println("invalid input");
        }
        String foodPreference = "";
        double totalMealCost = 0.0;
        if (foodCustomizationChoice.equals("yes")) {
            System.out.println("Select food type:");
            System.out.println("1. Vegetarian");
            System.out.println("2. Non-Vegetarian");
            System.out.print("Enter your choice: ");
            int foodChoice = scanner.nextInt();
            scanner.nextLine();
            switch (foodChoice) {
                case 1:
                    System.out.println("Select Vegetarian Meal Type:");
                    System.out.println("1. North Indian Meal (70 per plate)");
                    System.out.println("2. South Indian Meal (90 per plate)");
                    System.out.print("Enter your choice: ");
                    int vegMealChoice = scanner.nextInt();
                    scanner.nextLine();

                    if (vegMealChoice == 1) {
                        foodPreference = "Vegetarian - North Indian";
                        totalMealCost = 70.00 * numberOfAttendees;
                    } else if (vegMealChoice == 2) {
                        foodPreference = "Vegetarian - South Indian";
                        totalMealCost = 90.00 * numberOfAttendees;
                    } else {
                        System.out.println("Invalid choice for vegetarian meal.");
                        return;
                    }
                    break;
                case 2:
                    System.out.println("Select Non-Vegetarian Meal Type:");
                    System.out.println("1. North Indian Meal (90 per plate)");
                    System.out.println("2. South Indian Meal (100 per plate)");
                    System.out.print("Enter your choice: ");
                    int nonVegMealChoice = scanner.nextInt();
                    scanner.nextLine();

                    if (nonVegMealChoice == 1) {
                        foodPreference = "Non-Vegetarian - North Indian";
                        totalMealCost = 90.00 * numberOfAttendees;
                    } else if (nonVegMealChoice == 2) {
                        foodPreference = "Non-Vegetarian - South Indian";
                        totalMealCost = 100.00 * numberOfAttendees;
                    } else {
                        System.out.println("Invalid choice for non-vegetarian meal.");
                        return;
                    }
                    break;

                default:
                    System.out.println("Invalid food customization choice.");
                    return;
            }
            System.out.printf("Total Food Price for %d attendees (%s): %.2f%n", numberOfAttendees, foodPreference, totalMealCost);
        }

        System.out.print("Do you want decoration customization? (yes/no): ");
        String decorationCustomizationChoice = scanner.nextLine().toLowerCase();
        try {
            if (!decorationCustomizationChoice.equals("yes") && !decorationCustomizationChoice.equals("no")) {

            }
        }catch (Exception e){
            System.out.println("invalid input");
        }
        double decorationPrice = 0.0;
        String decorationPreference = "";

        if (decorationCustomizationChoice.equals("yes")) {
            System.out.println("Select decoration preferences:");
            System.out.println("1. Floral (4000)");
            System.out.println("2. Modern (6000)");
            System.out.println("3. Classic (2000)");
            System.out.print("Enter your choice: ");
            int decorationChoice = scanner.nextInt();
            scanner.nextLine();
            switch (decorationChoice) {
                case 1:
                    decorationPreference = "Floral";
                    decorationPrice = 4000.00;
                    break;
                case 2:
                    decorationPreference = "Modern";
                    decorationPrice = 6000.00;
                    break;
                case 3:
                    decorationPreference = "Classic";
                    decorationPrice = 2000.00;
                    break;
                default:
                    System.out.println("Invalid decoration choice.");
                    return;
            }
            System.out.printf("Decoration selected: %s, Price: %.2f%n", decorationPreference, decorationPrice);
        }

        System.out.print("Do you want to hire a photographer (price: 3000)? (yes/no): ");
        String photographerChoice = scanner.nextLine().toLowerCase();
        try {
            if (!photographerChoice.equals("yes") && !photographerChoice.equals("no")) {

            }
        }catch (Exception e){
            System.out.println("invalid input");
        }
        double photographerPrice = photographerChoice.equals("yes") ? 3000.0 : 0.0;

        System.out.print("Do you want to hire a videographer (price: 5000)? (yes/no): ");
        String videographerChoice = scanner.nextLine().toLowerCase();
        try {
            if (!videographerChoice.equals("yes") && !videographerChoice.equals("no")) {
            }
        }catch (Exception e){
            System.out.println("invalid input");
        }
        double videographerPrice = videographerChoice.equals("yes") ? 5000.0 : 0.0;
        double finalTotalPrice = venuePrice + totalMealCost + decorationPrice + photographerPrice + videographerPrice;
        System.out.printf("Total Price calculated: %.2f%n", finalTotalPrice);
        System.out.print("Do you want to proceed with the payment? (yes/no): ");
        if (scanner.nextLine().equalsIgnoreCase("yes")) {
            double userBalance = depositDAO.getDepositBalance(user.getUserId());
            System.out.printf("Your current deposit balance: %.2f%n", userBalance);
            System.out.printf("Total event cost: %.2f%n", finalTotalPrice);

            System.out.print("Do you want to confirm the payment of this amount? (yes/no): ");
            if (scanner.nextLine().equalsIgnoreCase("yes")) {

                System.out.print("Please enter your password to confirm the payment: ");
                String enteredPassword = scanner.nextLine();

                if (userDAO.validatePassword(user.getUserId(), enteredPassword)) {
                    if (userBalance >= finalTotalPrice) {
                        System.out.println("Using deposit for payment...");
                        boolean paymentSuccess = paymentDAO.processPayment(user.getUserId(),
                                finalTotalPrice, "Online");
                        if (paymentSuccess) {
                            boolean depositUsed = depositDAO.useDeposit(user.getUserId(), finalTotalPrice);
                            if (depositUsed) {
                                System.out.println("Booking confirmed.");
                                System.out.printf("Payment successful using deposit. Amount deducted: %.2f%n", finalTotalPrice);
                                createBooking(user, eventId, venueId, booking_date, booking_time,
                                        numberOfAttendees, foodPreference, decorationPreference,
                                        totalMealCost, decorationPrice, photographerPrice,
                                        videographerPrice, paymentDAO);

                            } else {
                                System.out.println("Failed to deduct deposit. Please check your balance or try again.");
                            }
                        } else {
                            System.out.println("Failed to process payment. Please check your payment details.");
                        }
                    } else {
                        System.out.println("Insufficient deposit. Please deposit more funds or use an alternate payment method.");
                    }
                } else {
                    System.out.println("Invalid password. Payment cannot be processed.");
                }
            } else {
                System.out.println("Payment process cancelled.");
            }
        }
    }

    private static void createBooking(User user, int eventId, int venueId, Date bookingDate, Time bookingTime,
                                      int numberOfAttendees, String foodPreference, String decorationPreference,
                                      double totalMealCost, double decorationPrice, double photographerPrice,
                                      double videographerPrice, PaymentDAO paymentDAO)
    {
        double venuePrice = paymentDAO.getVenuePrice(venueId);

        double finalTotalPrice =venuePrice+ totalMealCost + decorationPrice + photographerPrice + videographerPrice;

        String paymentStatus = "Pending"; // Default to pending; you might update this based on actual payment processing

        // Assuming you have payment processing logic that might change the payment status to "Paid"
        boolean paymentProcessed = true; // Set this based on actual payment logic
        if (paymentProcessed) {
            paymentStatus = "Paid";
        }
        // Logic to create and save the booking in the database
        boolean bookingSuccess = paymentDAO.createBooking(eventId, user.getUserId(), venueId, foodPreference, decorationPreference, user.getUsername(), bookingDate, bookingTime, finalTotalPrice, paymentStatus
        );
        if (bookingSuccess) {
            System.out.println("Booking created successfully!");
        } else {
            System.out.println("Booking creation failed.");
        }
    }

    private static void viewBookedEvents(UserDAO userDAO, User user) {
        if (user != null) {
            List<Booking> bookings = userDAO.getBookedEvents(user.getUserId());

            System.out.println("Your booked events:");
            for (Booking booking : bookings) {
                System.out.println("Username: " + booking.getUsername() +
                        ", Event: " + booking.getEventName() +
                        ", Venue: " + booking.getVenueName() +
                        ", Total Price: " + booking.getPrice() +
                        ", Booked At: " + booking.getBookedAt() +
                        ",Booking date:" + booking.getBookingDate()+
                        ", Booking time: " + booking.getBookingTime()+
                        ", Payment Status: " + (booking.getPaymentStatus() != null ? booking.getPaymentStatus() : "Pending"));
            }
        } else {
            System.out.println("You need to be logged in to view booked events.");
        }
    }
    private static void viewPayments(PaymentDAO paymentDAO, int userId) {
        List<Payment> payments = paymentDAO.getPaymentsByUserId(userId);
        if (payments.isEmpty()) {
            System.out.println("No payments found for this user.");
        } else {
            for (Payment payment : payments) {
                System.out.println("Payment ID: " + payment.getPaymentId() +
                        ", Amount: " + payment.getTotalAmount() +
                        ", Method: " + payment.getPaymentMethod() +
                        ", Date: " + payment.getPaymentDate());
            }
        }
    }

    private static void viewDepositBalance(DepositDAO depositDAO, int userId) {
        double balance = depositDAO.getDepositBalance(userId);
        System.out.println("Your current deposit balance: " + balance);
    }

    private static void depositMoney(DepositDAO depositDAO, int userId, Scanner scanner) {
        System.out.print("Enter amount to deposit: ");
        double amount = scanner.nextDouble();
        scanner.nextLine(); // Consume the newline character
        if (amount <= 0) {
            System.out.println("Amount must be positive. Please enter a valid amount.");
            return;
        }
        boolean success = depositDAO.addDeposit(userId, amount);
        if (success) {
            System.out.println("Deposit successful.");

            // Immediately check the new balance
            double newBalance = depositDAO.getDepositBalance(userId);
            System.out.println("New balance: " + newBalance);
        } else {
            System.out.println("Failed to deposit money. Please try again.");
        }
    }

}
