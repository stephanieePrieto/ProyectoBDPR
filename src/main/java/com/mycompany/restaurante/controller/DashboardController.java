package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.modelo.pojo.Usuario;
import java.io.IOException;
import java.net.URL;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

/**
 * Controlador del Panel Principal (Dashboard).
 * Gestiona el control de acceso basado en roles (RBAC), administrando la 
 * visibilidad de los componentes y el enrutamiento seguro de las pantallas.
 * * @author Stephanie Hernandez
 */
public class DashboardController {

    // --- SECCIONES DE TEXTO INFORMATIVO ---
    @FXML private Label lblSeccionGerente;
    @FXML private Label lblSeccionRecepcionista;
    @FXML private Label lblSeccionCajero;
    @FXML private Label lblSeccionMesero;

    // --- CONTROLES OPERATIVOS ---
    @FXML private Button btnAltaMenu;
    @FXML private Button btnEmpleados;
    @FXML private Button btnReservaciones;
    @FXML private Button btnCobro;
    @FXML private Button btnPedido;
    @FXML private Button btnAsignar;
    @FXML private Button btnGenerar;
    @FXML private Button btnEstado;
    @FXML private Button btnLista;
    @FXML private Button btnFacturacion;
    @FXML private Button btnAsistencia;
    @FXML private Button mbGestionOrdenes;
    @FXML private Button btnReportePicos; // Variable inyectada correctamente
    @FXML private MenuButton mbControl;
    @FXML private MenuItem miReporteVentas;
    @FXML private MenuItem miReporteAlmacen;

    /**
     * Inicializa el estado del controlador y configura los permisos de seguridad.
     */
    @FXML
    public void initialize() {
        if (App.usuarioLogueado != null) {
            configurarPermisos(App.usuarioLogueado.getRol());
        }
    }

    /**
     * Configura contextualmente la sesión actual vinculada a un usuario.
     * @param usuario Instancia del objeto POJO con las credenciales activas.
     */
    public void configurarUsuario(Usuario usuario) {
        configurarPermisos(usuario.getRol());
    }

    /**
     * Administra el ocultamiento y despliegue de módulos según el rol (RBAC).
     * @param rol Nombre del rol jerárquico.
     */
    private void configurarPermisos(String rol) {
        // Añadimos btnReportePicos a la lista global de purga inicial
        Node[] todos = {
            lblSeccionGerente, lblSeccionRecepcionista, 
            lblSeccionCajero, lblSeccionMesero,
            btnAltaMenu, btnEmpleados, mbControl, btnReservaciones, 
            btnAsignar, btnLista, btnCobro, btnGenerar, btnFacturacion, 
            btnPedido, btnEstado, mbGestionOrdenes, btnAsistencia,
            btnReportePicos
        };

        for (Node n : todos) {
            if (n != null) {
                n.setVisible(false);
                n.setManaged(false);
            }
        }

        // --- ASIGNACIÓN DE PRIVILEGIOS POR ROL OPERATIVO ---
        if ("Gerente".equals(rol)) {
            activar(lblSeccionGerente); 
            activar(btnAltaMenu); 
            activar(btnEmpleados); 
            activar(mbControl); 
            activar(btnAsistencia);
            activar(btnReportePicos); // ¡EXCLUSIVO!: Solo el Gerente lo puede ver
        } else if ("Recepcionista".equals(rol)) {
            activar(lblSeccionRecepcionista); 
            activar(btnReservaciones); 
            activar(btnAsignar); 
            activar(btnLista); 
            activar(btnAsistencia);
        } else if ("Cajero".equals(rol)) {
            activar(lblSeccionCajero); 
            activar(btnCobro); 
            activar(btnGenerar); 
            activar(btnFacturacion); 
            activar(btnAsistencia);
        } else if ("Mesero".equals(rol)) {
            activar(lblSeccionMesero); 
            activar(btnPedido); 
            activar(btnEstado); 
            activar(mbGestionOrdenes); 
            activar(btnAsistencia);
        }
    }

    private void activar(Node n) {
        if (n != null) {
            n.setVisible(true);
            n.setManaged(true);
        }
    }

    private void cambiarPantalla(ActionEvent event, String fxml, String titulo) 
            throws IOException {
        FXMLLoader loader = App.getFXMLLoader(fxml);
        Parent root = loader.load();

        Stage stage;
        if (event != null && event.getSource() instanceof Node) {
            stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        } else {
            stage = (Stage) btnEmpleados.getScene().getWindow(); 
        }

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(titulo);
        stage.centerOnScreen();
        stage.show();
    }

    // --- MÉTODOS DE NAVEGACIÓN ---

    @FXML private void abrirAsistencia(ActionEvent event) {
        try { cambiarPantalla(event, "RegistrarAsistenciaEmpleados", "Registro de Asistencia"); } catch (IOException ex) { ex.printStackTrace(); }
    }

    @FXML private void abrirPantallaPedido(ActionEvent event) {
        try { cambiarPantalla(event, "Pedido", "Tomar Pedido"); } catch (IOException ex) { ex.printStackTrace(); }
    }

    @FXML private void abrirRegistroPlatillo(ActionEvent event) {
        try { cambiarPantalla(event, "RegistroPlatillo", "Gestión de Menú"); } catch (IOException ex) { ex.printStackTrace(); }
    }

    @FXML private void abrirRegistroEmpleado(ActionEvent event) {
        try { cambiarPantalla(event, "RegistroEmpleado", "Gestión de Empleados"); } catch (IOException ex) { ex.printStackTrace(); }
    }

    @FXML private void abrirReporteVentas(ActionEvent event) {
        try { cambiarPantalla(event, "ReporteVentas", "Reporte de Ventas"); } catch (IOException ex) { ex.printStackTrace(); }
    }

    @FXML private void abrirAlmacen(ActionEvent event) {
        try { cambiarPantalla(event, "Almacen", "Control de Almacén"); } catch (IOException ex) { ex.printStackTrace(); }
    }

    @FXML private void abrirModificarReservaciones(ActionEvent event) {
        try { cambiarPantalla(event, "Reservacion", "Gestión de Reservaciones"); } catch (IOException ex) { ex.printStackTrace(); }
    }

    @FXML private void abrirEstadoMesas(ActionEvent event) {
        try { cambiarPantalla(event, "EstadoMesa", "Estado de Mesas"); } catch (IOException ex) { ex.printStackTrace(); }
    }

    @FXML private void clicGenerarCuenta(ActionEvent event) {
        try { cambiarPantalla(event, "GenerarCuenta", "Generar Cuenta"); } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void clicCobro(ActionEvent event) {
        try { cambiarPantalla(event, "RegistrarPago", "Registro de Pago"); } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void clicFacturacion(ActionEvent event) {
        try { cambiarPantalla(event, "Factura", "Facturación"); } catch (IOException ex) { ex.printStackTrace(); }
    }

    @FXML private void abrirAsignarMesa(ActionEvent event) {
        try { cambiarPantalla(event, "AsignarMesa", "Asignar Mesa"); } catch (IOException ex) { ex.printStackTrace(); }
    }

    @FXML private void abrirGestionOrdenes(ActionEvent event) {
        try { cambiarPantalla(event, "GestionOrdenes", "Gestionar Ordenes"); } catch (IOException ex) { ex.printStackTrace(); }
    }

    @FXML private void abrirListaEspera(ActionEvent event) {
        try { cambiarPantalla(event, "ListaDeEspera", "Lista de Espera"); } catch (IOException ex) { ex.printStackTrace(); }
    }

    @FXML private void cerrarSesion(ActionEvent event) {
        try {
            App.usuarioLogueado = null; 
            cambiarPantalla(event, "Login", "Acceso al Sistema");
        } catch (IOException ex) { ex.printStackTrace(); }
    }

    @FXML
    void abrirReportePicos(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReportePicos.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle("Reporte de Picos de Actividad - Pizzatron 3000");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}