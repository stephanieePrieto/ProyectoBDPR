package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.App;
import com.mycompany.restaurante.modelo.sql.OracleConnect; // Conexión a Oracle Cloud
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
 * Controlador encargado del módulo de registro e inicialización (Onboarding)
 * para clientes de primera vez.
 * Migrado a sintaxis y arquitectura exclusiva de Oracle Cloud.
 */
public class RegistroClienteController {

    @FXML private TextField txtNombre;
    @FXML private PasswordField txtTelefono;

    /**
     * Procesa la solicitud de inscripción de un nuevo cliente al ecosistema del restaurante.
     * Lee la tabla de clientes existente en la Nube, genera secuencialmente el 
     * siguiente ID alfanumérico disponible (Ej. de CP001 salta a CP002) y persiste el perfil.
     * @param event Evento disparado por el botón "Registrarse".
     */
    @FXML
    void clicRegistrar(ActionEvent event) {
        String nombre = txtNombre.getText().trim();
        String telefono = txtTelefono.getText().trim();

        if (nombre.isEmpty() || telefono.isEmpty()) {
            mostrarAlerta("Campos Vacíos", "Ey, no dejes nada en blanco. Llena tu nombre y tu teléfono.", Alert.AlertType.WARNING);
            return;
        }

        try (Connection con = OracleConnect.getConexion()) {
            
            // 1. Lógica para auto-generar el ID alfanumérico secuencial
            // MAGIA ORACLE: Se utiliza la cláusula `FETCH FIRST 1 ROW ONLY` que reemplaza al clásico `LIMIT 1` de MySQL
            String nuevoId = "CP001";
            String sqlMax = "SELECT id_cliente FROM clientes WHERE id_cliente LIKE 'CP%' ORDER BY id_cliente DESC FETCH FIRST 1 ROW ONLY";
            
            try (PreparedStatement psMax = con.prepareStatement(sqlMax);
                 ResultSet rsMax = psMax.executeQuery()) {
                if (rsMax.next()) {
                    String maxId = rsMax.getString("id_cliente");
                    // Extrae el número (ej. "001"), le suma uno y lo recodifica a texto rellenando ceros a la izquierda
                    int numero = Integer.parseInt(maxId.substring(2)) + 1;
                    nuevoId = String.format("CP%03d", numero);
                }
            }

            // 2. Almacenamiento seguro del nuevo perfil
            String sqlInsert = "INSERT INTO clientes (id_cliente, nombre, telefono) VALUES (?, ?, ?)";
            try (PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {
                psInsert.setString(1, nuevoId);
                psInsert.setString(2, nombre);
                psInsert.setString(3, telefono); 
                psInsert.executeUpdate();
                
                mostrarAlerta("¡Registro Exitoso!", 
                    "¡Bienvenido al Pizzatron, " + nombre + "!\n\n" +
                    "TU ID DE PINGÜINO ES: " + nuevoId + "\n" +
                    "TU CONTRASEÑA ES: " + telefono + "\n\n" +
                    "Anota tu ID, lo necesitarás para iniciar sesión.", 
                    Alert.AlertType.INFORMATION);
                
                irAlLogin(); // Despacha al cliente al login tras una captura exitosa
            }
            
        } catch (SQLException e) {
            mostrarAlerta("Error", "Hubo un problema al crear la cuenta en Oracle: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Interrumpe el proceso de registro descartando los datos y 
     * retorna la navegación a la interfaz principal de autenticación.
     * @param event Evento disparado por el botón "Volver".
     */
    @FXML
    void clicVolver(ActionEvent event) {
        irAlLogin(); 
    }

    /**
     * Concentrador lógico para evitar redundancia de código al invocar la pantalla de Login.
     */
    private void irAlLogin() {
        try {
            FXMLLoader loader = App.getFXMLLoader("Login");
            Parent root = loader.load();
            Stage stage = (Stage) txtNombre.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Iniciar Sesión - Pizzatron 3000");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error de Navegación", "No se pudo regresar a la pantalla de Login.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Generador estandarizado de notificaciones modales visuales para la UI.
     */
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}