package com.mycompany.restaurante.modelo.pojo;

import java.util.Date;
import org.bson.types.ObjectId;

/**
 * Representa una opinión, queja o sugerencia enviada por un cliente.
 * Contiene campos comunes y atributos específicos dependiendo del tipo de opinión.
 */
public class Opinion {
    private ObjectId id; 
    private String cliente;
    private Date fechaHora;
    private String tipo; // "Comentario", "Queja" o "Sugerencia"
    private String contenido;
    private String idSesion;

    // Atributos específicos para Comentarios
    private Integer calificacionEstrellas;
    private String mejorAspecto;
    private String emojiFinal;

    // Atributos específicos para Quejas
    private String tipoProblema;
    private String gravedad;

    // Atributos específicos para Sugerencias
    private String categoriaSugerencia;
    private String verloPronto;

    /**
     * Constructor vacío necesario para el mapeo de objetos en MongoDB.
     */
    public Opinion() {}

    /**
     * Métodos para obtener y establecer los valores de los atributos de la opinión.
     */
    public ObjectId getId() { return id; }
    public void setId(ObjectId id) { this.id = id; }
    public String getCliente() { return cliente; }
    public void setCliente(String cliente) { this.cliente = cliente; }
    public Date getFechaHora() { return fechaHora; }
    public void setFechaHora(Date fechaHora) { this.fechaHora = fechaHora; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public Integer getCalificacionEstrellas() { return calificacionEstrellas; }
    public void setCalificacionEstrellas(Integer calificacionEstrellas) { this.calificacionEstrellas = calificacionEstrellas; }
    public String getMejorAspecto() { return mejorAspecto; }
    public void setMejorAspecto(String mejorAspecto) { this.mejorAspecto = mejorAspecto; }
    public String getEmojiFinal() { return emojiFinal; }
    public void setEmojiFinal(String emojiFinal) { this.emojiFinal = emojiFinal; }
    public String getTipoProblema() { return tipoProblema; }
    public void setTipoProblema(String tipoProblema) { this.tipoProblema = tipoProblema; }
    public String getGravedad() { return gravedad; }
    public void setGravedad(String gravedad) { this.gravedad = gravedad; }
    public String getCategoriaSugerencia() { return categoriaSugerencia; }
    public void setCategoriaSugerencia(String categoriaSugerencia) { this.categoriaSugerencia = categoriaSugerencia; }
    public String getVerloPronto() { return verloPronto; }
    public void setVerloPronto(String verloPronto) { this.verloPronto = verloPronto; }
    public String getIdSesion() { return idSesion; }
    public void setIdSesion(String idSesion) { this.idSesion = idSesion; }
}