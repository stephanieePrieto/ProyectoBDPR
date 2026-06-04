package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.dao.ListaEsperaDAO;
import com.mycompany.restaurante.modelo.pojo.ListaDeEspera;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * Controlador para la gestión de la lista de espera de clientes (Walk-ins).
 * Permite al staff registrar comensales cuando el restaurante se encuentra a su capacidad máxima,
 * monitorear el tamaño de los grupos (pax) y el orden de llegada, y marcarlos como atendidos
 * una vez que se les asigna una mesa física.
 */
public class ListaEsperaController implements Initializable {

    // --- Controles de Entrada (Input) ---
    @FXML private TextField Nombre;
    @FXML private TextField Telefono;
    @FXML private Spinner<Integer> ListaEspera;

    // --- Controles de Visualización (TableView) ---
    @FXML private TableView<ListaDeEspera> FilaEspera;
    @FXML private TableColumn<ListaDeEspera, Integer> colId;
    @FXML private TableColumn<ListaDeEspera, String> colNombre;
    @FXML private TableColumn<ListaDeEspera, Integer> colPax;
    @FXML private TableColumn<ListaDeEspera, String> colHora;

    // Colección reactiva para sincronizar datos del DAO con la UI
    private ObservableList<ListaDeEspera> lista;

    /**
     * Inicializa los componentes de la vista al cargar la pantalla.
     * Configura las limitantes numéricas del spinner de personas, enlaza el modelo de datos
     * a las columnas de la tabla y ejecuta la primera carga del registro histórico.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarSpinner();
        configurarTabla();
        cargarTabla();
    }

    /**
     * Configura las restricciones lógicas para el selector de número de personas (Pax).
     * Establece un mínimo de 1 persona y un máximo de 20 por grupo, con un valor por defecto de 1.
     */
    private void configurarSpinner() {
        SpinnerValueFactory<Integer> valores =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1);
        ListaEspera.setValueFactory(valores);
    }

    /**
     * Mapea las propiedades del objeto POJO ListaDeEspera con las columnas visuales del TableView.
     */
    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idEspera"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCliente"));
        colPax.setCellValueFactory(new PropertyValueFactory<>("pax"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("horaLlegada"));

        // Propiedad visual para que las columnas abarquen todo el ancho sin dejar espacio libre
        FilaEspera.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    /**
     * Sincroniza la tabla visual con la información extraída de la base de datos.
     * Recupera exclusivamente a los clientes que siguen con el estatus de espera.
     */
    private void cargarTabla() {
        ListaEsperaDAO dao = new ListaEsperaDAO();
        lista = dao.obtenerListaEspera();
        FilaEspera.setItems(lista);
    }

    /**
     * Valida y procesa el ingreso de un nuevo grupo de comensales a la fila virtual.
     * Persiste los datos de contacto y tamaño del grupo mediante el DAO e inmediatamente
     * refresca la tabla visual para reflejar el nuevo turno en pantalla.
     * * @param event Evento disparado por el botón "Agregar".
     */
    @FXML
    private void clicAgregarLista(ActionEvent event) {
        String nombreCliente = Nombre.getText().trim();
        int pax = ListaEspera.getValue();
        String telefonoCliente = Telefono.getText().trim();

        if (nombreCliente.isEmpty()) {
            mostrarAlerta("Campos vacíos", "Ingresa el nombre del cliente.", Alert.AlertType.WARNING);
            return;
        }

        ListaDeEspera cliente = new ListaDeEspera();
        cliente.setNombreCliente(nombreCliente);
        cliente.setPax(pax);
        cliente.setTelefono(telefonoCliente);

        ListaEsperaDAO dao = new ListaEsperaDAO();
        boolean resultado = dao.insertarClienteEspera(cliente);

        if (resultado) {
            mostrarAlerta("Registro exitoso", "Cliente agregado a lista de espera.", Alert.AlertType.INFORMATION);
            cargarTabla();
            limpiarCampos();
        } else {
            mostrarAlerta("Error", "No se pudo registrar el cliente.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Manda la instancia del cliente seleccionado directamente a la pantalla de Asignar Mesa.
     * Facilita el flujo de trabajo evitando tener que volver a teclear la información.
     * * @param event Evento disparado por el botón "Asignar Mesa".
     */
    @FXML
    private void clicAsignarMesa(ActionEvent event) {
        ListaDeEspera cliente = FilaEspera.getSelectionModel().getSelectedItem();

        if (cliente == null) {
            mostrarAlerta("Selecciona un cliente", "Primero selecciona un cliente de la lista de espera.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = App.getFXMLLoader("AsignarMesa");
            Parent root = loader.load();

            // Pasamos la variable del cliente al nuevo controlador destino
            AsignarMesaController controller = loader.getController();
            controller.recibirClienteEspera(cliente);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Asignar Mesa - Pizzatron 3000");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la pantalla de Asignar Mesa.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Limpia los componentes del formulario de registro y purga cualquier selección activa en la tabla.
     * Nota Técnica: Esta función NO elimina registros, únicamente limpia la UI.
     * * @param event Evento disparado por el botón "Cancelar".
     */
    @FXML
    private void CancelarMesa(ActionEvent event) {
        limpiarCampos();
        FilaEspera.getSelectionModel().clearSelection();
    }

    /**
     * Gestiona la navegación de retorno al panel de control general.
     * * @param event El evento disparado al presionar el botón "Volver".
     */
    @FXML
    void volverDashboard(ActionEvent event) {
        try {
            Parent root = App.getFXMLLoader("Dashboard").load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (IOException ex) {
            ex.printStackTrace();
            mostrarAlerta("Error", "No se pudo regresar al Dashboard.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Método auxiliar privado para restablecer los valores de entrada de texto al estatus inicial.
     */
    private void limpiarCampos() {
        Nombre.clear();
        Telefono.clear();
        ListaEspera.getValueFactory().setValue(1);
    }

    /**
     * Construye y despliega un cuadro de diálogo estandarizado.
     * * @param titulo  Encabezado del cuadro de diálogo.
     * @param mensaje Cuerpo informativo o de error a comunicar.
     * @param tipo    Nivel de severidad para alterar la iconografía de la alerta.
     */
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}