package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.dao.OpinionDAO;
import com.mycompany.restaurante.modelo.pojo.Opinion;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * Controlador diseñado para interactuar con bases de datos orientadas a documentos (MongoDB).
 * Administra el envío, visualización y gestión (CRUD) de Comentarios, Quejas y Sugerencias 
 * a través de interfaces reactivas y generación de feeds estilo red social.
 */
public class OpinionesController implements Initializable {

    private final OpinionDAO opinionDAO = new OpinionDAO();
    
    // Generador de un identificador de sesión único para simular persistencia de usuario
    // y permitir al cliente editar/borrar exclusivamente sus propias opiniones durante esta sesión.
    private final String idSesionUsuario = UUID.randomUUID().toString();
    
    // Variables de estado del formulario interactivo
    private int estrellasSeleccionadas = 0;
    private String emojiSeleccionado = "serio"; 
    private String gravedadSeleccionada = "Baja"; 
    private String verloProntoSeleccionado = "SI"; 

    // Recursos gráficos precargados para el sistema de calificación
    private final Image estrellaVacia = new Image(getClass().getResourceAsStream("/img/estrellaVacia.png"));
    private final Image estrellaLlena = new Image(getClass().getResourceAsStream("/img/estrellaLlena.png"));

    @FXML private Button btnComentarios, btnQuejas, btnSugerencias, btnVolver;
    @FXML private Pane paneComentarios, paneQuejas, paneSugerencias;
    @FXML private TextArea txtComentario, txtQueja, txtSugerencia;
    @FXML private ComboBox<String> cbMejorAspecto, cbTipoProblema, cbCategoria;
    @FXML private ImageView imgStar1, imgStar2, imgStar3, imgStar4, imgStar5;
    @FXML private Button btnEmojiEnojado, btnEmojiTriste, btnEmojiNeutral, btnEmojiSerio, btnEmojiFeliz, btnEmojiFan;
    @FXML private Button btnBaja, btnMedia, btnAlta, btnSi, btnNo, btnAlgunDia;
    @FXML private VBox VboxTarjetas; // Contenedor vertical del Feed de opiniones

    /**
     * Carga inicial de datos estructurados para los selectores del usuario, 
     * asignación de listeners a los botones de reacción y carga del feed principal.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cbMejorAspecto.setItems(FXCollections.observableArrayList("Pizza", "Servicio", "Ambiente", "Rapidez", "Música"));
        cbTipoProblema.setItems(FXCollections.observableArrayList("Atención lenta", "Pedido incorrecto", "Comida fría", "Mal servicio", "Problema con mesa", "Cobro incorrecto", "Otro"));
        cbCategoria.setItems(FXCollections.observableArrayList("Menú", "Decoración", "Música", "Atención", "Nuevos sabores", "Promociones", "Aplicación"));
        
        configurarEstrellas();
        configurarBotonesEmojis();
        configurarBotonesGravedad();
        configurarBotonesUrgencia();
        mostrarComentarios(); 
    }

    // --- MOTOR DE NAVEGACIÓN ENTRE CATEGORÍAS ---
    
    @FXML private void mostrarComentarios(ActionEvent event) { mostrarComentarios(); }
    private void mostrarComentarios() {
        cambiarPanel(paneComentarios, paneQuejas, paneSugerencias);
        cargarFeedOpiniones("Comentario");
    }

    @FXML private void mostrarQuejas(ActionEvent event) { mostrarQuejas(); }
    private void mostrarQuejas() {
        cambiarPanel(paneQuejas, paneComentarios, paneSugerencias);
        cargarFeedOpiniones("Queja");
    }

    @FXML private void mostrarSugerencias(ActionEvent event) { mostrarSugerencias(); }
    private void mostrarSugerencias() {
        cambiarPanel(paneSugerencias, paneComentarios, paneQuejas);
        cargarFeedOpiniones("Sugerencia");
    }

    /**
     * Alterna la visibilidad booleana de los paneles para crear el flujo tipo Tab.
     */
    private void cambiarPanel(Pane v, Pane h1, Pane h2) {
        v.setVisible(true); v.setManaged(true);
        h1.setVisible(false); h1.setManaged(false);
        h2.setVisible(false); h2.setManaged(false);
    }

    /**
     * Realiza una petición NoSQL al DAO para recuperar la colección de documentos, 
     * iterándola para renderizar bloques gráficos (Tarjetas) por cada documento recuperado.
     * @param filtro Permite solicitar únicamente opiniones que coincidan con la categoría actual.
     */
    private void cargarFeedOpiniones(String filtro) {
        VboxTarjetas.getChildren().clear();
        try {
            List<Opinion> historial = opinionDAO.obtainAllOpiniones();
            if (historial == null) return;
            
            for (Opinion o : historial) {
                // Filtrado por categoría
                if (filtro != null && !o.getTipo().equals(filtro)) continue;

                VBox tarjeta = new VBox(6); 
                tarjeta.setPrefWidth(370);
                tarjeta.setMaxWidth(370);
                tarjeta.setPadding(new Insets(12)); 

                // Lógica de identidad visual: Asigna paletas de colores basadas en el sentimiento del ticket
                String estiloComun = "-fx-border-width: 3; -fx-border-radius: 15; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 3);";
                if (o.getTipo().equals("Comentario")) tarjeta.setStyle(estiloComun + "-fx-background-color: #E8F5E9; -fx-border-color: #81C784;");
                else if (o.getTipo().equals("Queja")) tarjeta.setStyle(estiloComun + "-fx-background-color: #FFEBEE; -fx-border-color: #E57373;");
                else tarjeta.setStyle(estiloComun + "-fx-background-color: #E3F2FD; -fx-border-color: #64B5F6;");

                // 1. Encabezado de la Tarjeta
                Label lblEncabezado = new Label("👤 " + o.getTipo().toUpperCase() + " - Anónimo");
                lblEncabezado.setStyle("-fx-font-weight: bold; -fx-text-fill: #003366; -fx-font-size: 13px;");

                // 2. Extracción y dibujado del contenido textual (Ajuste automático de texto largo)
                Label lblCuerpo = new Label("💬 \"" + o.getContenido() + "\"");
                lblCuerpo.setWrapText(true);
                lblCuerpo.setStyle("-fx-text-fill: #37474F; -fx-font-size: 12px;");

                // 3. Renderizado de Controles de Dueño (Privilegios mediante UUID)
                HBox botones = new HBox(10);
                botones.setAlignment(Pos.CENTER_RIGHT);

                if (o.getIdSesion() != null && o.getIdSesion().equals(idSesionUsuario)) {
                    Button btnEditar = crearBoton("Editar", "#FFD54F", e -> abrirDialogoEdicion(o, filtro));
                    Button btnEliminar = crearBoton("Borrar", "#E57373", e -> { opinionDAO.eliminarOpinion(o.getId()); cargarFeedOpiniones(filtro); });
                    botones.getChildren().addAll(btnEditar, btnEliminar);
                }

                // Ensamblaje e inserción al VBox principal
                tarjeta.getChildren().addAll(lblEncabezado, lblCuerpo, botones);
                VboxTarjetas.getChildren().add(tarjeta);
            
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    /**
     * Utilidad de fábrica gráfica para la inserción de botones en las tarjetas dinámicas.
     */
    private Button crearBoton(String t, String c, javafx.event.EventHandler<ActionEvent> a) {
        Button b = new Button(t);
        b.setStyle("-fx-background-color: " + c + "; -fx-background-radius: 10; -fx-cursor: hand;");
        b.setOnAction(a);
        return b;
    }

    /**
     * Despliega un cuadro de diálogo permitiendo al dueño del documento alterar su contenido (Update NoSQL).
     */
    private void abrirDialogoEdicion(Opinion o, String filtro) {
        TextInputDialog dialog = new TextInputDialog(o.getContenido());
        dialog.setTitle("Editar"); dialog.setHeaderText("Modificar opinión:");
        dialog.setContentText("Nuevo texto:");
        dialog.showAndWait().ifPresent(nuevoTexto -> {
            if (opinionDAO.actualizarOpinion(o.getId(), nuevoTexto)) cargarFeedOpiniones(filtro);
        });
    }

    /**
     * Concentrador de operaciones para salvar cualquier tipo de documento en la base de datos, 
     * inyectándole metadatos compartidos.
     */
    private void procesarGuardado(Opinion op) {
        op.setFechaHora(new Date());
        op.setCliente("Anónimo");
        op.setIdSesion(idSesionUsuario); // Firma electrónica del equipo cliente
        
        if (opinionDAO.registrarOpinion(op)) {
            mostrarAlerta("Éxito", "Enviado con éxito", Alert.AlertType.INFORMATION);
            limpiarCampos();
            cargarFeedOpiniones(op.getTipo());
        }
    }

    // --- ACCIONES DE FORMULARIOS ---

    @FXML
    private void clicEnviarComentario(ActionEvent event) {
        if (txtComentario.getText() == null || txtComentario.getText().trim().isEmpty()) {
            mostrarAlerta("Campos Incompletos", "Por favor, escribe un comentario sobre tu experiencia.", Alert.AlertType.WARNING);
            return;
        }
        if (estrellasSeleccionadas == 0) {
            mostrarAlerta("Evaluación Requerida", "Por favor, selecciona una calificación haciendo clic en las estrellas.", Alert.AlertType.WARNING);
            return;
        }

        Opinion op = new Opinion();
        op.setTipo("Comentario");
        op.setContenido(txtComentario.getText().trim());
        op.setCalificacionEstrellas(estrellasSeleccionadas);
        op.setMejorAspecto(cbMejorAspecto.getValue() != null ? cbMejorAspecto.getValue() : "No especificado");
        op.setEmojiFinal(emojiSeleccionado);

        procesarGuardado(op);
    }

    @FXML
    private void clicEnviarQueja(ActionEvent event) {
        if (cbTipoProblema.getValue() == null) {
            mostrarAlerta("Campos Incompletos", "Debe seleccionar un tipo de problema para clasificar la queja.", Alert.AlertType.WARNING);
            return;
        }
        if (txtQueja.getText() == null || txtQueja.getText().trim().isEmpty()) {
            mostrarAlerta("Campos Incompletos", "Por favor, describe el problema ocurrido en la caja de texto.", Alert.AlertType.WARNING);
            return;
        }

        Opinion op = new Opinion();
        op.setTipo("Queja");
        op.setContenido(txtQueja.getText().trim());
        op.setTipoProblema(cbTipoProblema.getValue());
        op.setGravedad(gravedadSeleccionada);

        procesarGuardado(op);
    }
    
    @FXML
    private void clicEnviarSugerencia(ActionEvent event) {
        if (cbCategoria.getValue() == null) {
            mostrarAlerta("Campos Incompletos", "Por favor, escoge una categoría para ordenar tu sugerencia.", Alert.AlertType.WARNING);
            return;
        }
        if (txtSugerencia.getText() == null || txtSugerencia.getText().trim().isEmpty() || txtSugerencia.getText().contains("¿Que te gustaría ver")) {
            mostrarAlerta("Campos Incompletos", "Escribe la idea que tienes para implementar en Pizzatron 3000.", Alert.AlertType.WARNING);
            return;
        }

        Opinion op = new Opinion();
        op.setTipo("Sugerencia");
        op.setContenido(txtSugerencia.getText().trim());
        op.setCategoriaSugerencia(cbCategoria.getValue());
        op.setVerloPronto(verloProntoSeleccionado);

        procesarGuardado(op);
    }

    // --- CONFIGURACIÓN DE COMPONENTES DE INTERFAZ REACTIVA ---

    private void configurarEstrellas() {
        imgStar1.setOnMouseClicked(e -> actualizarEstrellasVisuales(1));
        imgStar2.setOnMouseClicked(e -> actualizarEstrellasVisuales(2));
        imgStar3.setOnMouseClicked(e -> actualizarEstrellasVisuales(3));
        imgStar4.setOnMouseClicked(e -> actualizarEstrellasVisuales(4));
        imgStar5.setOnMouseClicked(e -> actualizarEstrellasVisuales(5));
    }

    private void actualizarEstrellasVisuales(int estrellas) {
        estrellasSeleccionadas = estrellas;
        imgStar1.setImage(estrellas >= 1 ? estrellaLlena : estrellaVacia);
        imgStar2.setImage(estrellas >= 2 ? estrellaLlena : estrellaVacia);
        imgStar3.setImage(estrellas >= 3 ? estrellaLlena : estrellaVacia);
        imgStar4.setImage(estrellas >= 4 ? estrellaLlena : estrellaVacia);
        imgStar5.setImage(estrellas >= 5 ? estrellaLlena : estrellaVacia);
    }

    private void configurarBotonesEmojis() {
            if (btnEmojiEnojado != null) btnEmojiEnojado.setOnAction(e -> emojiSeleccionado = "enojado");
            if (btnEmojiTriste != null) btnEmojiTriste.setOnAction(e -> emojiSeleccionado = "triste");
            if (btnEmojiFeliz != null) btnEmojiFeliz.setOnAction(e -> emojiSeleccionado = "feliz");
            if (btnEmojiFan != null) btnEmojiFan.setOnAction(e -> emojiSeleccionado = "encantado");
            if (btnEmojiSerio != null) {
                btnEmojiSerio.setOnAction(e -> emojiSeleccionado = "serio");
            } else if (btnEmojiNeutral != null) {
                btnEmojiNeutral.setOnAction(e -> emojiSeleccionado = "serio");
            }
        }

    private void configurarBotonesGravedad() {
        btnBaja.setOnAction(e -> gravedadSeleccionada = "Baja");
        btnMedia.setOnAction(e -> gravedadSeleccionada = "Media");
        btnAlta.setOnAction(e -> gravedadSeleccionada = "Alta");
    }

    private void configurarBotonesUrgencia() {
        if (btnSi != null) btnSi.setOnAction(e -> verloProntoSeleccionado = "SI");
        if (btnNo != null) btnNo.setOnAction(e -> verloProntoSeleccionado = "NO");
        if (btnAlgunDia != null) btnAlgunDia.setOnAction(e -> verloProntoSeleccionado = "ALGÚN DÍA");
    }

    private void limpiarCampos() {
        txtComentario.clear();
        txtQueja.clear();
        txtSugerencia.clear();
        cbMejorAspecto.setValue(null);
        cbTipoProblema.setValue(null);
        cbCategoria.setValue(null);
        actualizarEstrellasVisuales(0);
        emojiSeleccionado = "serio";
        gravedadSeleccionada = "Baja";
        verloProntoSeleccionado = "SI";
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    @FXML
    private void volverDashboard(ActionEvent event) {
        try {
            Stage s = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Parent r = new FXMLLoader(getClass().getResource("/fxml/VerMenuCliente.fxml")).load();
            s.setScene(new Scene(r)); s.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}