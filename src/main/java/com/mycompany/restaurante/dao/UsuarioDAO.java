package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.pojo.Usuario;
import com.mycompany.restaurante.modelo.sql.OracleConnect; // Conexión a la nube
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Clase de Acceso a Datos (DAO) para la gestión del personal y autenticación en Oracle.
 */
public class UsuarioDAO {

    public Usuario validarLogin(String user, String pass) {
        String sql = "SELECT e.idEmpleado, e.nombre, e.usuario, e.password, "
                   + "e.idRol, r.nombre AS nombreRol "
                   + "FROM empleados e "
                   + "INNER JOIN rol r ON e.idRol = r.idRol "
                   + "WHERE e.usuario = ? AND e.password = ?";
        
        try (Connection con = OracleConnect.getConexion(); 
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, user);
            ps.setString(2, pass);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                        rs.getInt("idEmpleado"),
                        rs.getString("nombre"),
                        rs.getString("usuario"),
                        rs.getString("password"),
                        rs.getInt("idRol"),
                        rs.getString("nombreRol")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en Login Oracle: " + e.getMessage());
        }
        return null;
    }

    public List<Usuario> obtenerEmpleados() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT e.idEmpleado, e.nombre, e.usuario, e.password, "
                   + "e.idRol, r.nombre AS nombreRol "
                   + "FROM empleados e "
                   + "INNER JOIN rol r ON e.idRol = r.idRol";
                    
        try (Connection con = OracleConnect.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                lista.add(new Usuario(
                    rs.getInt("idEmpleado"),
                    rs.getString("nombre"),
                    rs.getString("usuario"),
                    rs.getString("password"),
                    rs.getInt("idRol"),
                    rs.getString("nombreRol")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error obtenerEmpleados Oracle: " + e.getMessage());
        }
        return lista;
    }

    public boolean registrarEmpleado(Usuario u) {
        // Nota: En Oracle, si usas IDENTITY para idEmpleado, no es necesario incluirlo aquí
        String sql = "INSERT INTO empleados (nombre, usuario, password, idRol) VALUES (?, ?, ?, ?)";
        try (Connection con = OracleConnect.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getUsername());
            ps.setString(3, u.getPassword());
            ps.setInt(4, u.getIdRol());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar en Oracle: " + e.getMessage());
            return false;
        }
    }

    public boolean actualizarEmpleado(Usuario u) {
        String sql = "UPDATE empleados SET nombre=?, usuario=?, password=?, idRol=? WHERE idEmpleado=?";
        try (Connection con = OracleConnect.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getUsername());
            ps.setString(3, u.getPassword());
            ps.setInt(4, u.getIdRol());
            ps.setInt(5, u.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar en Oracle: " + e.getMessage());
            return false;
        }
    }

    public ObservableList<String> obtenerNombresMeseros() {
        ObservableList<String> listaMeseros = FXCollections.observableArrayList();
        String sql = "SELECT nombre FROM empleados WHERE idRol = 2";
        
        try (Connection con = OracleConnect.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                listaMeseros.add(rs.getString("nombre"));
            }
        } catch (SQLException e) {
            System.err.println("Error en obtenerNombresMeseros Oracle: " + e.getMessage());
        }
        return listaMeseros;
    }
}