// FACTURA CONTROLLER
package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.dao.DetalleFacturaDAO;
import com.mycompany.restaurante.modelo.pojo.DetalleFactura;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import javafx.stage.Stage;

// estos imports sirven para el pdf
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;

// estos igual son para el pdf pero en especifico son los que hacen que salga todo como tabla
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

/**
 * Controlador del módulo de Facturación Electrónica.
 * Gestiona la captura de datos fiscales del cliente (RFC, Régimen, Uso CFDI),
 * recupera el desglose de consumos de una mesa específica y compila físicamente
 * un comprobante fiscal en formato PDF utilizando la librería iText.
 * 
 * @author Ricardo, Diego, Angel, Stephi
 */
public class FacturaController implements Initializable {

    // datos del cliente
    @FXML private TextField txtRFC;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCP;
    @FXML private TextField txtCorreo;
    @FXML private ComboBox<String> cbUsoCFDI;
    @FXML private ComboBox<String> cbRegimenReceptor;

    // datos generales de la factura
    @FXML private TextField txtFolio;
    @FXML private TextField txtFecha;
    @FXML private ComboBox<String> cbFormaPago;
    @FXML private ComboBox<Integer> cbMesaFactura;

    // tabla donde se muestran los productos de la mesa
    @FXML private TableView<DetalleFactura> tvFactura;
    @FXML private TableColumn<DetalleFactura, String> colClave;
    @FXML private TableColumn<DetalleFactura, Integer> colCantidad;
    @FXML private TableColumn<DetalleFactura, String> colUnidad;
    @FXML private TableColumn<DetalleFactura, String> colPlatillo;
    @FXML private TableColumn<DetalleFactura, Double> colPrecio;
    @FXML private TableColumn<DetalleFactura, Double> colTotal;

    // campos que quedaron del diseño original
    @FXML private ComboBox<String> cbAgregarConcepto;
    @FXML private TextField txtConceptoConsumo;
    @FXML private ComboBox<String> cbRegimenFiscalAbajo;

    // totales de la factura
    @FXML private TextField txtSubtotal;
    @FXML private TextField txtIVA;
    @FXML private TextField txtTotalGeneral;

    // botones
    @FXML private Button btnVolver;
    @FXML private Button btnGenerarFactura;

    private int idMesa;
    private DetalleFacturaDAO dao = new DetalleFacturaDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarCombos();
        configurarFechaFolio();
        configurarMesas();
        configurarTabla();
    }

    // aqui se llenan los combos de la factura
    private void configurarCombos() {
        cbUsoCFDI.getItems().addAll(
            "G01 - Adquisición de mercancías",
            "G03 - Gastos en general",
            "S01 - Sin efectos fiscales"
        );
        cbUsoCFDI.setValue("G03 - Gastos en general");

        cbRegimenReceptor.getItems().addAll(
            "601 - General de Ley Personas Morales",
            "603 - Personas Morales con Fines no Lucrativos",
            "605 - Sueldos y Salarios e Ingresos Asimilados a Salarios",
            "606 - Arrendamiento",
            "612 - Personas Físicas con Actividades Empresariales y Profesionales",
            "616 - Sin obligaciones fiscales",
            "621 - Incorporación Fiscal",
            "626 - Régimen Simplificado de Confianza (RESICO)"
        );
        cbRegimenReceptor.setValue("616 - Sin obligaciones fiscales");

        cbFormaPago.getItems().addAll(
            "01 - Efectivo",
            "04 - Tarjeta de debito/credito"
        );
        cbFormaPago.setValue("01 - Efectivo");
    }

    // fecha automática y folio sencillo
    private void configurarFechaFolio() {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        txtFecha.setText(LocalDateTime.now().format(formato));

        if (txtFolio != null && txtFolio.getText().isEmpty()) {
            txtFolio.setText("F-" + System.currentTimeMillis());
        }
    }

    // llena el combo de mesas y carga la factura al seleccionar una
    private void configurarMesas() {
        if (cbMesaFactura == null) {
            return;
        }

        cbMesaFactura.getItems().clear();

        for (int i = 1; i <= 12; i++) {
            cbMesaFactura.getItems().add(i);
        }

        cbMesaFactura.setPromptText("Mesas");

        cbMesaFactura.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                inicializarFactura(newVal);
            }
        });
    }

    // columnas de la tabla de factura
    private void configurarTabla() {
        colClave.setCellValueFactory(new PropertyValueFactory<>("claveProdServ"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colUnidad.setCellValueFactory(new PropertyValueFactory<>("unidad"));
        colPlatillo.setCellValueFactory(new PropertyValueFactory<>("platillo"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
    }

    @FXML
    private void btnGenerarFacturaAction() {
        String rfc = obtenerTexto(txtRFC).trim().toUpperCase();

        if (!rfc.matches("[A-ZÑ&]{3,4}\\d{6}[A-Z0-9]{3}")) {
            mostrarAlerta("RFC inválido", "Formato incorrecto.\nEjemplo: ABCD010203EF1");
            return;
        }

        if (tvFactura.getItems().isEmpty()) {
            mostrarAlerta("Sin productos", "Selecciona una mesa con consumo antes de generar la factura.");
            return;
        }

        try {
            String ruta = "Factura_" + rfc + ".pdf";

            PdfWriter writer = new PdfWriter(ruta);
            PdfDocument pdf = new PdfDocument(writer);
            pdf.setDefaultPageSize(PageSize.A4);

            Document document = new Document(pdf);
            document.setMargins(25, 25, 25, 25);

            PdfFont fontNormal = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont fontBold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            DeviceRgb azul = new DeviceRgb(0, 91, 150);
            DeviceRgb grisClaro = new DeviceRgb(240, 240, 240);
            DeviceRgb grisBorde = new DeviceRgb(160, 160, 160);

            document.setFont(fontNormal);

            agregarEncabezado(document, fontNormal, fontBold, azul);
            agregarDatosCliente(document, fontNormal, fontBold, azul, grisBorde, rfc);
            agregarDatosComprobante(document, fontNormal, fontBold, azul);
            agregarTablaProductos(document, fontNormal, fontBold, azul);
            agregarTotales(document, fontNormal, fontBold, grisClaro, grisBorde);
            agregarSello(document, fontNormal, fontBold, grisBorde);

            document.add(new Paragraph("Este comprobante fue generado por Pizzatron CP para fines demostrativos.")
                .setFont(fontNormal)
                .setFontSize(7)
                .setTextAlignment(TextAlignment.CENTER));

            document.close();

            java.awt.Desktop.getDesktop().open(new java.io.File(ruta));

            mostrarAlerta("Éxito", "Factura PDF generada correctamente.");

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo generar el PDF.");
        }
    }

    // encabezado principal del PDF
    private void agregarEncabezado(Document document, PdfFont fontNormal, PdfFont fontBold, DeviceRgb azul) {
        Table encabezado = new Table(UnitValue.createPercentArray(new float[]{70, 30}));
        encabezado.setWidth(UnitValue.createPercentValue(100));

        Cell datosEmpresa = new Cell()
            .add(new Paragraph("PIZZATRON CP S.A. DE C.V.").setFont(fontBold).setFontSize(14))
            .add(new Paragraph("RFC: PIZ240101ABC").setFont(fontNormal).setFontSize(8))
            .add(new Paragraph("Domicilio: Av. Club Penguin #123, Xalapa, Veracruz").setFont(fontNormal).setFontSize(8))
            .add(new Paragraph("Régimen Fiscal: 601 - General de Ley Personas Morales").setFont(fontNormal).setFontSize(8))
            .setBorder(Border.NO_BORDER);

        Cell datosFactura = new Cell()
            .add(new Paragraph("FACTURA").setFont(fontBold).setFontSize(18).setFontColor(azul).setTextAlignment(TextAlignment.RIGHT))
            .add(new Paragraph("Folio: " + obtenerTexto(txtFolio)).setFont(fontNormal).setFontSize(9).setTextAlignment(TextAlignment.RIGHT))
            .add(new Paragraph("Fecha: " + obtenerTexto(txtFecha)).setFont(fontNormal).setFontSize(9).setTextAlignment(TextAlignment.RIGHT))
            .add(new Paragraph("Tipo: Ingreso").setFont(fontNormal).setFontSize(9).setTextAlignment(TextAlignment.RIGHT))
            .setBorder(Border.NO_BORDER);

        encabezado.addCell(datosEmpresa);
        encabezado.addCell(datosFactura);

        document.add(encabezado);
        document.add(new Paragraph(" ").setFontSize(4));
    }

    // datos del receptor o cliente
    private void agregarDatosCliente(Document document, PdfFont fontNormal, PdfFont fontBold, DeviceRgb azul, DeviceRgb grisBorde, String rfc) {
        Table receptor = new Table(UnitValue.createPercentArray(new float[]{100}));
        receptor.setWidth(UnitValue.createPercentValue(100));

        receptor.addCell(new Cell()
            .add(new Paragraph("DATOS DEL CLIENTE / RECEPTOR").setFont(fontBold).setFontSize(10).setFontColor(ColorConstants.WHITE))
            .setBackgroundColor(azul)
            .setBorder(new SolidBorder(azul, 1)));

        Table datosReceptor = new Table(UnitValue.createPercentArray(new float[]{20, 30, 20, 30}));
        datosReceptor.setWidth(UnitValue.createPercentValue(100));

        datosReceptor.addCell(celdaEtiqueta("RFC:", fontBold));
        datosReceptor.addCell(celdaDato(rfc, fontNormal));
        datosReceptor.addCell(celdaEtiqueta("Cliente:", fontBold));
        datosReceptor.addCell(celdaDato(obtenerTexto(txtNombre), fontNormal));

        datosReceptor.addCell(celdaEtiqueta("Código Postal:", fontBold));
        datosReceptor.addCell(celdaDato(obtenerTexto(txtCP), fontNormal));
        datosReceptor.addCell(celdaEtiqueta("Correo:", fontBold));
        datosReceptor.addCell(celdaDato(obtenerTexto(txtCorreo), fontNormal));

        datosReceptor.addCell(celdaEtiqueta("Uso CFDI:", fontBold));
        datosReceptor.addCell(celdaDato(valorCombo(cbUsoCFDI), fontNormal));
        datosReceptor.addCell(celdaEtiqueta("Régimen Fiscal:", fontBold));
        datosReceptor.addCell(celdaDato(valorCombo(cbRegimenReceptor), fontNormal));

        receptor.addCell(new Cell().add(datosReceptor).setBorder(new SolidBorder(grisBorde, 1)));
        document.add(receptor);
        document.add(new Paragraph(" ").setFontSize(4));
    }

    // forma de pago, metodo, moneda y lugar
    private void agregarDatosComprobante(Document document, PdfFont fontNormal, PdfFont fontBold, DeviceRgb azul) {
        Table comprobante = new Table(UnitValue.createPercentArray(new float[]{25, 25, 25, 25}));
        comprobante.setWidth(UnitValue.createPercentValue(100));

        comprobante.addCell(celdaTituloAzul("Forma de Pago", fontBold, azul));
        comprobante.addCell(celdaTituloAzul("Método de Pago", fontBold, azul));
        comprobante.addCell(celdaTituloAzul("Moneda", fontBold, azul));
        comprobante.addCell(celdaTituloAzul("Lugar Expedición", fontBold, azul));

        comprobante.addCell(celdaDato(valorCombo(cbFormaPago), fontNormal));
        comprobante.addCell(celdaDato("PUE - Pago en una sola exhibición", fontNormal));
        comprobante.addCell(celdaDato("MXN", fontNormal));
        comprobante.addCell(celdaDato("91000", fontNormal));

        document.add(comprobante);
        document.add(new Paragraph(" ").setFontSize(4));
    }

    // tabla con los platillos o productos consumidos
    private void agregarTablaProductos(Document document, PdfFont fontNormal, PdfFont fontBold, DeviceRgb azul) {
        Table tablaProductos = new Table(UnitValue.createPercentArray(new float[]{14, 10, 12, 34, 15, 15}));
        tablaProductos.setWidth(UnitValue.createPercentValue(100));

        tablaProductos.addHeaderCell(celdaTituloAzul("Clave", fontBold, azul));
        tablaProductos.addHeaderCell(celdaTituloAzul("Cant.", fontBold, azul));
        tablaProductos.addHeaderCell(celdaTituloAzul("Unidad", fontBold, azul));
        tablaProductos.addHeaderCell(celdaTituloAzul("Descripción", fontBold, azul));
        tablaProductos.addHeaderCell(celdaTituloAzul("P. Unitario", fontBold, azul));
        tablaProductos.addHeaderCell(celdaTituloAzul("Importe", fontBold, azul));

        for (DetalleFactura detalle : tvFactura.getItems()) {
            tablaProductos.addCell(celdaDato(detalle.getClaveProdServ(), fontNormal));
            tablaProductos.addCell(celdaDato(String.valueOf(detalle.getCantidad()), fontNormal));
            tablaProductos.addCell(celdaDato(detalle.getUnidad(), fontNormal));
            tablaProductos.addCell(celdaDato(detalle.getPlatillo(), fontNormal));
            tablaProductos.addCell(celdaDato("$" + String.format("%.2f", detalle.getPrecioUnitario()), fontNormal));
            tablaProductos.addCell(celdaDato("$" + String.format("%.2f", detalle.getTotal()), fontNormal));
        }

        document.add(tablaProductos);
        document.add(new Paragraph(" ").setFontSize(4));
    }

    // totales de la factura
    private void agregarTotales(Document document, PdfFont fontNormal, PdfFont fontBold, DeviceRgb grisClaro, DeviceRgb grisBorde) {
        Table totales = new Table(UnitValue.createPercentArray(new float[]{65, 20, 15}));
        totales.setWidth(UnitValue.createPercentValue(100));

        Cell nota = new Cell(4, 1)
            .add(new Paragraph("Observaciones:").setFont(fontBold).setFontSize(8))
            .add(new Paragraph("Consumo de alimentos realizado en restaurante.").setFont(fontNormal).setFontSize(8))
            .add(new Paragraph("Documento generado por el sistema Pizzatron CP.").setFont(fontNormal).setFontSize(8))
            .setBorder(new SolidBorder(grisBorde, 1));

        totales.addCell(nota);
        totales.addCell(celdaEtiqueta("Subtotal:", fontBold));
        totales.addCell(celdaImporte("$" + obtenerTexto(txtSubtotal), fontNormal));
        totales.addCell(celdaEtiqueta("IVA 16%:", fontBold));
        totales.addCell(celdaImporte("$" + obtenerTexto(txtIVA), fontNormal));
        totales.addCell(celdaEtiqueta("Total:", fontBold));

        totales.addCell(new Cell()
            .add(new Paragraph("$" + obtenerTexto(txtTotalGeneral)).setFont(fontBold).setFontSize(12).setTextAlignment(TextAlignment.RIGHT))
            .setBackgroundColor(grisClaro)
            .setBorder(new SolidBorder(grisBorde, 1)));

        totales.addCell(celdaEtiqueta("Moneda:", fontBold));
        totales.addCell(celdaImporte("MXN", fontNormal));

        document.add(totales);
        document.add(new Paragraph(" ").setFontSize(4));
    }

    // parte final tipo sello/QR simulado
    private void agregarSello(Document document, PdfFont fontNormal, PdfFont fontBold, DeviceRgb grisBorde) {
        Table sello = new Table(UnitValue.createPercentArray(new float[]{25, 75}));
        sello.setWidth(UnitValue.createPercentValue(100));

        Cell qr = new Cell()
            .add(new Paragraph("QR").setFont(fontBold).setFontSize(18).setTextAlignment(TextAlignment.CENTER))
            .add(new Paragraph("Simulado").setFont(fontNormal).setFontSize(7).setTextAlignment(TextAlignment.CENTER))
            .setHeight(90)
            .setBorder(new SolidBorder(grisBorde, 1));

        Cell selloTexto = new Cell()
            .add(new Paragraph("Sello digital del CFDI").setFont(fontBold).setFontSize(8))
            .add(new Paragraph("Cadena original generada por el sistema para fines académicos.").setFont(fontNormal).setFontSize(7))
            .add(new Paragraph("UUID: 00000000-0000-0000-0000-000000000000").setFont(fontNormal).setFontSize(7))
            .add(new Paragraph("No. certificado: 00001000000000000000").setFont(fontNormal).setFontSize(7))
            .setBorder(new SolidBorder(grisBorde, 1));

        sello.addCell(qr);
        sello.addCell(selloTexto);
        document.add(sello);
    }

    // se manda llamar cuando se selecciona una mesa
    public void inicializarFactura(int idMesa) {
        this.idMesa = idMesa;

        tvFactura.setItems(dao.obtenerDetallesFactura(idMesa));

        double subtotal = dao.obtenerSubtotalMesa(idMesa);
        double iva = subtotal * 0.16;
        double total = subtotal + iva;

        txtSubtotal.setText(String.format("%.2f", subtotal));
        txtIVA.setText(String.format("%.2f", iva));
        txtTotalGeneral.setText(String.format("%.2f", total));
    }

    // boton para regresar al dashboard
    @FXML
    void volverDashboard(ActionEvent event) {
        try {
            Parent root = App.getFXMLLoader("Dashboard").load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (IOException ex) {
            ex.printStackTrace();
            mostrarAlerta("Error", "No se pudo regresar al Dashboard.");
        }
    }

    // celdas azules para titulos de tablas
    private Cell celdaTituloAzul(String texto, PdfFont fuente, DeviceRgb colorFondo) {
        return new Cell()
            .add(new Paragraph(texto).setFont(fuente).setFontSize(8).setFontColor(ColorConstants.WHITE).setTextAlignment(TextAlignment.CENTER))
            .setBackgroundColor(colorFondo)
            .setBorder(new SolidBorder(colorFondo, 1));
    }

    // celdas de etiqueta, como RFC, Cliente, Total, etc.
    private Cell celdaEtiqueta(String texto, PdfFont fuente) {
        return new Cell()
            .add(new Paragraph(texto).setFont(fuente).setFontSize(8))
            .setBackgroundColor(new DeviceRgb(240, 240, 240))
            .setBorder(new SolidBorder(new DeviceRgb(160, 160, 160), 1));
    }

    // celdas normales con información
    private Cell celdaDato(String texto, PdfFont fuente) {
        if (texto == null) {
            texto = "";
        }

        return new Cell()
            .add(new Paragraph(texto).setFont(fuente).setFontSize(8))
            .setBorder(new SolidBorder(new DeviceRgb(160, 160, 160), 1));
    }

    // celdas para importes alineados a la derecha
    private Cell celdaImporte(String texto, PdfFont fuente) {
        return new Cell()
            .add(new Paragraph(texto).setFont(fuente).setFontSize(8).setTextAlignment(TextAlignment.RIGHT))
            .setBorder(new SolidBorder(new DeviceRgb(160, 160, 160), 1));
    }

    // evita errores si un TextField viene vacío o no existe
    private String obtenerTexto(TextField campo) {
        if (campo == null || campo.getText() == null) {
            return "";
        }

        return campo.getText();
    }

    // evita errores si un ComboBox no tiene valor seleccionado
    private String valorCombo(ComboBox<String> combo) {
        if (combo == null || combo.getValue() == null) {
            return "";
        }

        return combo.getValue();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}