package com.mycompany.restaurante.modelo.pojo;

/**
 * Representa un periodo de tiempo con su respectiva actividad de ventas.
 */
public class PicoActividad {
    private String fecha;
    private int cantidadPedidos;
    private double ingresosTotales;

    /**
     * Constructor para inicializar los datos de actividad.
     * @param fecha Fecha del registro.
     * @param cantidadPedidos Número total de pedidos realizados.
     * @param ingresosTotales Suma total de ingresos obtenidos.
     */
    public PicoActividad(String fecha, int cantidadPedidos, double ingresosTotales) {
        this.fecha = fecha;
        this.cantidadPedidos = cantidadPedidos;
        this.ingresosTotales = ingresosTotales;
    }

    /**
     * Métodos para obtener los datos de fecha, cantidad de pedidos e ingresos totales.
     */
    public String getFecha() { return fecha; }
    public int getCantidadPedidos() { return cantidadPedidos; }
    public double getIngresosTotales() { return ingresosTotales; }
}