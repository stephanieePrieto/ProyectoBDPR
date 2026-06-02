package com.mycompany.restaurante.utils;

import java.sql.Connection;
import com.mycompany.restaurante.modelo.sql.MySQLConnect;

public class ConexionBD {
    public static Connection conectar() {
        return MySQLConnect.getConexion();
    }
}