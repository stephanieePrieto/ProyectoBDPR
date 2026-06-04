package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.modelo.sql.OracleConnect; // Conexión a Oracle Cloud
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 * Controlador de componente autónomo correspondiente a la Tarjeta de Pedido.
 * Migrado a arquitectura Oracle Cloud.
 * @author Ricardo, Diego, Angel, Stephy
 */
public class TarjetaPedidoController {

    @FXML private Label lblMesa;
    @FXML private Label lblHora;
    @FXML private TextArea txtContenido;
    @FXML private Button btnListo;

    private int idPedido;
    private ChefController pantallaPadre;

    /**
     * Acopla la información del pedido sobre los controles de la tarjeta.
     * @param idPedido Identificador de la comanda en Oracle.
     * @param idMesa Número de locación que solicita el consumo.
     * @param hora Cadena con la hora de captura del registro.
     * @param textoPlatillos Bloque textual con el desglose del alimento.
     * @param padre Instancia de referencia del controlador principal.
     */
    public void configurarTarjeta(int idPedido, int idMesa, String hora, 
            String textoPlatillos, ChefController padre) {
        this.idPedido = idPedido;
        this.pantallaPadre = padre; 

        this.lblMesa.setText("MESA " + idMesa);
        this.lblHora.setText("Pedido #" + idPedido + " - " + hora);
        this.txtContenido.setText(textoPlatillos);
    }

    /**
     * Modifica el estado de la orden en Oracle Cloud y remueve el componente visual.
     * @param event Evento de acción disparado por el botón "Listo".
     */

@FXML
void clicDespachar(ActionEvent event) {
    // 1. SELECT: Obtenemos el objeto 'info' completo.
    // Usamos el alias 'p' para la tabla.
    String sqlSelect = "SELECT p.info FROM pedidos p WHERE p.idPedido = ?";
    String sqlUpdate = "UPDATE pedidos p SET p.info = auditoria_pedido_typ(?, ?) WHERE p.idPedido = ?";
    
    try (Connection con = OracleConnect.getConexion()) {
        java.sql.Timestamp fechaOriginal = null;
        
        // Obtenemos el objeto info
        try (PreparedStatement psSel = con.prepareStatement(sqlSelect)) {
            psSel.setInt(1, idPedido);
            try (ResultSet rs = psSel.executeQuery()) {
                if (rs.next()) {
                    // Accedemos al objeto como una estructura (STRUCT)
                    java.sql.Struct struct = (java.sql.Struct) rs.getObject(1); // <--- Índice 1
                    Object[] attrs = struct.getAttributes();
                    fechaOriginal = (java.sql.Timestamp) attrs[0]; // <--- Índice 0 (fecha)
                }
            }
        }
        
        // Actualizamos usando los atributos extraídos
        if (fechaOriginal != null) {
            try (PreparedStatement psUp = con.prepareStatement(sqlUpdate)) {
                psUp.setTimestamp(1, fechaOriginal);
                psUp.setString(2, "Listo");
                psUp.setInt(3, idPedido);
                psUp.executeUpdate();
            }
        }
        
        if (pantallaPadre != null) pantallaPadre.cargarComandasActivas();
        
    } catch (SQLException e) {
        System.err.println("❌ Error al despachar pedido: " + e.getMessage());
        e.printStackTrace();
    }
}
}