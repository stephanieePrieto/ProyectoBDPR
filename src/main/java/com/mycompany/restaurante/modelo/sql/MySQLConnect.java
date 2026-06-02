package com.mycompany.restaurante.modelo.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnect {
    private Connection conn;
    private String host = "localhost";
    private String port = "3306";
    private String db = "restaurante"; 
    private String username = "root";
    private String password = "mandala1406S."; 
    
    private static MySQLConnect connect;

    public MySQLConnect() {
        String driver = "com.mysql.cj.jdbc.Driver";
        try {
            Class.forName(driver);
            connection(); 
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        connect = this;
    }

    public Connection connection() {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + db + "?useTimezone=true&serverTimezone=UTC";
        try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(url, username, password);
            }
        } catch (SQLException e) {
            System.err.println("Error Crítico de Conexión: " + e.getMessage());
        }
        return conn;
    }

    public void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static MySQLConnect getConnect() { 
        if (connect == null) {
            connect = new MySQLConnect();
        }
        return connect; 
    }

    public static Connection getConexion() {
        return getConnect().connection();
    }
}