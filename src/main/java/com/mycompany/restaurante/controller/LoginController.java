package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.modelo.pojo.Usuario;
import com.mycompany.restaurante.dao.UsuarioDAO;
import com.mycompany.restaurante.utils.ConexionBD;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controlador principal encargado de gestionar el acceso y la autenticación al sistema.
 * Implementa una puerta de enlace bifurcada para procesar inicios de sesión tanto de clientes 
 * como de empleados, inyectando las sesiones en memoria y enrutando al usuario a la vista 
 * correspondiente según su nivel de privilegios y rol.
 * * @author Ricardo, Diego, Angel, Stephy
 */
public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    
    @FXML private RadioButton rbEmpleado;
    @FXML private RadioButton rbCliente;

    /**
     * Captura y valida los datos iniciales del formulario de acceso.
     * Actúa como filtro primario validando campos vacíos y selección de perfil, para evitar 
     * consultas nulas a la base de datos.
     * Nota Técnica (Seguridad): Purga preventivamente cualquier variable de sesión global 
     * antes de intentar una nueva autenticación para evitar cruce de credenciales.
     * * @param event El evento disparado al presionar el botón "Ingresar".
     */
    @FXML
    void clicIngresar(ActionEvent event) {
        String user = txtUsuario.getText().trim();
        String pass = txtPassword.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            mostrarAlerta("Campos Requeridos", "Por favor, escribe tus datos de acceso antes de continuar.");
            return;
        }

        if (!rbCliente.isSelected() && !rbEmpleado.isSelected()) {
            mostrarAlerta("Selección Requerida", "Por favor, selecciona si eres 'Cliente' o 'Empleado' para poder ingresar.");
            return;
        }

        // LIMPIAMOS SESIONES POR SEGURIDAD ANTES DE ENTRAR
        App.usuarioLogueado = null;
        App.idClienteLogueado = null;

        if (rbCliente.isSelected()) {
            validarClienteBD(user, pass);
        } else if (rbEmpleado.isSelected()) {
            validarEmpleadoDAO(user, pass);
        }
    }

    /**
     * Procesa la autenticación exclusiva para clientes del restaurante.
     * Ejecuta una consulta directa validando el ID autogenerado y su teléfono asociado.
     * Si es exitoso, inicializa la variable de sesión del cliente para activar los blindajes IDOR 
     * en los módulos siguientes y redirige al menú digital interactivo.
     * * @param idCliente El identificador único del cliente (Ej. CP001).
     * @param telefono El número de teléfono registrado utilizado como contraseña.
     */
    private void validarClienteBD(String idCliente, String telefono) {
        String sql = "SELECT * FROM clientes WHERE id_cliente = ? AND telefono = ?";
        
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            if (con == null) {
                throw new SQLException("No se pudo establecer comunicación con el servidor local de MySQL.");
            }
            
            ps.setString(1, idCliente);
            ps.setString(2, telefono);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("¡Acceso de Cliente exitoso!");
                    
                    // --- AQUÍ ESTÁ LA MAGIA: GUARDAMOS EL ID DEL CLIENTE EN LA MEMORIA GLOBAL ---
                    App.idClienteLogueado = idCliente;
                    
                    FXMLLoader loader = App.getFXMLLoader("VerMenuCliente");
                    Parent root = loader.load();
                    
                    Stage stage = (Stage) txtUsuario.getScene().getWindow();
                    stage.setScene(new Scene(root));
                    stage.setTitle("Menú Digital - Pizzatron 3000");
                    stage.show();
                } else {
                    mostrarAlerta("Credenciales Incorrectas", 
                        "El ID de Cliente o el Teléfono ingresados no coinciden con nuestros registros.\n\n" +
                        "Por favor, verifícalos o solicita tu registro con el personal del restaurante.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Conexión", "No se pudo consultar la base de datos de clientes.\nDetalle técnico: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Sistema", "No se pudo cargar la pantalla del menú digital para el cliente.");
        }
    }

    /**
     * Procesa la autenticación para el personal interno mediante la capa de acceso a datos (DAO).
     * En caso de éxito, inicializa el objeto global del empleado y delega el enrutamiento 
     * al método encargado de distribuir la interfaz operativa.
     * * @param user El nombre de usuario de acceso del empleado.
     * @param pass La contraseña de seguridad.
     */
    private void validarEmpleadoDAO(String user, String pass) {
        try {
            UsuarioDAO dao = new UsuarioDAO();
            Usuario usuarioValidado = dao.validarLogin(user, pass);

            if (usuarioValidado != null) {
                App.usuarioLogueado = usuarioValidado; 
                entrarAlDashboard(usuarioValidado);
            } else {
                mostrarAlerta("Acceso Denegado", 
                    "El usuario o la contraseña de personal son incorrectos.\n\n" +
                    "Asegúrate de que las mayúsculas estén bien escritas y que tengas seleccionada la opción 'Empleado'.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error de Sistema", "Hubo un problema interno al validar las credenciales de staff:\n" + e.getMessage());
        }
    }

    /**
     * Orquesta el enrutamiento post-login para el personal administrativo y operativo.
     * Aplica una regla de negocio de segregación: aísla el rol de 'Chef' enrutándolo directamente 
     * a su visualizador de comandas, mientras dirige al resto del staff al panel de control central (Dashboard),
     * pasándoles sus credenciales para levantar el RBAC (Permisos).
     * * @param usuario El objeto de empleado validado a inyectar en las siguientes vistas.
     */
    private void entrarAlDashboard(Usuario usuario) {
        String fxmlParaCargar = "";
        try {
            if (usuario.getRol().equalsIgnoreCase("Chef")) {
                fxmlParaCargar = "Chef";
            } else {
                fxmlParaCargar = "Dashboard"; 
            }

            FXMLLoader loader = App.getFXMLLoader(fxmlParaCargar);
            Parent root = loader.load();

            if (fxmlParaCargar.equals("Dashboard")) {
                DashboardController dashboardCtrl = loader.getController();
                dashboardCtrl.configurarUsuario(usuario);
            }

            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            
            if (fxmlParaCargar.equals("Dashboard")) {
                stage.setTitle("Panel de Control - Staff");
            } else {
                stage.setTitle("Módulo de Producción (Cocina) - Pizzatron 3000");
            }
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Navegación", "No se pudo abrir la vista '" + fxmlParaCargar + ".fxml'.\n\nDetalle: " + e.getMessage());
        }
    }

    /**
     * Redirige al flujo de alta para comensales nuevos (Walk-ins o clientes sin ID).
     * Carga el FXML para solicitar los datos básicos y asignar un identificador formal en la DB.
     * * @param event El evento disparado al presionar el botón "Registrarse".
     */
    @FXML
    void clicRegistrar(ActionEvent event) {
        try {
            FXMLLoader loader = App.getFXMLLoader("RegistroCliente");
            Parent root = loader.load();
            Stage stage = (Stage) txtUsuario.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Registro de Pingüinos - Pizzatron");
            stage.show();
        } catch (Exception e) {
            System.out.println("Error al ir a la pantalla de registro: " + e.getMessage());
            mostrarAlerta("Error de Navegación", "No se pudo cargar la pantalla de registro de clientes.");
        }
    }

    /**
     * Método auxiliar de interfaz gráfica para desplegar alertas tipo ERROR interactivas.
     * * @param titulo  Título que encabezará el cuadro de diálogo.
     * @param mensaje Cuerpo descriptivo del fallo a mostrar al usuario.
     */
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}