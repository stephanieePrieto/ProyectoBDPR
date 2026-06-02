package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.dao.PedidoDAO;
import com.mycompany.restaurante.modelo.pojo.Pedido;
import com.mycompany.restaurante.modelo.sql.OracleConnect; // Conectado a Oracle
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

public class GestionOrdenesController implements Initializable {

    @FXML private TableView<Pedido> tablaPedido; 
    @FXML private TableColumn<Pedido, Integer> colIdOrden;
    @FXML private TableColumn<Pedido, String> colHoraLlegada;
    @FXML private TableColumn<Pedido, String> colDetallePedido;
    @FXML private TableColumn<Pedido, String> colEstado;
    @FXML private javafx.scene.control.Button btnCancelarOrden;

    private ObservableList<Pedido> listaHistorial = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        cargarHistorialPedidos();
    }   

    private void configurarColumnas() {
        colIdOrden.setCellValueFactory(new PropertyValueFactory<>("idPedido"));
        colDetallePedido.setCellValueFactory(new PropertyValueFactory<>("detalleTexto"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        colHoraLlegada.setCellValueFactory(cellData -> {
            if (cellData.getValue().getFechaHora() != null) {
                return new SimpleStringProperty(cellData.getValue().getFechaHora().format(formatter));
            }
            return new SimpleStringProperty("");
        });
        tablaPedido.setItems(listaHistorial);
    }

    @FXML
    void clicActualizar(ActionEvent event) {
        cargarHistorialPedidos();
    }

    @FXML
    private void clicCancelarOrden(ActionEvent event) {
        Pedido pedidoSeleccionado = tablaPedido.getSelectionModel().getSelectedItem();
        if (pedidoSeleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Atención", "Por favor, selecciona una orden para cancelarla.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Cancelación");
        confirmacion.setContentText("¿Deseas cancelar la Orden #" + pedidoSeleccionado.getIdPedido() + "? Se liberará la mesa.");
        
        if (confirmacion.showAndWait().get() == ButtonType.OK) {
            String sqlPedido = "UPDATE pedidos SET estado = 'Cancelado' WHERE idPedido = ?";
            String sqlMesa = "UPDATE mesa SET estado = 'Libre' WHERE idMesa = ?";

            try (Connection con = OracleConnect.getConexion()) {
                con.setAutoCommit(false); 
                try (PreparedStatement psPedido = con.prepareStatement(sqlPedido);
                     PreparedStatement psMesa = con.prepareStatement(sqlMesa)) {
                    psPedido.setInt(1, pedidoSeleccionado.getIdPedido());
                    psPedido.executeUpdate();
                    psMesa.setInt(1, pedidoSeleccionado.getIdMesa());
                    psMesa.executeUpdate();
                    con.commit(); 
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Orden Cancelada", "Orden #" + pedidoSeleccionado.getIdPedido() + " cancelada.");
                    cargarHistorialPedidos();
                } catch (SQLException e) {
                    con.rollback();
                    throw e;
                }
            } catch (SQLException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Servidor", "No se pudo cancelar: " + e.getMessage());
            }
        }
    }

    public void cargarHistorialPedidos() {
        listaHistorial.clear();
        try (Connection con = OracleConnect.getConexion()) {
            PedidoDAO pedidoDAO = new PedidoDAO(con);
            listaHistorial.addAll(pedidoDAO.buscarPedidosPorEstado("Pendiente"));
            listaHistorial.addAll(pedidoDAO.buscarPedidosPorEstado("Listo"));
            listaHistorial.addAll(pedidoDAO.buscarPedidosPorEstado("Cancelado"));
            tablaPedido.refresh();
        } catch (SQLException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Base de Datos", "No se pudo cargar: " + e.getMessage());
        }
    }

    @FXML
    void volverDashboard(ActionEvent event) {
        try {
            Parent root = App.getFXMLLoader("Dashboard").load();
            Stage stage = (Stage) tablaPedido.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Panel de Control - Staff");
        } catch (IOException ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo regresar.");
        }
    }

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

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}