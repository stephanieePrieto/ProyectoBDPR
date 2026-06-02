package com.mycompany.restaurante.modelo.pojo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Plain Old Java Object (POJO) que representa la entidad Pedido.
 * Almacena los atributos primordiales de una comanda mapeada con MySQL,
 * incluyendo auxiliares estructurados para renderizar celdas en las vistas.
 * * @author Ricardo, Diego, Angel, Stephy
 */
public class Pedido {

    /** Identificador único auto-incremental del pedido. */
    private int idPedido; 
    
    /** Estado operativo del pedido (e.g., "Pendiente", "Listo", "Cancelado"). */
    private String estado;
    
    /** Estampa cronológica de registro del pedido. */
    private LocalDateTime fechaHora; 
    
    /** Identificador de la mesa asociada a la comanda. */
    private int idMesa;
    
    /** Identificador del empleado responsable del pedido. */
    private int idEmpleado; 
    
    /** Colección de platillos que conforman el pedido. */
    private List<Platillo> listaPlatillos;
    
    /** Monto total acumulado de la comanda. */
    private double total;
    
    /** Resumen consolidado de platillos en formato de texto para visualización en tablas. */
    private String detalleTexto; 

    /**
     * Constructor predeterminado de la clase Pedido.
     */
    public Pedido() {}

    /**
     * Obtiene la clave primaria del pedido.
     * @return Clave numérica incremental.
     */
    public int getIdPedido() { return idPedido; }

    /**
     * Establece la clave primaria del pedido.
     * @param idPedido Nueva clave numérica a asignar.
     */
    public void setIdPedido(int idPedido) { this.idPedido = idPedido; }

    /**
     * Obtiene el estado actual del flujo del pedido.
     * @return Cadena descriptiva del estado.
     */
    public String getEstado() { return estado; }

    /**
     * Establece el estado actual del flujo del pedido.
     * @param estado Nuevo string de estado operativo.
     */
    public void setEstado(String estado) { this.estado = estado; }

    /**
     * Obtiene la estampa cronológica de registro del pedido.
     * @return Objeto LocalDateTime con la marca temporal.
     */
    public LocalDateTime getFechaHora() { return fechaHora; }

    /**
     * Establece la estampa cronológica de registro del pedido.
     * @param fechaHora Nuevo objeto temporal a asignar.
     */
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    /**
     * Obtiene el resumen consolidado de alimentos en formato plano.
     * @return Cadena formateada idónea para tablas visuales.
     */
    public String getDetalleTexto() { return detalleTexto; }

    /**
     * Establece el resumen consolidado de alimentos en formato plano.
     * @param detalleTexto Cadena descriptiva para celdas FX.
     */
    public void setDetalleTexto(String detalleTexto) { this.detalleTexto = detalleTexto; }

    /**
     * Obtiene el ID numérico de la locación vinculada.
     * @return Identificador de la mesa.
     */
    public int getIdMesa() { return idMesa; }

    /**
     * Establece el ID numérico de la locación vinculada.
     * @param idMesa Nuevo identificador de mesa a asignar.
     */
    public void setIdMesa(int idMesa) { this.idMesa = idMesa; }

    /**
     * Obtiene el ID del mesero responsable de la cuenta.
     * @return Identificador único del empleado.
     */
    public int getIdEmpleado() { return idEmpleado; }

    /**
     * Establece el ID del mesero responsable de la cuenta.
     * @param idEmpleado Nuevo identificador único de empleado.
     */
    public void setIdEmpleado(int idEmpleado) { this.idEmpleado = idEmpleado; }

    /**
     * Obtiene la colección completa de objetos platillo asociados.
     * @return Lista observable o genérica de alimentos.
     */
    public List<Platillo> getListaPlatillos() { return listaPlatillos; }

    /**
     * Establece la colección completa de objetos platillo asociados.
     * @param listaPlatillos Nueva lista conteniendo el desglose alimenticio.
     */
    public void setListaPlatillos(List<Platillo> listaPlatillos) { this.listaPlatillos = listaPlatillos; }

    /**
     * Obtiene el importe acumulado sin impuestos añadidos.
     * @return Monto neto en valor double.
     */
    public double getTotal() { return total; }

    /**
     * Establece el importe acumulado sin impuestos añadidos.
     * @param total Nuevo valor numérico double para el neto.
     */
    public void setTotal(double total) { this.total = total; }
}