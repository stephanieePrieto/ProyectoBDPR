package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.pojo.Reservacion;
import com.mycompany.restaurante.modelo.sql.OracleConnect;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase encargada de gestionar las operaciones de base de datos para las reservaciones.
 */
public class ReservacionDAO {
    private Connection conexion;

    /**
     * Constructor que inicializa la conexión con la base de datos.
     */
    public ReservacionDAO() {
        this.conexion = OracleConnect.getConexion();
    }

    /**
     * Verifica si la conexión está activa, de lo contrario intenta reconectar.
     * @throws SQLException Si ocurre un error al verificar o establecer la conexión.
     */
    private void verificarConexion() throws SQLException {
        if (this.conexion == null || this.conexion.isClosed()) {
            this.conexion = OracleConnect.getConexion();
        }
    }

    /**
     * Identifica reservaciones confirmadas que excedieron el tiempo de tolerancia (15 minutos)
     * y las cancela, liberando las mesas correspondientes.
     */
    public void depurarReservacionesVencidas() {
        String sqlUpdateReservas = "UPDATE reservaciones SET estado = 'Cancelada' "
                                 + "WHERE TRUNC(fecha) = TRUNC(SYSDATE) AND estado = 'Confirmada' "
                                 + "AND (hora + INTERVAL '15' MINUTE) < SYSTIMESTAMP";

        String sqlLiberarMesas = "UPDATE mesa SET estado = 'Libre' "
                               + "WHERE idMesa IN (SELECT idMesa FROM reservaciones "
                               + "WHERE TRUNC(fecha) = TRUNC(SYSDATE) AND estado = 'Cancelada' "
                               + "AND idMesa IS NOT NULL)";

        try {
            verificarConexion();
            this.conexion.setAutoCommit(false);
            try (PreparedStatement psRes = this.conexion.prepareStatement(sqlUpdateReservas);
                 PreparedStatement psMesa = this.conexion.prepareStatement(sqlLiberarMesas)) {
                psRes.executeUpdate();
                psMesa.executeUpdate();
                this.conexion.commit();
            } catch (SQLException e) {
                this.conexion.rollback();
            } finally {
                this.conexion.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            System.err.println("Error en depuración Oracle: " + ex.getMessage());
        }
    }

    /**
     * Busca una reservación específica utilizando su código de folio único.
     * @param folio Código identificador de la reservación.
     * @return Objeto Reservacion si se encuentra, o null si no existe.
     * @throws SQLException Si ocurre un error durante la consulta.
     */
    public Reservacion buscarPorFolio(String folio) throws SQLException {
        verificarConexion();
        String sql = "SELECT r.idReservacion, r.folioUnico, r.id_cliente, c.nombre AS nombre_cliente, "
                    + "r.idMesa, r.fecha, r.hora, r.num_personas, r.estado FROM reservaciones r "
                    + "LEFT JOIN clientes c ON r.id_cliente = c.id_cliente "
                    + "WHERE r.folioUnico = ? FETCH FIRST 1 ROWS ONLY";

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, folio);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Reservacion(rs.getInt("idReservacion"), rs.getString("folioUnico"),
                            rs.getString("id_cliente"), rs.getString("nombre_cliente"),
                            rs.getInt("idMesa"), rs.getString("fecha"), rs.getString("hora"),
                            rs.getInt("num_personas"), rs.getString("estado"));
                }
            }
        }
        return null;
    }

    /**
     * Obtiene una lista de todas las reservaciones registradas, ordenadas de la más reciente a la antigua.
     * @return Lista de objetos Reservacion.
     * @throws SQLException Si ocurre un error al obtener los datos.
     */
    public List<Reservacion> obtenerTodasLasReservaciones() throws SQLException {
        verificarConexion();
        List<Reservacion> lista = new ArrayList<>();
        String sql = "SELECT r.idReservacion, r.folioUnico, r.id_cliente, c.nombre AS nombre_cliente, "
                    + "r.idMesa, r.fecha, r.hora, r.num_personas, r.estado FROM reservaciones r "
                    + "LEFT JOIN clientes c ON r.id_cliente = c.id_cliente ORDER BY r.idReservacion DESC";

        try (PreparedStatement ps = conexion.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Reservacion(rs.getInt("idReservacion"), rs.getString("folioUnico"),
                        rs.getString("id_cliente"), (rs.getString("nombre_cliente") == null ? "Cliente General" : rs.getString("nombre_cliente")),
                        rs.getInt("idMesa"), rs.getString("fecha"), rs.getString("hora"),
                        rs.getInt("num_personas"), rs.getString("estado")));
            }
        }
        return lista;
    }

    /**
     * Obtiene el ID de un cliente existente por nombre o crea un registro nuevo si no existe.
     * @param nombreCliente Nombre del cliente a buscar o registrar.
     * @return ID del cliente obtenido o generado.
     * @throws SQLException Si ocurre un error al consultar o insertar el cliente.
     */
    public String obtenerOGenerarIdCliente(String nombreCliente) throws SQLException {
        verificarConexion();
        String sqlBuscar = "SELECT id_cliente FROM clientes WHERE nombre = ? FETCH FIRST 1 ROWS ONLY";
        try (PreparedStatement psBuscar = conexion.prepareStatement(sqlBuscar)) {
            psBuscar.setString(1, nombreCliente);
            try (ResultSet rs = psBuscar.executeQuery()) { if (rs.next()) return rs.getString("id_cliente"); }
        }

        String sqlMax = "SELECT id_cliente FROM clientes WHERE id_cliente LIKE 'CP%' ORDER BY id_cliente DESC FETCH FIRST 1 ROWS ONLY";
        String nuevoId = "CP001";
        try (PreparedStatement psMax = conexion.prepareStatement(sqlMax); ResultSet rsMax = psMax.executeQuery()) {
            if (rsMax.next()) {
                String maxId = rsMax.getString("id_cliente");
                int numero = Integer.parseInt(maxId.substring(2)) + 1;
                nuevoId = String.format("CP%03d", numero);
            }
        } catch (Exception e) { nuevoId = "CP" + String.valueOf(System.currentTimeMillis()).substring(10); }

        String sqlInsertarCliente = "INSERT INTO clientes (id_cliente, nombre) VALUES (?, ?)";
        try (PreparedStatement psIns = conexion.prepareStatement(sqlInsertarCliente)) {
            psIns.setString(1, nuevoId); psIns.setString(2, nombreCliente);
            psIns.executeUpdate();
        }
        return nuevoId;
    }

    /**
     * Inserta una nueva reservación en la base de datos.
     * @param r Objeto Reservacion con la información a guardar.
     * @return Verdadero si la inserción fue exitosa, falso en caso contrario.
     * @throws SQLException Si ocurre un error al realizar la inserción.
     */
    public boolean insertarReservacion(Reservacion r) throws SQLException {
        verificarConexion();
        String idRealCliente = obtenerOGenerarIdCliente(r.getNombreCliente());
        
        String sql = "INSERT INTO reservaciones (folioUnico, id_cliente, idMesa, fecha, hora, num_personas, estado) " +
                     "VALUES (?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'), ?, ?, ?)";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, r.getFolioUnico()); 
            ps.setString(2, idRealCliente);
            ps.setInt(3, r.getIdMesa()); 
            ps.setString(4, r.getFecha()); 
            ps.setString(5, r.getHora()); 
            ps.setInt(6, r.getNumPersonas());
            ps.setString(7, r.getEstado());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Actualiza la información de una reservación existente.
     * @param r Objeto Reservacion con los datos actualizados.
     * @return Verdadero si la actualización fue exitosa, falso en caso contrario.
     * @throws SQLException Si ocurre un error al realizar la actualización.
     */
    public boolean actualizarReservacion(Reservacion r) throws SQLException {
        verificarConexion();
        String idRealCliente = obtenerOGenerarIdCliente(r.getNombreCliente());
        
        String sql = "UPDATE reservaciones SET id_cliente = ?, idMesa = ?, fecha = TO_DATE(?, 'YYYY-MM-DD'), hora = ?, num_personas = ? " +
                     "WHERE idReservacion = ?";
        
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, idRealCliente); 
            ps.setInt(2, r.getIdMesa());
            ps.setString(3, r.getFecha());
            ps.setString(4, r.getHora());
            ps.setInt(5, r.getNumPersonas()); 
            ps.setInt(6, r.getIdReservacion());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cambia el estado de una reservación a 'Cancelada'.
     * @param idReservacion Identificador único de la reservación a cancelar.
     * @return Verdadero si el estado se cambió correctamente, falso en caso contrario.
     * @throws SQLException Si ocurre un error durante la actualización.
     */
    public boolean cancelarReservacion(int idReservacion) throws SQLException {
        verificarConexion();
        String sql = "UPDATE reservaciones SET estado = 'Cancelada' WHERE idReservacion = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idReservacion);
            return ps.executeUpdate() > 0;
        }
    }
}