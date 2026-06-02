package com.mycompany.restaurante.modelo.pojo;

public class PicoActividad {
    private String fecha;
    private int cantidadPedidos;
    private double ingresosTotales;

    public PicoActividad(String fecha, int cantidadPedidos, double ingresosTotales) {
        this.fecha = fecha;
        this.cantidadPedidos = cantidadPedidos;
        this.ingresosTotales = ingresosTotales;
    }

    public String getFecha() { return fecha; }
    public int getCantidadPedidos() { return cantidadPedidos; }
    public double getIngresosTotales() { return ingresosTotales; }
}