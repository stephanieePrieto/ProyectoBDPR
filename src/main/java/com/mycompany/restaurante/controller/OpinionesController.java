package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.dao.OpinionDAO;
import com.mycompany.restaurante.modelo.pojo.Opinion;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class OpinionesController implements Initializable {

    private final OpinionDAO opinionDAO = new OpinionDAO();
    
    private int estrellasSeleccionadas = 0;
    private String emojiSeleccionado = "serio"; 
    private String gravedadSeleccionada = "Baja"; 
    private String verloProntoSeleccionado = "SI"; 

    private final Image estrellaVacia = new Image(getClass().getResourceAsStream("/img/estrellaVacia.png"));
    private final Image estrellaLlena = new Image(getClass().getResourceAsStream("/img/estrellaLlena.png"));

    @FXML private Button btnComentarios, btnQuejas, btnSugerencias, btnVolver;
    @FXML private Pane paneComentarios, paneQuejas, paneSugerencias;
    
    // Formulario Comentarios
    @FXML private TextArea txtComentario;
    @FXML private ComboBox<String> cbMejorAspecto;
    @FXML private Button btnEnviarComentario;
    @FXML private ImageView imgStar1, imgStar2, imgStar3, imgStar4, imgStar5;
    @FXML private Button btnEmojiEnojado, btnEmojiTriste, btnEmojiNeutral, btnEmojiSerio, btnEmojiFeliz, btnEmojiFan;
    
    // Formulario Quejas
    @FXML private ComboBox<String> cbTipoProblema;
    @FXML private TextArea txtQueja;
    @FXML private Button btnBaja, btnMedia, btnAlta, btnEnviarQueja;
    
    // Formulario Sugerencias
    @FXML private ComboBox<String> cbCategoria;
    @FXML private TextArea txtSugerencia;
    @FXML private Button btnSi, btnNo, btnAlgunDia, btnEnviarSugerencia;
    
    // Feed Histórico Derecho
    @FXML private VBox VboxTarjetas;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        mostrarComentarios();
        
        cbMejorAspecto.setItems(FXCollections.observableArrayList("Pizza", "Servicio", "Ambiente", "Rapidez", "Música"));
        cbTipoProblema.setItems(FXCollections.observableArrayList("Atención lenta", "Pedido incorrecto", "Comida fría", "Mal servicio", "Problema con mesa", "Cobro incorrecto", "Otro"));
        cbCategoria.setItems(FXCollections.observableArrayList("Menú", "Decoración", "Música", "Atención", "Nuevos sabores", "Promociones", "Aplicación"));
        
        configurarEstrellas();
        configurarBotonesEmojis();
        configurarBotonesGravedad();
        configurarBotonesUrgencia();

        cargarFeedOpiniones();
    }

    private void mostrarComentarios() {
        paneComentarios.setVisible(true); paneComentarios.setManaged(true);
        paneQuejas.setVisible(false); paneQuejas.setManaged(false);
        paneSugerencias.setVisible(false); paneSugerencias.setManaged(false);
    }

    private void mostrarQuejas() {
        paneComentarios.setVisible(false); paneComentarios.setManaged(false);
        paneQuejas.setVisible(true); paneQuejas.setManaged(true);
        paneSugerencias.setVisible(false); paneSugerencias.setManaged(false);
    }

    private void mostrarSugerencias() {
        paneComentarios.setVisible(false); paneComentarios.setManaged(false);
        paneQuejas.setVisible(false); paneQuejas.setManaged(false);
        paneSugerencias.setVisible(true); paneSugerencias.setManaged(true);
    }
    
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

    private void procesarGuardado(Opinion op) {
        op.setFechaHora(new Date());
        op.setCliente("Anónimo"); // 🟢 Eliminada la línea de idMesa por completo

        try {
            boolean exito = opinionDAO.registrarOpinion(op);
            if (exito) {
                mostrarAlerta("¡Muchas Gracias!", "Tu " + op.getTipo() + " ha sido enviado de forma anónima con éxito.", Alert.AlertType.INFORMATION);
                limpiarCampos();
                cargarFeedOpiniones(); 
            } else {
                mostrarAlerta("Error de Registro", "La base NoSQL rechazó el documento.", Alert.AlertType.ERROR);
            }
        } catch (Exception ex) {
            System.err.println("❌ EXCEPCIÓN EN MONGO: " + ex.getMessage());
            mostrarAlerta("Error de Servidor NoSQL", "No hay comunicación con MongoDB.", Alert.AlertType.ERROR);
            ex.printStackTrace();
        }
    }

    private void cargarFeedOpiniones() {
        VboxTarjetas.getChildren().clear();
        VboxTarjetas.setSpacing(15); 

        try {
            List<Opinion> historial = opinionDAO.obtainAllOpiniones(); // Nota: Asegúrate que tu DAO mantenga el nombre del método (obtenerTodasLasOpiniones)
            if (historial == null) return;
            
            for (Opinion o : historial) {
                VBox tarjeta = new VBox();
                tarjeta.setSpacing(6);
                tarjeta.setPrefWidth(370);
                tarjeta.setMaxWidth(370);
                
                String estiloComun = "-fx-padding: 12; -fx-border-width: 3; -fx-border-radius: 15; -fx-background-radius: 15; "
                                   + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 6, 0, 0, 3);";
                
                if (o.getTipo().equals("Comentario")) {
                    tarjeta.setStyle(estiloComun + "-fx-background-color: #E8F5E9; -fx-border-color: #81C784;"); 
                } else if (o.getTipo().equals("Queja")) {
                    tarjeta.setStyle(estiloComun + "-fx-background-color: #FFEBEE; -fx-border-color: #E57373;"); 
                } else if (o.getTipo().equals("Sugerencia")) {
                    tarjeta.setStyle(estiloComun + "-fx-background-color: #E3F2FD; -fx-border-color: #64B5F6;"); 
                }

                // 🟢 CORREGIDO: Ya no se concatena la mesa aquí, queda limpio y directo
                Label lblEncabezado = new Label("👤 " + o.getTipo().toUpperCase() + " - Anónimo");
                lblEncabezado.setStyle("-fx-font-weight: bold; -fx-text-fill: #003366; -fx-font-size: 13px;");
                tarjeta.getChildren().add(lblEncabezado);

                HBox filaDetalles = new HBox();
                filaDetalles.setSpacing(8);
                filaDetalles.setAlignment(Pos.CENTER_LEFT);

                if (o.getTipo().equals("Comentario")) {
                    Label lblEstrellas = new Label("⭐ " + o.getCalificacionEstrellas() + "/5");
                    lblEstrellas.setStyle("-fx-font-weight: bold; -fx-text-fill: #FFA000; -fx-font-size: 12px;");
                    filaDetalles.getChildren().add(lblEstrellas);
                    
                    if (o.getEmojiFinal() != null && !o.getEmojiFinal().equals("Ninguno")) {
                        try {
                            ImageView imgEmoji = new ImageView();
                            imgEmoji.setFitHeight(22);
                            imgEmoji.setFitWidth(22);
                            imgEmoji.setPreserveRatio(true);
                            String pathEmoji = "/img/" + o.getEmojiFinal().toLowerCase() + ".png";
                            URL urlImg = getClass().getResource(pathEmoji);
                            if (urlImg != null) {
                                imgEmoji.setImage(new Image(urlImg.toExternalForm()));
                                filaDetalles.getChildren().add(imgEmoji);
                            }
                        } catch (Exception ex) {}
                    }
                    
                    Label lblAspecto = new Label("• Mejor: " + o.getMejorAspecto());
                    lblAspecto.setStyle("-fx-text-fill: #555555; -fx-font-style: italic; -fx-font-size: 11px;");
                    filaDetalles.getChildren().add(lblAspecto);

                } else if (o.getTipo().equals("Queja")) {
                    Label lblGravedad = new Label("🚨 " + o.getGravedad().toUpperCase());
                    if (o.getGravedad().equalsIgnoreCase("Alta")) {
                        lblGravedad.setStyle("-fx-background-color: #D32F2F; -fx-text-fill: white; -fx-padding: 1 5 1 5; -fx-background-radius: 4; -fx-font-weight: bold; -fx-font-size: 11px;");
                    } else if (o.getGravedad().equalsIgnoreCase("Media")) {
                        lblGravedad.setStyle("-fx-background-color: #F57C00; -fx-text-fill: white; -fx-padding: 1 5 1 5; -fx-background-radius: 4; -fx-font-weight: bold; -fx-font-size: 11px;");
                    } else {
                        lblGravedad.setStyle("-fx-background-color: #388E3C; -fx-text-fill: white; -fx-padding: 1 5 1 5; -fx-background-radius: 4; -fx-font-weight: bold; -fx-font-size: 11px;");
                    }
                    
                    Label lblAsunto = new Label("• " + o.getTipoProblema());
                    lblAsunto.setStyle("-fx-text-fill: #555555; -fx-font-size: 11px;");
                    filaDetalles.getChildren().addAll(lblGravedad, lblAsunto);

                } else if (o.getTipo().equals("Sugerencia")) {
                    Label lblUrge = new Label("⏱️ Ver pronto: " + o.getVerloPronto());
                    lblUrge.setStyle("-fx-font-weight: bold; -fx-text-fill: #1976D2; -fx-font-size: 11px;");
                    
                    Label lblCat = new Label("• Sección: " + o.getCategoriaSugerencia());
                    lblCat.setStyle("-fx-text-fill: #555555; -fx-font-size: 11px;");
                    filaDetalles.getChildren().addAll(lblUrge, lblCat);
                }

                tarjeta.getChildren().add(filaDetalles);

                Label lblCuerpo = new Label("💬 \"" + o.getContenido() + "\"");
                lblCuerpo.setWrapText(true);
                lblCuerpo.setStyle("-fx-text-fill: #37474F; -fx-font-size: 12px; -fx-padding: 4 0 0 0;");
                tarjeta.getChildren().add(lblCuerpo);

                VboxTarjetas.getChildren().add(tarjeta);
            }
        } catch (Exception ex) {
            System.err.println("❌ Excepción al renderizar el feed derecho: " + ex.getMessage());
        }
    }

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

// =========================================================================
    // 🚪 NAVEGACIÓN Y SALIDA A VER MENU CLIENTE (CORREGIDO)
    // =========================================================================
    @FXML
    private void volverDashboard(ActionEvent event) {
        try {
            // Mandamos a llamar la raíz de tu App con el nombre exacto de tu archivo FXML
            com.mycompany.restaurante.App.setRoot("VerMenuCliente");
        } catch (IOException e) {
            System.err.println("❌ Error crítico al regresar a VerMenuCliente: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML private void mostrarComentarios(ActionEvent event) { mostrarComentarios(); }
    @FXML private void mostrarQuejas(ActionEvent event) { mostrarQuejas(); }
    @FXML private void mostrarSugerencias(ActionEvent event) { mostrarSugerencias(); }
}