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

/**
 * Controlador de la interfaz gráfica para la gestión del flujo de órdenes.
 * Visualiza el estado actual del pedido y maneja cancelaciones utilizando control de
 * transacciones manuales y tipos de datos de objetos nativos en Oracle Cloud.
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
     * Enlaza el diseño visual al momento de abrir la pantalla e inicializa
     * la primera recuperación de datos de la base de datos.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        cargarHistorialPedidos();
    }   

    /**
     * Configura el formateo de las columnas de la tabla. Intercepta
     * la columna de fechas para aplicarle un DateTimeFormatter legible.
     */
    private void configurarColumnas() {
        colIdOrden.setCellValueFactory(new PropertyValueFactory<>("idPedido"));
        colDetallePedido.setCellValueFactory(new PropertyValueFactory<>("detalleTexto"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Formateador personalizado para la hora (Ejemplo: 02:30 PM)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        colHoraLlegada.setCellValueFactory(cellData -> {
            if (cellData.getValue().getFechaHora() != null) {
                return new SimpleStringProperty(cellData.getValue().getFechaHora().format(formatter));
            }
            return new SimpleStringProperty("");
        });
        tablaPedido.setItems(listaHistorial);
    }

    /**
     * Re-dispara la búsqueda manual del historial de pedidos.
     */
    @FXML
    void clicActualizar(ActionEvent event) {
        cargarHistorialPedidos();
    }

    /**
     * Aplica la cancelación de una orden.
     * Este método implementa control de transacciones (TCL) para asegurar que la
     * cancelación del ticket y la liberación física de la mesa ocurran atómicamente.
     * Adicionalmente interactúa con un Tipo de Objeto de Oracle (AUDITORIA_PEDIDO_TYP).
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
        confirmacion.setContentText("¿Deseas cancelar la Orden #" + pedidoSeleccionado.getIdPedido() + "? Se liberará la mesa.");
        
        if (confirmacion.showAndWait().get() == ButtonType.OK) {
            // MAGIA ORACLE: Actualizamos el estado interno del objeto AUDITORIA_PEDIDO_TYP
            // utilizando su propio constructor pasándole la fecha original y el nuevo estado (parámetro).
            String sqlPedido = "UPDATE pedidos p SET info = AUDITORIA_PEDIDO_TYP(p.info.FECHA_HORA,?) WHERE p.idPedido = ?";
            // Consulta para limpiar la ocupación
            String sqlMesa = "UPDATE mesa SET estado = 'Libre' WHERE idMesa = ?";

            try (Connection con = OracleConnect.getConexion()) {
                // Se apaga el autocommit para ejecutar una transacción (ACID)
                con.setAutoCommit(false); 
                try (PreparedStatement psPedido = con.prepareStatement(sqlPedido);
                     PreparedStatement psMesa = con.prepareStatement(sqlMesa)) {
                     
                    psPedido.setString(1, "Cancelado"); // Set de estado a Cancelado
                    psPedido.setInt(2, pedidoSeleccionado.getIdPedido());
                    psPedido.executeUpdate();
                    
                    psMesa.setInt(1, pedidoSeleccionado.getIdMesa());
                    psMesa.executeUpdate();
                    
                    con.commit(); // Si todo salió bien, guardamos en la base de datos
                    
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Orden Cancelada", "Orden #" + pedidoSeleccionado.getIdPedido() + " cancelada.");
                    cargarHistorialPedidos();
                } catch (SQLException e) {
                    con.rollback(); // Si falla, echamos atrás los cambios para no dejar mesas huérfanas
                    throw e;
                }
            } catch (SQLException e) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Servidor", "No se pudo cancelar: " + e.getMessage());
            }
        }
    }

    /**
     * Consulta a la base de datos para cargar las órdenes pendientes, listas y canceladas.
     */
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

    /**
     * Acciones de retroceso genérico para regresar al menú principal.
     */
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