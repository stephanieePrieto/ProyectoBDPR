package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.dao.ListaEsperaDAO;
import com.mycompany.restaurante.modelo.pojo.ListaDeEspera;
import com.mycompany.restaurante.utils.ConexionBD;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controlador para la gestión interna de asignación de mesas por parte del staff.
 */
public class AsignarMesaController implements Initializable {

    @FXML private GridPane gridMesas;
    @FXML private Label lblMesaSeleccionada;
    @FXML private TextField txtNombreCliente;
    @FXML private Spinner<Integer> spPersonas;
    @FXML private ComboBox<String> cbEstadoMesa;

    private ListaDeEspera clienteEspera;
    private ListaEsperaDAO listaEsperaDAO = new ListaEsperaDAO();

    private int idMesaSeleccionada = -1;
    private int numeroMesaSeleccionada = -1;
    private final String RUTA_PINGUINO = "/img/pinguinomesa.png";

    /**
     * Inicializa componentes, configura los valores por defecto del spinner
     * y genera la visualización del mapa del restaurante.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Rango de personas admitidas en el spinner: Mín 1, Máx 10, Inicio en 2.
        spPersonas.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 2)
        );
        cbEstadoMesa.setItems(FXCollections.observableArrayList("Libre", "Ocupada"));
        cbEstadoMesa.setValue("Ocupada");
        actualizarMapaMesas();
    }

    /**
     * Traslada la información de un cliente desde la pantalla de Lista de Espera 
     * hacia el formulario de asignación de mesa.
     */
    public void recibirClienteEspera(ListaDeEspera cliente) {
        this.clienteEspera = cliente;
        txtNombreCliente.setText(cliente.getNombreCliente());
        spPersonas.getValueFactory().setValue(cliente.getPax());
        cbEstadoMesa.setValue("Ocupada");
        lblMesaSeleccionada.setText("Cliente de lista: " + cliente.getNombreCliente());
    }

    /**
     * Establece el aforo máximo permitido dependiendo de la mesa solicitada.
     * @param numMesa Identificador físico de la mesa.
     * @return Límite de pax permitidos.
     */
    private int obtenerCapacidadMesa(int numMesa) {
        switch (numMesa) {
            case 3: case 7: case 9: return 2;
            case 1: case 2: case 5: case 6: case 10: case 12: return 4;
            case 4: case 11: return 6;
            case 8: return 8;
            default: return 4;
        }
    }

    /**
     * Construye un botón dinámico por cada mesa hallada en la base de datos 
     * y lo posiciona en la cuadrícula visual (GridPane).
     */
    public void actualizarMapaMesas() {
        gridMesas.getChildren().clear();
        String sql = "SELECT idMesa, numero, estado FROM mesa ORDER BY numero ASC";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            int col = 0, row = 0;
            while (rs.next()) {
                int id = rs.getInt("idMesa");
                int num = rs.getInt("numero");
                String estado = rs.getString("estado");
                int capacidad = obtenerCapacidadMesa(num);

                Button mesaBtn = new Button();
                mesaBtn.setPrefSize(140, 120);

                if (estado.equalsIgnoreCase("Libre")) {
                    // Diseño simple en color verde
                    mesaBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; "
                            + "-fx-font-weight: bold; -fx-background-radius: 15; -fx-cursor: hand;");
                    mesaBtn.setText("Mesa " + num + "\n(LIBRE)\nMax: " + capacidad + " pax");
                    mesaBtn.setContentDisplay(ContentDisplay.TEXT_ONLY);
                    mesaBtn.setOnAction(e -> seleccionarMesa(id, num, "Libre"));
                } else {
                    // Diseño complejo en color rojo (agrega gráfica/imagen)
                    mesaBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; "
                            + "-fx-font-weight: bold; -fx-background-radius: 15; -fx-cursor: hand;");

                    try {
                        URL imageUrl = getClass().getResource(RUTA_PINGUINO);
                        if (imageUrl != null) {
                            Image img = new Image(imageUrl.toString());
                            ImageView view = new ImageView(img);
                            view.setFitHeight(60);
                            view.setFitWidth(60);
                            view.setPreserveRatio(true);

                            VBox content = new VBox(2);
                            content.setAlignment(Pos.CENTER);

                            Label lblMesa = new Label("Mesa " + num + "\n(OCUPADA)\nMax: " + capacidad + " pax");
                            lblMesa.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px;");

                            content.getChildren().addAll(view, lblMesa);
                            mesaBtn.setGraphic(content);
                            mesaBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        } else {
                            mesaBtn.setText("Mesa " + num + "\n(OCUPADA)\nMax: " + capacidad + " pax");
                            mesaBtn.setContentDisplay(ContentDisplay.TEXT_ONLY);
                        }
                    } catch (Exception ex) {
                        mesaBtn.setText("Mesa " + num + "\n(OCUPADA)\nMax: " + capacidad + " pax");
                        mesaBtn.setContentDisplay(ContentDisplay.TEXT_ONLY);
                    }
                    mesaBtn.setOnAction(e -> seleccionarMesa(id, num, "Ocupada"));
                }

                gridMesas.add(mesaBtn, col, row);
                col++;
                if (col > 3) { col = 0; row++; }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sincroniza la información de la mesa a la que se le hizo clic con el formulario.
     */
    private void seleccionarMesa(int idMesa, int numMesa, String estadoActual) {
        this.idMesaSeleccionada = idMesa;
        this.numeroMesaSeleccionada = numMesa;
        lblMesaSeleccionada.setText("Mesa " + numMesa + " (" + estadoActual.toUpperCase() + ")");
        cbEstadoMesa.setValue(estadoActual.equalsIgnoreCase("Ocupada") ? "Ocupada" : "Libre");
    }

    /**
     * Valida el aforo permitido y consolida la actualización del estado de la mesa en BD.
     */
    @FXML
    private void asignarMesaActual(ActionEvent event) {
        if (idMesaSeleccionada == -1) {
            mostrarAlerta("Atención", "Selecciona una mesa primero.");
            return;
        }

        String nuevoEstado = cbEstadoMesa.getValue();
        // Garantizamos que si viene de lista de espera asuma estado de Ocupado
        if (clienteEspera != null) { nuevoEstado = "Ocupada"; }

        // Validación de reglas de negocio para la capacidad máxima del establecimiento
        if (nuevoEstado.equalsIgnoreCase("Ocupada")) {
            int numPersonasSolicitadas = spPersonas.getValue();
            int capacidadMax = obtenerCapacidadMesa(numeroMesaSeleccionada);

            if (numPersonasSolicitadas > capacidadMax) {
                mostrarAlerta("Límite Excedido", "La Mesa " + numeroMesaSeleccionada
                        + " solo tiene capacidad para " + capacidadMax + " pingüinos.");
                return;
            }
        }

        String sql = "UPDATE mesa SET estado = ? WHERE idMesa = ?";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado);
            ps.setInt(2, idMesaSeleccionada);
            ps.executeUpdate();

            // Desencolamiento del cliente si fue despachado desde la lista de espera
            if (clienteEspera != null) {
                listaEsperaDAO.eliminarDeLista(clienteEspera.getIdEspera());
                clienteEspera = null;
            }

            mostrarAlerta("Éxito", "Mesa " + numeroMesaSeleccionada + " actualizada.");
            limpiarFormulario();
            actualizarMapaMesas();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Función administrativa para vaciar el plano completo (útil al cierre del día).
     */
    @FXML
    private void liberarTodasLasMesas(ActionEvent event) {
        try (Connection con = ConexionBD.conectar();
             Statement st = con.createStatement()) {
            st.executeUpdate("UPDATE mesa SET estado = 'Libre'");
            actualizarMapaMesas();
            limpiarFormulario();
            mostrarAlerta("Éxito", "Todas las mesas fueron liberadas.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Maneja la navegación de retorno al panel principal del sistema.
     * Preserva la sesión del empleado activo de forma segura.
     * @param event El evento del clic en el botón de regreso.
     */
    @FXML
    private void volverDashboard(ActionEvent event) {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();
            

            DashboardController dc = loader.getController();
            if (dc != null && com.mycompany.restaurante.App.usuarioLogueado != null) {
                dc.configurarUsuario(com.mycompany.restaurante.App.usuarioLogueado);
            }

            Stage stage = (Stage) gridMesas.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Panel de Control - Staff");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            System.err.println("Error al regresar al Dashboard desde Asignar Mesa: " 
                    + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Vuelve el formulario a sus valores predeterminados (estado base).
     */
    private void limpiarFormulario() {
        idMesaSeleccionada = -1;
        numeroMesaSeleccionada = -1;
        lblMesaSeleccionada.setText("Ninguna mesa seleccionada");
        txtNombreCliente.clear();
        spPersonas.getValueFactory().setValue(2);
        cbEstadoMesa.setValue("Ocupada");
    }

    /**
     * Wrapper de UI para invocar alertas genéricas.
     */
    private void mostrarAlerta(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }
}