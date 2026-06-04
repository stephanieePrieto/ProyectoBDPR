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
 * Clase encargada de gestionar las operaciones de base de datos para el control de mesas.
 */
public class MesaDAO {

    /**
     * Obtiene una lista de todas las mesas registradas, ordenadas por su identificador.
     * @return Lista de objetos Mesa con su respectivo estado actual.
     */
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

    /**
     * Actualiza el estado de una mesa específica.
     * @param idMesa Identificador de la mesa.
     * @param estado Nuevo estado a asignar.
     * @return Verdadero si la actualización fue exitosa, falso en caso contrario.
     */
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

    /**
     * Cambia el estado de una mesa a 'Libre'.
     * @param idMesa Identificador de la mesa a liberar.
     * @return Verdadero si el estado se actualizó, falso en caso contrario.
     */
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
     * Consulta y concatena los platillos activos consumidos en una mesa.
     * @param idMesa Identificador de la mesa a consultar.
     * @return Cadena con el resumen de platillos o "Sin consumo" si no hay pedidos activos.
     */
    public String obtenerDetallesMesa(int idMesa) {
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