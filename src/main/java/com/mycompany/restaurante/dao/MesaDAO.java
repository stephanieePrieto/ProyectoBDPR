//MESA DAO
package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.pojo.Mesa;
import com.mycompany.restaurante.utils.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase de Acceso a Datos (DAO) para la gestión del inventario físico de mesas.
 * Encapsula la lógica necesaria para consultar el estado operativo de las mesas,
 * así como para obtener el detalle de los platillos servidos en cada una y
 * realizar cambios de estado.
 *
 * @author Ricardo, Diego, Angel, Stephi
 */
public class MesaDAO {

    /**
     * Recupera el listado completo de mesas registradas en el restaurante junto con
     * su estado actual.
     *
     * @return Una lista con todos los objetos Mesa encontrados.
     */
    public List<Mesa> listarMesas() {
        List<Mesa> mesas = new ArrayList<>();

        String sql = "SELECT idMesa, estado FROM mesa ORDER BY idMesa ASC";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Mesa mesa = new Mesa();

                mesa.setIdMesa(rs.getInt("idMesa"));
                mesa.setEstado(rs.getString("estado"));

                mesas.add(mesa);
            }

        } catch (Exception e) {
            System.out.println("Error listar mesas: " + e.getMessage());
            e.printStackTrace();
        }

        return mesas;
    }

    /**
     * Actualiza el estado de una mesa en la base de datos.
     * Este método se puede usar para cambiar una mesa a Libre, Ocupada o Sucia.
     *
     * @param idMesa El identificador de la mesa.
     * @param estado El nuevo estado de la mesa.
     * @return true si se actualizó correctamente.
     */
    public boolean actualizarEstadoMesa(int idMesa, String estado) {
        String sql = "UPDATE mesa SET estado = ? WHERE idMesa = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, estado);
            ps.setInt(2, idMesa);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza el estado de una mesa a Libre.
     * Esta operación se usa cuando se libera una mesa después de pagar o limpiar.
     *
     * @param idMesa El identificador de la mesa.
     * @return true si la mesa fue liberada correctamente.
     */
    public boolean liberarMesa(int idMesa) {
        String sql = "UPDATE mesa SET estado = 'Libre' WHERE idMesa = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idMesa);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Construye un resumen textual de los platillos actualmente servidos en una mesa.
     * Realiza una consulta entre pedidos, detallepedidos y platillos.
     *
     * @param idMesa El identificador de la mesa.
     * @return Texto con los platillos y cantidades, o "Sin consumo".
     */
    public String obtenerDetallesMesa(int idMesa) {
        String sql = "SELECT GROUP_CONCAT(CONCAT(dp.cantidad, 'x ', p.nombre) SEPARATOR ', ') AS detalles "
                + "FROM pedidos pe "
                + "INNER JOIN detallepedidos dp ON pe.idPedido = dp.idPedido "
                + "INNER JOIN platillos p ON dp.idPlatillo = p.idPlatillo "
                + "WHERE pe.idMesa = ? AND pe.estado IN ('Pendiente', 'Listo', 'Preparando')";

        try (Connection con = ConexionBD.conectar();
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

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "Sin consumo";
    }
}