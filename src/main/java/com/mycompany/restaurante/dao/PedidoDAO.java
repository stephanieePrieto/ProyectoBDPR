package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.pojo.Pedido;
import com.mycompany.restaurante.modelo.pojo.Platillo;
import com.mycompany.restaurante.modelo.sql.MySQLConnect;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de Acceso a Datos (DAO) para la gestión operativa de comandas.
 * Sostiene las consultas transaccionales a MySQL para la creación de pedidos,
 * sincronización de carritos de consumo y el rastreo de estados.
 * * @author Ricardo, Diego, Angel, Stephy
 */
public class PedidoDAO {
    private Connection conexion;

    /**
     * Constructor predeterminado que inicializa la conexión global a la BD.
     */
    public PedidoDAO() {
        this.conexion = MySQLConnect.getConexion();
    }

    /**
     * Constructor parametrizado que inyecta una conexión activa.
     * @param conexion Objeto de conexión hacia la base de datos MySQL.
     */
    public PedidoDAO(Connection conexion) {
        this.conexion = conexion;
    }

    /**
     * Recupera el ID del pedido activo vinculado a una mesa específica.
     * @param idMesa Identificador único numérico de la mesa.
     * @return El ID del pedido si se encuentra activo; -1 si no existe.
     * @throws SQLException Si ocurre una anomalía en la consulta SQL.
     */
    public int obtenerPedidoActivoPorMesa(int idMesa) throws SQLException {
        String sql = "SELECT idPedido FROM pedidos WHERE idMesa = ? "
                + "AND estado IN ('Pendiente', 'Listo')";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idMesa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idPedido");
                }
            }
        }
        return -1;
    }

    /**
     * Inserta una nueva comanda en estado inicial 'Pendiente' en MySQL.
     * @param idMesa Identificador numérico de la mesa que consume.
     * @param idEmpleado Identificador numérico del mesero responsable.
     * @return La clave primaria auto-incrementable generada para el pedido.
     * @throws SQLException Si se produce un error durante la inserción.
     */
    public int crearNuevoPedido(int idMesa, int idEmpleado) throws SQLException {
        String sql = "INSERT INTO pedidos (idMesa, idEmpleado, estado) "
                + "VALUES (?, ?, 'Pendiente')";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idMesa);
            ps.setInt(2, idEmpleado);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("Error al crear el pedido.");
    }

    /**
     * Sincroniza y vuelca el contenido del carrito en los detalles de la orden.
     * Limpia filas previas e inserta el nuevo bloque.
     * @param idPedido Identificador del pedido que se va a actualizar.
     * @param carrito Colección con la lista de platillos comandados.
     * @throws SQLException Si un platillo no existe o la inserción falla.
     */
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
                String nota = (p.getEstadoPlatillo() == null || p.getEstadoPlatillo().isEmpty()) 
                        ? "Normal" : p.getEstadoPlatillo();
                psInsert.setString(3, nota); 
                psInsert.setString(4, p.getNombre());
                if (psInsert.executeUpdate() == 0) {
                    throw new SQLException("El platillo '" + p.getNombre() + "' no existe en la BD.");
                }
            }
        }
    }

    /**
     * Remueve o actualiza la cantidad de un artículo dentro de la comanda.
     * @param idPedido Identificador único de la orden activa.
     * @param nombrePlatillo Cadena descriptiva que identifica al alimento.
     * @param nuevaCantidad Monto numérico final.
     * @return true si la tabla sufrió modificaciones; false en caso contrario.
     */
    public boolean eliminarOActualizarPlatilloDePedido(int idPedido, String nombrePlatillo, int nuevaCantidad) throws SQLException {
        if (nuevaCantidad <= 0) {
            String sqlDelete = "DELETE FROM detallepedidos WHERE idPedido = ? "
                    + "AND idPlatillo = (SELECT idPlatillo FROM platillos WHERE nombre = ? LIMIT 1)";
            try (PreparedStatement ps = conexion.prepareStatement(sqlDelete)) {
                ps.setInt(1, idPedido);
                ps.setString(2, nombrePlatillo);
                return ps.executeUpdate() > 0;
            }
        } else {
            String sqlUpdate = "UPDATE detallepedidos SET cantidad = ? "
                    + "WHERE idPedido = ? AND idPlatillo = (SELECT idPlatillo FROM platillos WHERE nombre = ? LIMIT 1)";
            try (PreparedStatement ps = conexion.prepareStatement(sqlUpdate)) {
                ps.setInt(1, nuevaCantidad);
                ps.setInt(2, idPedido);
                ps.setString(3, nombrePlatillo);
                return ps.executeUpdate() > 0;
            }
        }
    }

    /**
     * Consulta masiva del historial de comandas filtrado por un estado específico.
     * @param estado Criterio de búsqueda ("Pendiente", "Listo", "Cancelado").
     * @return Una colección con los pedidos encontrados.
     */
    public List<Pedido> buscarPedidosPorEstado(String estado) throws SQLException {
        List<Pedido> lista = new ArrayList<>();
        String sql = "SELECT idPedido, estado, fechaHora FROM pedidos "
                + "WHERE estado = ? ORDER BY fechaHora ASC";
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

    /**
     * Modifica de forma directa el estado contextual de una comanda.
     * @param idPedido Identificador numérico.
     * @param nuevoEstado Destino del estado ("Listo", "Pagado", "Cancelado").
     * @return true si se actualizó el registro.
     */
    public boolean actualizarEstadoPedido(int idPedido, String nuevoEstado) throws SQLException {
        String sql = "UPDATE pedidos SET estado = ? WHERE idPedido = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idPedido);
            return ps.executeUpdate() > 0;
        }
    }
}