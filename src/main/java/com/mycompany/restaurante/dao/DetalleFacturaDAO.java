package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.pojo.DetalleFactura;
import com.mycompany.restaurante.modelo.pojo.Platillo;
import com.mycompany.restaurante.modelo.sql.OracleConnect;
import java.sql.*;
import java.util.*;
import javafx.collections.*;

/**
 * Clase encargada de gestionar las operaciones de base de datos para la generación y consulta de detalles de facturación.
 */
public class DetalleFacturaDAO {

    /**
     * Calcula la suma total de los productos consumidos en una mesa, considerando solo pedidos pendientes o listos.
     * @param idMesa Identificador de la mesa.
     * @return El valor del subtotal calculado.
     */
    public double obtenerSubtotalMesa(int idMesa) {
        String sql = "SELECT SUM(p.precio * dp.cantidad) AS subtotal "
                   + "FROM detallepedidos dp JOIN platillos p ON dp.idPlatillo = p.idPlatillo "
                   + "JOIN pedidos pe ON dp.idPedido = pe.idPedido "
                   + "WHERE pe.idMesa = ? AND pe.info.ESTADO IN ('Pendiente', 'Listo')"; 

        try (Connection con = OracleConnect.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMesa);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("subtotal") : 0.0;
            }
        } catch (SQLException e) { e.printStackTrace(); return 0.0; }
    }

    /**
     * Obtiene la lista de platillos que conforman el pedido actual de una mesa específica.
     * @param idMesa Identificador de la mesa.
     * @return Lista de objetos Platillo con nombre, cantidad y precio unitario.
     */
    public List<Platillo> obtenerDetallePedidoPorMesa(int idMesa) {
        List<Platillo> lista = new ArrayList<>();
        String sql = "SELECT p.nombre, dp.cantidad, p.precio "
                   + "FROM detallepedidos dp JOIN platillos p ON dp.idPlatillo = p.idPlatillo "
                   + "JOIN pedidos pe ON dp.idPedido = pe.idPedido "
                   + "WHERE pe.idMesa = ? AND pe.info.ESTADO IN ('Pendiente', 'Listo') "
                   + "ORDER BY pe.idPedido DESC";

        try (Connection con = OracleConnect.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMesa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Platillo platillo = new Platillo();
                    platillo.setNombre(rs.getString("nombre"));
                    platillo.setCantidad(rs.getInt("cantidad"));
                    platillo.setPrecio(rs.getDouble("precio"));
                    lista.add(platillo);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    /**
     * Recupera el identificador del pedido más reciente asociado a una mesa, sin importar su estado.
     * @param idMesa Identificador de la mesa.
     * @return El ID del pedido encontrado, o 0 si no existe.
     */
    public int obtenerPedidoPorMesa(int idMesa) {
        String sql = "SELECT idPedido FROM vista_pedidos_plana " +
                     "WHERE idMesa = ? AND ESTADO IN ('Pendiente', 'Listo', 'Pagado') " +
                     "ORDER BY idPedido DESC FETCH FIRST 1 ROWS ONLY";

        try (Connection con = OracleConnect.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMesa);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("idPedido") : 0;
            }
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    /**
     * Obtiene los detalles de facturación correspondientes al último pedido pagado de una mesa.
     * @param idMesa Identificador de la mesa.
     * @return Lista observable con el detalle desglosado para la factura.
     */
    public ObservableList<DetalleFactura> obtenerDetallesFactura(int idMesa) {
        ObservableList<DetalleFactura> lista = FXCollections.observableArrayList();
        
        String sql = "SELECT p.nombre, d.cantidad, p.precio, (p.precio * d.cantidad) AS fila_subtotal "
                   + "FROM detallepedidos d "
                   + "JOIN platillos p ON d.idPlatillo = p.idPlatillo "
                   + "JOIN vista_pedidos_plana pe ON d.idPedido = pe.idPedido "
                   + "WHERE pe.idMesa = ? AND pe.ESTADO = 'Pagado' "
                   + "AND pe.idPedido = (SELECT MAX(idPedido) FROM vista_pedidos_plana WHERE idMesa = ? AND ESTADO = 'Pagado')";

        try (Connection con = OracleConnect.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMesa); 
            ps.setInt(2, idMesa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new DetalleFactura(
                        "90101501", rs.getInt("cantidad"), "E48", 
                        rs.getString("nombre"), rs.getDouble("precio"), rs.getDouble("fila_subtotal")
                    ));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }
}