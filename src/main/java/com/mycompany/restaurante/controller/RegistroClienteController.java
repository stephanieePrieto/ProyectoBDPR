package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.modelo.sql.MySQLConnect;
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
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controlador encargado del módulo de registro para nuevos clientes.
 * Gestiona la captura de datos de contacto y automatiza la creación de 
 * identificadores únicos (IDs) alfanuméricos, garantizando la integridad 
 * de la base de datos sin requerir asignaciones manuales.
 * * @author Ricardo, Diego, Angel, Stephy
 */
public class RegistroClienteController {

    @FXML private TextField txtNombre;
    @FXML private PasswordField txtTelefono;

    /**
     * Procesa la solicitud de inscripción de un nuevo cliente al sistema.
     * Valida la entrada de datos, genera secuencialmente el siguiente ID disponible 
     * (Ej. de CP001 a CP002) y persiste la información en la base de datos.
     * Finaliza mostrando las credenciales generadas para que el cliente las resguarde 
     * y redirige automáticamente a la pantalla de inicio de sesión.
     * * @param event Evento disparado por el botón "Registrarse".
     */
    @FXML
    void clicRegistrar(ActionEvent event) {
        String nombre = txtNombre.getText().trim();
        String telefono = txtTelefono.getText().trim();

        if (nombre.isEmpty() || telefono.isEmpty()) {
            mostrarAlerta("Campos Vacíos", "Ey, no dejes nada en blanco. Llena tu nombre y tu teléfono.", Alert.AlertType.WARNING);
            return;
        }

        try (Connection con = MySQLConnect.getConexion()) {
            // 1. Lógica para auto-generar el ID alfanumérico (CP001, CP002, etc.)
            String nuevoId = "CP001";
            String sqlMax = "SELECT id_cliente FROM clientes WHERE id_cliente LIKE 'CP%' ORDER BY id_cliente DESC LIMIT 1";
            
            try (PreparedStatement psMax = con.prepareStatement(sqlMax);
                 ResultSet rsMax = psMax.executeQuery()) {
                if (rsMax.next()) {
                    String maxId = rsMax.getString("id_cliente");
                    int numero = Integer.parseInt(maxId.substring(2)) + 1;
                    nuevoId = String.format("CP%03d", numero);
                }
            }

            // 2. Guardar el nuevo registro en la base de datos
            String sqlInsert = "INSERT INTO clientes (id_cliente, nombre, telefono) VALUES (?, ?, ?)";
            try (PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {
                psInsert.setString(1, nuevoId);
                psInsert.setString(2, nombre);
                psInsert.setString(3, telefono); // Guardamos el teléfono como contraseña de acceso
                psInsert.executeUpdate();
                
                // Mostrar alerta de éxito ENORME para que el cliente anote su ID
                mostrarAlerta("¡Registro Exitoso!", 
                    "¡Bienvenido al Pizzatron, " + nombre + "!\n\n" +
                    "TU ID DE PINGÜINO ES: " + nuevoId + "\n" +
                    "TU CONTRASEÑA ES: " + telefono + "\n\n" +
                    "Anota tu ID, lo necesitarás para iniciar sesión.", 
                    Alert.AlertType.INFORMATION);
                
                // Redirección encapsulada por seguridad
                irAlLogin();
            }
            
        } catch (SQLException e) {
            mostrarAlerta("Error", "Hubo un problema al crear la cuenta: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Interrumpe el proceso de registro y retorna a la interfaz principal de autenticación.
     * * @param event Evento disparado por el botón "Volver".
     */
    @FXML
    void clicVolver(ActionEvent event) {
        irAlLogin(); 
    }

    /**
     * Centraliza la lógica de navegación hacia la vista de Login.
     * Implementada como un método privado para garantizar que tanto las salidas voluntarias 
     * como las redirecciones automáticas post-registro sigan el mismo flujo seguro.
     */
    private void irAlLogin() {
        try {
            FXMLLoader loader = App.getFXMLLoader("Login");
            Parent root = loader.load();
            Stage stage = (Stage) txtNombre.getScene().getWindow(); // Agarra la ventana actual sin fallar
            stage.setScene(new Scene(root));
            stage.setTitle("Iniciar Sesión - Pizzatron 3000");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error de Navegación", "No se pudo regresar a la pantalla de Login.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Construye y despliega un cuadro de diálogo dinámico para notificar al usuario.
     * * @param titulo  El texto que se mostrará en la barra de título de la alerta.
     * @param mensaje El cuerpo descriptivo de la información o error.
     * @param tipo    La categoría visual de la ventana (Ej. WARNING, ERROR, INFORMATION).
     */
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}