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
 * Despliega resúmenes financieros y desglosa el ranking de popularidad de los platillos.
 * Ajustado para soportar el dialecto SQL de Oracle Cloud.
 */
public class ReporteVentasController implements Initializable {

    // --- Filtros ---
    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpFechaFin;
    @FXML private Label lblTotalPeriodo;

    // --- Tabla A: Rendimiento Financiero por Día ---
    @FXML private TableView<FilaVenta> tblVentas;
    @FXML private TableColumn<FilaVenta, String> colFecha;
    @FXML private TableColumn<FilaVenta, String> colTotal;

    // --- Tabla B: Top Platillos (Volumen) ---
    @FXML private TableView<FilaProducto> tblProductos;
    @FXML private TableColumn<FilaProducto, String> colPlatillo;
    @FXML private TableColumn<FilaProducto, Integer> colCantidad;

    private ObservableList<FilaVenta> listaVentas = FXCollections.observableArrayList();
    private ObservableList<FilaProducto> listaProductos = FXCollections.observableArrayList();

    /**
     * Configura el formateo inicial de las tablas y detona el primer cálculo 
     * asumiendo un análisis del día en curso (Corte de caja exprés).
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTablas();
        dpFechaInicio.setValue(LocalDate.now());
        dpFechaFin.setValue(LocalDate.now());
        procesarConsultaFinanciera(LocalDate.now(), LocalDate.now());
    }

    /**
     * Liga las columnas lógicas con las propiedades enlazables (Properties) de 
     * las clases Wrapper estáticas anidadas.
     */
    private void configurarTablas() {
        colFecha.setCellValueFactory(cellData -> cellData.getValue().fechaProperty());
        colTotal.setCellValueFactory(cellData -> cellData.getValue().totalProperty());
        tblVentas.setItems(listaVentas);

        colPlatillo.setCellValueFactory(cellData -> cellData.getValue().platilloProperty());
        colCantidad.setCellValueFactory(cellData -> cellData.getValue().cantidadProperty().asObject());
        tblProductos.setItems(listaProductos);
    }

    /**
     * Orquesta las transacciones asíncronas hacia Oracle para obtener el resumen 
     * monetario y el ranking de unidades de platillos vendidos en un rango de fechas.
     * @param inicio Fecha límite inferior
     * @param fin Fecha límite superior
     */
    private void procesarConsultaFinanciera(LocalDate inicio, LocalDate fin) {
        listaVentas.clear();
        listaProductos.clear();
        double acumuladoTotal = 0.0;

        // Oracle SQL: TRUNC() se asegura de aislar la fecha eliminando las horas para agrupación
        String sqlHistorico = "SELECT TRUNC(fecha) AS fecha_limpia, SUM(total) AS total_dia "
                + "FROM pagos WHERE TRUNC(fecha) BETWEEN TO_DATE(?, 'YYYY-MM-DD') AND TO_DATE(?, 'YYYY-MM-DD') "
                + "GROUP BY TRUNC(fecha) ORDER BY fecha_limpia ASC";

        // Oracle SQL: Uso de FETCH FIRST n ROWS ONLY en lugar de LIMIT para limitar resultados al Top 5
        String sqlTopPlatillos = "SELECT p.nombre, SUM(dp.cantidad) AS total_vendido "
                + "FROM detallepedidos dp "
                + "JOIN platillos p ON dp.idPlatillo = p.idPlatillo "
                + "JOIN pedidos pe ON dp.idPedido = pe.idPedido "
                + "WHERE pe.info.estado = 'Pagado' AND TRUNC(pe.info.fecha_hora) BETWEEN TO_DATE(?, 'YYYY-MM-DD') AND TO_DATE(?, 'YYYY-MM-DD') "
                + "GROUP BY p.nombre ORDER BY total_vendido DESC FETCH FIRST 5 ROWS ONLY";

        try (Connection con = OracleConnect.getConexion()) {
            
            // Ejecución Consulta 1: Rendimiento por Día
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
            
            // Ejecución Consulta 2: Ranking Top 5
            try (PreparedStatement ps = con.prepareStatement(sqlTopPlatillos)) {
                ps.setString(1, inicio.toString());
                ps.setString(2, fin.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        listaProductos.add(new FilaProducto(rs.getString("nombre"), rs.getInt("total_vendido")));
                    }
                }
            }
            
            // Refleja el gran total calculado en la etiqueta visual
            lblTotalPeriodo.setText(String.format("$%.2f", acumuladoTotal));
            
        } catch (SQLException e) {
            System.err.println("Error en reportes: " + e.getMessage());
            mostrarAlerta("Error de Consulta", "Fallo al conectar con Oracle Cloud.");
        }
    }

    /**
     * Validador de rangos. Detona el proceso manual al presionar "Buscar".
     */
    @FXML
    private void clicBuscar(ActionEvent event) {
        if (dpFechaInicio.getValue() == null || dpFechaFin.getValue() == null) {
            mostrarAlerta("Campos vacíos", "Define el rango cronológico.");
            return;
        }
        procesarConsultaFinanciera(dpFechaInicio.getValue(), dpFechaFin.getValue());
    }

    /**
     * Acceso rápido (Macro): Configura automáticamente el calendario desde el primer 
     * día del mes actual hasta el día en curso.
     */
    @FXML
    private void clicGenerarMensual(ActionEvent event) {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        dpFechaInicio.setValue(inicioMes);
        dpFechaFin.setValue(hoy);
        procesarConsultaFinanciera(inicioMes, hoy);
    }

    /**
     * Regresa la vista al Dashboard preservando el RBAC del Gerente.
     */
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

    // =====================================================================
    // =                  CLASES INTERNAS (WRAPPERS FX)                    =
    // = Utilizadas para facilitar el Data Binding con las TableView       =
    // =====================================================================

    /**
     * Estructura contenedora para enlazar registros financieros con la interfaz.
     */
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

    /**
     * Estructura contenedora para enlazar registros de volumen con la interfaz.
     */
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