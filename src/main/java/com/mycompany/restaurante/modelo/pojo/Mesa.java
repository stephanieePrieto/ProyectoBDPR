package com.mycompany.restaurante.modelo.pojo;

/**
 * Clase de modelo (POJO) que representa una mesa física dentro del restaurante.
 * Esta entidad gestiona el estado operativo de la mesa y almacena una representación 
 * textual de los pedidos activos asociados, facilitando su visualización en el dashboard.
 * * @author Ricardo, Diego, Angel, Stephy
 */
public class Mesa {
    
    private int idMesa;
    private String estado;
    private String detalles;

    /**
     * Constructor vacío por defecto.
     */
    public Mesa() {
    }

    /**
     * Constructor para inicializar la mesa con su identificador y estado actual.
     * * @param idMesa Identificador físico de la mesa.
     * @param estado Estado de la mesa (Ej. "Libre", "Ocupada").
     */
    public Mesa(int idMesa, String estado) {
        this.idMesa = idMesa;
        this.estado = estado;
    }

    // --- MÉTODOS ACCESORES Y MUTADORES ---

    public int getIdMesa() {
        return idMesa;
    }

    public void setIdMesa(int idMesa) {
        this.idMesa = idMesa;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    /**
     * Obtiene el desglose de los platillos consumidos en esta mesa.
     * * @return Cadena formateada con los platillos y cantidades actuales.
     */
    public String getDetalles() {
        return detalles;
    }

    /**
     * Asigna la cadena de detalles de consumo. 
     * Este valor suele ser poblado por el MesaDAO mediante una consulta de JOIN.
     * * @param detalles Cadena descriptiva de los platillos en la mesa.
     */
    public void setDetalles(String detalles) {
        this.detalles = detalles;
    }
}