package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.pojo.Pago;
import com.mycompany.restaurante.modelo.sql.OracleConnect; // Cambio a la conexión de Oracle Cloud
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Clase de Acceso a Datos (DAO) para el procesamiento de pagos.
 * Implementa lógica transaccional para Oracle Cloud.
 */
public class PagoDAO {

    /**
     * Registra un pago y libera la mesa mediante una transacción atómica.
     * @param pago Objeto con los detalles del pago.
     * @param idMesa Identificador físico de la mesa.
     * @return true si la transacción fue exitosa.
     */
    public boolean registrarPago(Pago pago, int idMesa) {
        String sqlPago = "INSERT INTO pagos (total, metodoPago, idPedido) VALUES (?, ?, ?)";
        String sqlPedido = "UPDATE pedidos SET estado = 'Pagado' WHERE idPedido = ?";
        String sqlMesa = "UPDATE mesa SET estado = 'Libre' WHERE idMesa = ?";
        
        Connection con = null;
        try {
            // Usamos la conexión de Oracle Cloud
            con = OracleConnect.getConexion();
            con.setAutoCommit(false); 

            // 1. Insertar el ticket de pago
            try (PreparedStatement psPago = con.prepareStatement(sqlPago)) {
                psPago.setDouble(1, pago.getTotal());
                psPago.setString(2, pago.getMetodo());
                psPago.setInt(3, pago.getIdPedido());
                psPago.executeUpdate();
            }

            // 2. Actualizar estado del pedido
            try (PreparedStatement psPedido = con.prepareStatement(sqlPedido)) {
                psPedido.setInt(1, pago.getIdPedido());
                psPedido.executeUpdate();
            }

            // 3. Liberar mesa
            try (PreparedStatement psMesa = con.prepareStatement(sqlMesa)) {
                psMesa.setInt(1, idMesa);
                psMesa.executeUpdate();
            }

            con.commit(); 
            System.out.println(">> [PagoDAO] Transacción completada en Oracle Cloud. Mesa " + idMesa + " liberada.");
            return true;
            
        } catch (SQLException e) {
            if (con != null) {
                try { 
                    con.rollback(); 
                    System.err.println(">> [PagoDAO] Error: Rollback ejecutado en Oracle.");
                } catch (SQLException ex) { 
                    ex.printStackTrace(); 
                }
            }
            System.err.println("Error crítico en transacción de pago (Oracle): " + e.getMessage());
            return false;
        } finally {
            if (con != null) {
                try { 
                    con.setAutoCommit(true); // Restaurar estado normal antes de cerrar
                    con.close(); 
                } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}