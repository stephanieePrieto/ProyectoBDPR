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

public class ReportePicosController {

    @FXML private DatePicker dpInicio;
    @FXML private DatePicker dpFin;
    @FXML private TableView<PicoActividad> tblPicos;
    @FXML private TableColumn<PicoActividad, String> colFecha;
    @FXML private TableColumn<PicoActividad, Integer> colPedidos;
    @FXML private TableColumn<PicoActividad, Double> colIngresos;

    @FXML private Label lblDiaMayorAfluencia;
    @FXML private Label lblPedidosMayorAfluencia;
    @FXML private Label lblIngresosMayorAfluencia;

    private ObservableList<PicoActividad> listaPicos;

    @FXML
    public void initialize() {
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colPedidos.setCellValueFactory(new PropertyValueFactory<>("cantidadPedidos"));
        colIngresos.setCellValueFactory(new PropertyValueFactory<>("ingresosTotales"));
        
        listaPicos = FXCollections.observableArrayList();
        tblPicos.setItems(listaPicos);
    }

    @FXML
    private void clicGenerarReporte(ActionEvent event) {
        LocalDate inicio = dpInicio.getValue();
        LocalDate fin = dpFin.getValue();

        if (inicio == null || fin == null) {
            mostrarAlerta("Campos vacíos", "⚠️ Por favor selecciona una fecha de inicio y fin.");
            return;
        }

        // Consulta SQL ajustada a sintaxis Oracle: TRUNC para fechas y TO_DATE para parámetros
        String sql = "SELECT TRUNC(p.fechaHora) as dia, COUNT(p.idPedido) as totalPedidos, COALESCE(SUM(pa.total), 0) as totalIngresos " +
                     "FROM pedidos p LEFT JOIN pagos pa ON p.idPedido = pa.idPedido " +
                     "WHERE TRUNC(p.fechaHora) BETWEEN TO_DATE(?, 'YYYY-MM-DD') AND TO_DATE(?, 'YYYY-MM-DD') " +
                     "GROUP BY TRUNC(p.fechaHora) ORDER BY totalPedidos DESC";

        listaPicos.clear();
        
        try (Connection con = OracleConnect.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, inicio.toString());
            ps.setString(2, fin.toString());
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    listaPicos.add(new PicoActividad(
                        rs.getDate("dia").toString(),
                        rs.getInt("totalPedidos"),
                        rs.getDouble("totalIngresos")
                    ));
                }
            }
            
            if (!listaPicos.isEmpty()) {
                PicoActividad topDia = listaPicos.get(0);
                lblDiaMayorAfluencia.setText(topDia.getFecha());
                lblPedidosMayorAfluencia.setText(topDia.getCantidadPedidos() + " Pedidos");
                lblIngresosMayorAfluencia.setText(String.format("$%.2f", topDia.getIngresosTotales()));
            } else {
                lblDiaMayorAfluencia.setText("Sin datos");
                lblPedidosMayorAfluencia.setText("0 Pedidos");
                lblIngresosMayorAfluencia.setText("$0.00");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de BD", "Fallo al consultar Oracle: " + e.getMessage());
        }
    }

    @FXML
    private void clicVolver(ActionEvent event) {
        try {
            FXMLLoader loader = App.getFXMLLoader("Dashboard");
            Parent root = loader.load();
            DashboardController controller = loader.getController();
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