package com.mycompany.restaurante;

import com.mycompany.restaurante.modelo.pojo.Usuario;
import com.mycompany.restaurante.modelo.sql.OracleConnect;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

public class App extends Application {

    private static Scene scene;
    
    // Sesión de Empleados y Clientes
    public static Usuario usuarioLogueado; 
    public static String idClienteLogueado; 
    
    // Conexión a MongoDB estática
    public static MongoDatabase mongoDB;

    @Override
    public void start(Stage stage) throws IOException {
        // 1. VERIFICACIÓN DE ORACLE
        System.out.println("🔍 Iniciando verificación de infraestructura...");
        try {
            java.sql.Connection con = OracleConnect.getConexion();
            if (con != null && !con.isClosed()) {
                System.out.println("✅ ¡Infraestructura lista y conectada a Oracle Cloud!");
            }
        } catch (Exception e) {
            System.err.println("❌ ERROR FATAL ORACLE: " + e.getMessage());
        }

        // 2. INICIALIZACIÓN DE MONGODB
        try {
            // Asegúrate de tener tu URI de Atlas correcta aquí
            String uri = "mongodb+srv://<usuario>:<password>@cluster.mongodb.net/?retryWrites=true&w=majority";
            MongoClient mongoClient = MongoClients.create(uri);
            mongoDB = mongoClient.getDatabase("SistemaRestaurante");
            System.out.println("✅ ¡Conectado a MongoDB Atlas!");
        } catch (Exception e) {
            System.err.println("❌ ERROR FATAL MONGODB: " + e.getMessage());
        }

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