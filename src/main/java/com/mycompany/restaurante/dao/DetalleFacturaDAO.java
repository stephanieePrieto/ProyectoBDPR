package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.pojo.DetalleFactura;
import com.mycompany.restaurante.modelo.pojo.Platillo;
import com.mycompany.restaurante.modelo.sql.OracleConnect;
import java.sql.*;
import java.util.*;
import javafx.collections.*;

public class DetalleFacturaDAO {

    public double obtenerSubtotalMesa(int idMesa) {
        String sql = "SELECT SUM(p.precio * dp.cantidad) AS subtotal "
                   + "FROM detallepedidos dp JOIN platillos p ON dp.idPlatillo = p.idPlatillo "
                   + "JOIN pedidos pe ON dp.idPedido = pe.idPedido "
                   + "WHERE pe.idMesa = ? AND pe.estado IN ('Pendiente', 'Listo')";

        try (Connection con = OracleConnect.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMesa);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble("subtotal") : 0.0;
            }
        } catch (SQLException e) { e.printStackTrace(); return 0.0; }
    }

    public List<Platillo> obtenerDetallePedidoPorMesa(int idMesa) {
        List<Platillo> lista = new ArrayList<>();
        String sql = "SELECT p.nombre, dp.cantidad, p.precio "
                   + "FROM detallepedidos dp JOIN platillos p ON dp.idPlatillo = p.idPlatillo "
                   + "JOIN pedidos pe ON dp.idPedido = pe.idPedido "
                   + "WHERE pe.idMesa = ? AND pe.estado IN ('Pendiente', 'Listo') "
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

    public int obtenerPedidoPorMesa(int idMesa) {
        // Oracle: FETCH FIRST 1 ROWS ONLY para sustituir LIMIT 1
        String sql = "SELECT idPedido FROM pedidos WHERE idMesa = ? AND estado IN ('Pendiente', 'Listo', 'Pagado') "
                   + "ORDER BY idPedido DESC FETCH FIRST 1 ROWS ONLY";

        try (Connection con = OracleConnect.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMesa);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("idPedido") : 0;
            }
        } catch (SQLException e) { e.printStackTrace(); return 0; }
    }

    public ObservableList<DetalleFactura> obtenerDetallesFactura(int idMesa) {
        ObservableList<DetalleFactura> lista = FXCollections.observableArrayList();
        // Subconsulta para el pedido pagado más reciente
        String sql = "SELECT p.nombre, d.cantidad, p.precio, (p.precio * d.cantidad) AS fila_subtotal "
                   + "FROM detallepedidos d JOIN platillos p ON d.idPlatillo = p.idPlatillo "
                   + "JOIN pedidos pe ON d.idPedido = pe.idPedido "
                   + "WHERE pe.idMesa = ? AND pe.estado = 'Pagado' "
                   + "AND pe.idPedido = (SELECT MAX(idPedido) FROM pedidos WHERE idMesa = ? AND estado = 'Pagado')";

        try (Connection con = OracleConnect.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMesa); ps.setInt(2, idMesa);
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