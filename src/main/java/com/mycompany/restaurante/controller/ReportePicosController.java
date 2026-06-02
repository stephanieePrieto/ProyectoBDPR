package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.modelo.pojo.PicoActividad;
import com.mycompany.restaurante.modelo.pojo.Usuario;
import com.mycompany.restaurante.modelo.sql.MySQLConnect;
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

        // ESCUDO: Validación estricta para cumplir el caso de prueba CP-CU14-02
        if (inicio == null || fin == null) {
            mostrarAlerta("Campos vacíos", "⚠️ Por favor selecciona una fecha de inicio y fin.");
            return; // Detiene la ejecución aquí mismo
        }

        String sql = "SELECT DATE(p.fechaHora) as dia, COUNT(p.idPedido) as totalPedidos, COALESCE(SUM(pa.total), 0) as totalIngresos " +
                     "FROM pedidos p LEFT JOIN pagos pa ON p.idPedido = pa.idPedido " +
                     "WHERE DATE(p.fechaHora) BETWEEN ? AND ? " +
                     "GROUP BY DATE(p.fechaHora) ORDER BY totalPedidos DESC";

        listaPicos.clear();
        MySQLConnect mysql = new MySQLConnect();
        
        try (Connection con = mysql.connection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, inicio.toString());
            ps.setString(2, fin.toString());
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    listaPicos.add(new PicoActividad(
                        rs.getString("dia"),
                        rs.getInt("totalPedidos"),
                        rs.getDouble("totalIngresos")
                    ));
                }
            }
            
            if (!listaPicos.isEmpty()) {
                PicoActividad topDia = listaPicos.get(0);
                lblDiaMayorAfluencia.setText(topDia.getFecha());
                lblPedidosMayorAfluencia.setText(topDia.getCantidadPedidos() + " Mesas");
                lblIngresosMayorAfluencia.setText(String.format("$%.2f", topDia.getIngresosTotales()));
            } else {
                lblDiaMayorAfluencia.setText("Sin datos");
                lblPedidosMayorAfluencia.setText("0 Mesas");
                lblIngresosMayorAfluencia.setText("$0.00");
            }

        } catch (SQLException e) {
            e.printStackTrace();
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

    // MÉTODO NUEVO PARA MOSTRAR LAS ALERTAS EN PANTALLA
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}