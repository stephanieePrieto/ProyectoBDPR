package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.modelo.sql.OracleConnect; // Conexión a Oracle Cloud
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

/**
 * Controlador del sistema de visualización de cocina (KDS).
 * Migrado a Oracle Cloud.
 * @author Ricardo, Diego, Angel, Stephy
 */
public class ChefController implements Initializable {

    @FXML private HBox panelComandas;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarComandasActivas();
        configurarRefrescoAutomatico();   
    }   

    private void configurarRefrescoAutomatico() {
        Timeline temporizador = new Timeline(
                new KeyFrame(Duration.seconds(8), event -> {
                    cargarComandasActivas();
                })
        );
        temporizador.setCycleCount(Timeline.INDEFINITE);
        temporizador.play();
    }

    @FXML
    void clicActualizarManual(ActionEvent event) {
        cargarComandasActivas();
    }

    public void cargarComandasActivas() {
        panelComandas.getChildren().clear(); 
        
        // Oracle: Usamos TO_CHAR para obtener la hora en formato de 12 horas
        String sqlPedidos = "SELECT idPedido, idMesa, TO_CHAR(fechaHora, 'HH:MI:SS AM') as hora "
                          + "FROM pedidos WHERE estado = 'Pendiente' ORDER BY fechaHora ASC";
        
        try (Connection con = OracleConnect.getConexion()) {
            if (con == null) return;
            
            try (PreparedStatement psPedidos = con.prepareStatement(sqlPedidos);
                 ResultSet rsPedidos = psPedidos.executeQuery()) {
                
                while (rsPedidos.next()) {
                    int idPedido = rsPedidos.getInt("idPedido");
                    int idMesa = rsPedidos.getInt("idMesa");
                    String hora = rsPedidos.getString("hora");
                    
                    String textoPlatillos = obtenerDetallesTexto(con, idPedido);
                    
                    FXMLLoader loader = App.getFXMLLoader("TarjetaPedido");
                    Parent tarjeta = loader.load();

                    TarjetaPedidoController tarjetaCtrl = loader.getController();
                    tarjetaCtrl.configurarTarjeta(idPedido, idMesa, hora, textoPlatillos, this);
                    
                    panelComandas.getChildren().add(tarjeta);
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private String obtenerDetallesTexto(Connection con, int idPedido) throws SQLException {
        StringBuilder sb = new StringBuilder();
        String notaGeneral = "";
        String sqlDetalles = "SELECT p.nombre, dp.cantidad, dp.estadoPlatillo "
                + "FROM detallepedidos dp "
                + "JOIN platillos p ON dp.idPlatillo = p.idPlatillo "
                + "WHERE dp.idPedido = ?";
        
        try (PreparedStatement ps = con.prepareStatement(sqlDetalles)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String nombre = rs.getString("nombre");
                    int cant = rs.getInt("cantidad");
                    String notaEspecial = rs.getString("estadoPlatillo");
                    
                    if (notaEspecial != null 
                            && !notaEspecial.trim().isEmpty() 
                            && !notaEspecial.equalsIgnoreCase("Normal")) {
                        notaGeneral = notaEspecial.toUpperCase();
                    }
                    sb.append(String.format("%dx %s\n", cant, nombre));
                }
                sb.append("---------------------\n");
                
                if (!notaGeneral.isEmpty()) {
                    sb.append(" NOTA GENERAL:\n ")
                            .append(notaGeneral).append("\n");
                    sb.append("---------------------\n");
                }        
            }
        }
        return sb.toString();
    }
}