package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.dao.PlatilloDAO;
import com.mycompany.restaurante.modelo.pojo.Platillo;
import com.mycompany.restaurante.modelo.sql.MySQLConnect;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class RegistroPlatilloController {

    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtPrecio;
    @FXML private ComboBox<String> cmbCategoria;
    
    // NUEVO COMBOBOX PARA ALMACÉN
    @FXML private ComboBox<String> cmbIngrediente;
    
    @FXML private javafx.scene.control.TableView<Platillo> tblPlatillos;
    @FXML private javafx.scene.control.TableColumn<Platillo, String> colNombre;
    @FXML private javafx.scene.control.TableColumn<Platillo, Double> colPrecio;
    
    @FXML private Label lblNombreImagen;
    @FXML private ImageView imgVistaPrevia;
    private File archivoImagenSeleccionado;
    private String nombreImagenFinal = "default.png";

    private javafx.collections.ObservableList<Platillo> listaPlatillos;
    
    private Platillo platilloSeleccionado;

    @FXML
    public void initialize() {
        cmbCategoria.getItems().addAll("Pizzas", "Bebidas", "Pasteles", "Extras", "Especiales");
        
        // Cargar los ingredientes disponibles desde la BD
        cargarIngredientesAlmacen();
        
        colNombre.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("nombre"));
        colPrecio.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("precio"));
        listaPlatillos = javafx.collections.FXCollections.observableArrayList();
        tblPlatillos.setItems(listaPlatillos);
        cargarTabla();
        
        tblPlatillos.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                platilloSeleccionado = newSelection;
                txtNombre.setText(platilloSeleccionado.getNombre());
                txtDescripcion.setText(platilloSeleccionado.getDescripcion() != null ? platilloSeleccionado.getDescripcion() : "");
                txtPrecio.setText(String.valueOf(platilloSeleccionado.getPrecio()));
                
                String catStr = "Pizzas";
                switch(platilloSeleccionado.getIdCategoria()){
                    case 1: catStr = "Pizzas"; break;
                    case 2: catStr = "Bebidas"; break;
                    case 3: catStr = "Pasteles"; break;
                    case 4: catStr = "Extras"; break;
                    case 5: catStr = "Especiales"; break;
                }
                cmbCategoria.setValue(catStr);
                
                // Reconocer el Ingrediente Asignado en el ComboBox
                int idInsumo = platilloSeleccionado.getIdInsumoClave();
                boolean encontrado = false;
                for (String item : cmbIngrediente.getItems()) {
                    if (item.startsWith(idInsumo + " - ")) {
                        cmbIngrediente.setValue(item);
                        encontrado = true;
                        break;
                    }
                }
                if (!encontrado) cmbIngrediente.setValue("0 - Ninguno (Venta Libre)");
                
                nombreImagenFinal = platilloSeleccionado.getImagen();
                if (nombreImagenFinal == null || nombreImagenFinal.isEmpty()) nombreImagenFinal = "default.png";
                lblNombreImagen.setText(nombreImagenFinal);
                
                try {
                    java.net.URL urlImg = getClass().getResource("/img/" + nombreImagenFinal);
                    if (urlImg != null) {
                        imgVistaPrevia.setImage(new Image(urlImg.toExternalForm()));
                    } else {
                        imgVistaPrevia.setImage(null);
                    }
                } catch (Exception e) {
                    imgVistaPrevia.setImage(null);
                }
            }
        });
    }

    /**
     * Consulta la tabla de almacén para que el Gerente pueda enlazar los platillos.
     */
    private void cargarIngredientesAlmacen() {
        cmbIngrediente.getItems().clear();
        cmbIngrediente.getItems().add("0 - Ninguno (Venta Libre)");
        
        MySQLConnect mysql = new MySQLConnect(); 
        try (Connection conexion = mysql.connection()) {
            if (conexion != null) {
                String sql = "SELECT idMateriaPrima, nombre FROM almacen";
                try(PreparedStatement ps = conexion.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {
                    while(rs.next()) {
                        cmbIngrediente.getItems().add(rs.getInt("idMateriaPrima") + " - " + rs.getString("nombre"));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al cargar almacén: " + e.getMessage());
        }
    }

    @FXML
    void clicSeleccionarImagen(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Buscar Foto del Platillo");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg")
        );

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        archivoImagenSeleccionado = fileChooser.showOpenDialog(stage);

        if (archivoImagenSeleccionado != null) {
            nombreImagenFinal = archivoImagenSeleccionado.getName();
            lblNombreImagen.setText(nombreImagenFinal);
            Image image = new Image(archivoImagenSeleccionado.toURI().toString());
            imgVistaPrevia.setImage(image);
        }
    }

    @FXML
    void clicGuardar(ActionEvent event) {
        String nombre = txtNombre.getText();
        String desc = txtDescripcion.getText();
        String precioTexto = txtPrecio.getText();
        String categoria = cmbCategoria.getValue();

        if (nombre.trim().isEmpty() || precioTexto.trim().isEmpty() || categoria == null) {
            mostrarAlerta("Datos Incompletos", "⚠️ Nombre, Precio y Categoría son obligatorios.", Alert.AlertType.WARNING);
            return;
        }

        try {
            double precio = Double.parseDouble(precioTexto);
            boolean esBebida = categoria.equals("Bebidas");

            int idCategoria = 1;
            switch (categoria) {
                case "Pizzas": idCategoria = 1; break;
                case "Bebidas": idCategoria = 2; break;
                case "Pasteles": idCategoria = 3; break;
                case "Extras": idCategoria = 4; break;
                case "Especiales": idCategoria = 5; break;
            }

            // Extraer el ID del ingrediente seleccionado
            int idInsumo = 0;
            if (cmbIngrediente.getValue() != null && !cmbIngrediente.getValue().equals("0 - Ninguno (Venta Libre)")) {
                try {
                    idInsumo = Integer.parseInt(cmbIngrediente.getValue().split(" - ")[0]);
                } catch (Exception e) {
                    idInsumo = 0;
                }
            }

            if (archivoImagenSeleccionado != null) {
                try {
                    Path destino = Paths.get("src/main/resources/img/" + nombreImagenFinal);
                    Files.copy(archivoImagenSeleccionado.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    System.err.println("Aviso: No se pudo copiar la imagen: " + e.getMessage());
                }
            }

            Platillo platillo = new Platillo(0, nombre, desc, precio, categoria, nombreImagenFinal, esBebida, true, idCategoria);
            platillo.setIdInsumoClave(idInsumo); // Inyectamos la conexión con el almacén

            MySQLConnect mysql = new MySQLConnect(); 
            Connection conexion = mysql.connection(); 

            if (conexion != null) {
                PlatilloDAO dao = new PlatilloDAO(conexion);
                
                if (platilloSeleccionado == null) {
                    if (dao.registrarPlatillo(platillo)) {
                        mostrarAlerta("Éxito", "¡Platillo registrado correctamente!", Alert.AlertType.INFORMATION);
                    } else {
                        mostrarAlerta("Error", "No se pudo registrar.", Alert.AlertType.ERROR);
                    }
                } else {
                    platillo.setIdPlatillo(platilloSeleccionado.getIdPlatillo()); 
                    if (dao.actualizarPlatillo(platillo)) {
                        mostrarAlerta("Éxito", "¡Platillo actualizado correctamente!", Alert.AlertType.INFORMATION);
                    } else {
                        mostrarAlerta("Error", "No se pudo actualizar.", Alert.AlertType.ERROR);
                    }
                }
                
                limpiarCampos();
                cargarTabla();
                mysql.close(); 
            }

        } catch (NumberFormatException e) {
            mostrarAlerta("Error de Formato", "El precio debe ser un número válido (Ej. 120.50).", Alert.AlertType.ERROR);
        }
    }

    @FXML
    void clicLimpiar(ActionEvent event) {
        limpiarCampos();
    }

    @FXML
    void clicCancelar(ActionEvent event) {
        try {
            FXMLLoader loader = App.getFXMLLoader("Dashboard"); 
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard - Sistema Restaurante");
            stage.show();
        } catch (java.io.IOException ex) {
            ex.printStackTrace();
            mostrarAlerta("Error", "No se pudo cargar la pantalla de inicio.", Alert.AlertType.ERROR);
        }
    }

    private void limpiarCampos() {
        txtNombre.clear();
        txtDescripcion.clear();
        txtPrecio.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        cmbIngrediente.getSelectionModel().clearSelection();
        cmbIngrediente.setValue(null);
        
        lblNombreImagen.setText("default.png");
        imgVistaPrevia.setImage(null);
        archivoImagenSeleccionado = null;
        nombreImagenFinal = "default.png";
        
        platilloSeleccionado = null;
        tblPlatillos.getSelectionModel().clearSelection();
    }

    private void cargarTabla() {
        listaPlatillos.clear();
        MySQLConnect mysql = new MySQLConnect(); 
        try (Connection conexion = mysql.connection()) {
            if (conexion != null) {
                PlatilloDAO dao = new PlatilloDAO(conexion);
                listaPlatillos.addAll(dao.obtenerPlatillosActivos());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    void clicDarDeBaja(ActionEvent event) {
        if (platilloSeleccionado == null) {
            mostrarAlerta("Selección requerida", "⚠️ Por favor, selecciona un platillo de la tabla para darlo de baja.", Alert.AlertType.WARNING);
            return;
        }

        MySQLConnect mysql = new MySQLConnect(); 
        try (Connection conexion = mysql.connection()) {
            if (conexion != null) {
                PlatilloDAO dao = new PlatilloDAO(conexion);
                if (dao.darDeBajaPlatillo(platilloSeleccionado.getIdPlatillo())) {
                    mostrarAlerta("Éxito", "El platillo ha sido dado de baja del menú.", Alert.AlertType.INFORMATION);
                    limpiarCampos();
                    cargarTabla();
                } else {
                    mostrarAlerta("Error", "No se pudo actualizar el estado en MySQL.", Alert.AlertType.ERROR);
                }
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "Fallo de conexión.", Alert.AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}