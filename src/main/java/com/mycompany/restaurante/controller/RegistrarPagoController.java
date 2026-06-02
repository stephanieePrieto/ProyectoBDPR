package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.dao.DetalleFacturaDAO;
import com.mycompany.restaurante.dao.PagoDAO;
import com.mycompany.restaurante.modelo.pojo.Pago;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controlador encargado del módulo de caja y cobro de cuentas.
 * Gestiona la selección visual de mesas, calcula automáticamente el importe
 * total incluyendo impuestos y ejecuta la transacción final.
 * * @author Ricardo, Diego, Angel, Stephy
 */
public class RegistrarPagoController implements Initializable {

    @FXML private ComboBox<Integer> cmbMesa;
    @FXML private TextField txtMonto;
    @FXML private ComboBox<String> cbMetodo;

    private PagoDAO pagoDAO = new PagoDAO();
    private DetalleFacturaDAO cuentaDAO = new DetalleFacturaDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbMetodo.getItems().addAll(
            "Efectivo",
            "Tarjeta de debito/credito"
        );

        for (int i = 1; i <= 12; i++) {
            cmbMesa.getItems().add(i);
        }

        txtMonto.setEditable(false);

        cmbMesa.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                cargarMontoAutomatico(newVal);
            }
        });
    }

    private void cargarMontoAutomatico(int idMesa) {
        try {
            double subtotal = cuentaDAO.obtenerSubtotalMesa(idMesa);

            if (subtotal > 0) {
                double iva = subtotal * 0.16;
                double totalConIva = subtotal + iva;
                txtMonto.setText(String.format("%.2f", totalConIva));
            } else {
                txtMonto.clear();
                mostrarAlerta("Mesa sin consumo", "La mesa " + idMesa 
                        + " no tiene pedidos pendientes de pago.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar el monto.");
        }
    }

    /**
     * Registra el pago, cambia el pedido a pagado y libera la mesa.
     */
    @FXML
    private void btnRegistrarPago(ActionEvent event) {
        if (cmbMesa.getValue() == null || txtMonto.getText().isEmpty() 
                || cbMetodo.getValue() == null) {
            mostrarAlerta("Campos vacíos", 
                    "Selecciona la mesa y el método de pago.");
            return;
        }

        try {
            int idMesa = cmbMesa.getValue();
            String metodo = cbMetodo.getValue();
            double monto = Double.parseDouble(txtMonto.getText());

            int idPedido = cuentaDAO.obtenerPedidoPorMesa(idMesa);

            if (idPedido == 0) {
                mostrarAlerta("Error", "No se encontró una orden activa.");
                return;
            }

            Pago nuevoPago = new Pago();
            nuevoPago.setTotal(monto);
            nuevoPago.setMetodo(metodo);
            nuevoPago.setIdPedido(idPedido);

            boolean resultado = pagoDAO.registrarPago(nuevoPago, idMesa);

            if (resultado) {
                mostrarAlerta("Éxito", "Pago de $" + monto 
                        + " registrado correctamente.\nMesa " 
                        + idMesa + " liberada.");
                limpiarCampos(); 
            } else {
                mostrarAlerta("Error de BD", "No se pudo registrar el pago.");
            }
        } catch (Exception e) { // <--- CORREGIDO: Bloque catch e integridad añadidos
            e.printStackTrace();
            mostrarAlerta("Error", "Ocurrió un fallo al procesar el pago: " 
                    + e.getMessage());
        }
    }

    private void limpiarCampos() {
        cmbMesa.getSelectionModel().clearSelection();
        txtMonto.clear();
        cbMetodo.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    @FXML
    void volverDashboard(ActionEvent event) {
        try {
            Parent root = App.getFXMLLoader("Dashboard").load();
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException ex) {
            ex.printStackTrace();
            mostrarAlerta("Error", "No se pudo regresar al Dashboard.");
        }
    }
}