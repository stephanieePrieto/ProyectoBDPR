package com.mycompany.restaurante.modelo.pojo;

/**
 * Clase de modelo (POJO) que representa el desglose de productos para facturación.
 * Esta entidad está estructurada para cumplir con los estándares de CFDI, 
 * incluyendo claves de catálogo de productos/servicios y unidades de medida (SAT).
 * * @author Ricardo, Diego, Angel, Stephy
 */
public class DetalleFactura {
    
    private String claveProdServ;
    private int cantidad;
    private String unidad;
    private String platillo;
    private double precioUnitario;
    private double total;

    /**
     * Constructor vacío.
     */
    public DetalleFactura() {
    }

    /**
     * Constructor completo para inicializar el detalle de un renglón de factura.
     * * @param claveProdServ Clave del catálogo SAT de productos y servicios.
     * @param cantidad Número de unidades consumidas.
     * @param unidad Clave de la unidad de medida (Ej. "E48" para servicio).
     * @param platillo Nombre comercial del producto.
     * @param precioUnitario Precio individual.
     * @param total Monto calculado para la fila (precio * cantidad).
     */
    public DetalleFactura(String claveProdServ, int cantidad, String unidad, String platillo, double precioUnitario, double total) {
        this.claveProdServ = claveProdServ;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.platillo = platillo;
        this.precioUnitario = precioUnitario;
        this.total = total;
    }

    // --- MÉTODOS ACCESORES Y MUTADORES ---

    public String getClaveProdServ() { 
        return claveProdServ; 
    }
    
    public void setClaveProdServ(String claveProdServ) { 
        this.claveProdServ = claveProdServ; 
    }

    public int getCantidad() { 
        return cantidad; 
    }
    
    // Método auxiliar para compatibilidad con frameworks de UI
    public int getQuantity() { 
        return cantidad; 
    }

    public void setCantidad(int cantidad) { 
        this.cantidad = cantidad; 
    }

    public String getUnidad() { 
        return unidad; 
    }
    
    public void setUnidad(String unidad) { 
        this.unidad = unidad; 
    }

    public String getPlatillo() { 
        return platillo; 
    }
    
    public void setPlatillo(String platillo) { 
        this.platillo = platillo; 
    }

    public double getPrecioUnitario() { 
        return precioUnitario; 
    }
    
    public void setPrecioUnitario(double precioUnitario) { 
        this.precioUnitario = precioUnitario; 
    }

    public double getTotal() { 
        return total; 
    }
    
    public void setTotal(double total) { 
        this.total = total; 
    }
}