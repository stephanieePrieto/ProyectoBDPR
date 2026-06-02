package com.mycompany.restaurante.modelo.pojo;

import java.util.Date;
import org.bson.types.ObjectId;

public class Opinion {
    private ObjectId id; 
    private String cliente;
    private Date fechaHora;
    private String tipo; // "Comentario", "Queja", "Sugerencia"
    private String contenido;

    // Específicos de Comentarios
    private Integer calificacionEstrellas;
    private String mejorAspecto;
    private String emojiFinal;

    // Específicos de Quejas
    private String tipoProblema;
    private String gravedad;

    // Específicos de Sugerencias
    private String categoriaSugerencia;
    private String verloPronto;

    public Opinion() {}

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
}