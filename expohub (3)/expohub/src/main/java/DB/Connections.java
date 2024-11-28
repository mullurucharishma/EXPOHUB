package DB;

import java.sql.Connection;
import java.sql.DriverManager;

public class Connections {
    private final static String URL="jdbc:mysql://localhost:3306/expo2";
    private final static String USERNAME="root";
    private final static String PASSWORD="Cherry123";
    public static Connection getConnection(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("class loaded successfully");
            Connection connection= DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("connection is established successfully");
            return connection;
        } catch (Exception e) {
            System.out.println("class not found");
            System.out.println("connection not established");
        }
        return null;
    }
}
