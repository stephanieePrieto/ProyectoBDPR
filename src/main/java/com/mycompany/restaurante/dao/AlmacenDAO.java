package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.pojo.ProductoAlmacen;
import com.mycompany.restaurante.utils.ConexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de Acceso a Datos (DAO) para el control del inventario de insumos.
 * Conectada directamente con la tabla 'almacen' de tu MySQL real.
 * * @author Stephanie Hernandez
 */
public class AlmacenDAO {

    public List<ProductoAlmacen> obtenerProductos() {
        List<ProductoAlmacen> lista = new ArrayList<>();
        // Ajustamos los nombres a como están en tu base de datos real
        String sql = "SELECT idMateriaPrima, nombre, stock, unidad, stockMinimo "
                + "FROM almacen";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                lista.add(new ProductoAlmacen(
                    rs.getInt("idMateriaPrima"), // Traducido de la BD
                    rs.getString("nombre"),
                    rs.getDouble("stock"),       // Traducido de la BD
                    rs.getString("unidad"),
                    rs.getDouble("stockMinimo")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar almacén: " + e.getMessage());
        }
        return lista;
    }

    public boolean registrarProducto(ProductoAlmacen p) {
        // Apuntamos a la tabla 'almacen' y columna 'stock'
        String sql = "INSERT INTO almacen (nombre, stock, unidad, "
                + "stockMinimo) VALUES (?, ?, ?, ?)";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getCantidad()); // El POJO le llama cantidad, la BD stock
            ps.setString(3, p.getUnidad());
            ps.setDouble(4, p.getStockMinimo());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar insumo: " + e.getMessage());
            return false;
        }
    }
    
    public boolean eliminarProducto(int idProducto) {
        // Apuntamos a la tabla 'almacen' y su llave primaria real
        String sql = "DELETE FROM almacen WHERE idMateriaPrima = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idProducto);
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al eliminar insumo: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarProducto(ProductoAlmacen p) {
        // Apuntamos a la tabla 'almacen'
        String sql = "UPDATE almacen SET nombre=?, stock=?, unidad=?, "
                + "stockMinimo=? WHERE idMateriaPrima=?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getCantidad());
            ps.setString(3, p.getUnidad());
            ps.setDouble(4, p.getStockMinimo());
            ps.setInt(5, p.getIdProducto());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar insumo: " + e.getMessage());
            return false;
        }
    }
}
