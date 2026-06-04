package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.pojo.ProductoAlmacen;
import com.mycompany.restaurante.modelo.sql.OracleConnect;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AlmacenDAO {

    private final String TABLE_NAME = "INVENTARIOMATERIAPRIMA";

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