package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.pojo.Pago;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Clase de Acceso a Datos (DAO) para el procesamiento de pagos.
 * Implementa lógica transaccional crítica para asegurar que el registro de ingresos,
 * la actualización de estados de pedidos y la liberación de mesas físicas 
 * ocurran de manera síncrona y atómica.
 * * @author Ricardo, Diego, Angel, Stephy
 */
public class PagoDAO {

    /**
     * Registra un pago de cliente y ejecuta las actualizaciones de estado correspondientes.
     * Esta operación utiliza transacciones manuales (AutoCommit = false) para garantizar
     * que el sistema mantenga la integridad referencial. Si alguna de las tres operaciones 
     * SQL falla, se ejecuta un 'rollback' para revertir cualquier cambio parcial.
     * * Pasos de la transacción:
     * 1. Inserta el registro del pago en la tabla 'pagos'.
     * 2. Actualiza el estado del pedido asociado a 'Pagado'.
     * 3. Libera la mesa correspondiente cambiando su estado a 'Libre'.
     * * * @param pago Objeto con los detalles del pago (monto, método, ID pedido).
     * @param idMesa Identificador físico de la mesa que se liberará.
     * @return true si la transacción completa fue exitosa, false si hubo un error.
     */
    public boolean registrarPago(Pago pago, int idMesa) {
        String sqlPago = "INSERT INTO pagos (total, metodoPago, idPedido) VALUES (?, ?, ?)";
        String sqlPedido = "UPDATE pedidos SET estado = 'Pagado' WHERE idPedido = ?";
        String sqlMesa = "UPDATE mesa SET estado = 'Libre' WHERE idMesa = ?";
        
        Connection con = null;
        try {
            con = com.mycompany.restaurante.utils.ConexionBD.conectar();
            con.setAutoCommit(false); 

            // 1. Insertar el ticket de pago en la BD
            try (PreparedStatement psPago = con.prepareStatement(sqlPago)) {
                psPago.setDouble(1, pago.getTotal());
                psPago.setString(2, pago.getMetodo());
                psPago.setInt(3, pago.getIdPedido());
                psPago.executeUpdate();
            }

            // 2. Cambiar el estado de la comanda a Pagado
            try (PreparedStatement psPedido = con.prepareStatement(sqlPedido)) {
                psPedido.setInt(1, pago.getIdPedido());
                psPedido.executeUpdate();
            }

            // 3. Liberar la mesa para que vuelva a estar disponible
            try (PreparedStatement psMesa = con.prepareStatement(sqlMesa)) {
                psMesa.setInt(1, idMesa);
                psMesa.executeUpdate();
            }

            con.commit(); 
            System.out.println(">> [PagoDAO] Transacción completada con éxito. Mesa " + idMesa + " liberada.");
            return true;
            
        } catch (SQLException e) {
            if (con != null) {
                try { 
                    con.rollback(); 
                    System.err.println(">> [PagoDAO] Transacción cancelada (Rollback ejecutado).");
                } catch (SQLException ex) { 
                    ex.printStackTrace(); 
                }
            }
            System.err.println("Error crítico en la transacción de pago: " + e.getMessage());
            return false;
        } finally {
            if (con != null) {
                try { con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}