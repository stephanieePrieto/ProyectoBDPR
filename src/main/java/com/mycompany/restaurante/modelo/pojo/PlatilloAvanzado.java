package com.mycompany.restaurante.modelo.pojo;

public class PlatilloAvanzado {
    private int id;
    private String nombre;
    private double precio;
    private String tipoMasa;
    private String esPicante;

    public PlatilloAvanzado(int id, String nombre, double precio, String tipoMasa, String esPicante) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.tipoMasa = tipoMasa;
        this.esPicante = esPicante;
    }

    // GETTERS NECESARIOS
    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
    public String getTipoMasa() { return tipoMasa; }
    public String getEsPicante() { return esPicante; }
}