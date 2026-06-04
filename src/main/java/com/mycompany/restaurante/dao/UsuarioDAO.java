package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.pojo.Usuario;
import com.mycompany.restaurante.modelo.sql.OracleConnect; 
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Clase encargada de gestionar las operaciones de base de datos para usuarios.
 */
public class UsuarioDAO {

    /**
     * Verifica las credenciales de un usuario en la base de datos.
     * @param user Nombre de usuario.
     * @param pass Contraseña del usuario.
     * @return Objeto Usuario si las credenciales son correctas, o null si no existen.
     */
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

    /**
     * Obtiene una lista completa de todos los empleados registrados.
     * @return Lista de objetos Usuario.
     */
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

    /**
     * Registra un nuevo empleado en la base de datos.
     * @param u Objeto Usuario con la información del nuevo empleado.
     * @return Verdadero si el registro fue exitoso, falso en caso contrario.
     */
    public boolean registrarEmpleado(Usuario u) {
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

    /**
     * Actualiza la información de un empleado existente mediante su ID.
     * @param u Objeto Usuario con los nuevos datos.
     * @return Verdadero si la actualización fue exitosa, falso en caso contrario.
     */
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

    /**
     * Consulta los nombres de todos los empleados que tienen asignado el rol de mesero.
     * @return Lista observable de cadenas con los nombres de los meseros.
     */
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