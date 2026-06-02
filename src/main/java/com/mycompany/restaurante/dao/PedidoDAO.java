package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.pojo.Pedido;
import com.mycompany.restaurante.modelo.pojo.Platillo;
import com.mycompany.restaurante.modelo.sql.OracleConnect;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de Acceso a Datos (DAO) para la gestión operativa de comandas en Oracle Cloud.
 */
public class PedidoDAO {
    private Connection conexion;

    public PedidoDAO() {
        this.conexion = OracleConnect.getConexion();
    }

    public PedidoDAO(Connection conexion) {
        this.conexion = conexion;
    }

    public int obtenerPedidoActivoPorMesa(int idMesa) throws SQLException {
        // Oracle: FETCH FIRST 1 ROWS ONLY sustituye al LIMIT 1
        String sql = "SELECT idPedido FROM pedidos WHERE idMesa = ? "
                   + "AND estado IN ('Pendiente', 'Listo') FETCH FIRST 1 ROWS ONLY";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idMesa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("idPedido");
            }
        }
        return -1;
    }

    public int crearNuevoPedido(int idMesa, int idEmpleado) throws SQLException {
        // En Oracle, las tablas con IDENTITY generan las keys automáticamente
        String sql = "INSERT INTO pedidos (idMesa, idEmpleado, estado, fechaHora) "
                   + "VALUES (?, ?, 'Pendiente', SYSDATE)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, new String[] {"idPedido"})) {
            ps.setInt(1, idMesa);
            ps.setInt(2, idEmpleado);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Error al crear el pedido en Oracle.");
    }

    public void guardarDetallesPedido(int idPedido, List<Platillo> carrito) throws SQLException {
        actualizarEstadoPedido(idPedido, "Pendiente");
        String sqlDelete = "DELETE FROM detallepedidos WHERE idPedido = ?";
        try (PreparedStatement psDelete = conexion.prepareStatement(sqlDelete)) {
            psDelete.setInt(1, idPedido);
            psDelete.executeUpdate();
        }

        String sqlInsert = "INSERT INTO detallepedidos (idPedido, idPlatillo, cantidad, estadoPlatillo) "
                         + "SELECT ?, idPlatillo, ?, ? FROM platillos WHERE nombre = ?";
        try (PreparedStatement psInsert = conexion.prepareStatement(sqlInsert)) {
            for (Platillo p : carrito) {
                psInsert.setInt(1, idPedido);
                psInsert.setInt(2, p.getCantidad());
                psInsert.setString(3, (p.getEstadoPlatillo() == null || p.getEstadoPlatillo().isEmpty()) ? "Normal" : p.getEstadoPlatillo());
                psInsert.setString(4, p.getNombre());
                if (psInsert.executeUpdate() == 0) throw new SQLException("El platillo '" + p.getNombre() + "' no existe.");
            }
        }
    }

    public boolean eliminarOActualizarPlatilloDePedido(int idPedido, String nombrePlatillo, int nuevaCantidad) throws SQLException {
        if (nuevaCantidad <= 0) {
            String sqlDelete = "DELETE FROM detallepedidos WHERE idPedido = ? AND idPlatillo = "
                             + "(SELECT idPlatillo FROM platillos WHERE nombre = ? FETCH FIRST 1 ROWS ONLY)";
            try (PreparedStatement ps = conexion.prepareStatement(sqlDelete)) {
                ps.setInt(1, idPedido); ps.setString(2, nombrePlatillo);
                return ps.executeUpdate() > 0;
            }
        } else {
            String sqlUpdate = "UPDATE detallepedidos SET cantidad = ? WHERE idPedido = ? AND idPlatillo = "
                             + "(SELECT idPlatillo FROM platillos WHERE nombre = ? FETCH FIRST 1 ROWS ONLY)";
            try (PreparedStatement ps = conexion.prepareStatement(sqlUpdate)) {
                ps.setInt(1, nuevaCantidad); ps.setInt(2, idPedido); ps.setString(3, nombrePlatillo);
                return ps.executeUpdate() > 0;
            }
        }
    }

    public List<Pedido> buscarPedidosPorEstado(String estado) throws SQLException {
        List<Pedido> lista = new ArrayList<>();
        String sql = "SELECT idPedido, estado, fechaHora FROM pedidos WHERE estado = ? ORDER BY fechaHora ASC";
        PlatilloDAO platilloDao = new PlatilloDAO(this.conexion);
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Pedido pedido = new Pedido();
                    int id = rs.getInt("idPedido");
                    pedido.setIdPedido(id);
                    pedido.setEstado(rs.getString("estado"));
                    pedido.setFechaHora(rs.getTimestamp("fechaHora").toLocalDateTime());
                    
                    List<Platillo> platos = platilloDao.obtenerPlatillosPorOrden(id);
                    StringBuilder sb = new StringBuilder();
                    for (Platillo p : platos) {
                        if (sb.length() > 0) sb.append(", ");
                        sb.append(p.getCantidad()).append(" ").append(p.getNombre());
                    }
                    pedido.setDetalleTexto(sb.toString());
                    lista.add(pedido);
                }
            }
        }
        return lista;
    }

    public boolean actualizarEstadoPedido(int idPedido, String nuevoEstado) throws SQLException {
        String sql = "UPDATE pedidos SET estado = ? WHERE idPedido = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idPedido);
            return ps.executeUpdate() > 0;
        }
    }
}