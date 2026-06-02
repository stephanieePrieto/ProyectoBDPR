package com.mycompany.restaurante.utils;

import com.mycompany.restaurante.modelo.sql.OracleConnect;
import java.sql.Connection;

public class ConexionBD {
    // Redirigimos la llamada antigua al nuevo motor Oracle
    public static Connection conectar() {
        return OracleConnect.getConexion();
    }
}