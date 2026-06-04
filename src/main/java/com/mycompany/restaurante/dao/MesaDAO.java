package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.pojo.Mesa;
import com.mycompany.restaurante.modelo.sql.OracleConnect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de Acceso a Datos (DAO) para la gestión del inventario físico de mesas.
 */
public class MesaDAO {

    public List<Mesa> listarMesas() {
        List<Mesa> mesas = new ArrayList<>();
        String sql = "SELECT idMesa, estado FROM mesa ORDER BY idMesa ASC";

        try (Connection con = OracleConnect.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Mesa mesa = new Mesa();
                mesa.setIdMesa(rs.getInt("idMesa"));
                mesa.setEstado(rs.getString("estado"));
                mesas.add(mesa);
            }
        } catch (SQLException e) {
            System.err.println("Error listar mesas en Oracle: " + e.getMessage());
            e.printStackTrace();
        }
        return mesas;
    }

    public boolean actualizarEstadoMesa(int idMesa, String estado) {
        String sql = "UPDATE mesa SET estado = ? WHERE idMesa = ?";

        try (Connection con = OracleConnect.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, estado);
            ps.setInt(2, idMesa);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean liberarMesa(int idMesa) {
        String sql = "UPDATE mesa SET estado = 'Libre' WHERE idMesa = ?";

        try (Connection con = OracleConnect.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idMesa);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Construye un resumen textual de platillos servidos usando LISTAGG.
     */
    public String obtenerDetallesMesa(int idMesa) {
        // Oracle usa LISTAGG para concatenar filas. Usamos '||' para concatenar texto.
        String sql = "SELECT LISTAGG(dp.cantidad || 'x ' || p.nombre, ', ') WITHIN GROUP (ORDER BY p.nombre) AS detalles "
                   + "FROM pedidos pe "
                   + "INNER JOIN detallepedidos dp ON pe.idPedido = dp.idPedido "
                   + "INNER JOIN platillos p ON dp.idPlatillo = p.idPlatillo "
                   + "WHERE pe.idMesa = ? AND pe.estado IN ('Pendiente', 'Listo', 'Preparando')";

        try (Connection con = OracleConnect.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idMesa);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String detalles = rs.getString("detalles");
                    if (detalles != null && !detalles.isEmpty()) {
                        return detalles;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "Sin consumo";
    }
}