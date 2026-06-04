package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.pojo.PlatilloAvanzado;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AvanzadoDAO {
    private Connection con;

    public AvanzadoDAO(Connection con) { this.con = con; }

    public List<PlatilloAvanzado> obtenerPizzasAvanzadas() throws SQLException {
        List<PlatilloAvanzado> lista = new ArrayList<>();
        // Consultamos la tabla que usa el tipo Pizza_T
        String sql = "SELECT p.idPlatillo, p.nombre, p.precio, p.tipo_masa, p.es_picante FROM platillos_avanzados p";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new PlatilloAvanzado(
                    rs.getInt("idPlatillo"), 
                    rs.getString("nombre"), 
                    rs.getDouble("precio"),
                    rs.getString("tipo_masa"),
                    rs.getString("es_picante")
                ));
            }
        }
        return lista;
    }
}