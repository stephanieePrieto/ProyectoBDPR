//TICKET DAO
package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.utils.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Clase de Acceso a Datos (DAO) para el registro histórico de emisión de tickets.
 * Encapsula la lógica necesaria para persistir cada impresión de comprobante, 
 * permitiendo auditar cuántas veces se ha generado un ticket para una orden específica.
 * * @author Ricardo, Diego, Angel, Stephi
 */
public class TicketDAO {

    /**
     * Registra en la base de datos la emisión de un ticket físico o digital para un pedido.
     * Utiliza la función nativa 'NOW()' de MySQL para asegurar que el registro de 
     * emisión sea preciso respecto al tiempo del servidor.
     * * @param idPedido El identificador único del pedido al que se le generó el ticket.
     * @return true si el registro fue insertado exitosamente, false en caso de error de conexión.
     */
    public boolean generarTicket(int idPedido) {
        // SQL para insertar un registro de ticket asociado a un pedido
        // Asegura la trazabilidad mediante la fecha/hora de emisión
        String sql = "INSERT INTO ticket (id_pedido, fecha_emision) VALUES (?, NOW())";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idPedido);
            
            // Retorna verdadero si al menos una fila fue insertada
            int filasAfectadas = ps.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al generar el ticket en BD: " + e.getMessage());
            return false;
        }
    }
}