package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.modelo.sql.MySQLConnect;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 * Controlador de componente autónomo correspondiente a la Tarjeta de Pedido.
 * Gobierna el comportamiento de los nodos de comanda en el panel de cocina, 
 * permitiendo despachar pedidos de forma síncrona.
 * * @author Ricardo, Diego, Angel, Stephy
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
     * @param idPedido Identificador de la comanda en MySQL.
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
     * Modifica el estado de la orden en la BD y remueve el componente visual.
     * Ejecuta un UPDATE transaccional sobre la tabla para cambiar la orden a
     * estado 'Listo' e instruye al controlador padre refrescar la cocina.
     * @param event Evento de acción disparado por el botón "Listo".
     */
    @FXML
    void clicDespachar(ActionEvent event) {
        MySQLConnect mysql = new MySQLConnect();
        String sql = "UPDATE pedidos SET estado = 'Listo' WHERE idPedido = ?";
        
        try (Connection con = mysql.connection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, idPedido);
            ps.executeUpdate();
            
            // Actualiza la vista del Chef automáticamente
            pantallaPadre.cargarComandasActivas();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}