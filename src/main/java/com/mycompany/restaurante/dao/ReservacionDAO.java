package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.pojo.Reservacion;
import com.mycompany.restaurante.modelo.sql.OracleConnect;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de Acceso a Datos (DAO) para la gestión de reservaciones 
 */
public class ReservacionDAO {
    private Connection conexion;

    public ReservacionDAO() {
        this.conexion = OracleConnect.getConexion();
    }

    private void verificarConexion() throws SQLException {
        if (this.conexion == null || this.conexion.isClosed()) {
            this.conexion = OracleConnect.getConexion();
        }
    }

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

    public boolean insertarReservacion(Reservacion r) throws SQLException {
        verificarConexion();
        String idRealCliente = obtenerOGenerarIdCliente(r.getNombreCliente());
        String sql = "INSERT INTO reservaciones (folioUnico, id_cliente, idMesa, fecha, hora, num_personas, estado) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, r.getFolioUnico()); ps.setString(2, idRealCliente);
            ps.setInt(3, r.getIdMesa()); ps.setString(4, r.getFecha());
            ps.setString(5, r.getHora()); ps.setInt(6, r.getNumPersonas());
            ps.setString(7, r.getEstado());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizarReservacion(Reservacion r) throws SQLException {
        verificarConexion();
        String idRealCliente = obtenerOGenerarIdCliente(r.getNombreCliente());
        String sql = "UPDATE reservaciones SET id_cliente = ?, idMesa = ?, fecha = ?, hora = ?, num_personas = ? WHERE idReservacion = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, idRealCliente); ps.setInt(2, r.getIdMesa());
            ps.setString(3, r.getFecha()); ps.setString(4, r.getHora());
            ps.setInt(5, r.getNumPersonas()); ps.setInt(6, r.getIdReservacion());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean cancelarReservacion(int idReservacion) throws SQLException {
        verificarConexion();
        String sql = "UPDATE reservaciones SET estado = 'Cancelada' WHERE idReservacion = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idReservacion);
            return ps.executeUpdate() > 0;
        }
    }
}