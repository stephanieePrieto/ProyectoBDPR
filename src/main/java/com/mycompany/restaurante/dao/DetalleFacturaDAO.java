package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.pojo.DetalleFactura;
import com.mycompany.restaurante.modelo.pojo.Platillo;
import com.mycompany.restaurante.utils.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Clase de Acceso a Datos (DAO) para la gestión de detalles de facturación.
 * Encapsula la lógica para consolidar los consumos por mesa, aislando
 * las comandas activas de los pedidos liquidados históricamente.
 * * @author Stephanie Hernandez
 */
public class DetalleFacturaDAO {

    /**
     * Calcula el subtotal acumulado de un pedido activo en el comedor.
     * Restringe la búsqueda estrictamente a estados 'Pendiente' o 'Listo'.
     */
    public double obtenerSubtotalMesa(int idMesa) {
        double subtotal = 0.0;
        String sql = "SELECT SUM(p.precio * dp.cantidad) AS subtotal "
                + "FROM detallepedidos dp "
                + "JOIN platillos p ON dp.idPlatillo = p.idPlatillo "
                + "JOIN pedidos pe ON dp.idPedido = pe.idPedido "
                + "WHERE pe.idMesa = ? AND pe.estado IN ('Pendiente', 'Listo')";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMesa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    subtotal = rs.getDouble("subtotal");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener subtotal: " + e.getMessage());
        }
        return subtotal;
    }

    /**
     * Recupera el listado de platillos activos de una mesa en servicio.
     */
    public List<Platillo> obtenerDetallePedidoPorMesa(int idMesa) {
        List<Platillo> lista = new ArrayList<>();
        String sql = "SELECT p.nombre, dp.cantidad, p.precio "
                + "FROM detallepedidos dp "
                + "JOIN platillos p ON dp.idPlatillo = p.idPlatillo "
                + "JOIN pedidos pe ON dp.idPedido = pe.idPedido "
                + "WHERE pe.idMesa = ? AND pe.estado IN ('Pendiente', 'Listo') "
                + "ORDER BY pe.idPedido DESC";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
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
        } catch (SQLException e) {
            System.err.println("Error al obtener detalle: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Obtiene la última comanda activa o recientemente pagada de una mesa.
     */
    public int obtenerPedidoPorMesa(int idMesa) {
        int idPedido = 0;
        String sql = "SELECT idPedido FROM pedidos "
                + "WHERE idMesa = ? AND estado IN ('Pendiente', 'Listo', 'Pagado') "
                + "ORDER BY idPedido DESC LIMIT 1";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMesa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    idPedido = rs.getInt("idPedido");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener idPedido: " + e.getMessage());
        }
        return idPedido;
    }

    /**
     * Recupera los conceptos de facturación CFDI del último pedido LIQUIDADO.
     * Previene colisiones si la mesa vuelve a ser ocupada en el mismo turno.
     */
    public ObservableList<DetalleFactura> obtenerDetallesFactura(int idMesa) {
        ObservableList<DetalleFactura> lista = FXCollections.observableArrayList();
        String sql = "SELECT p.nombre, d.cantidad, p.precio, "
                + "(p.precio * d.cantidad) AS fila_subtotal "
                + "FROM detallepedidos d "
                + "JOIN platillos p ON d.idPlatillo = p.idPlatillo "
                + "JOIN pedidos pe ON d.idPedido = pe.idPedido "
                + "WHERE pe.idMesa = ? AND pe.estado = 'Pagado' "
                + "AND pe.idPedido = (SELECT MAX(idPedido) FROM pedidos "
                + "WHERE idMesa = ? AND estado = 'Pagado')";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idMesa);
            ps.setInt(2, idMesa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new DetalleFactura(
                            "90101501", // Clave SAT: Restaurantes
                            rs.getInt("text_cantidad"),
                            "E48",      // Clave SAT: Servicio
                            rs.getString("nombre"),
                            rs.getDouble("precio"),
                            rs.getDouble("fila_subtotal")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en detalles factura: " + e.getMessage());
        }
        return lista;
    }
}