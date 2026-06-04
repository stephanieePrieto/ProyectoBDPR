package com.mycompany.restaurante.dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mycompany.restaurante.modelo.pojo.Opinion;
import com.mycompany.restaurante.modelo.sql.MongoConnect;
import java.util.ArrayList;
import java.util.List;

public class OpinionDAO {

    private MongoCollection<Opinion> getColeccion() {
        MongoDatabase db = MongoConnect.getBaseDatos();
        return db.getCollection("opiniones_clientes", Opinion.class);
    }

    // Guardar cualquier opinión (Comentario, Queja o Sugerencia) de forma Anónima
    public boolean registrarOpinion(Opinion nuevaOpinion) {
        try {
            getColeccion().insertOne(nuevaOpinion);
            System.out.println("🍃 [MongoDB] Documento tipo " + nuevaOpinion.getTipo() + " guardado con éxito.");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error al insertar en MongoDB: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Renombrado a obtainAllOpiniones() para que haga match exacto con tu controlador unificado
    public List<Opinion> obtainAllOpiniones() {
        List<Opinion> lista = new ArrayList<>();
        try {
            getColeccion().find().into(lista);
        } catch (Exception e) {
            System.err.println("❌ Error al consultar MongoDB: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }
}