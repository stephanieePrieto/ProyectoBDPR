package com.mycompany.restaurante.modelo.pojo;

/**
 * Clase de modelo (POJO) para la generación de reportes financieros.
 * Agrupa los datos de ventas por periodo, facilitando la visualización de 
 * ingresos acumulados en las herramientas de analítica del sistema.
 * * @author Ricardo, Diego, Angel, Stephy
 */
public class VentaReporte {
    
    private String fecha;
    private double total;

    /**
     * Constructor completo para inicializar un registro de reporte de ventas.
     * * @param fecha La fecha o periodo del reporte (Ej. "2026-05-31").
     * @param total La suma total de ingresos registrados en dicho periodo.
     */
    public VentaReporte(String fecha, double total) {
        this.fecha = fecha;
        this.total = total;
    }

    // --- MÉTODOS ACCESORES Y MUTADORES ---

    /**
     * Obtiene la fecha del registro de ventas.
     * * @return La cadena representando la fecha.
     */
    public String getFecha() { 
        return fecha; 
    }
    
    public void setFecha(String fecha) { 
        this.fecha = fecha; 
    }

    /**
     * Obtiene el ingreso total acumulado para la fecha especificada.
     * * @return El monto total de las ventas.
     */
    public double getTotal() { 
        return total; 
    }
    
    public void setTotal(double total) { 
        this.total = total; 
    }
}