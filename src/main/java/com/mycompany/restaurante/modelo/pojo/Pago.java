package com.mycompany.restaurante.modelo.pojo;

/**
 * Clase de modelo (POJO) que representa una transacción de pago realizada.
 * Esta entidad encapsula la información necesaria para registrar el cobro de una comanda,
 * vinculando el monto total, el método de pago empleado y la referencia al pedido original.
 * * @author Ricardo, Diego, Angel, Stephy
 */
public class Pago {
    
    private double total;
    private String metodo;
    private int idPedido;
    private int idPago;       
    private int idMesa;
    private double monto;

    /**
     * Constructor vacío por defecto.
     */
    public Pago() {
    }

    // --- MÉTODOS ACCESORES Y MUTADORES ---

    public double getTotal() { 
        return total; 
    }
    
    public void setTotal(double total) { 
        this.total = total; 
    }

    public String getMetodo() { 
        return metodo; 
    }
    
    public void setMetodo(String metodo) { 
        this.metodo = metodo; 
    }

    public int getIdPedido() { 
        return idPedido; 
    }
    
    public void setIdPedido(int idPedido) { 
        this.idPedido = idPedido; 
    }

    public int getIdPago() {
        return idPago;
    }

    public void setIdPago(int idPago) {
        this.idPago = idPago;
    }

    public int getIdMesa() {
        return idMesa;
    }

    public void setIdMesa(int idMesa) {
        this.idMesa = idMesa;
    }

    public double getMonto() {
        return monto;
    }

    public void setMonto(double monto) {
        this.monto = monto;
    }
}