package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.pojo.ProductoAlmacen;
import com.mycompany.restaurante.modelo.sql.OracleConnect;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase encargada de gestionar las operaciones de base de datos para el inventario de materia prima.
 */
public class AlmacenDAO {

    private final String TABLE_NAME = "INVENTARIOMATERIAPRIMA";

    /**
     * Obtiene la lista completa de productos registrados en el inventario.
     * @return Lista de objetos ProductoAlmacen con sus datos actuales.
     */
    public List<ProductoAlmacen> obtenerProductos() {
        List<ProductoAlmacen> lista = new ArrayList<>();
        String sql = "SELECT idMateriaPrima, nombre, stock, unidad, stockMinimo FROM " + TABLE_NAME;

        try (Connection con = OracleConnect.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                lista.add(new ProductoAlmacen(
                    rs.getInt("idMateriaPrima"),
                    rs.getString("nombre"),
                    rs.getDouble("stock"),
                    rs.getString("unidad"),
                    rs.getDouble("stockMinimo")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al cargar inventario: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Registra un nuevo producto en el inventario.
     * @param p Objeto ProductoAlmacen con los datos del nuevo insumo.
     * @return Verdadero si el registro fue exitoso, falso en caso contrario.
     */
    public boolean registrarProducto(ProductoAlmacen p) {
        String sql = "INSERT INTO " + TABLE_NAME + " (nombre, stock, unidad, stockMinimo) VALUES (?, ?, ?, ?)";

        try (Connection con = OracleConnect.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getCantidad());
            ps.setString(3, p.getUnidad());
            ps.setDouble(4, p.getStockMinimo());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar insumo: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Elimina un producto del inventario mediante su identificador.
     * @param idProducto Identificador del producto a eliminar.
     * @return Verdadero si la eliminación fue exitosa, falso en caso contrario.
     */
    public boolean eliminarProducto(int idProducto) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE idMateriaPrima = ?";

        try (Connection con = OracleConnect.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idProducto);
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al eliminar insumo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza la información de un producto existente en el inventario.
     * @param p Objeto ProductoAlmacen con los datos actualizados.
     * @return Verdadero si la actualización fue exitosa, falso en caso contrario.
     */
    public boolean actualizarProducto(ProductoAlmacen p) {
        String sql = "UPDATE " + TABLE_NAME + " SET nombre=?, stock=?, unidad=?, stockMinimo=? WHERE idMateriaPrima=?";

        try (Connection con = OracleConnect.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, p.getNombre());
            ps.setDouble(2, p.getCantidad());
            ps.setString(3, p.getUnidad());
            ps.setDouble(4, p.getStockMinimo());
            ps.setInt(5, p.getIdProducto());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Oracle, Error al actualizar insumo: " + e.getMessage());
            return false;
        }
    }
}