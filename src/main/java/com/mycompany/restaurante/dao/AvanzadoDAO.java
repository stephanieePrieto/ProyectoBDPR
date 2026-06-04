package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.pojo.PlatilloAvanzado;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase encargada de gestionar las operaciones de base de datos para platillos con características avanzadas.
 */
public class AvanzadoDAO {
    private Connection con;

    /**
     * Constructor que inicializa el DAO con una conexión activa.
     * @param con Objeto Connection para interactuar con la base de datos.
     */
    public AvanzadoDAO(Connection con) { 
        this.con = con; 
    }

    /**
     * Recupera una lista de pizzas avanzadas registradas en la base de datos.
     * @return Lista de objetos PlatilloAvanzado con sus atributos específicos como masa y nivel de picante.
     * @throws SQLException Si ocurre un error durante la ejecución de la consulta.
     */
    public List<PlatilloAvanzado> obtenerPizzasAvanzadas() throws SQLException {
        List<PlatilloAvanzado> lista = new ArrayList<>();
        String sql = "SELECT idPlatillo, nombre, precio, tipo_masa, es_picante FROM platillos_avanzados";
        
        try (Statement st = con.createStatement(); 
             ResultSet rs = st.executeQuery(sql)) {
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