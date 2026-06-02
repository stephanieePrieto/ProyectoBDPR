package com.mycompany.restaurante.modelo.pojo;

/**
 * Clase de modelo (POJO) que representa un insumo o producto en el inventario.
 * Gestiona las existencias físicas, las unidades de medida correspondientes 
 * y los umbrales de seguridad para alertas de reabastecimiento.
 * * @author Ricardo, Diego, Angel, Stephy
 */
public class ProductoAlmacen {
    private int idProducto;
    private String nombre;
    private double cantidad;
    private String unidad;
    private double stockMinimo;

    /**
     * Constructor vacío por defecto.
     */
    public ProductoAlmacen() {}

    /**
     * Constructor completo para inicializar un insumo de inventario.
     * * @param idProducto Identificador único del producto.
     * @param nombre Nombre descriptivo del insumo.
     * @param cantidad Existencia actual en almacén.
     * @param unidad Unidad de medida (Ej. "kg", "litros", "piezas").
     * @param stockMinimo Umbral crítico para alertas de inventario.
     */
    public ProductoAlmacen(int idProducto, String nombre, double cantidad, String unidad, double stockMinimo) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.stockMinimo = stockMinimo;
    }

    // --- MÉTODOS ACCESORES Y MUTADORES ---

    public int getIdProducto() { 
        return idProducto; 
    }
    
    public void setIdProducto(int idProducto) { 
        this.idProducto = idProducto; 
    }

    public String getNombre() { 
        return nombre; 
    }
    
    public void setNombre(String nombre) { 
        this.nombre = nombre; 
    }

    public double getCantidad() { 
        return cantidad; 
    }
    
    public void setCantidad(double cantidad) { 
        this.cantidad = cantidad; 
    }

    public String getUnidad() { 
        return unidad; 
    }
    
    public void setUnidad(String unidad) { 
        this.unidad = unidad; 
    }

    public double getStockMinimo() { 
        return stockMinimo; 
    }
    
    public void setStockMinimo(double stockMinimo) { 
        this.stockMinimo = stockMinimo; 
    }
}