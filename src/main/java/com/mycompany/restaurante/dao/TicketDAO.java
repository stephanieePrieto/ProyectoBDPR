package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.sql.OracleConnect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Clase de Acceso a Datos (DAO) para el registro histórico de emisión de tickets.
 * Adaptada para Oracle Cloud.
 */
public class TicketDAO {

    /**
     * Registra en la base de datos la emisión de un ticket físico o digital para un pedido.
     * Utiliza 'SYSDATE' de Oracle para la precisión temporal.
     * * @param idPedido El identificador único del pedido al que se le generó el ticket.
     * @return true si el registro fue insertado exitosamente, false en caso de error.
     */
    public boolean generarTicket(int idPedido) {
        // Oracle utiliza SYSDATE para obtener la fecha y hora actual del servidor
        String sql = "INSERT INTO ticket (id_pedido, fecha_emision) VALUES (?, SYSDATE)";

        try (Connection con = OracleConnect.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idPedido);
            
            // Retorna verdadero si la inserción fue exitosa
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al generar el ticket en Oracle: " + e.getMessage());
            return false;
        }
    }
}