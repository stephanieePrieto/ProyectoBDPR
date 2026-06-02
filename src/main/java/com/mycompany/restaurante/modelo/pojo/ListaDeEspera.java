package com.mycompany.restaurante.modelo.pojo;

/**
 * Clase de modelo (POJO) que representa un registro en la lista de espera del restaurante.
 * Almacena la información de contacto y el estatus de los comensales que aguardan 
 * por una mesa disponible, permitiendo su correcta gestión administrativa.
 * * @author Ricardo, Diego, Angel, Stephy
 */
public class ListaDeEspera {

    private int idEspera;
    private String nombreCliente;
    private int pax; // Número de personas (Pax)
    private String telefono;
    private String horaLlegada;
    private String estado;

    /**
     * Constructor vacío.
     */
    public ListaDeEspera() {
    }

    /**
     * Constructor completo para inicializar un nuevo registro de espera.
     * * @param idEspera Identificador único.
     * @param nombreCliente Nombre del responsable.
     * @param pax Cantidad de personas.
     * @param telefono Contacto telefónico.
     * @param horaLlegada Registro de tiempo de entrada a la lista.
     * @param estado Estatus actual (Ej. "EN_ESPERA", "ATENDIDO").
     */
    public ListaDeEspera(int idEspera, String nombreCliente, int pax,
            String telefono, String horaLlegada, String estado) {
        this.idEspera = idEspera;
        this.nombreCliente = nombreCliente;
        this.pax = pax;
        this.telefono = telefono;
        this.horaLlegada = horaLlegada;
        this.estado = estado;
    }
    
    // --- MÉTODOS ACCESORES Y MUTADORES ---

    public int getIdEspera() {
        return idEspera;
    }

    public void setIdEspera(int idEspera) {
        this.idEspera = idEspera;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }
    
    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }
    
    public int getPax() {
        return pax;
    }

    public void setPax(int pax) {
        this.pax = pax;
    }

    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getHoraLlegada() {
        return horaLlegada;
    }

    public void setHoraLlegada(String horaLlegada) {
        this.horaLlegada = horaLlegada;
    }

    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
}