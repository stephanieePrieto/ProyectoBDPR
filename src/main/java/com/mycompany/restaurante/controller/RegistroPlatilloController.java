package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.dao.PlatilloDAO;
import com.mycompany.restaurante.modelo.pojo.Platillo;
import com.mycompany.restaurante.modelo.sql.OracleConnect; // Conexión Oracle
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Controlador de Registro de Platillos migrado a Oracle.
 */
public class RegistroPlatilloController {

    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtPrecio;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private ComboBox<String> cmbIngrediente;
    @FXML private TableView<Platillo> tblPlatillos;
    @FXML private TableColumn<Platillo, String> colNombre;
    @FXML private TableColumn<Platillo, Double> colPrecio;
    @FXML private Label lblNombreImagen;
    @FXML private ImageView imgVistaPrevia;

    private File archivoImagenSeleccionado;
    private String nombreImagenFinal = "default.png";
    private ObservableList<Platillo> listaPlatillos;
    private Platillo platilloSeleccionado;

    @FXML
    public void initialize() {
        cmbCategoria.getItems().addAll("Pizzas", "Bebidas", "Pasteles", "Extras", "Especiales");
        cargarIngredientesAlmacen();
        
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        listaPlatillos = FXCollections.observableArrayList();
        tblPlatillos.setItems(listaPlatillos);
        cargarTabla();

        // Listener de selección
        tblPlatillos.getSelectionModel().selectedItemProperty().addListener((obs, old, nvo) -> {
            if (nvo != null) {
                platilloSeleccionado = nvo;
                txtNombre.setText(nvo.getNombre());
                txtDescripcion.setText(nvo.getDescripcion());
                txtPrecio.setText(String.valueOf(nvo.getPrecio()));
            }
        });
    }

    private void cargarIngredientesAlmacen() {
        cmbIngrediente.getItems().clear();
        cmbIngrediente.getItems().add("0 - Ninguno (Venta Libre)");
        try (Connection con = OracleConnect.getConexion()) {
            String sql = "SELECT idMateriaPrima, nombre FROM inventariomateriaprima";
            try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    cmbIngrediente.getItems().add(rs.getInt("idMateriaPrima") + " - " + rs.getString("nombre"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    void clicSeleccionarImagen(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg"));
        archivoImagenSeleccionado = fc.showOpenDialog(null);
        if (archivoImagenSeleccionado != null) {
            nombreImagenFinal = archivoImagenSeleccionado.getName();
            lblNombreImagen.setText(nombreImagenFinal);
            imgVistaPrevia.setImage(new Image(archivoImagenSeleccionado.toURI().toString()));
        }
    }

    @FXML
    void clicGuardar(ActionEvent event) {
        String nombre = txtNombre.getText();
        String precioTexto = txtPrecio.getText();
        String categoria = cmbCategoria.getValue();

        if (nombre.isEmpty() || precioTexto.isEmpty() || categoria == null) {
            mostrarAlerta("Error", "Campos obligatorios vacíos", Alert.AlertType.WARNING);
            return;
        }

        try {
            double precio = Double.parseDouble(precioTexto);
            // Lógica de categoría
            int idCat = 1; 
            
            // Procesar imagen
            if (archivoImagenSeleccionado != null) {
                Path destino = Paths.get("src/main/resources/img/" + nombreImagenFinal);
                Files.copy(archivoImagenSeleccionado.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
            }

            Platillo p = new Platillo(0, nombre, txtDescripcion.getText(), precio, categoria, nombreImagenFinal, false, true, idCat);
            
            try (Connection con = OracleConnect.getConexion()) {
                PlatilloDAO dao = new PlatilloDAO(con);
                if (platilloSeleccionado == null) dao.registrarPlatillo(p);
                else {
                    p.setIdPlatillo(platilloSeleccionado.getIdPlatillo());
                    dao.actualizarPlatillo(p);
                }
            }
            limpiarCampos();
            cargarTabla();
            mostrarAlerta("Éxito", "Platillo procesado correctamente", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            mostrarAlerta("Error", "Fallo al guardar: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void cargarTabla() {
        listaPlatillos.clear();
        try (Connection con = OracleConnect.getConexion()) {
            PlatilloDAO dao = new PlatilloDAO(con);
            listaPlatillos.addAll(dao.obtenerPlatillosActivos());
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    void clicLimpiar(ActionEvent event) { limpiarCampos(); }

    @FXML
    void clicCancelar(ActionEvent event) {
        try {
            Parent root = App.getFXMLLoader("Dashboard").load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) { e.printStackTrace(); }
    }
    
    @FXML
    void clicDarDeBaja(ActionEvent event) {
        if (platilloSeleccionado == null) {
            mostrarAlerta("Selección requerida", "Por favor, selecciona un platillo de la tabla para darlo de baja.", Alert.AlertType.WARNING);
            return;
        }

        try (Connection conexion = OracleConnect.getConexion()) {
            PlatilloDAO dao = new PlatilloDAO(conexion);
            // Asegúrate de que este método exista en tu PlatilloDAO
            if (dao.darDeBajaPlatillo(platilloSeleccionado.getIdPlatillo())) {
                mostrarAlerta("Éxito", "El platillo ha sido dado de baja.", Alert.AlertType.INFORMATION);
                limpiarCampos();
                cargarTabla();
            } else {
                mostrarAlerta("Error", "No se pudo actualizar el estado.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "Fallo de conexión: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void limpiarCampos() {
        txtNombre.clear(); txtDescripcion.clear(); txtPrecio.clear();
        cmbCategoria.setValue(null); cmbIngrediente.setValue(null);
        imgVistaPrevia.setImage(null); platilloSeleccionado = null;
    }

    private void mostrarAlerta(String titulo, String msg, Alert.AlertType tipo) {
        Alert a = new Alert(tipo); a.setTitle(titulo); a.setContentText(msg); a.showAndWait();
    }
    
    
}