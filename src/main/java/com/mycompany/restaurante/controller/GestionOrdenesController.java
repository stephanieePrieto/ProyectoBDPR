package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.dao.PedidoDAO;
import com.mycompany.restaurante.modelo.pojo.Pedido;
import com.mycompany.restaurante.modelo.sql.MySQLConnect;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.sql.PreparedStatement;

/**
 * Controlador para la auditoría y gestión de las órdenes activas e históricas del restaurante.
 * Permite al staff operativo visualizar el estatus de las comandas, recargar el historial
 * y cancelar pedidos de manera segura, liberando las mesas vinculadas de forma automática.
 * * @author Ricardo, Diego, Angel, Stephy
 */
public class GestionOrdenesController implements Initializable {

    @FXML private TableView<Pedido> tablaPedido; 
    @FXML private TableColumn<Pedido, Integer> colIdOrden;
    @FXML private TableColumn<Pedido, String> colHoraLlegada;
    @FXML private TableColumn<Pedido, String> colDetallePedido;
    @FXML private TableColumn<Pedido, String> colEstado;
    @FXML private javafx.scene.control.Button btnCancelarOrden;

    private ObservableList<Pedido> listaHistorial = FXCollections.observableArrayList();

    /**
     * Inicializa la vista de gestión de órdenes.
     * Vincula las propiedades del modelo de datos con la interfaz tabular.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        cargarHistorialPedidos();
    }    

    /**
     * Configura el mapeo de las columnas de la TableView.
     */
    private void configurarColumnas() {
        colIdOrden.setCellValueFactory(new PropertyValueFactory<>("idPedido"));
        colDetallePedido.setCellValueFactory(new PropertyValueFactory<>("detalleTexto"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        colHoraLlegada.setCellValueFactory(cellData -> {
            if (cellData.getValue().getFechaHora() != null) {
                return new SimpleStringProperty(
                        cellData.getValue().getFechaHora().format(formatter));
            }
            return new SimpleStringProperty("");
        });

        tablaPedido.setItems(listaHistorial);
    }

    /**
     * Actualiza el historial de comandas bajo demanda.
     * @param event Evento generado al presionar el botón de actualizar.
     */
    @FXML
    void clicActualizar(ActionEvent event) {
        cargarHistorialPedidos();
    }

    /**
     * Procesa la cancelación operativa de una orden seleccionada.
     * Implementa una transacción atómica para actualizar el pedido y liberar la mesa.
     */
    @FXML
    private void clicCancelarOrden(ActionEvent event) {
        Pedido pedidoSeleccionado = tablaPedido.getSelectionModel().getSelectedItem();
        
        if (pedidoSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atención", "Por favor, selecciona una orden para cancelarla.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Cancelación");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Deseas cancelar la Orden #" + pedidoSeleccionado.getIdPedido() + "? Se liberará la mesa.");
        
        java.util.Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
            
            MySQLConnect mysql = new MySQLConnect();
            String sqlPedido = "UPDATE pedidos SET estado = 'Cancelado' WHERE idPedido = ?";
            String sqlMesa = "UPDATE mesa SET estado = 'Libre' WHERE idMesa = ?";

            try (Connection con = mysql.connection()) {
                if (con != null) {
                    con.setAutoCommit(false); 

                    try (PreparedStatement psPedido = con.prepareStatement(sqlPedido)) {
                        psPedido.setInt(1, pedidoSeleccionado.getIdPedido());
                        psPedido.executeUpdate();
                    }

                    try (PreparedStatement psMesa = con.prepareStatement(sqlMesa)) {
                        psMesa.setInt(1, pedidoSeleccionado.getIdMesa());
                        psMesa.executeUpdate();
                    }

                    con.commit(); 
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Orden Cancelada", "La Orden #" + pedidoSeleccionado.getIdPedido() + " se marcó como 'Cancelado' con éxito.");
                    cargarHistorialPedidos();
                }
            } catch (SQLException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Servidor", "No se pudo cancelar la comanda: " + e.getMessage());
            }
        }
    }

    /**
     * Consulta el catálogo de pedidos para consolidar el historial.
     */
    public void cargarHistorialPedidos() {
        listaHistorial.clear();
        MySQLConnect mysql = new MySQLConnect();
        
        try (Connection con = mysql.connection()) {
            if (con == null) return;
            
            PedidoDAO pedidoDAO = new PedidoDAO(con);
            
            List<Pedido> pendientes = pedidoDAO.buscarPedidosPorEstado("Pendiente");
            List<Pedido> listos = pedidoDAO.buscarPedidosPorEstado("Listo");
            List<Pedido> cancelados = pedidoDAO.buscarPedidosPorEstado("Cancelado"); 
            
            listaHistorial.addAll(pendientes);
            listaHistorial.addAll(listos);
            listaHistorial.addAll(cancelados);
            
            tablaPedido.refresh();
            
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Base de Datos", "No se pudo cargar el historial: " + e.getMessage());
        }
    }

    /**
     * Redirige la navegación al panel de control principal.
     */
    @FXML
    void volverDashboard(ActionEvent event) {
        try {
            Parent root = App.getFXMLLoader("Dashboard").load();
            Stage stage = (Stage) tablaPedido.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Panel de Control - Staff");
        } catch (IOException ex) {
            ex.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Navegación", "No se pudo regresar al Dashboard.");
        }
    }

    /**
     * Redirige al Dashboard utilizando el nodo origen.
     */
    @FXML
    void volverAlMenu(ActionEvent event) {
        try {
            Parent root = App.getFXMLLoader("Dashboard").load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Despliega cuadros de diálogo emergentes.
     */
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}