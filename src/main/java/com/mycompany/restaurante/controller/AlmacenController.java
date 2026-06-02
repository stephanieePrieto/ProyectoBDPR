package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.dao.AlmacenDAO;
import com.mycompany.restaurante.modelo.pojo.ProductoAlmacen;
import com.mycompany.restaurante.modelo.pojo.Usuario;
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
import java.util.List;

/**
 * Controlador de UI para la administración del inventario de insumos.
 * Sostiene la lógica del formulario reactivo conectado a la tabla 
 * materiaprima, gestionando flujos síncronos de inserción, edición y bajas.
 */
public class AlmacenController {

    // Cambiamos el TextField por ComboBox para el nombre
    @FXML private ComboBox<String> cmbNombre;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtStockMinimo;
    @FXML private ComboBox<String> cbUnidad;
    
    @FXML private TableView<ProductoAlmacen> tblAlmacen;
    @FXML private TableColumn<ProductoAlmacen, String> colNombre;
    @FXML private TableColumn<ProductoAlmacen, String> colUnidad;
    @FXML private TableColumn<ProductoAlmacen, Double> colCantidad;
    @FXML private TableColumn<ProductoAlmacen, Double> colMinimo;

    private AlmacenDAO dao = new AlmacenDAO();
    private ObservableList<ProductoAlmacen> listaProductos;
    private ProductoAlmacen productoSeleccionado;

    @FXML
    public void initialize() {
        cbUnidad.getItems().addAll(
            "Kilogramos (kg)", 
            "Litros (L)", 
            "Gramos (g)", 
            "Mililitros (ml)", 
            "Piezas (pz)",
            "Porciones",
            "Rebanadas",
            "Tazas",
            "Unidades",
            "Latas",
            "Vasos"
        );
        
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colUnidad.setCellValueFactory(new PropertyValueFactory<>("unidad"));
        colMinimo.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
        
        cargarDatos();
        
        // Listener para poblar el formulario al seleccionar una fila de la tabla
        tblAlmacen.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                productoSeleccionado = newSelection;
                // Asignamos el valor al ComboBox
                cmbNombre.setValue(productoSeleccionado.getNombre());
                txtCantidad.setText(String.valueOf(productoSeleccionado.getCantidad()));
                cbUnidad.setValue(productoSeleccionado.getUnidad());
                txtStockMinimo.setText(String.valueOf(productoSeleccionado.getStockMinimo()));
            }
        });
    }

    private void cargarDatos() {
        List<ProductoAlmacen> productosDB = dao.obtenerProductos();
        listaProductos = FXCollections.observableArrayList(productosDB);
        tblAlmacen.setItems(listaProductos);
        
        // Actualizar la lista desplegable del ComboBox con los nombres actuales
        cmbNombre.getItems().clear();
        for (ProductoAlmacen p : productosDB) {
            cmbNombre.getItems().add(p.getNombre());
        }
    }

    @FXML
    private void clicGuardar(ActionEvent event) {
        // Al ser editable, debemos extraer el texto directamente del editor interno
        String nombre = "";
        if (cmbNombre.getEditor().getText() != null) {
            nombre = cmbNombre.getEditor().getText().trim();
        }
        
        String unidad = cbUnidad.getValue();
        
        if (nombre.isEmpty() || txtCantidad.getText().isEmpty() 
                || unidad == null || txtStockMinimo.getText().isEmpty()) {
            mostrarAlerta("Error", "Todos los campos son obligatorios.");
            return;
        }

        try {
            double cantidad = Double.parseDouble(txtCantidad.getText());
            double stockMinimo = Double.parseDouble(txtStockMinimo.getText());
 
            // Si el usuario escribió un nombre que ya existe pero no lo seleccionó en la tabla,
            // lo detectamos automáticamente para actualizarlo en lugar de duplicarlo.
            if (productoSeleccionado == null) {
                for (ProductoAlmacen p : listaProductos) {
                    if (p.getNombre().equalsIgnoreCase(nombre)) {
                        productoSeleccionado = p;
                        break;
                    }
                }
            }

            if (productoSeleccionado == null) {
                // Inserción de un ingrediente totalmente nuevo
                ProductoAlmacen nuevo = new ProductoAlmacen(
                        0, nombre, cantidad, unidad, stockMinimo);
                if (dao.registrarProducto(nuevo)) {
                    mostrarAlerta("Éxito", "Insumo agregado al inventario de materia prima.");
                } else {
                    mostrarAlerta("Error", "No se pudo registrar.");
                }
            } else {
                // Actualización del inventario existente
                productoSeleccionado.setNombre(nombre); // Por si corrigió una letra
                productoSeleccionado.setCantidad(cantidad);
                productoSeleccionado.setUnidad(unidad);
                productoSeleccionado.setStockMinimo(stockMinimo);
                
                if (dao.actualizarProducto(productoSeleccionado)) {
                    mostrarAlerta("Éxito", "Inventario actualizado correctamente.");
                } else {
                    mostrarAlerta("Error", "No se pudo actualizar.");
                }
            }
            
            cargarDatos();
            clicLimpiar(null);
            
        } catch (NumberFormatException e) {
            mostrarAlerta("Formato inválido", "La cantidad y el stock mínimo deben ser números.");
        }
    }

    @FXML
    private void clicLimpiar(ActionEvent event) {
        cmbNombre.getEditor().clear(); // Limpiamos el texto escrito
        cmbNombre.getSelectionModel().clearSelection(); // Limpiamos la selección
        cmbNombre.setValue(null);
        
        txtCantidad.clear();
        txtStockMinimo.clear();
        cbUnidad.setValue(null);
        productoSeleccionado = null;
        tblAlmacen.getSelectionModel().clearSelection();
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
            stage.setTitle("Panel de Control - Staff");
        } catch (Exception ex) {
            System.err.println("Error al volver al Dashboard: " + ex.getMessage());
        }
    }

    @FXML
    void clicEliminar(ActionEvent event) {
        ProductoAlmacen prodSeleccionado = tblAlmacen.getSelectionModel().getSelectedItem();

        if (prodSeleccionado == null) {
            mostrarAlerta("Selección requerida", 
                    " Selecciona un insumo de la tabla para eliminarlo.");
            return; 
        }

        if (dao.eliminarProducto(prodSeleccionado.getIdProducto())) {
            mostrarAlerta("Éxito", "El insumo fue removido correctamente.");
            cargarDatos();
            clicLimpiar(null);
        } else {
            mostrarAlerta("Error", "No se pudo eliminar de la base de datos.");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}