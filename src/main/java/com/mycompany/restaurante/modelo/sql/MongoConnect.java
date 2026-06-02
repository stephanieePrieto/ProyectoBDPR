package com.mycompany.restaurante.modelo.sql;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import java.util.Collections;

public class MongoConnect {
    private MongoClient mongoClient;
    private MongoDatabase database;
    
    private String host = "localhost";
    private String port = "27017"; 
    private String dbName = "SistemaRestaurante"; // Coincide con tu base de datos en Compass
    
    private static MongoConnect instance;

    public MongoConnect() {
        try {
            // 1. Configurar el registro de Codecs para mapear POJOs automáticamente sin usar clases estáticas sueltas
            CodecRegistry pojoCodecRegistry = CodecRegistries.fromRegistries(
                    MongoClientSettings.getDefaultCodecRegistry(),
                    CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
            );

            // 2. Ajustar las configuraciones del cliente con el host, puerto y el CodecRegistry
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyToClusterSettings(builder -> 
                            builder.hosts(Collections.singletonList(new ServerAddress(host, Integer.parseInt(port)))))
                    .codecRegistry(pojoCodecRegistry)
                    .build();
            
            // 3. Crear la instancia del cliente y apuntar a la base de datos
            this.mongoClient = MongoClients.create(settings);
            this.database = mongoClient.getDatabase(dbName);
            
            System.out.println("🍃 ¡Conexión exitosa a MongoDB con mapeo de POJOs habilitado!");
        } catch (Exception e) {
            System.err.println("❌ Error Crítico de Conexión MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
        instance = this;
    }

    public MongoDatabase getDb() {
        return database;
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("🔒 [MongoDB] Conexión cerrada correctamente.");
        }
    }

    public static MongoConnect getConnect() { 
        if (instance == null) {
            instance = new MongoConnect();
        }
        return instance; 
    }

    public static MongoDatabase getBaseDatos() {
        return getConnect().getDb();
    }
}