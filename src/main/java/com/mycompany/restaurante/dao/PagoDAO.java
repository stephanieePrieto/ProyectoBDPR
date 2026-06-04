package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.pojo.Pago;
import com.mycompany.restaurante.modelo.sql.OracleConnect; 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Clase de Acceso a Datos (DAO) para el procesamiento de pagos.
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
    
    // CORRECCIÓN AQUÍ: Usamos el constructor del tipo para actualizar el estado dentro del objeto 'info'
    // p.info = AUDITORIA_PEDIDO_TYP(fecha_actual, 'Pagado')
    String sqlPedido = "UPDATE pedidos p SET p.info = AUDITORIA_PEDIDO_TYP(p.info.FECHA_HORA, 'Pagado') WHERE p.idPedido = ?";
    
    String sqlMesa = "UPDATE mesa SET estado = 'Libre' WHERE idMesa = ?";
    
    Connection con = null;
    try {
        con = OracleConnect.getConexion();
        con.setAutoCommit(false); 

        // 1. Insertar el ticket de pago
        try (PreparedStatement psPago = con.prepareStatement(sqlPago)) {
            psPago.setDouble(1, pago.getTotal());
            psPago.setString(2, pago.getMetodo());
            psPago.setInt(3, pago.getIdPedido());
            psPago.executeUpdate();
        }

        // 2. Actualizar estado del pedido (USANDO EL CONSTRUCTOR DEL TIPO)
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
        return true;
        
    } catch (SQLException e) {
        if (con != null) {
            try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        }
        System.err.println("Error crítico en transacción de pago: " + e.getMessage());
        return false;
    } finally {
        if (con != null) {
            try { con.setAutoCommit(true); con.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}
}