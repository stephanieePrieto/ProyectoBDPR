package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.modelo.sql.MySQLConnect;
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
 * Administra el despliegue dinámico de comandas activas, implementando
 * hilos de refresco automático para asegurar la sincronización con los meseros.
 * * @author Ricardo, Diego, Angel, Stephy
 */
public class ChefController implements Initializable {

    @FXML private HBox panelComandas;

    /**
     * Inicializa los componentes visuales y el hilo de actualización automática.
     * @param url Ubicación relativa para resolver el objeto raíz.
     * @param rb Recursos utilizados para localizar el objeto raíz.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarComandasActivas();
        configurarRefrescoAutomatico();   
    }    

    /**
     * Configura un objeto Timeline para consultar a MySQL cada 8 segundos.
     */
    private void configurarRefrescoAutomatico() {
        Timeline temporizador = new Timeline(
                new KeyFrame(Duration.seconds(8), event -> {
                    cargarComandasActivas();
                })
        );
        temporizador.setCycleCount(Timeline.INDEFINITE);
        temporizador.play();
    }

    /**
     * Fuerza la sincronización manual del panel de comandas.
     * @param event Evento de acción disparado por el control UI.
     */
    @FXML
    void clicActualizarManual(ActionEvent event) {
        cargarComandasActivas();
    }

    /**
     * Consulta las comandas en estado 'Pendiente' y reconstruye el panel visual.
     * Monta de forma dinámica los elementos hijos de tipo TarjetaPedido.fxml.
     */
    public void cargarComandasActivas() {
        panelComandas.getChildren().clear(); 
        
        MySQLConnect mysql = new MySQLConnect();
        String sqlPedidos = "SELECT idPedido, idMesa, "
                + "DATE_FORMAT(fechaHora, '%r') as hora FROM pedidos "
                + "WHERE estado = 'Pendiente' ORDER BY fechaHora ASC";
        
        try (Connection con = mysql.connection()) {
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

    /**
     * Compila y concatena el desglose de productos de un pedido.
     * @param con Conexión activa con MySQL.
     * @param idPedido Identificador del pedido.
     * @return Cadena formateada con los platillos y notas.
     * @throws SQLException Si ocurre error en la consulta.
     */
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