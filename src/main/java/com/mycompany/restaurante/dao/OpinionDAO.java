package com.mycompany.restaurante.dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mycompany.restaurante.modelo.pojo.Opinion;
import com.mycompany.restaurante.modelo.sql.MongoConnect;
import java.util.ArrayList;
import java.util.List;
import com.mongodb.client.model.Filters;
import org.bson.types.ObjectId;
import com.mongodb.client.model.Updates;

/**
 * Clase encargada de gestionar las operaciones de base de datos para las opiniones de los clientes en MongoDB.
 */
public class OpinionDAO {

    /**
     * Obtiene la colección de opiniones desde la base de datos configurada.
     * @return Colección de documentos de tipo Opinion.
     */
    private MongoCollection<Opinion> getColeccion() {
        MongoDatabase db = MongoConnect.getBaseDatos();
        return db.getCollection("opiniones_clientes", Opinion.class);
    }

    /**
     * Registra una nueva opinión (comentario, queja o sugerencia) de forma anónima.
     * @param nuevaOpinion Objeto Opinion con los datos a insertar.
     * @return Verdadero si el registro fue exitoso, falso en caso contrario.
     */
    public boolean registrarOpinion(Opinion nuevaOpinion) {
        try {
            getColeccion().insertOne(nuevaOpinion);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error al insertar en MongoDB: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Recupera todas las opiniones almacenadas en la colección.
     * @return Lista con todos los objetos Opinion registrados.
     */
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
    
    /**
     * Elimina una opinión de la base de datos utilizando su identificador único.
     * @param id Identificador único (ObjectId) de la opinión.
     * @return Verdadero si la eliminación fue exitosa, falso en caso contrario.
     */
    public boolean eliminarOpinion(ObjectId id) {
        try {
            var resultado = getColeccion().deleteOne(Filters.eq("_id", id));
            return resultado.getDeletedCount() > 0;
        } catch (Exception e) {
            System.err.println("❌ Error al eliminar en MongoDB: " + e.getMessage());
            return false;
        }
    }

    /**
     * Actualiza el contenido de una opinión existente.
     * @param id Identificador único (ObjectId) de la opinión a modificar.
     * @param nuevoContenido Cadena de texto con el nuevo comentario o sugerencia.
     * @return Verdadero si la actualización fue exitosa, falso en caso contrario.
     */
    public boolean actualizarOpinion(ObjectId id, String nuevoContenido) {
        try {
            var resultado = getColeccion().updateOne(
                Filters.eq("_id", id), 
                Updates.set("contenido", nuevoContenido)
            );
            return resultado.getModifiedCount() > 0;
        } catch (Exception e) {
            System.err.println("❌ Error al actualizar en MongoDB: " + e.getMessage());
            return false;
        }
    }
}