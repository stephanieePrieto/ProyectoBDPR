package com.mycompany.restaurante;

import com.mycompany.restaurante.modelo.pojo.Usuario;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class App extends Application {

    private static Scene scene;
    
    // Este objeto mantiene la sesión de los EMPLEADOS
    public static Usuario usuarioLogueado; 
    
    // --- ESTA ES LA NUEVA VARIABLE PARA LOS CLIENTES ---
    // Este String mantiene la sesión del PINGÜINO (ej. "CP001")
    public static String idClienteLogueado; 

    @Override
    public void start(Stage stage) throws IOException {
        // Al iniciar, limpiamos todas las sesiones
        usuarioLogueado = null; 
        idClienteLogueado = null;
        
        Parent root = loadFXML("Login"); 
        scene = new Scene(root, 1024, 768);
        
        stage.setTitle("Sistema Restaurante - Acceso");
        stage.setScene(scene);
        stage.setResizable(false); 
        stage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    public static FXMLLoader getFXMLLoader(String fxml) throws IOException {
        String path = "/fxml/" + fxml + ".fxml";
       URL resource = App.class.getResource(path);
        if (resource == null) {
            resource = App.class.getResource(fxml + ".fxml");
        }
        return new FXMLLoader(resource);
    }

    private static Parent loadFXML(String fxml) throws IOException {
        return getFXMLLoader(fxml).load();
    }

    public static void main(String[] args) {
        launch(args);
    }
}