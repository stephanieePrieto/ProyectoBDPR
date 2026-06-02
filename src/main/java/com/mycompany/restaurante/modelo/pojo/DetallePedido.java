package com.mycompany.restaurante.modelo.pojo;

/**
 * Clase de modelo (POJO) que representa una línea de detalle dentro de un pedido.
 * Actúa como contenedor de datos para mostrar los platillos individuales, sus 
 * cantidades y el costo calculado dentro de las interfaces de facturación o revisión de comandas.
 * * @author Ricardo, Diego, Angel, Stephy
 */
public class DetallePedido {
    private String platillo;
    private int cantidad;
    private double precio;
    private double subtotal;

    /**
     * Constructor completo para inicializar un detalle de pedido.
     * * @param platillo Nombre comercial del producto solicitado.
     * @param cantidad Número de unidades requeridas.
     * @param precio Precio unitario del producto.
     * @param subtotal Monto calculado (cantidad * precio).
     */
    public DetallePedido(String platillo, int cantidad, double precio, double subtotal) {
        this.platillo = platillo;
        this.cantidad = cantidad;
        this.precio = precio;
        this.subtotal = subtotal;
    }

    // --- MÉTODOS ACCESORES ---
    // Nota: Estos métodos son requeridos por las TableColumn para el binding de datos en la UI.

    public String getPlatillo() { 
        return platillo; 
    }

    public int getCantidad() { 
        return cantidad; 
    }

    public double getPrecio() { 
        return precio; 
    }

    public double getSubtotal() { 
        return subtotal; 
    }
}