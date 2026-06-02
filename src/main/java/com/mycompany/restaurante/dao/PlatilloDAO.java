package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.pojo.Platillo;
import com.mycompany.restaurante.modelo.sql.OracleConnect; // Importación correcta
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de Acceso a Datos (DAO) para la gestión del catálogo de platillos en Oracle Cloud.
 */
public class PlatilloDAO {
    
    private Connection conexion;

    public PlatilloDAO() {
        this.conexion = OracleConnect.getConexion();
    }

    public PlatilloDAO(Connection conexion) {
        this.conexion = conexion;
    }

    private int obtenerIdCategoriaNumerico(String nombreCategoria) {
        if (nombreCategoria == null) return 1;
        switch (nombreCategoria.trim()) {
            case "Pizzas": case "1": return 1;
            case "Bebidas": case "2": return 2;
            case "Pasteles": case "Postres": case "3": return 3;
            case "Extras": case "4": return 4;
            case "Especiales": case "5": return 5;
            default: return 1;
        }
    }

    public List<Platillo> obtenerPlatillosActivos() throws SQLException {
        List<Platillo> lista = new ArrayList<>();
        // Consulta adaptada para Oracle
        String sql = "SELECT p.*, a.stock AS stockDisponible " +
                     "FROM platillos p " +
                     "LEFT JOIN inventariomateriaprima a ON p.idInsumoClave = a.idMateriaPrima " +
                     "WHERE p.estado = 'Disponible'";
                     
        try (PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                Platillo p = new Platillo();
                p.setIdPlatillo(rs.getInt("idPlatillo"));
                p.setNombre(rs.getString("nombre"));
                p.setPrecio(rs.getDouble("precio"));
                p.setImagen(rs.getString("imagen"));
                p.setIdCategoria(rs.getInt("idCategoria"));
                p.setIdInsumoClave(rs.getInt("idInsumoClave"));
                p.setStockDisponible(rs.getInt("stockDisponible"));
                lista.add(p);
            }
        }
        return lista;
    }   

    public boolean registrarPlatillo(Platillo platillo) {
        String sql = "INSERT INTO platillos (nombre, descripcion, precio, estado, idCategoria, imagen, idInsumoClave) "
                   + "VALUES (?, ?, ?, 'Disponible', ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, platillo.getNombre());
            ps.setString(2, platillo.getDescripcion());
            ps.setDouble(3, platillo.getPrecio());
            ps.setInt(4, obtenerIdCategoriaNumerico(platillo.getCategoria()));
            ps.setString(5, platillo.getImagen());
            
            if (platillo.getIdInsumoClave() > 0) {
                ps.setInt(6, platillo.getIdInsumoClave());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar platillo en Oracle: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarPlatillo(Platillo platillo) {
        String sql = "UPDATE platillos SET nombre = ?, descripcion = ?, precio = ?, idCategoria = ?, "
                   + "imagen = ?, idInsumoClave = ? WHERE idPlatillo = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, platillo.getNombre());
            ps.setString(2, platillo.getDescripcion());
            ps.setDouble(3, platillo.getPrecio());
            ps.setInt(4, obtenerIdCategoriaNumerico(platillo.getCategoria()));
            ps.setString(5, platillo.getImagen());
            
            if (platillo.getIdInsumoClave() > 0) {
                ps.setInt(6, platillo.getIdInsumoClave());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }
            ps.setInt(7, platillo.getIdPlatillo());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar platillo en Oracle: " + e.getMessage());
            return false;
        }
    }

    public List<Platillo> obtenerPlatillosPorOrden(int idOrden) throws SQLException {
        List<Platillo> listaPlatillos = new ArrayList<>();
        String sql = "SELECT p.nombre, p.descripcion, p.precio, dp.cantidad "
                   + "FROM detallepedidos dp "
                   + "JOIN platillos p ON dp.idPlatillo = p.idPlatillo "
                   + "WHERE dp.idPedido = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Platillo platillo = new Platillo();
                    platillo.setNombre(rs.getString("nombre"));
                    platillo.setDescripcion(rs.getString("descripcion"));
                    platillo.setPrecio(rs.getDouble("precio"));
                    platillo.setCantidad(rs.getInt("cantidad")); 
                    listaPlatillos.add(platillo);
                }
            }
        }
        return listaPlatillos;
    }

    public boolean darDeBajaPlatillo(int idPlatillo) {
        String sql = "UPDATE platillos SET estado = 'Inactivo' WHERE idPlatillo = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idPlatillo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}