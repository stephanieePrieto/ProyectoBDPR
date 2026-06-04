package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.modelo.pojo.PicoActividad;
import com.mycompany.restaurante.modelo.pojo.Usuario;
import com.mycompany.restaurante.modelo.sql.OracleConnect; // Conexión a Oracle
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Controlador enfocado en analítica de negocio (Business Intelligence).
 * Genera reportes de picos de afluencia cruzando datos de pedidos e ingresos,
 * utilizando características y tipos de objetos avanzados de Oracle Cloud (Collections/Varrays).
 */
public class ReportePicosController {

    // --- Filtros de Tiempo ---
    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFin;
    
    // --- Tabla de Resultados ---
    @FXML private TableView<PicoActividad> tblPicos;
    @FXML private TableColumn<PicoActividad, String> colFecha;
    @FXML private TableColumn<PicoActividad, Integer> colPedidos;
    @FXML private TableColumn<PicoActividad, Double> colIngresos;

    // --- Tarjetas Resumen (Kardex) ---
    @FXML private Label lblDiaMayorAfluencia;
    @FXML private Label lblPedidosMayorAfluencia;
    @FXML private Label lblIngresosMayorAfluencia;

    private ObservableList<PicoActividad> listaPicos;

    /**
     * Inicializa los bindings de las columnas con las propiedades del POJO PicoActividad.
     */
    @FXML
    public void initialize() {
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colPedidos.setCellValueFactory(new PropertyValueFactory<>("cantidadPedidos"));
        colIngresos.setCellValueFactory(new PropertyValueFactory<>("ingresosTotales"));
        
        listaPicos = FXCollections.observableArrayList();
        tblPicos.setItems(listaPicos);
    }

    /**
     * Orquesta la generación del reporte mediante una consulta SQL compleja en Oracle.
     * Analiza el comportamiento del restaurante en el rango de tiempo seleccionado, agrupando
     * métricas por día e identificando la jornada de mayor rentabilidad.
     */
    @FXML
    private void clicGenerarReporte(ActionEvent event) {
        LocalDate inicio = dpInicio.getValue();
        LocalDate fin = dpFin.getValue();

        if (inicio == null || fin == null) {
            mostrarAlerta("Campos vacíos", "Por favor selecciona una fecha de inicio y fin.");
            return;
        }

        /* * MAGIA ORACLE:
         * Uso de TABLE(CAST(MULTISET(...))) para desempaquetar la colección anidada 
         * 'auditoria_pedido_tab' y acceder de forma relacional al objeto 'info' que contiene 
         * metadatos vitales como la 'FECHA_HORA' del pedido. Se agrupa empleando TRUNC() 
         * para truncar horas y agrupar estrictamente por día.
         */
        String sql = "SELECT TRUNC(t.FECHA_HORA) as fecha_truncada, " +
                     "COUNT(p.idPedido) as totalPedidos, " +
                     "COALESCE(SUM(pa.total), 0) as totalIngresos " +
                     "FROM pedidos p, TABLE(CAST(MULTISET(SELECT p.info FROM DUAL) AS auditoria_pedido_tab)) t " +
                     "LEFT JOIN pagos pa ON p.idPedido = pa.idPedido " +
                     "WHERE TRUNC(t.FECHA_HORA) BETWEEN TO_DATE(?, 'YYYY-MM-DD') AND TO_DATE(?, 'YYYY-MM-DD') " +
                     "GROUP BY TRUNC(t.FECHA_HORA) " +
                     "ORDER BY TRUNC(t.FECHA_HORA) DESC";

        listaPicos.clear();
        try (Connection con = OracleConnect.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, inicio.toString());
            ps.setString(2, fin.toString());
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    listaPicos.add(new PicoActividad(
                        rs.getDate("fecha_truncada").toString(), 
                        rs.getInt("totalPedidos"), 
                        rs.getDouble("totalIngresos")
                    ));
                }
            }
            
            // Lógica para alimentar el Resumen Superior (Kardex Top 1)
            if (!listaPicos.isEmpty()) {
                // Al venir ordenado, asume el primer registro como caso destacado
                PicoActividad topDia = listaPicos.get(0);
                lblDiaMayorAfluencia.setText(topDia.getFecha());
                lblPedidosMayorAfluencia.setText(topDia.getCantidadPedidos() + " Pedidos");
                lblIngresosMayorAfluencia.setText(String.format("$%.2f", topDia.getIngresosTotales()));
            } else {
                // Limpieza visual si no hay datos en el rango
                lblDiaMayorAfluencia.setText("Sin datos");
                lblPedidosMayorAfluencia.setText("0 Pedidos");
                lblIngresosMayorAfluencia.setText("$0.00");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de BD", "Fallo al consultar Oracle: " + e.getMessage());
        }
    }

    /**
     * Enrutador para regresar de forma segura al panel del Gerente.
     */
    @FXML
    private void clicVolver(ActionEvent event) {
        try {
            FXMLLoader loader = App.getFXMLLoader("Dashboard");
            Parent root = loader.load();
            DashboardController controller = loader.getController();
            
            // Simulación/Restauración de contexto administrativo para el RBAC
            Usuario admin = new Usuario();
            admin.setRol("Gerente");
            controller.configurarUsuario(admin);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}