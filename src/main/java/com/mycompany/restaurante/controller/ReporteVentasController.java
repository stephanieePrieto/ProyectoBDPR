package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.modelo.sql.OracleConnect;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Controlador de UI para la analítica y auditoría de ingresos.
 * Ajustado para arquitectura Oracle Cloud.
 * @author Stephanie Prieto
 */
public class ReporteVentasController implements Initializable {

    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpFechaFin;
    @FXML private Label lblTotalPeriodo;

    @FXML private TableView<FilaVenta> tblVentas;
    @FXML private TableColumn<FilaVenta, String> colFecha;
    @FXML private TableColumn<FilaVenta, String> colTotal;

    @FXML private TableView<FilaProducto> tblProductos;
    @FXML private TableColumn<FilaProducto, String> colPlatillo;
    @FXML private TableColumn<FilaProducto, Integer> colCantidad;

    private ObservableList<FilaVenta> listaVentas = FXCollections.observableArrayList();
    private ObservableList<FilaProducto> listaProductos = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTablas();
        dpFechaInicio.setValue(LocalDate.now());
        dpFechaFin.setValue(LocalDate.now());
        procesarConsultaFinanciera(LocalDate.now(), LocalDate.now());
    }

    private void configurarTablas() {
        colFecha.setCellValueFactory(cellData -> cellData.getValue().fechaProperty());
        colTotal.setCellValueFactory(cellData -> cellData.getValue().totalProperty());
        tblVentas.setItems(listaVentas);

        colPlatillo.setCellValueFactory(cellData -> cellData.getValue().platilloProperty());
        colCantidad.setCellValueFactory(cellData -> cellData.getValue().cantidadProperty().asObject());
        tblProductos.setItems(listaProductos);
    }

    private void procesarConsultaFinanciera(LocalDate inicio, LocalDate fin) {
        listaVentas.clear();
        listaProductos.clear();
        double acumuladoTotal = 0.0;

        String sqlHistorico = "SELECT TRUNC(fecha) AS fecha_limpia, SUM(total) AS total_dia "
                + "FROM pagos WHERE TRUNC(fecha) BETWEEN TO_DATE(?, 'YYYY-MM-DD') AND TO_DATE(?, 'YYYY-MM-DD') "
                + "GROUP BY TRUNC(fecha) ORDER BY fecha_limpia ASC";

        String sqlTopPlatillos = "SELECT p.nombre, SUM(dp.cantidad) AS total_vendido "
                + "FROM detallepedidos dp "
                + "JOIN platillos p ON dp.idPlatillo = p.idPlatillo "
                + "JOIN pedidos pe ON dp.idPedido = pe.idPedido "
                + "WHERE pe.info.estado = 'Pagado' AND TRUNC(pe.info.fecha_hora) BETWEEN TO_DATE(?, 'YYYY-MM-DD') AND TO_DATE(?, 'YYYY-MM-DD') "
                + "GROUP BY p.nombre ORDER BY total_vendido DESC FETCH FIRST 5 ROWS ONLY";

        try (Connection con = OracleConnect.getConexion()) {
            try (PreparedStatement ps = con.prepareStatement(sqlHistorico)) {
                ps.setString(1, inicio.toString());
                ps.setString(2, fin.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        double totalDia = rs.getDouble("total_dia");
                        acumuladoTotal += totalDia;
                        listaVentas.add(new FilaVenta(rs.getDate("fecha_limpia").toString(), String.format("$%.2f", totalDia)));
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement(sqlTopPlatillos)) {
                ps.setString(1, inicio.toString());
                ps.setString(2, fin.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        listaProductos.add(new FilaProducto(rs.getString("nombre"), rs.getInt("total_vendido")));
                    }
                }
            }
            lblTotalPeriodo.setText(String.format("$%.2f", acumuladoTotal));
        } catch (SQLException e) {
            System.err.println("Error en reportes: " + e.getMessage());
            mostrarAlerta("Error de Consulta", "Fallo al conectar con Oracle Cloud.");
        }
    }

    @FXML
    private void clicBuscar(ActionEvent event) {
        if (dpFechaInicio.getValue() == null || dpFechaFin.getValue() == null) {
            mostrarAlerta("Campos vacíos", "Define el rango cronológico.");
            return;
        }
        procesarConsultaFinanciera(dpFechaInicio.getValue(), dpFechaFin.getValue());
    }

    @FXML
    private void clicGenerarMensual(ActionEvent event) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        dpFechaInicio.setValue(inicioMes);
        dpFechaFin.setValue(hoy);
        procesarConsultaFinanciera(inicioMes, hoy);
    }

    @FXML
    private void clicVolver(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();
            DashboardController dashCtrl = loader.getController();
            if (dashCtrl != null && App.usuarioLogueado != null) {
                dashCtrl.configurarUsuario(App.usuarioLogueado);
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Panel de Control - Staff");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException ex) {
            System.err.println("Error al regresar: " + ex.getMessage());
        }
    }

    private void mostrarAlerta(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }

    public static class FilaVenta {
        private final SimpleStringProperty fecha;
        private final SimpleStringProperty total;
        public FilaVenta(String fecha, String total) {
            this.fecha = new SimpleStringProperty(fecha);
            this.total = new SimpleStringProperty(total);
        }
        public SimpleStringProperty fechaProperty() { return fecha; }
        public SimpleStringProperty totalProperty() { return total; }
    }

    public static class FilaProducto {
        private final SimpleStringProperty platillo;
        private final SimpleIntegerProperty cantidad;
        public FilaProducto(String platillo, int cantidad) {
            this.platillo = new SimpleStringProperty(platillo);
            this.cantidad = new SimpleIntegerProperty(cantidad);
        }
        public SimpleStringProperty platilloProperty() { return platillo; }
        public SimpleIntegerProperty cantidadProperty() { return cantidad; }
    }
}