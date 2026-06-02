package com.mycompany.restaurante.modelo.pojo;

/**
 * Clase de modelo (POJO) que representa una cuenta de consumo.
 * Gestiona el estado financiero de una orden, permitiendo distinguir entre 
 * cuentas pendientes de pago y cuentas liquidadas.
 * * @author Ricardo, Diego, Angel, Stephy
 */
public class Cuenta {
    
    private int idCuenta;
    private double total;
    private String estado; // Pendiente / Pagada

    /**
     * Constructor vacío por defecto.
     */
    public Cuenta() {
    }

    /**
     * Constructor completo para inicializar una cuenta.
     * * @param idCuenta Identificador único de la cuenta.
     * @param total Monto acumulado de los consumos.
     * @param estado Estado actual de la cuenta ("Pendiente" o "Pagada").
     */
    public Cuenta(int idCuenta, double total, String estado) {
        this.idCuenta = idCuenta;
        this.total = total;
        this.estado = estado;
    }

    // --- MÉTODOS ACCESORES Y MUTADORES ---

    public int getIdCuenta() {
        return idCuenta;
    }

    public void setIdCuenta(int idCuenta) {
        this.idCuenta = idCuenta;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}