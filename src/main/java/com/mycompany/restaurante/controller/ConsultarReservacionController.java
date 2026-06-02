package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.modelo.sql.OracleConnect;
import com.mycompany.restaurante.dao.ReservacionDAO;
import com.mycompany.restaurante.modelo.pojo.Reservacion;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controlador de la vista del cliente para el seguimiento de sus reservaciones.
 * Migrado a arquitectura Oracle Cloud.
 * @author Ricardo, Diego, Angel, Stephy
 */
public class ConsultarReservacionController {

    @FXML private TextField txtFolio;
    @FXML private Button btnBuscar;
    @FXML private VBox panelInfo;
    @FXML private Label lblCliente, lblMesa, lblFecha, lblHora, lblPersonas, lblEstado;
    @FXML private Button btnModificar, btnCancelar;

    private Reservacion reservacionActual = null;
    private final ReservacionDAO dao = new ReservacionDAO();

    @FXML
    void handleRegresar(ActionEvent event) {
        try {
            FXMLLoader loader = App.getFXMLLoader("ReservacionCliente");
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void clicBuscar(ActionEvent event) {
        String idBuscado = txtFolio.getText().trim().toUpperCase();
        
        if (idBuscado.isEmpty()) {
            mostrarAlerta("ID Vacío", "Ingresa un ID válido (Ej: CP001) para buscar.", Alert.AlertType.WARNING);
            return;
        }

        if (App.idClienteLogueado != null && !idBuscado.equals(App.idClienteLogueado)) {
            mostrarAlerta("Acceso Denegado", 
                "¡Ojo ahí! Solo tienes permiso para consultar tus propias reservaciones.", 
                Alert.AlertType.ERROR);
            limpiarCampos();
            return;
        }

        // Consulta ajustada para Oracle: Uso de FETCH FIRST 1 ROW ONLY
        String sql = "SELECT r.idReservacion, r.folioUnico, r.id_cliente, c.nombre AS nombre_cliente, " +
                     "r.idMesa, r.fecha, r.hora, r.num_personas, r.estado " +
                     "FROM reservaciones r " +
                     "LEFT JOIN clientes c ON r.id_cliente = c.id_cliente " +
                     "WHERE r.id_cliente = ? ORDER BY r.idReservacion DESC FETCH FIRST 1 ROW ONLY";

        try (Connection con = OracleConnect.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, idBuscado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    reservacionActual = new Reservacion(
                        rs.getInt("idReservacion"),
                        rs.getString("folioUnico"),
                        rs.getString("id_cliente"),
                        rs.getString("nombre_cliente"),
                        rs.getInt("idMesa"),
                        rs.getDate("fecha").toString(),
                        rs.getString("hora"),
                        rs.getInt("num_personas"),
                        rs.getString("estado")
                    );
                    
                    lblCliente.setText(reservacionActual.getNombreCliente());
                    lblMesa.setText("Mesa No. " + reservacionActual.getIdMesa());
                    lblFecha.setText(reservacionActual.getFecha());
                    lblHora.setText(reservacionActual.getHora());
                    lblPersonas.setText(String.valueOf(reservacionActual.getNumPersonas()));
                    lblEstado.setText(reservacionActual.getEstado().toUpperCase());
                    
                    boolean activa = !lblEstado.getText().equalsIgnoreCase("CANCELADA");
                    btnModificar.setDisable(!activa);
                    btnCancelar.setDisable(!activa);
                } else {
                    limpiarCampos();
                    mostrarAlerta("No Encontrado", "No tienes ninguna reservación activa bajo este ID.", Alert.AlertType.INFORMATION);
                }
            }
        } catch (SQLException e) {
            mostrarAlerta("Error de Conexión", "Problema al conectar con Oracle: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    void clicModificar(ActionEvent event) {
        if (reservacionActual == null) return;

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modificar Reservación");
        dialog.setHeaderText("Modificando Reserva de: " + reservacionActual.getNombreCliente());

        ButtonType btnGuardar = new ButtonType("Guardar Cambios", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        DatePicker dpNuevaFecha = new DatePicker(LocalDate.parse(reservacionActual.getFecha()));
        dpNuevaFecha.setEditable(false);
        dpNuevaFecha.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) setDisable(true); 
            }
        });

        ComboBox<String> cbNuevaHora = new ComboBox<>(FXCollections.observableArrayList("13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00"));
        cbNuevaHora.setValue(reservacionActual.getHora().substring(0, 5)); 
        
        Spinner<Integer> spNuevasPersonas = new Spinner<>(1, 10, reservacionActual.getNumPersonas());

        grid.add(new Label("Nueva Fecha:"), 0, 0);
        grid.add(dpNuevaFecha, 1, 0);
        grid.add(new Label("Nueva Hora:"), 0, 1);
        grid.add(cbNuevaHora, 1, 1);
        grid.add(new Label("Pingüinos:"), 0, 2);
        grid.add(spNuevasPersonas, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> resultado = dialog.showAndWait();
        
        if (resultado.isPresent() && resultado.get() == btnGuardar) {
            String horaLimpia = cbNuevaHora.getValue() + ":00";
            
            Reservacion modificada = new Reservacion(
                reservacionActual.getIdReservacion(), reservacionActual.getFolioUnico(),
                reservacionActual.getIdCliente(), reservacionActual.getNombreCliente(),
                reservacionActual.getIdMesa(), dpNuevaFecha.getValue().toString(),
                horaLimpia, spNuevasPersonas.getValue(), reservacionActual.getEstado()
            );

            try {
                if (dao.actualizarReservacion(modificada)) {
                    mostrarAlerta("Éxito", "La reservación se actualizó correctamente.", Alert.AlertType.INFORMATION);
                    clicBuscar(null); 
                }
            } catch (SQLException e) {
                mostrarAlerta("Error", "No se pudo actualizar en la base de datos.", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    void clicCancelar(ActionEvent event) {
        if (reservacionActual == null) return;

        Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
        conf.setTitle("Confirmar Cancelación");
        conf.setHeaderText("Estás a punto de cancelar la reserva");
        conf.setContentText("¿Seguro que quieres cancelar la Mesa " + reservacionActual.getIdMesa() + "?");
        
        if (conf.showAndWait().get() == ButtonType.OK) {
            try {
                if (dao.cancelarReservacion(reservacionActual.getIdReservacion())) {
                    try (Connection con = OracleConnect.getConexion();
                         PreparedStatement ps = con.prepareStatement("UPDATE mesa SET estado = 'Libre' WHERE idMesa = ?")) {
                        ps.setInt(1, reservacionActual.getIdMesa());
                        ps.executeUpdate();
                    }
                    mostrarAlerta("Cancelada", "Reservación cancelada y mesa liberada con éxito.", Alert.AlertType.INFORMATION);
                    clicBuscar(null); 
                }
            } catch (SQLException e) {
                mostrarAlerta("Error", "Hubo un fallo al intentar cancelar.", Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    private void limpiarCampos() {
        reservacionActual = null;
        lblCliente.setText("---");
        lblMesa.setText("---");
        lblFecha.setText("---");
        lblHora.setText("---");
        lblPersonas.setText("---");
        lblEstado.setText("---");
        btnModificar.setDisable(true);
        btnCancelar.setDisable(true);
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}