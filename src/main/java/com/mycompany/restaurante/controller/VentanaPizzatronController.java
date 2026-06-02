package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.dao.ReservacionDAO;
import com.mycompany.restaurante.utils.ConexionBD;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VentanaPizzatronController implements Initializable {

    @FXML private GridPane gridMesas;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<String> cbHora;
    @FXML private Spinner<Integer> spPersonas;
    
    @FXML private TextField txtNombreCliente;

    private final String RUTA_PINGUINO = "/img/pinguinomesa.png";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Llenar datos de reserva
        cbHora.setItems(FXCollections.observableArrayList("13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00"));
        cbHora.setValue("15:00");
        spPersonas.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, 2));
        
        dpFecha.setEditable(false);
        dpFecha.setValue(LocalDate.now());
        dpFecha.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (date.isBefore(LocalDate.now())) {
                    setDisable(true);
                    setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #b71c1c;");
                }
            }
        });
        
        actualizarMapaMesas();
    }
    private int obtenerCapacidadMesa(int numMesa) {
        switch (numMesa) {
            case 3: case 7: case 9: 
                return 2;
            case 1: case 2: case 5: case 6: case 10: case 12: 
                return 4;
            case 4: case 11: 
                return 6;
            case 8: 
                return 8;
            default: 
                return 4;
        }
    }

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
                
                // Obtenemos la capacidad para mostrarla en el botón
                int capacidad = obtenerCapacidadMesa(num);

                Button mesaBtn = new Button("Mesa " + num);
                mesaBtn.setPrefSize(140, 120); // Un poco más grandes para que quepa el pingu

               
                mesaBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY); 
                mesaBtn.setAlignment(Pos.CENTER);

                if (estado.equalsIgnoreCase("Libre")) {
                    mesaBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15; -fx-cursor: hand;");
                    // Mostramos el límite visualmente en la mesa libre
                    mesaBtn.setText("Mesa " + num + "\n(LIBRE)\nMax: " + capacidad + " pax");
                    mesaBtn.setContentDisplay(ContentDisplay.TEXT_ONLY);
                    mesaBtn.setOnAction(e -> ejecutarReserva(id, num));
                    mesaBtn.setGraphic(null); 
                } else if (estado.equalsIgnoreCase("Ocupada")) {
                    try {
                        // CARGA SEGURA: Buscamos la imagen en /src/main/resources/img/
                        URL imageUrl = getClass().getResource(RUTA_PINGUINO);
                        if (imageUrl != null) {
                            Image img = new Image(imageUrl.toString());
                            ImageView view = new ImageView(img);
                            
                            // Ajustes de tamaño y proporción para que el pingu no se vea estirado
                            view.setFitHeight(75); // Lo hice un pelín más chico para que quepa el texto extra
                            view.setFitWidth(75);
                            view.setPreserveRatio(true);
                            view.setSmooth(true); // Suaviza los bordes

                            // VBox para centrar imagen y texto dentro del botón
                            VBox content = new VBox(2); 
                            content.setAlignment(Pos.CENTER);
                            // Mostramos el límite visualmente debajo del pingüino
                            Label lblMesa = new Label("Mesa " + num + "\nMax: " + capacidad + " pax");
                            lblMesa.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-alignment: center; -fx-font-size: 11px;");
                            
                            content.getChildren().addAll(view, lblMesa);
                            mesaBtn.setGraphic(content);
                            mesaBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

                        } else {
                            System.out.println("Error Crítico: No se halló la imagen en: " + RUTA_PINGUINO);
                            mesaBtn.setText("Mesa " + num + "\n(RESERVADA)\nMax: " + capacidad + " pax");
                            mesaBtn.setContentDisplay(ContentDisplay.TEXT_ONLY);
                        }
                    } catch (Exception e) {
                        System.out.println("Excepción al cargar imagen: " + e.getMessage());
                        mesaBtn.setText("Mesa " + num + "\n(RESERVADA)\nMax: " + capacidad + " pax");
                        mesaBtn.setContentDisplay(ContentDisplay.TEXT_ONLY);
                    }
                    mesaBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15;");
                    mesaBtn.setDisable(true); 
                } else {
                    mesaBtn.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 15;");
                    mesaBtn.setDisable(true);
                }

                gridMesas.add(mesaBtn, col, row);
                col++; if (col > 3) { col = 0; row++; }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void ejecutarReserva(int idMesa, int numMesa) {
        if (dpFecha.getValue() == null || cbHora.getValue() == null) {
            mostrarAlerta("Datos incompletos", "El pingüino necesita saber día y hora.");
            return;
        }
        
        int capacidadMax = obtenerCapacidadMesa(numMesa);
        int personasSolicitadas = spPersonas.getValue();
        
        if (personasSolicitadas > capacidadMax) {
            mostrarAlerta("Límite Excedido", 
                "¡Ojo ahí! La Mesa " + numMesa + " solo tiene capacidad para " + capacidadMax + " pingüinos.\n\n" +
                "No puedes meter a " + personasSolicitadas + " personas en esta mesa. Por favor, selecciona una mesa más grande o reduce el número de personas.");
            return; // Cortamos la ejecución, no lo deja avanzar a la base de datos
        }

        String nombre = txtNombreCliente != null && txtNombreCliente.getText() != null ? txtNombreCliente.getText().trim() : "";
        if (nombre.isEmpty()) {
            mostrarAlerta("Nombre Faltante", "¡Ey! Escribe tu nombre completo para la reserva.");
            return;
        }

        LocalDate fechaSel = dpFecha.getValue();
        if (fechaSel.isBefore(LocalDate.now())) {
            mostrarAlerta("Fecha Inválida", "No puedes viajar en el tiempo. Elige una fecha de hoy en adelante.");
            return;
        }

        if (fechaSel.isEqual(LocalDate.now())) {
            LocalTime horaSel = LocalTime.parse(cbHora.getValue());
            if (horaSel.isBefore(LocalTime.now())) {
                mostrarAlerta("Hora Inválida", "Esa hora ya pasó el día de hoy. Elige un horario posterior.");
                return;
            }
        }

        try (Connection con = ConexionBD.conectar()) {
            
            PreparedStatement psCheck = con.prepareStatement("SELECT COUNT(*) FROM reservaciones WHERE idMesa = ? AND fecha = ? AND hora = ? AND estado != 'Cancelada'");
            psCheck.setInt(1, idMesa);
            psCheck.setString(2, fechaSel.toString());
            psCheck.setString(3, cbHora.getValue() + ":00");
            ResultSet rsCheck = psCheck.executeQuery();
            if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                 mostrarAlerta("Mesa Ocupada", "¡Chin! La Mesa " + numMesa + " ya está apartada exactamente en esa fecha y hora. ¡Elige otra!");
                 return;
            }

            PreparedStatement psCheckPerson = con.prepareStatement("SELECT COUNT(*) FROM reservaciones r INNER JOIN clientes c ON r.id_cliente = c.id_cliente WHERE c.nombre = ? AND r.fecha = ? AND r.estado != 'Cancelada'");
            psCheckPerson.setString(1, nombre);
            psCheckPerson.setString(2, fechaSel.toString());
            ResultSet rsPerson = psCheckPerson.executeQuery();
            if (rsPerson.next() && rsPerson.getInt(1) > 0) {
                 mostrarAlerta("Límite Alcanzado", "El pingüino '" + nombre + "' ya tiene una mesa reservada para este día.");
                 return;
            }

            // Si pasa todas las pruebas, obtenemos/creamos su ID usando tu DAO
            ReservacionDAO dao = new ReservacionDAO();
            String idClienteReal = dao.obtenerOGenerarIdCliente(nombre);

            // Actualizar la mesa seleccionada
            PreparedStatement ps = con.prepareStatement("UPDATE mesa SET estado = 'Ocupada' WHERE idMesa = ?");
            ps.setInt(1, idMesa);
            ps.executeUpdate();

            // Insertar registro en reservaciones
            PreparedStatement psRes = con.prepareStatement("INSERT INTO reservaciones (folioUnico, id_cliente, idMesa, fecha, hora, num_personas, estado) VALUES (?,?,?,?,?,?,'Confirmada')");
            psRes.setString(1, UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            psRes.setString(2, idClienteReal); // Usamos el ID real en vez de CP001
            psRes.setInt(3, idMesa);
            psRes.setString(4, dpFecha.getValue().toString());
            psRes.setString(5, cbHora.getValue() + ":00"); // Añadimos los segundos para MySQL
            psRes.setInt(6, spPersonas.getValue());
            psRes.executeUpdate();

            actualizarMapaMesas(); 
            mostrarAlerta("¡Reserva Confirmada!", "¡La mesa " + numMesa + " ya es tuya! Mira, ya llegó tu pingüino.");
        } catch (Exception e) { 
            e.printStackTrace(); 
            mostrarAlerta("Error Crítico", "Hubo un problema de base de datos: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegresar() {
        try {
            FXMLLoader loader = App.getFXMLLoader("VerMenuCliente");
            Parent root = loader.load();
            Stage stage = (Stage) gridMesas.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void cancelarReserva() {
        try (Connection con = ConexionBD.conectar()) {
            con.prepareStatement("UPDATE mesa SET estado = 'Libre' WHERE estado = 'Ocupada'").executeUpdate();
            actualizarMapaMesas();
            mostrarAlerta("Sistema Reiniciado", "Todas las mesas vuelven a estar libres.");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void mostrarAlerta(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION); 
        a.setTitle(t); 
        a.setHeaderText(null); 
        a.setContentText(m); 
        a.showAndWait();
    }

    @FXML
    private void irPantallaConsultar(ActionEvent event) {
        try {
            // Cargamos el FXML de la pantalla de consulta que arreglamos ayer
           FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConsultarReservacionesCliente.fxml"));
            Parent root = loader.load();
            
            // Conseguimos la ventana actual y le cambiamos la escena
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Consultar Reservación - Pizzatron CP");
            stage.show();
        } catch (IOException e) {
            System.out.println("Error al abrir la pantalla de consulta: " + e.getMessage());
            e.printStackTrace();
        }
    }
}