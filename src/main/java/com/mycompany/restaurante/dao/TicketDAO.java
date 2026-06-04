package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.sql.OracleConnect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Clase de Acceso a Datos (DAO) para el registro histórico de emisión de tickets.
 */
public class TicketDAO {

    /**
     * Registra en la base de datos la emisión de un ticket físico o digital para un pedido.
     * * @param idPedido El identificador único del pedido al que se le generó el ticket.
     * @return true si el registro fue insertado exitosamente, false en caso de error.
     */
    public boolean generarTicket(int idPedido) {
        String sql = "INSERT INTO ticket (id_pedido, fecha_emision) VALUES (?, SYSDATE)";

        try (Connection con = OracleConnect.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idPedido);
            

            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al generar el ticket en Oracle: " + e.getMessage());
            return false;
        }
    }
}