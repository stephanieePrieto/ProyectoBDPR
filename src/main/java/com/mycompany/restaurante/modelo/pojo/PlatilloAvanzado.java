package com.mycompany.restaurante.modelo.pojo;

/**
 * Representa un platillo con características específicas extendidas (tipo de masa y nivel de picante).
 */
public class PlatilloAvanzado {
    private int id;
    private String nombre;
    private double precio;
    private String tipoMasa;
    private String esPicante;

    /**
     * Constructor para inicializar un platillo con sus atributos extendidos.
     * @param id Identificador único del platillo.
     * @param nombre Nombre del platillo.
     * @param precio Costo del platillo.
     * @param tipoMasa Tipo de masa utilizada.
     * @param esPicante Indicador de si el platillo es picante o no.
     */
    public PlatilloAvanzado(int id, String nombre, double precio, String tipoMasa, String esPicante) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.tipoMasa = tipoMasa;
        this.esPicante = esPicante;
    }

    /**
     * Obtiene los datos
     */
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public String getTipoMasa() { return tipoMasa; }
    public String getEsPicante() { return esPicante; }
}