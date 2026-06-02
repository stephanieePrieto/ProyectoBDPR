package com.mycompany.restaurante.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import com.mycompany.restaurante.dao.MesaDAO;
import com.mycompany.restaurante.modelo.pojo.Mesa;

/**
 * Controlador de UI para el monitoreo y liberación del plano de mesas.
 * Sostiene la lógica del TableView conectada síncronamente con MesaDAO.
 * * @author Stephanie Hernandez
 */
public class EstadoMesaController implements Initializable {

    @FXML private TableView<Mesa> tvMesas;
    @FXML private TableColumn<Mesa, Integer> colNumero;
    @FXML private TableColumn<Mesa, String> colEstado;
    @FXML private TableColumn<Mesa, String> colDetalles;

    @FXML private Label lblMesaSeleccionada;
    @FXML private Button btnLiberar;
    @FXML private Button btnVolver;

    private final MesaDAO mesaDAO = new MesaDAO();
    private final ObservableList<Mesa> listaObservable =
            FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabla();
        refrescarTabla();

        // Listener reactivo para habilitar o deshabilitar el botón de liberación
        tvMesas.getSelectionModel().selectedItemProperty()
                .addListener((obs, viejaSeleccion, nuevaSeleccion) -> {
            if (nuevaSeleccion != null) {
                lblMesaSeleccionada.setText("Mesa " + nuevaSeleccion.getIdMesa());
                btnLiberar.setDisable(
                        nuevaSeleccion.getEstado().equalsIgnoreCase("Libre"));
            }
        });
    }

    private void configurarTabla() {
        // Vincular columnas obligatorias con las propiedades de la entidad Mesa
        if (colNumero != null) {
            colNumero.setCellValueFactory(new PropertyValueFactory<>("idMesa"));
        }
        if (colEstado != null) {
            colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
            
            // Renderizador cosmético para pintar las celdas según el estado
            colEstado.setCellFactory(column -> new TableCell<Mesa, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        setAlignment(javafx.geometry.Pos.CENTER);

                        if (item.equalsIgnoreCase("Ocupada")) {
                            setStyle("-fx-background-color: #C82333; "
                                    + "-fx-text-fill: white; "
                                    + "-fx-font-weight: bold;");
                        } else if (item.equalsIgnoreCase("Libre") 
                                || item.equalsIgnoreCase("Disponible")) {
                            setStyle("-fx-background-color: #218838; "
                                    + "-fx-text-fill: white; "
                                    + "-fx-font-weight: bold;");
                        } else if (item.equalsIgnoreCase("Sucia")) {
                            setStyle("-fx-background-color: #D5C295; "
                                    + "-fx-text-fill: black; "
                                    + "-fx-font-weight: bold;");
                        } else {
                            setStyle("");
                        }
                    }
                }
            });
        }
        
        // Escudo de protección: Solo mapear si la inyección de FXML fue exitosa
        if (colDetalles != null) {
            colDetalles.setCellValueFactory(
                    new PropertyValueFactory<>("detalles"));
        }
    }

    private void refrescarTabla() {
        listaObservable.clear();
        listaObservable.addAll(mesaDAO.listarMesas());
        tvMesas.setItems(listaObservable);
    }

    @FXML
    private void btnLiberarMesaAction(ActionEvent event) {
        Mesa mesaSeleccionada = tvMesas.getSelectionModel().getSelectedItem();
        if (mesaSeleccionada != null) {
            mesaDAO.liberarMesa(mesaSeleccionada.getIdMesa());
            lblMesaSeleccionada.setText("[ Ninguna ]");
            refrescarTabla();
            tvMesas.getSelectionModel().clearSelection();
        }
    }

    @FXML
    private void volverDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();
            Stage stageActual = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();
            stageActual.setScene(new Scene(root));
            stageActual.setTitle("Dashboard - Panel Staff");
        } catch (IOException e) {
            System.err.println("Error al volver al Dashboard: " 
                    + e.getMessage());
        }
    }
}