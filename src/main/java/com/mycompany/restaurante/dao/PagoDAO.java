package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.pojo.Pago;
import com.mycompany.restaurante.modelo.sql.OracleConnect; 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Clase encargada de gestionar las operaciones de base de datos relacionadas con los pagos.
 */
public class PagoDAO {

    /**
     * Registra el pago, actualiza el estado del pedido y libera la mesa en una sola transacción atómica.
     * @param pago Objeto con los detalles del pago realizado.
     * @param idMesa Identificador físico de la mesa a liberar.
     * @return Verdadero si la transacción se completó con éxito, falso si ocurrió un error.
     */
    public boolean registrarPago(Pago pago, int idMesa) {
        String sqlPago = "INSERT INTO pagos (total, metodoPago, idPedido) VALUES (?, ?, ?)";
        
        // Actualiza el estado del pedido a 'Pagado' utilizando el tipo definido en la base de datos
        String sqlPedido = "UPDATE pedidos p SET p.info = AUDITORIA_PEDIDO_TYP(p.info.FECHA_HORA, 'Pagado') WHERE p.idPedido = ?";
        
        String sqlMesa = "UPDATE mesa SET estado = 'Libre' WHERE idMesa = ?";
        
        Connection con = null;
        try {
            con = OracleConnect.getConexion();
            con.setAutoCommit(false); 

            // 1. Insertar el registro del pago
            try (PreparedStatement psPago = con.prepareStatement(sqlPago)) {
                psPago.setDouble(1, pago.getTotal());
                psPago.setString(2, pago.getMetodo());
                psPago.setInt(3, pago.getIdPedido());
                psPago.executeUpdate();
            }

            // 2. Actualizar estado del pedido a pagado
            try (PreparedStatement psPedido = con.prepareStatement(sqlPedido)) {
                psPedido.setInt(1, pago.getIdPedido());
                psPedido.executeUpdate();
            }

            // 3. Cambiar el estado de la mesa a 'Libre'
            try (PreparedStatement psMesa = con.prepareStatement(sqlMesa)) {
                psMesa.setInt(1, idMesa);
                psMesa.executeUpdate();
            }

            con.commit(); 
            return true;
            
        } catch (SQLException e) {
            // Si ocurre un error, deshace todos los cambios realizados en la transacción
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.err.println("Error crítico en transacción de pago: " + e.getMessage());
            return false;
        } finally {
            // Restaura la conexión y la cierra
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}