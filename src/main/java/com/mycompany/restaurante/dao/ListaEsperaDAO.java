package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.pojo.ListaDeEspera;
import com.mycompany.restaurante.modelo.sql.OracleConnect;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Clase de Acceso a Datos (DAO) para la gestión del flujo de clientes en lista de espera.
 * Adaptada para Oracle Cloud Autonomous Database.
 */
public class ListaEsperaDAO {

    /**
     * Registra un nuevo comensal en la base de datos.
     * En Oracle, los valores de ID suelen ser manejados por IDENTITY columns,
     * por lo que no es necesario insertarlos manualmente.
     */
    public boolean insertarClienteEspera(ListaDeEspera cliente) {
        String sql = "INSERT INTO listaespera (nombreCliente, pax, telefono, estado) VALUES (?, ?, ?, 'EN_ESPERA')";

        try (Connection conn = OracleConnect.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cliente.getNombreCliente());
            ps.setInt(2, cliente.getPax());
            ps.setString(3, cliente.getTelefono());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Recupera a los clientes pendientes utilizando la conexión a Oracle Cloud.
     */
    public ObservableList<ListaDeEspera> obtenerListaEspera() {
        ObservableList<ListaDeEspera> lista = FXCollections.observableArrayList();
        String sql = "SELECT * FROM listaespera WHERE estado = 'EN_ESPERA'";

        try (Connection conn = OracleConnect.getConexion();
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Elimina un registro de la lista de espera.
     */
    public boolean eliminarDeLista(int idEspera) {
        String sql = "DELETE FROM listaespera WHERE idEspera = ?";

        try (Connection con = OracleConnect.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idEspera);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza el estado a ATENDIDO en la base de datos Oracle.
     * @param idEspera
     * @return 
     */
    public boolean atenderCliente(int idEspera) {
        String sql = "UPDATE listaespera SET estado = 'ATENDIDO' WHERE idEspera = ?";

        try (Connection conn = OracleConnect.getConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idEspera);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}