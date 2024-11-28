package org.example;

import DB.Connections;

import java.sql.Connection;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Connection conn = Connections.getConnection();
        if (conn != null) {
            Application app = new Application(conn, new Scanner(System.in));
            app.run();
        } else {
            System.out.println("Connection failed!");
        }
    }
}
