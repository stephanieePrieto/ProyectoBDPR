package com.mycompany.restaurante.modelo.pojo;

/**
 * Clase de modelo (POJO) que representa el resultado de un análisis de ventas.
 * Esta entidad se utiliza para estructurar los datos del "Top de productos" 
 * en las pantallas de reportes y analítica del restaurante.
 * * @author Ricardo, Diego, Angel, Stephy
 */
public class ProductoMasVendido {
    
    private String nombre;
    private int cantidadVendida;

    /**
     * Constructor vacío por defecto.
     */
    public ProductoMasVendido() {}

    /**
     * Constructor para inicializar una instancia de producto analizado.
     * * @param nombre Nombre comercial del platillo.
     * @param cantidadVendida Total acumulado de unidades vendidas en un periodo dado.
     */
    public ProductoMasVendido(String nombre, int cantidadVendida) {
        this.nombre = nombre;
        this.cantidadVendida = cantidadVendida;
    }

    // --- MÉTODOS ACCESORES Y MUTADORES ---

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getCantidadVendida() {
        return cantidadVendida;
    }

    public void setCantidadVendida(int cantidadVendida) {
        this.cantidadVendida = cantidadVendida;
    }
}