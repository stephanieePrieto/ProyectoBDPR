package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.pojo.Platillo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de Acceso a Datos (DAO) para la gestión del catálogo de platillos.
 * Centraliza las operaciones CRUD sobre la tabla platillos en MySQL,
 * permitiendo altas, modificaciones, bajas lógicas y consultas del menú,
 * integrando ahora la llave de enlace con el inventario del almacén.
 */
public class PlatilloDAO {
    
    private Connection conexion;

    /**
     * Constructor parametrizado que inyecta una conexión activa.
     * @param conexion Objeto de conexión hacia la base de datos MySQL.
     */
    public PlatilloDAO(Connection conexion) {
        this.conexion = conexion;
    }

    /**
     * Auxiliar interno para traducir el texto del ComboBox al ID numérico.
     * @param nombreCategoria Cadena con el nombre o ID de la categoría.
     * @return El identificador numérico entero correspondiente (1 al 5).
     */
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

    /**
     * Recupera el listado completo de platillos activos (Disponibles).
     * Ejecuta un cruce (JOIN) con el almacén para extraer las existencias reales.
     * @return Una colección List con objetos Platillo.
     */
    public List<Platillo> obtenerPlatillosActivos() throws SQLException {
        List<Platillo> lista = new ArrayList<>();
        
        String sql = "SELECT p.*, a.stock AS stockDisponible " +
                     "FROM platillos p " +
                     "LEFT JOIN almacen a ON p.idInsumoClave = a.idMateriaPrima " +
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

    /**
     * Registra un nuevo platillo de forma persistente en la base de datos, 
     * asociándolo a un ingrediente en el almacén si es necesario.
     * @param platillo Objeto POJO con la información del alimento.
     * @return true si la inserción fue exitosa; false si no.
     */
    public boolean registrarPlatillo(Platillo platillo) {
        // Se agregó la columna idInsumoClave a la consulta de inserción
        String sql = "INSERT INTO platillos (nombre, descripcion, precio, "
                + "estado, idCategoria, imagen, idInsumoClave) VALUES (?, ?, ?, 'Disponible', ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, platillo.getNombre());
            ps.setString(2, platillo.getDescripcion());
            ps.setDouble(3, platillo.getPrecio());
            ps.setInt(4, obtenerIdCategoriaNumerico(platillo.getCategoria()));
            ps.setString(5, platillo.getImagen());
            
            // Validación de seguridad para la llave foránea
            if (platillo.getIdInsumoClave() > 0) {
                ps.setInt(6, platillo.getIdInsumoClave());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al insertar platillo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza la información de un platillo existente en el sistema,
     * permitiendo modificar también su dependencia de inventario.
     * @param platillo Objeto POJO con los datos modificados.
     * @return true si se actualizó el registro; false si no.
     */
    public boolean actualizarPlatillo(Platillo platillo) {
        // Se agregó idInsumoClave a los atributos actualizables
        String sql = "UPDATE platillos SET nombre = ?, descripcion = ?, "
                + "precio = ?, idCategoria = ?, imagen = ?, idInsumoClave = ? WHERE idPlatillo = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, platillo.getNombre());
            ps.setString(2, platillo.getDescripcion());
            ps.setDouble(3, platillo.getPrecio());
            ps.setInt(4, obtenerIdCategoriaNumerico(platillo.getCategoria()));
            ps.setString(5, platillo.getImagen());
            
            // Validación de seguridad para la llave foránea
            if (platillo.getIdInsumoClave() > 0) {
                ps.setInt(6, platillo.getIdInsumoClave());
            } else {
                ps.setNull(6, java.sql.Types.INTEGER);
            }
            
            ps.setInt(7, platillo.getIdPlatillo());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar platillo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Recupera el desglose de platillos vinculados a una comanda.
     * @param idOrden Identificador numérico único del pedido.
     * @return Colección List conteniendo los artículos asociados.
     * @throws SQLException Si ocurre un error al procesar el JOIN relacional.
     */
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

    /**
     * Aplica una baja lógica a un platillo modificando su estado a 'Inactivo'.
     * @param idPlatillo Identificador único numérico del platillo.
     * @return true si el estado cambió exitosamente; false si no.
     */
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