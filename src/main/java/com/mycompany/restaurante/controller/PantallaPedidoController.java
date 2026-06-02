package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.dao.PedidoDAO;
import com.mycompany.restaurante.dao.PlatilloDAO;
import com.mycompany.restaurante.modelo.pojo.Platillo;
import com.mycompany.restaurante.utils.ConexionBD;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PantallaPedidoController implements Initializable {

    @FXML private GridPane gridBebidas;
    @FXML private GridPane gridEspeciales;
    @FXML private GridPane gridExtras;
    @FXML private GridPane gridPasteles;
    @FXML private GridPane gridPizza;
    
    @FXML private ComboBox<String> cmbMesas;
    @FXML private ComboBox<Platillo> cmbAgotados;
    
    @FXML private Label lblTotalText;
    @FXML private Label lblPedido;
    @FXML private TableView<Platillo> tablaPedido;
    @FXML private TableColumn<Platillo, String> colArticulo;
    @FXML private TableColumn<Platillo, Integer> colCant;
    @FXML private TableColumn<Platillo, Double> colTotal;
    @FXML private TextField txtNotasEspeciales;

    private ObservableList<Platillo> listaPedido = FXCollections.observableArrayList();
    private double totalMonto = 0.0;
    private int pedidoActivoActual = -1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarTabla();
        cargarMesasOcupadas();
        cargarMenuDinamico(); 
        cargarPlatillosParaAgotar(); 
        mostrarGrid("Pizzas");
        
        if (lblPedido != null) {
            lblPedido.setText("0000");
        }
        cmbMesas.setOnAction(event -> cargarPedidoMesaSeleccionada());
    }

    private void configurarTabla() {
        colArticulo.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCant.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("precio"));
        tablaPedido.setItems(listaPedido);
    }

    private void cargarMenuDinamico() {
        gridPizza.getChildren().clear();
        gridBebidas.getChildren().clear();
        gridPasteles.getChildren().clear();
        gridExtras.getChildren().clear();
        gridEspeciales.getChildren().clear();

        try (Connection conexion = ConexionBD.conectar()) {
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
                    if (cat < 1 || cat > 5) {
                        cat = 1; 
                    }

                    switch (cat) {
                        case 1: gridPizza.add(tarjeta, colP, rowP); colP++; if (colP == 3) { colP = 0; rowP++; } break;
                        case 2: gridBebidas.add(tarjeta, colB, rowB); colB++; if (colB == 3) { colB = 0; rowB++; } break;
                        case 3: gridPasteles.add(tarjeta, colPa, rowPa); colPa++; if (colPa == 3) { colPa = 0; rowPa++; } break;
                        case 4: gridExtras.add(tarjeta, colEx, rowEx); colEx++; if (colEx == 3) { colEx = 0; rowEx++; } break;
                        case 5: gridEspeciales.add(tarjeta, colEs, rowEs); colEs++; if (colEs == 3) { colEs = 0; rowEs++; } break;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al cargar menú dinámico: " + e.getMessage());
        }
    }

    private VBox crearTarjetaPlatillo(Platillo p) {
        VBox tarjeta = new VBox();
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-color: #A0D8EF; -fx-border-width: 3; -fx-border-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 4);");
        tarjeta.setPadding(new javafx.geometry.Insets(10));
        tarjeta.setPrefSize(140, 140);
        tarjeta.setMaxSize(140, 140);

        Label lblNombre = new Label(p.getNombre());
        lblNombre.setStyle("-fx-font-weight: bold; -fx-text-fill: #263238; -fx-font-size: 11px;");

        ImageView imgPlatillo = new ImageView();
        imgPlatillo.setFitHeight(60);
        imgPlatillo.setFitWidth(100);
        imgPlatillo.setPreserveRatio(true);
        
        try {
            String path = "/img/" + (p.getImagen() != null && !p.getImagen().isEmpty() ? p.getImagen() : "default.png");
            URL urlImg = getClass().getResource(path);
            if(urlImg == null) urlImg = getClass().getResource("/img/default.png");
            imgPlatillo.setImage(new Image(urlImg.toExternalForm()));
        } catch (Exception e) {
            System.err.println("Imagen no encontrada: " + p.getNombre());
        }

        HBox cajaPrecio = new HBox(5);
        cajaPrecio.setAlignment(Pos.CENTER);
        
        Label lblPrecio = new Label(String.format("$%.2f", p.getPrecio()));
        lblPrecio.setStyle("-fx-font-weight: bold; -fx-text-fill: #FF8F00; -fx-font-size: 13px;");

        Button btnAdd = new Button();
        btnAdd.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 0;");
        try {
            ImageView imgMas = new ImageView(new Image(getClass().getResource("/img/masmas.png").toExternalForm()));
            imgMas.setFitHeight(22);
            imgMas.setFitWidth(30);
            imgMas.setPreserveRatio(true);
            btnAdd.setGraphic(imgMas);
        } catch (Exception e) {
            btnAdd.setText("+");
        }
        

        btnAdd.setOnAction(e -> agregarAlTicket(p));

        cajaPrecio.getChildren().addAll(lblPrecio, btnAdd);
        VBox.setMargin(imgPlatillo, new javafx.geometry.Insets(5, 0, 5, 0));
        tarjeta.getChildren().addAll(lblNombre, imgPlatillo, cajaPrecio);
        
        return tarjeta;
    }

    private void cargarMesasOcupadas() {
        String sql = "SELECT idMesa FROM mesa WHERE estado = 'Ocupada'";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            cmbMesas.getItems().clear();
            while (rs.next()) {
                cmbMesas.getItems().add(String.valueOf(rs.getInt("idMesa")));
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }

    private void cargarPedidoMesaSeleccionada() {
        if (cmbMesas.getValue() == null) return;
        try {
            int idMesa = Integer.parseInt(cmbMesas.getValue());
            try (Connection con = ConexionBD.conectar()) {
                PedidoDAO pedidoDAO = new PedidoDAO(con);
                PlatilloDAO platilloDAO = new PlatilloDAO(con);
                pedidoActivoActual = pedidoDAO.obtenerPedidoActivoPorMesa(idMesa);
                listaPedido.clear();
                
                if (pedidoActivoActual != -1) {
                    if (lblPedido != null) {
                        lblPedido.setText(String.format("%04d", pedidoActivoActual));
                    }
                    List<Platillo> platillosBD = platilloDAO.obtenerPlatillosPorOrden(pedidoActivoActual);
                    for (Platillo p : platillosBD) {
                        p.setPrecio(p.getPrecio() * p.getCantidad());
                    }
                    listaPedido.setAll(platillosBD);
                } else {
                    if (lblPedido != null) lblPedido.setText("Nuevo");
                }
                calcularTotal();
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
    }

    @FXML
    void clicEnviarChef(ActionEvent event) {
        if (listaPedido.isEmpty() || cmbMesas.getValue() == null) {
            mostrarAlerta("Error", "Verifica la mesa y los platillos.");
            return;
        }
        int idMesa = Integer.parseInt(cmbMesas.getValue());
        int idEmpleado = App.usuarioLogueado != null ? App.usuarioLogueado.getId() : 1; 

        String comentarioMesero = (txtNotasEspeciales != null) ? txtNotasEspeciales.getText().trim() : "Normal";
        if (comentarioMesero.isEmpty()) comentarioMesero = "Normal";

        try (Connection con = ConexionBD.conectar()) {
            PedidoDAO pedidoDAO = new PedidoDAO(con);
            if (pedidoActivoActual == -1) {
                pedidoActivoActual = pedidoDAO.crearNuevoPedido(idMesa, idEmpleado);
            }
            
            for (Platillo p : listaPedido) {
                p.setEstadoPlatillo(comentarioMesero);
            }
            
            pedidoDAO.guardarDetallesPedido(pedidoActivoActual, listaPedido);
            mostrarAlertaExito("¡Comanda Enviada!", "La orden #" + pedidoActivoActual + " está en cocina.");
            limpiarPantalla();
        } catch (SQLException e) { 
            mostrarAlerta("Error", e.getMessage()); 
        }
    }

    private void limpiarPantalla() {
        listaPedido.clear();
        calcularTotal();
        cmbMesas.getSelectionModel().clearSelection();
        pedidoActivoActual = -1;
        if (lblPedido != null) lblPedido.setText("0000");
        if (txtNotasEspeciales != null) txtNotasEspeciales.clear();
    }

    @FXML void clicBorrarPedido(ActionEvent event) {
        listaPedido.clear();
        calcularTotal();
    }

    @FXML void clicEliminarRenglon(ActionEvent event) {
        Platillo seleccionado = tablaPedido.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Aviso", "Selecciona un platillo de la tabla.");
            return;
        }

        double precioUnitario = seleccionado.getPrecio() / seleccionado.getCantidad();
        int nuevaCantidad = seleccionado.getCantidad() - 1;

        if (pedidoActivoActual != -1) {
            try (Connection con = ConexionBD.conectar()) {
                PedidoDAO pedidoDAO = new PedidoDAO(con);
                pedidoDAO.eliminarOActualizarPlatilloDePedido(pedidoActivoActual, seleccionado.getNombre(), nuevaCantidad);
            } catch (SQLException e) {
                mostrarAlerta("Error", "No se pudo actualizar la BD: " + e.getMessage());
                return;
            }
        }

        if (nuevaCantidad > 0) {
            seleccionado.setCantidad(nuevaCantidad);
            seleccionado.setPrecio(precioUnitario * nuevaCantidad);
            tablaPedido.refresh();
        } else {
            listaPedido.remove(seleccionado);
        }
        calcularTotal();
    }

    private void agregarAlTicket(Platillo platilloBD) {
        if (cmbMesas.getValue() == null) {
            mostrarAlerta("Aviso", "Selecciona una mesa primero.");
            return;
        }
        
        int cantidadYaEnTicket = 0;
        Platillo platilloEnTicket = null;

        // 1. Buscamos si ya estaba en el carrito para sumar la cantidad
        for (Platillo p : listaPedido) {
            if (p.getNombre().equals(platilloBD.getNombre())) {
                platilloEnTicket = p;
                cantidadYaEnTicket = p.getCantidad();
                break;
            }
        }

        // 2. EL BLOQUEO: Revisamos si requiere inventario y si nos alcanza
        if (platilloBD.getIdInsumoClave() > 0) {
            if (cantidadYaEnTicket >= platilloBD.getStockDisponible()) {
                mostrarAlerta("Inventario Insuficiente", 
                    "No puedes agregar más. Solo quedan " + platilloBD.getStockDisponible() + 
                    " unidades del ingrediente principal en el almacén.");
                return; // Bloqueamos la venta, no pasa de aquí.
            }
        }

        // 3. Si hay inventario, lo agregamos al ticket
        if (platilloEnTicket != null) {
            platilloEnTicket.setCantidad(cantidadYaEnTicket + 1);
            platilloEnTicket.setPrecio(platilloBD.getPrecio() * platilloEnTicket.getCantidad());
        } else {
            Platillo n = new Platillo();
            n.setIdPlatillo(platilloBD.getIdPlatillo()); 
            n.setNombre(platilloBD.getNombre());
            n.setCantidad(1);
            n.setPrecio(platilloBD.getPrecio());
            n.setIdInsumoClave(platilloBD.getIdInsumoClave());
            listaPedido.add(n);
        }
        
        tablaPedido.refresh();
        calcularTotal();
    }

    private void calcularTotal() {
        totalMonto = 0;
        for (Platillo p : listaPedido) totalMonto += p.getPrecio();
        lblTotalText.setText("$ " + String.format("%.2f", totalMonto));
    }

    private void mostrarGrid(String cat) {
        gridPizza.setVisible(cat.equals("Pizzas"));
        gridPizza.setManaged(cat.equals("Pizzas"));
        
        gridBebidas.setVisible(cat.equals("Bebidas"));
        gridBebidas.setManaged(cat.equals("Bebidas"));
        
        gridPasteles.setVisible(cat.equals("Pasteles"));
        gridPasteles.setManaged(cat.equals("Pasteles"));
        
        gridExtras.setVisible(cat.equals("Extras"));
        gridExtras.setManaged(cat.equals("Extras"));
        
        gridEspeciales.setVisible(cat.equals("Especiales"));
        gridEspeciales.setManaged(cat.equals("Especiales"));
    }

    @FXML void clicVerPizzas(ActionEvent event) { mostrarGrid("Pizzas"); }
    @FXML void clicVerBebidas(ActionEvent event) { mostrarGrid("Bebidas"); }
    @FXML void clicVerPasteles(ActionEvent event) { mostrarGrid("Pasteles"); }
    @FXML void clicVerExtras(ActionEvent event) { mostrarGrid("Extras"); }
    @FXML void clicVerEspeciales(ActionEvent event) { mostrarGrid("Especiales"); }

    private void cargarPlatillosParaAgotar() {
        if(cmbAgotados != null) {
            cmbAgotados.getItems().clear();
            try (Connection con = ConexionBD.conectar()) {
                PlatilloDAO dao = new PlatilloDAO(con);
                List<Platillo> activos = dao.obtenerPlatillosActivos();
                cmbAgotados.getItems().addAll(activos);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML void clicMarcarAgotado(ActionEvent event) {
        Platillo seleccionado = cmbAgotados.getValue();
        if (seleccionado == null) {
            mostrarAlerta("Aviso", "Selecciona un platillo para marcar como agotado.");
            return;
        }

        try (Connection con = ConexionBD.conectar()) {
            PlatilloDAO dao = new PlatilloDAO(con);
            if (dao.darDeBajaPlatillo(seleccionado.getIdPlatillo())) {
                mostrarAlertaExito("Agotado", "El platillo '" + seleccionado.getNombre() + "' se retiró del menú.");
                cargarPlatillosParaAgotar(); 
                cargarMenuDinamico(); 
            } else {
                mostrarAlerta("Error", "No se pudo actualizar el platillo.");
            }
        } catch (SQLException e) {
            mostrarAlerta("Error", "Fallo de conexión al desactivar.");
        }
    }

    @FXML void volverDashboard(ActionEvent event) {
        try {
            Parent root = App.getFXMLLoader("Dashboard").load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException ex) { 
            ex.printStackTrace(); 
        }
    }

    private void mostrarAlerta(String t, String m) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }

    private void mostrarAlertaExito(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }
}