package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.dao.AvanzadoDAO;
import com.mycompany.restaurante.modelo.pojo.PlatilloAvanzado;
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
import java.sql.Statement;

/**
 * Controlador del sistema de visualización de cocina (KDS).
 * Migrado a Oracle Cloud.
 */
public class ChefController implements Initializable {

    @FXML private HBox panelComandas;

    /**
     * Inicializa el monitor de la cocina cargando los tickets base e invoca un hilo 
     * concurrente (Timeline) que refrescará las comandas automáticamente.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarComandasActivas();
        configurarRefrescoAutomatico();   
    }   

    /**
     * Configura el motor de recarga para realizar polling a la Base de Datos
     * cada 8 segundos con la finalidad de simular un entorno asíncrono/RealTime.
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

    @FXML
    void clicActualizarManual(ActionEvent event) {
        cargarComandasActivas();
    }

    /**
     * Recaba las órdenes pendientes desde una vista pre-procesada de la base de datos, 
     * inyectando dinámicamente un nodo FXML (TarjetaPedido) por cada solicitud activa en el HBox.
     */
    public void cargarComandasActivas() {
        panelComandas.getChildren().clear(); 
        
        // CORRECCIÓN: Accedemos a los atributos dentro de la vista 'vista_pedidos_plana' de Oracle
        String sqlPedidos = "SELECT idPedido, idMesa, TO_CHAR(FECHA_HORA, 'HH:MI:SS AM') as hora "
                      + "FROM vista_pedidos_plana "
                      + "WHERE ESTADO = 'Pendiente' "
                      + "ORDER BY FECHA_HORA ASC";
        
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

    /**
     * Ensambla una cadena informativa agrupando los platillos bajo un mismo ID de Pedido.
     * @param con Conexión activa a la BD Oracle
     * @param idPedido El número general del pedido a desglosar
     * @return El string final con notas especiales procesadas y estructuradas.
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
                    
                    // Condicional para destacar observaciones en cocina (Ej: "SIN CEBOLLA")
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

    /**
     * Módulo de diagnóstico avanzado. Demuestra conceptos de bases de datos 
     * Objeto-Relacionales, accediendo a objetos Struct en Oracle Cloud para desglosar herencias.
     */
    @FXML
    private void clicVerConsolaAvanzada(ActionEvent event) {
        StringBuilder sb = new StringBuilder();
        sb.append("--- DIAGNÓSTICO POST-RELACIONAL ---\n\n");
        
        try (Connection con = OracleConnect.getConexion()) {
            AvanzadoDAO avDao = new AvanzadoDAO(con);
            
            // 1. Mostrar Herencia de la base de datos (Ejecuta mapeos desde objetos PL/SQL)
            sb.append("1. Pizzas con Herencia (Pizza_T):\n");
            for(PlatilloAvanzado p : avDao.obtenerPizzasAvanzadas()) {
                sb.append("- ").append(p.getNombre()).append(" | Masa: ").append(p.getTipoMasa()).append("\n");
            }
            
            // 2. Mostrar Coordenadas de Mesas (Tipo objeto coordenadas_typ)
            sb.append("\n2. Ubicación de Mesas (coordenadas_typ):\n");
            String sqlMesas = "SELECT numero, posicion FROM mesa"; 

            try(java.sql.Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sqlMesas)) {
                while(rs.next()){
                    int numero = rs.getInt("numero");
                    
                    // Extraemos la columna tipificada como un objeto 'Struct' (java.sql.Struct)
                    java.sql.Struct objPosicion = (java.sql.Struct) rs.getObject("posicion");
                    
                    if (objPosicion != null) {
                        // Accedemos a las variables internas de la Estructura en Oracle Cloud (mapa_x y mapa_y)
                        Object[] atributos = objPosicion.getAttributes();
                        int x = ((java.math.BigDecimal) atributos[0]).intValue();
                        int y = ((java.math.BigDecimal) atributos[1]).intValue();
                        
                        sb.append("Mesa #").append(numero)
                          .append(" (X:").append(x).append(", Y:").append(y).append(")\n");
                    } else {
                        sb.append("Mesa #").append(numero).append(" (Sin coordenadas)\n");
                    }
                }
            }
            
            mostrarAlertaExito("Consola Avanzada Oracle", sb.toString());
            
        } catch (SQLException e) {
            mostrarAlerta("Error", "Fallo al consultar estructuras avanzadas: " + e.getMessage());
        }
    }

    private void mostrarAlerta(String t, String m) {
        javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }

    private void mostrarAlertaExito(String t, String m) {
        javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }
}