package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.dao.AlmacenDAO;
import com.mycompany.restaurante.modelo.pojo.ProductoAlmacen;
import java.io.IOException;
import java.util.List;
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

/**
 * Controlador para la gestión de inventario, alineado con Oracle Cloud.
 * Delegamos toda la persistencia a AlmacenDAO.
 */
public class AlmacenController {

    @FXML private ComboBox<String> cmbNombre;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtStockMinimo;
    @FXML private ComboBox<String> cbUnidad;
    
    @FXML private TableView<ProductoAlmacen> tblAlmacen;
    @FXML private TableColumn<ProductoAlmacen, String> colNombre;
    @FXML private TableColumn<ProductoAlmacen, String> colUnidad;
    @FXML private TableColumn<ProductoAlmacen, Double> colCantidad;
    @FXML private TableColumn<ProductoAlmacen, Double> colMinimo;

    // AlmacenDAO ya está configurado internamente para usar OracleConnect
    private AlmacenDAO dao = new AlmacenDAO(); 
    private ObservableList<ProductoAlmacen> listaProductos;
    private ProductoAlmacen productoSeleccionado;

    /**
     * Inicializa los componentes de la interfaz al cargar la vista.
     * Configura las unidades de medida por defecto y enlaza las columnas de la tabla.
     */
    @FXML
    public void initialize() {
        // Población de unidades de medida estándar
        cbUnidad.getItems().addAll(
            "Kilogramos (kg)", "Litros (L)", "Gramos (g)", 
            "Mililitros (ml)", "Piezas (pz)", "Porciones",
            "Rebanadas", "Tazas", "Unidades", "Latas", "Vasos"
        );
        
        // Mapeo de atributos del modelo a las columnas visuales
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colUnidad.setCellValueFactory(new PropertyValueFactory<>("unidad"));
        colMinimo.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
        
        cargarDatos();
        
        // Listener reactivo: al seleccionar un elemento en la tabla, se llena el formulario automáticamente
        tblAlmacen.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                productoSeleccionado = newSelection;
                cmbNombre.setValue(productoSeleccionado.getNombre());
                txtCantidad.setText(String.valueOf(productoSeleccionado.getCantidad()));
                cbUnidad.setValue(productoSeleccionado.getUnidad());
                txtStockMinimo.setText(String.valueOf(productoSeleccionado.getStockMinimo()));
            }
        });
    }

    /**
     * Consulta la base de datos a través del DAO para poblar la tabla y las opciones del ComboBox.
     */
    private void cargarDatos() {
        // La lógica de Oracle vive dentro de este método del DAO
        List<ProductoAlmacen> productosDB = dao.obtenerProductos();
        listaProductos = FXCollections.observableArrayList(productosDB);
        tblAlmacen.setItems(listaProductos);
        
        // Refresca los elementos del ComboBox para búsquedas rápidas
        cmbNombre.getItems().clear();
        for (ProductoAlmacen p : productosDB) {
            cmbNombre.getItems().add(p.getNombre());
        }
    }

    /**
     * Procesa la inserción o actualización de un insumo en el sistema.
     * @param event Evento disparado al hacer clic en "Guardar".
     */
    @FXML
    private void clicGuardar(ActionEvent event) {
        // Extracción segura de texto del ComboBox, permitiendo escritura libre
        String nombre = (cmbNombre.getEditor().getText() != null) ? cmbNombre.getEditor().getText().trim() : "";
        String unidad = cbUnidad.getValue();
        
        // Escudo de validación: campos obligatorios
        if (nombre.isEmpty() || txtCantidad.getText().isEmpty() || unidad == null || txtStockMinimo.getText().isEmpty()) {
            mostrarAlerta("Error", "Todos los campos son obligatorios.");
            return;
        }

        try {
            double cantidad = Double.parseDouble(txtCantidad.getText());
            double stockMinimo = Double.parseDouble(txtStockMinimo.getText());
            
            // Lógica anti-duplicados: Busca por nombre si el usuario no seleccionó un elemento de la tabla
            if (productoSeleccionado == null) {
                for (ProductoAlmacen p : listaProductos) {
                    if (p.getNombre().equalsIgnoreCase(nombre)) {
                        productoSeleccionado = p;
                        break;
                    }
                }
            }

            if (productoSeleccionado == null) {
                // Nuevo registro
                ProductoAlmacen nuevo = new ProductoAlmacen(0, nombre, cantidad, unidad, stockMinimo);
                if (dao.registrarProducto(nuevo)) {
                    mostrarAlerta("Éxito", "Insumo registrado en Oracle Cloud.");
                } else {
                    mostrarAlerta("Error", "No se pudo registrar en Oracle.");
                }
            } else {
                // Actualización (Update) de registro existente
                productoSeleccionado.setNombre(nombre);
                productoSeleccionado.setCantidad(cantidad);
                productoSeleccionado.setUnidad(unidad);
                productoSeleccionado.setStockMinimo(stockMinimo);
                
                if (dao.actualizarProducto(productoSeleccionado)) {
                    mostrarAlerta("Éxito", "Inventario actualizado en Oracle Cloud.");
                } else {
                    mostrarAlerta("Error", "No se pudo actualizar en Oracle.");
                }
            }
            // Limpia el entorno después de la operación
            cargarDatos();
            clicLimpiar(null);
        } catch (NumberFormatException e) {
            mostrarAlerta("Formato inválido", "La cantidad y el stock mínimo deben ser números.");
        }
    }

    /**
     * Limpia los campos del formulario y desmarca selecciones de la tabla.
     */
    @FXML
    private void clicLimpiar(ActionEvent event) {
        cmbNombre.getEditor().clear();
        cmbNombre.setValue(null);
        txtCantidad.clear();
        txtStockMinimo.clear();
        cbUnidad.setValue(null);
        productoSeleccionado = null;
        tblAlmacen.getSelectionModel().clearSelection();
    }

    /**
     * Remueve físicamente un registro seleccionado de la base de datos.
     */
    @FXML
    private void clicEliminar(ActionEvent event) {
        ProductoAlmacen prod = tblAlmacen.getSelectionModel().getSelectedItem();
        if (prod == null) {
            mostrarAlerta("Selección requerida", "Selecciona un insumo para eliminar.");
            return; 
        }
        if (dao.eliminarProducto(prod.getIdProducto())) {
            mostrarAlerta("Éxito", "El insumo fue removido de Oracle.");
            cargarDatos();
            clicLimpiar(null);
        } else {
            mostrarAlerta("Error", "No se pudo eliminar en Oracle.");
        }
    }

    /**
     * Regresa al panel de control (Dashboard) y restaura la sesión del empleado activo.
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

    /**
     * Generador unificado de alertas informativas en pantalla.
     */
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}