//LISTA DE ESPERA DAO
package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.pojo.ListaDeEspera;
import com.mycompany.restaurante.utils.ConexionBD;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.mycompany.restaurante.modelo.sql.MySQLConnect;

/**
 * Clase de Acceso a Datos (DAO) para la gestión del flujo de clientes en lista de espera.
 * Encapsula las operaciones CRUD necesarias para administrar a los comensales
 * que aguardan por una mesa disponible, manteniendo la trazabilidad del estado
 * (en espera vs atendido) en la base de datos.
 * 
 * @author Ricardo, Diego, Angel, Stephi
 */
public class ListaEsperaDAO {

    /**
     * Registra un nuevo comensal en la base de datos con estado inicial 'EN_ESPERA'.
     * 
     * @param cliente Objeto POJO con la información capturada del cliente.
     * @return true si el registro fue exitoso, false si ocurrió un error.
     */
    public boolean insertarClienteEspera(ListaDeEspera cliente) {
        String sql = "INSERT INTO listaespera "
                + "(nombreCliente, pax, telefono) "
                + "VALUES (?, ?, ?)";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cliente.getNombreCliente());
            ps.setInt(2, cliente.getPax());
            ps.setString(3, cliente.getTelefono());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Recupera únicamente a los clientes que se encuentran pendientes de asignación de mesa.
     * 
     * @return lista observable compatible con TableView.
     */
    public ObservableList<ListaDeEspera> obtenerListaEspera() {
        ObservableList<ListaDeEspera> lista = FXCollections.observableArrayList();

        String sql = "SELECT * FROM listaespera "
                + "WHERE estado = 'EN_ESPERA'";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new ListaDeEspera(
                        rs.getInt("idEspera"),
                        rs.getString("nombreCliente"),
                        rs.getInt("pax"),
                        rs.getString("telefono"),
                        rs.getString("horaLlegada"),
                        rs.getString("estado")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }

    // elimina al cliente cuando ya se le asignó una mesa
    public boolean eliminarDeLista(int idEspera) {
        String sql = "DELETE FROM listaespera WHERE idEspera = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idEspera);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cambia el estado del cliente a ATENDIDO sin borrarlo de la base de datos.
     * 
     * @param idEspera identificador del registro en listaespera.
     * @return true si se actualizó correctamente.
     */
    public boolean atenderCliente(int idEspera) {
        String sql = "UPDATE listaespera "
                + "SET estado = 'ATENDIDO' "
                + "WHERE idEspera = ?";

        try (Connection conn = ConexionBD.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEspera);
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}