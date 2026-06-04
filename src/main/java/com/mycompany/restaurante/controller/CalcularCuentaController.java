package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.dao.DetalleFacturaDAO;
import com.mycompany.restaurante.dao.UsuarioDAO;
import com.mycompany.restaurante.modelo.pojo.Platillo;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import java.io.File;
import java.awt.Desktop;

/**
 * Controlador para la emisión y cálculo de precuentas fiscales en PDF.
 */
public class CalcularCuentaController implements Initializable {

    @FXML private ComboBox<Integer> cbMesaTicket; 
    @FXML private ComboBox<String> cmbMesero;
    @FXML private TextField txtSubtotal;
    @FXML private TextField txtIVA;
    @FXML private TextField txtTotal;
    @FXML private TextArea txtAreaTicket; 

    private DetalleFacturaDAO cuentaDAO = new DetalleFacturaDAO();

    /**
     * Configura el comportamiento estático de los campos (sólo lectura) y 
     * asigna observadores (listeners) dinámicos a los ComboBoxes.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        UsuarioDAO usuarioDAO = new UsuarioDAO();
        
        // Cierre de campos de salida para prevenir alteraciones al cobro
        if (txtSubtotal != null) txtSubtotal.setEditable(false);
        if (txtIVA != null) txtIVA.setEditable(false);
        if (txtTotal != null) txtTotal.setEditable(false);
        if (txtAreaTicket != null) txtAreaTicket.setEditable(false);
        
        // Configuración de lista de empleados
        if (cmbMesero != null) {
            cmbMesero.setItems(usuarioDAO.obtenerNombresMeseros());
            if (!cmbMesero.getItems().isEmpty()) {
                cmbMesero.getSelectionModel().selectFirst(); 
            }
            cmbMesero.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldVal, newVal) -> {
                dispararCalculoAutomatico();
            });
        }

        // Poblado de número de mesas fijas e integración de cálculo automático
        if (cbMesaTicket != null) {
            cbMesaTicket.getItems().clear();
            for (int i = 1; i <= 12; i++) {
                cbMesaTicket.getItems().add(i);
            }
            cbMesaTicket.getSelectionModel().selectedItemProperty()
                    .addListener((obs, oldVal, newVal) -> {
                dispararCalculoAutomatico();
            });
        }
    }

    /**
     * Valida que existan ambos parámetros necesarios antes de procesar el cálculo.
     */
    private void dispararCalculoAutomatico() {
        if (cbMesaTicket.getValue() != null && cmbMesero.getValue() != null) {
            procesarPrecuentaMesa(cbMesaTicket.getValue());
        }
    }

    /**
     * Obtiene el subtotal desde el DAO, deduce los impuestos de forma fiscal 
     * y genera la cadena visual del ticket.
     * @param idMesa Mesa a procesar.
     */
    private void procesarPrecuentaMesa(int idMesa) {
        double subtotal = cuentaDAO.obtenerSubtotalMesa(idMesa);

        if (subtotal > 0) {
            double iva = subtotal * 0.16;
            double total = subtotal + iva;

            txtSubtotal.setText(String.format("%.2f", subtotal));
            txtIVA.setText(String.format("%.2f", iva));
            txtTotal.setText(String.format("%.2f", total));

            generarEstructuraTicket(idMesa, subtotal, iva, total);
        } else {
            // Protección en caso de consultar mesas inactivas
            limpiarCampos();
            if (txtAreaTicket != null) {
                txtAreaTicket.setText("=== PIZZATRON 3000 ===\n\n"
                        + "La mesa " + idMesa + " no cuenta con consumos "
                        + "activos en este turno.");
            }
        }
    }

    /**
     * Compone la estructura alfanumérica simulando la salida física de un POS térmico.
     */
    private void generarEstructuraTicket(int idMesa, double sub, 
            double iva, double tot) {
        List<Platillo> items = cuentaDAO.obtenerDetallePedidoPorMesa(idMesa);
        String mesero = cmbMesero.getValue() != null 
                ? cmbMesero.getValue() : "No asignado";
        
        StringBuilder ticket = new StringBuilder();
        ticket.append("=========================================\n");
        ticket.append("              PIZZATRON 3000              \n");
        ticket.append("         TICKET DE CONSUMO ACTIVO        \n");
        ticket.append("=========================================\n");
        ticket.append("Mesa: ").append(idMesa).append("\n");
        ticket.append("Mesero: ").append(mesero).append("\n");
        ticket.append("Fecha: ").append(java.time.LocalDate.now()).append("\n");
        ticket.append("-----------------------------------------\n");
        ticket.append(String.format("%-22s %-6s %-10s\n", 
                "Platillo", "Cant.", "Precio"));
        ticket.append("-----------------------------------------\n");

        for (Platillo p : items) {
            // Manejo estricto de padding para alineación de columnas
            String nom = p.getNombre().length() > 20 
                    ? p.getNombre().substring(0, 19) : p.getNombre();
            ticket.append(String.format("%-22s %-6d $%-10.2f\n", 
                    nom, p.getCantidad(), p.getPrecio()));
        }

        ticket.append("-----------------------------------------\n");
        ticket.append(String.format("%-28s $%-10.2f\n", "Subtotal:", sub));
        ticket.append(String.format("%-28s $%-10.2f\n", "IVA (16%):", iva));
        ticket.append("=========================================\n");
        ticket.append(String.format("%-28s $%-10.2f\n", "TOTAL A PAGAR:", tot));
        ticket.append("=========================================\n");
        ticket.append("      ¡Gracias por su preferencia!       \n");
        ticket.append("=========================================");

        if (txtAreaTicket != null) txtAreaTicket.setText(ticket.toString());
    }

    /**
     * Interpreta la cadena base del ticket y lo emite como un documento PDF 
     * exportable valiéndose del motor IText7.
     */
    @FXML
    private void btnGenerarTicket(ActionEvent event) {
        if (cbMesaTicket.getValue() == null || txtTotal.getText().isEmpty()) {
            mostrarAlerta("Campos vacíos", "Selecciona una mesa válida.");
            return;
        }

        int idMesa = cbMesaTicket.getValue();
        String nombreArchivo = "Ticket_Mesa_" + idMesa + ".pdf";

        try {
            PdfWriter writer = new PdfWriter(nombreArchivo);
            PdfDocument pdf = new PdfDocument(writer);
            Document documento = new Document(pdf);

            documento.add(new Paragraph(txtAreaTicket.getText())
                    .setFontSize(10).setMultipliedLeading(1.2f));
            documento.close();

            // Instrucción de entorno (OS) para abrir el archivo autogenerado
            File archivoPdf = new File(nombreArchivo);
            if (archivoPdf.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(archivoPdf);
            }
            mostrarAlerta("Éxito", "Ticket provisional PDF abierto.");
        } catch (Exception e) {
            mostrarAlerta("Error", "No se compiló el PDF: " + e.getMessage());
        }
    }

    /**
     * Vuelve al estado inicial los TextFields de cálculo si la mesa queda desocupada.
     */
    private void limpiarCampos() {
        if (txtSubtotal != null) txtSubtotal.clear();
        if (txtIVA != null) txtIVA.clear();
        if (txtTotal != null) txtTotal.clear();
        if (txtAreaTicket != null) txtAreaTicket.clear();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo); alerta.setHeaderText(null);
        alerta.setContentText(mensaje); alerta.showAndWait();
    }

    @FXML
    void volverDashboard(ActionEvent event) {
        try {
            Parent root = App.getFXMLLoader("Dashboard").load();
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException ex) { ex.printStackTrace(); }
    }
}