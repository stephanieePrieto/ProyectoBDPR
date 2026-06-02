package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.pojo.Usuario;
import com.mycompany.restaurante.utils.ConexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Clase de Acceso a Datos (DAO) para la gestión del personal y autenticación.
 * Centraliza las operaciones de validación de credenciales (Login) y mantenimiento 
 * del catálogo de usuarios (CRUD), vinculando los empleados con sus roles operativos.
 * * @author Ricardo, Diego, Angel, Stephy
 */
public class UsuarioDAO {

    /**
     * Valida las credenciales de un empleado para el acceso al sistema.
     * Utiliza un JOIN para obtener el nombre del rol asociado al ID de usuario.
     * @param user Nombre de usuario del empleado.
     * @param pass Contraseña asociada.
     * @return Objeto Usuario con sus datos si las credenciales son correctas, null en caso contrario.
     */
    public Usuario validarLogin(String user, String pass) {
        String sql = "SELECT e.idEmpleado, e.nombre, e.usuario, e.password, "
                + "e.idRol, r.nombre AS nombreRol "
                + "FROM empleados e "
                + "INNER JOIN rol r ON e.idRol = r.idRol "
                + "WHERE e.usuario = ? AND e.password = ?";
        
        try (Connection con = ConexionBD.conectar(); 
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
            System.err.println("Error en Login SQL: " + e.getMessage());
        }
        return null;
    }

    /**
     * Recupera el catálogo completo de empleados activos.
     * @return Lista de objetos Usuario.
     */
    public List<Usuario> obtenerEmpleados() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT e.idEmpleado, e.nombre, e.usuario, e.password, "
                + "e.idRol, r.nombre AS nombreRol "
                + "FROM empleados e "
                + "INNER JOIN rol r ON e.idRol = r.idRol";
                    
        try (Connection con = ConexionBD.conectar();
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
            System.err.println("Error obtenerEmpleados: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Registra un nuevo empleado en la base de datos.
     * @param u Objeto Usuario con la información del nuevo empleado.
     * @return true si la inserción fue exitosa.
     */
    public boolean registrarEmpleado(Usuario u) {
        String sql = "INSERT INTO empleados (nombre, usuario, password, idRol) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getUsername());
            ps.setString(3, u.getPassword());
            ps.setInt(4, u.getIdRol());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza los datos de un empleado existente mediante su ID único.
     * @param u Objeto Usuario con los datos modificados.
     * @return true si la actualización afectó a una fila.
     */
    public boolean actualizarEmpleado(Usuario u) {
        String sql = "UPDATE empleados SET nombre=?, usuario=?, password=?, idRol=? WHERE idEmpleado=?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, u.getNombre());
            ps.setString(2, u.getUsername());
            ps.setString(3, u.getPassword());
            ps.setInt(4, u.getIdRol());
            ps.setInt(5, u.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar: " + e.getMessage());
            return false;
        }
    }

    /**
     * Recupera exclusivamente los nombres de los empleados con rol de Mesero (ID 2).
     * @return Lista observable de nombres de meseros.
     */
    public ObservableList<String> obtenerNombresMeseros() {
        ObservableList<String> listaMeseros = FXCollections.observableArrayList();
        String sql = "SELECT nombre FROM empleados WHERE idRol = 2";
        
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                listaMeseros.add(rs.getString("nombre"));
            }
            System.out.println(">> [UsuarioDAO] Meseros cargados con éxito.");
        } catch (SQLException e) {
            System.err.println("Error en obtenerNombresMeseros: " + e.getMessage());
        }
        return listaMeseros;
    }
}