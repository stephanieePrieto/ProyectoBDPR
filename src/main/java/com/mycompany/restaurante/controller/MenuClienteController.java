package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.dao.PlatilloDAO;
import com.mycompany.restaurante.modelo.pojo.Platillo;
import com.mycompany.restaurante.modelo.sql.OracleConnect; // Cambio a Oracle
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controlador de la interfaz gráfica del Menú Digital para Clientes.
 * Migrado a arquitectura Oracle Cloud.
 * @author Ricardo, Diego, Angel, Stephy
 */
public class MenuClienteController implements Initializable {

    @FXML private GridPane gridBebidas;
    @FXML private GridPane gridEspeciales;
    @FXML private GridPane gridExtras;
    @FXML private GridPane gridPasteles;
    @FXML private GridPane gridPizza;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        GridPane[] todosLosGrids = {
            gridPizza, gridBebidas, gridPasteles, gridExtras, gridEspeciales
        };
        for (GridPane grid : todosLosGrids) {
            if (grid != null) {
                grid.getChildren().clear();
                grid.getColumnConstraints().clear();
                grid.getRowConstraints().clear();
                
                grid.setVisible(true);
                grid.setOpacity(1.0);
                grid.setManaged(true);
                grid.setHgap(20);
                grid.setVgap(20);
                
                for (int i = 0; i < 3; i++) {
                    ColumnConstraints cc = new ColumnConstraints();
                    cc.setPrefWidth(190); 
                    grid.getColumnConstraints().add(cc);
                }
            }
        }
        cargarMenuDinamico();
        mostrarGrid("Pizzas");
    }

    private void cargarMenuDinamico() {
        // Conexión a Oracle
        try (Connection conexion = OracleConnect.getConexion()) {
            if (conexion != null) {
                PlatilloDAO dao = new PlatilloDAO(conexion);
                List<Platillo> activos = dao.obtenerPlatillosActivos();

                int colP = 0, rowP = 0;
                int colB = 0, rowB = 0;
                int colPa = 0, rowPa = 0;
                int colEx = 0, rowEx = 0;
                int colEs = 0, rowEs = 0;

                for (Platillo p : activos) {
                    VBox tarjeta = crearTarjetaPlatillo(p);
                    
                    int cat = p.getIdCategoria(); 
                    if (cat < 1 || cat > 5) cat = 1;

                    switch (cat) {
                        case 1: 
                            gridPizza.add(tarjeta, colP, rowP);
                            colP++; if (colP == 3) { colP = 0; rowP++; }
                            break;
                        case 2: 
                            gridBebidas.add(tarjeta, colB, rowB);
                            colB++; if (colB == 3) { colB = 0; rowB++; }
                            break;
                        case 3: 
                            gridPasteles.add(tarjeta, colPa, rowPa);
                            colPa++; if (colPa == 3) { colPa = 0; rowPa++; }
                            break;
                        case 4: 
                            gridExtras.add(tarjeta, colEx, rowEx);
                            colEx++; if (colEx == 3) { colEx = 0; rowEx++; }
                            break;
                        case 5: 
                            gridEspeciales.add(tarjeta, colEs, rowEs);
                            colEs++; if (colEs == 3) { colEs = 0; rowEs++; }
                            break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al cargar menú en Oracle: " + e.getMessage());
        }
    }

    private VBox crearTarjetaPlatillo(Platillo p) {
        VBox tarjeta = new VBox();
        tarjeta.setAlignment(Pos.CENTER);
        
        String estiloTarjeta = "-fx-background-color: white; "
                + "-fx-background-radius: 15; -fx-border-color: #A0D8EF; "
                + "-fx-border-width: 3; -fx-border-radius: 15; "
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 4);";
        tarjeta.setStyle(estiloTarjeta);
        tarjeta.setPadding(new javafx.geometry.Insets(10)); 
        tarjeta.setPrefSize(180, 150); 
        tarjeta.setMaxSize(180, 150);

        Label lblNombre = new Label(p.getNombre());
        lblNombre.setStyle("-fx-font-weight: bold; -fx-text-fill: #263238; -fx-font-size: 13px;");

        ImageView imgPlatillo = new ImageView();
        imgPlatillo.setFitHeight(75);
        imgPlatillo.setFitWidth(120);
        imgPlatillo.setPreserveRatio(true);
        
        try {
            String nombreImagen = p.getImagen();
            if (nombreImagen == null || nombreImagen.trim().isEmpty()) nombreImagen = "default.png";
            
            URL urlImg = getClass().getResource("/img/" + nombreImagen);
            if (urlImg == null) urlImg = getClass().getResource("/img/default.png");
            
            if (urlImg != null) imgPlatillo.setImage(new Image(urlImg.toExternalForm()));
            else imgPlatillo.setStyle("-fx-background-color: #e0e0e0;");
        } catch (Exception e) {
            imgPlatillo.setStyle("-fx-background-color: #e0e0e0;");
        }

        HBox cajaPrecio = new HBox();
        cajaPrecio.setAlignment(Pos.CENTER);
        Label lblPrecio = new Label(String.format("$%.2f", p.getPrecio()));
        lblPrecio.setStyle("-fx-font-weight: bold; -fx-text-fill: #FF8F00; -fx-font-size: 15px;");
        cajaPrecio.getChildren().add(lblPrecio);

        VBox.setMargin(imgPlatillo, new javafx.geometry.Insets(8, 0, 8, 0));
        tarjeta.getChildren().addAll(lblNombre, imgPlatillo, cajaPrecio);
        return tarjeta;
    }

    private void mostrarGrid(String cat) {
        if (gridPizza != null) gridPizza.setVisible(cat.equals("Pizzas"));
        if (gridBebidas != null) gridBebidas.setVisible(cat.equals("Bebidas"));
        if (gridPasteles != null) gridPasteles.setVisible(cat.equals("Pasteles"));
        if (gridExtras != null) gridExtras.setVisible(cat.equals("Extras"));
        if (gridEspeciales != null) gridEspeciales.setVisible(cat.equals("Especiales"));
    }

    @FXML void clicVerPizzas(ActionEvent event) { mostrarGrid("Pizzas"); }
    @FXML void clicVerBebidas(ActionEvent event) { mostrarGrid("Bebidas"); }
    @FXML void clicVerPasteles(ActionEvent event) { mostrarGrid("Pasteles"); }
    @FXML void clicVerExtras(ActionEvent event) { mostrarGrid("Extras"); }
    @FXML void clicVerEspeciales(ActionEvent event) { mostrarGrid("Especiales"); }

    @FXML
    private void abrirPantallaReservar(ActionEvent event) {
        try { cambiarPantalla(event, "ReservacionCliente", "Menú Cliente - Pizzatron 3000"); } catch (IOException ex) { ex.printStackTrace(); }
    }

    @FXML
    private void volverAlLogin(ActionEvent event) {
        try { cambiarPantalla(event, "Login", "Iniciar Sesión - Pizzatron 3000"); } catch (IOException ex) { ex.printStackTrace(); }
    }

    @FXML
    private void abrirPantallaOpiniones(ActionEvent event) {
        try { 
            cambiarPantalla(event, "OpinionesClientes", "Buzón de Opiniones - MongoDB NoSQL"); 
        } catch (IOException ex) { 
            System.err.println("Error crítico al abrir el buzón de opiniones: " + ex.getMessage());
            ex.printStackTrace(); 
        }
    }

    private void cambiarPantalla(ActionEvent event, String fxmlName, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlName + ".fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.show();
    }
}