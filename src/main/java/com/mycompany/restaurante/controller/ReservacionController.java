package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.dao.ReservacionDAO;
import com.mycompany.restaurante.modelo.pojo.Reservacion;
import com.mycompany.restaurante.utils.ConexionBD;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * Controlador de UI para la administración integral de reservaciones por parte del Staff.
 * Integra validaciones en tiempo de ejecución para aplicar cancelaciones
 * automáticas por impuntualidad tras un umbral de 15 minutos, y provee un 
 * buscador en tiempo real sobre los registros activos.
 */
public class ReservacionController implements Initializable {

    // --- Controles de Formulario ---
    @FXML private TextField txtCliente;
    @FXML private TextField txtBuscador;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<String> cbHora;
    @FXML private ComboBox<Integer> cbPinguinos;
    @FXML private ComboBox<Integer> cbMesa;

    // --- Tabla de Reservaciones ---
    @FXML private TableView<Reservacion> tablaReservaciones;
    @FXML private TableColumn<Reservacion, String> colCliente;
    @FXML private TableColumn<Reservacion, String> colFecha;
    @FXML private TableColumn<Reservacion, String> colHora;
    @FXML private TableColumn<Reservacion, String> colEstado;
    @FXML private TableColumn<Reservacion, String> colID;
    @FXML private TableColumn<Reservacion, Integer> colPinguinos;
    @FXML private TableColumn<Reservacion, Integer> colMesa;

    // Estructuras reactivas para la gestión de datos visuales
    private ObservableList<Reservacion> listaReservaciones = 
            FXCollections.observableArrayList();
    private Reservacion reservacionSeleccionada = null;
    private ReservacionDAO reservacionesDao = new ReservacionDAO();
    
    // Bandera o semáforo lógico para evitar ciclos infinitos en el listener de selección
    private boolean modificandoTabla = false;

    /**
     * Inicializa los selectores con valores predeterminados de la lógica de negocio,
     * enlaza las columnas de la tabla y restringe el calendario.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Carga de catálogos estáticos
        cbHora.setItems(FXCollections.observableArrayList(
                "13:00", "14:00", "15:00", "16:00", 
                "17:00", "18:00", "19:00", "20:00"
        ));
        cbPinguinos.setItems(FXCollections.observableArrayList(
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10
        ));
        cbMesa.setItems(FXCollections.observableArrayList(
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12
        ));

        // Mapeo de columnas con atributos del POJO
        colCliente.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colPinguinos.setCellValueFactory(new PropertyValueFactory<>("numPersonas"));
        colMesa.setCellValueFactory(new PropertyValueFactory<>("idMesa"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colID.setCellValueFactory(new PropertyValueFactory<>("folioUnico"));
        
        // Configuración visual y de restricciones del DatePicker (evita fechas pasadas)
        dpFecha.setEditable(false);
        dpFecha.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffcdd2;"); // Tinte rojo para días inválidos
                }
            }
        });
        
        configurarBuscadorRealTime();
        cargarDatosTabla();

        // Sincronización del formulario con el registro seleccionado en la tabla
        tablaReservaciones.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldSel, newSel) -> {
            if (modificandoTabla) return;
            if (newSel != null) {
                reservacionSeleccionada = newSel;
                txtCliente.setText(newSel.getNombreCliente());
                dpFecha.setValue(LocalDate.parse(newSel.getFecha()));
                if (newSel.getHora() != null && newSel.getHora().length() >= 5) {
                    cbHora.setValue(newSel.getHora().substring(0, 5));
                }
                cbPinguinos.setValue(newSel.getNumPersonas());
                cbMesa.setValue(newSel.getIdMesa());
            }
        });
    }

    /**
     * Carga las reservaciones de la base de datos disparando previamente
     * la depuración de tolerancia cronológica para limpiar retrasos (No Shows).
     */
    private void cargarDatosTabla() {
        modificandoTabla = true; // Bloquea el listener para evitar errores de actualización
        try {
            // Lógica de negocio: Invalida reservas que superaron el tiempo de espera
            reservacionesDao.depurarReservacionesVencidas();
            
            List<Reservacion> deBD = 
                    reservacionesDao.obtenerTodasLasReservaciones();
            listaReservaciones.setAll(deBD);
            tablaReservaciones.refresh();
        } catch (SQLException e) { 
            mostrarAlerta("Error", e.getMessage()); 
        } finally { 
            modificandoTabla = false; 
        }
    }

    /**
     * Implementa un buscador reactivo que filtra la tabla dinámicamente
     * conforme el usuario teclea (por nombre, folio o mesa).
     */
    private void configurarBuscadorRealTime() {
        FilteredList<Reservacion> filteredData = 
                new FilteredList<>(listaReservaciones, p -> true);
        
        txtBuscador.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(reserva -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return (reserva.getNombreCliente().toLowerCase().contains(lower) || 
                        reserva.getFolioUnico().toLowerCase().contains(lower) || 
                        String.valueOf(reserva.getIdMesa()).contains(lower));
            });
        });
        tablaReservaciones.setItems(filteredData);
    }

    /**
     * Inserta una nueva reservación manualmente desde el módulo administrativo.
     */
    @FXML
    private void registrarReserva(ActionEvent event) {
        if (txtCliente.getText().isEmpty() || dpFecha.getValue() == null 
                || cbHora.getValue() == null) {
            mostrarAlerta("Campos incompletos", "Completa todos los campos.");
            return;
        }

        // Generador de folio alfanumérico único
        String folio = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        java.time.LocalTime horaFormateada = java.time.LocalTime.parse(cbHora.getValue());
        
        Reservacion nueva = new Reservacion(
                0, folio, "TEMP_ID", txtCliente.getText(), 
                cbMesa.getValue(), dpFecha.getValue().toString(), 
                horaFormateada.toString() + ":00", 
                cbPinguinos.getValue(), "Confirmada"
        );

        try {
            if (reservacionesDao.insertarReservacion(nueva)) {
                mostrarAlerta("Éxito", "Reservación guardada con folio: " + folio);
                cargarDatosTabla();
                limpiarFormulario();
            }
        } catch (SQLException e) { 
            mostrarAlerta("Error", e.getMessage()); 
        }
    }

    /**
     * Bloque placeholder para la lógica de modificación secuencial.
     */
    @FXML
    private void modificarSeleccion(ActionEvent event) {
        if (reservacionSeleccionada == null) return;
        // Lógica de modificación secuencial... (Pendiente de implementación en DAO)
        cargarDatosTabla();
        limpiarFormulario();
    }

    /**
     * Efectúa una baja lógica del registro seleccionado.
     */
    @FXML
    private void cancelarReserva(ActionEvent event) {
        if (reservacionSeleccionada == null) return;
        try {
            if (reservacionesDao.cancelarReservacion(
                    reservacionSeleccionada.getIdReservacion())) {
                mostrarAlerta("Cancelada", "Reservación dada de baja.");
                cargarDatosTabla();
                limpiarFormulario();
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }

    /**
     * Enrutador de retroceso. Devuelve al usuario a su panel correspondiente
     * dependiendo de si existe una sesión de staff activa.
     */
    @FXML
    private void handleRegresar(ActionEvent event) {
        try {
            String fxml = (App.usuarioLogueado != null) 
                    ? "Dashboard" : "VerMenuCliente";
            FXMLLoader loader = App.getFXMLLoader(fxml);
            Parent root = loader.load();
            Stage stage = (Stage) tablaReservaciones.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { 
            e.printStackTrace(); 
        }
    }

    @FXML void volverDashboard(ActionEvent event) { handleRegresar(event); }

    /**
     * Restablece el formulario a sus valores por defecto.
     */
    private void limpiarFormulario() {
        reservacionSeleccionada = null;
        txtCliente.clear();
        dpFecha.setValue(LocalDate.now());
        cbHora.setValue("13:00");
    }

    private void mostrarAlerta(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }
}