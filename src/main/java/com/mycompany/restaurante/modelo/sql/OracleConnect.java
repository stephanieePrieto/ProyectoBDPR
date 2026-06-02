package com.mycompany.restaurante.modelo.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class OracleConnect {

    private static final String URL = "jdbc:oracle:thin:@(description= (retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1521)(host=adb.us-phoenix-1.oraclecloud.com))(connect_data=(service_name=gf97f8355b83e0a_listibdpror2026_tp.adb.oraclecloud.com))(security=(ssl_server_dn_match=yes)))";
    
    private static final String USERNAME = "user16";
    private static final String PASSWORD = "ListiBDPRu16";
    
    private static Connection conn = null;

    public static Connection getConexion() {
        String driver = "oracle.jdbc.driver.OracleDriver";
        
        try {
            Class.forName(driver);
            
            if (conn == null || conn.isClosed()) {
                // Conexión directa usando la cadena segura de Oracle Cloud
                conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("Conexion establecida con exito en la nube de Oracle");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Error: No se encontro el driver de Oracle.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Error de Conexion Oracle" + e.getMessage());
        }
        
        return conn;
    }

    public static void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Conexion cerrada correctamente.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}