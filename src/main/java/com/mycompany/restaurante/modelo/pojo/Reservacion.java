package com.mycompany.restaurante.modelo.pojo;

/**
 * Plain Old Java Object (POJO) que representa la entidad Reservación.
 * Encapsula los atributos de un apartado de mesa en el restaurante, proveyendo
 * constructores y métodos accesores requeridos por JavaFX.
 * * @author Ricardo, Diego, Angel, Stephy
 */
public class Reservacion {
    private int idReservacion;
    private String folioUnico;
    private String idCliente;
    private String nombreCliente; 
    private int idMesa;
    private String fecha;
    private String hora;
    private int numPersonas;
    private String estado;

    /**
     * Constructor parametrizado de la clase Reservación.
     * Mapea de forma directa las propiedades lógicas y campos de texto.
     * @param idReservacion Identificador numérico secuencial en MySQL.
     * @param folioUnico Código de barra alfanumérico generado de 8 dígitos.
     * @param idCliente Clave alfanumérica foránea del registro del cliente.
     * @param nombreCliente Nombre completo del comensal.
     * @param idMesa Número de locación asignada en la sala.
     * @param fecha Cadena de texto representativa del día apartado.
     * @param hora Cadena de texto representativa del horario electo.
     * @param numPersonas Cantidad total de comensales.
     * @param estado Bandera contextual ("Confirmada", "Cancelada").
     */
    public Reservacion(int idReservacion, String folioUnico, String idCliente, 
                       String nombreCliente, int idMesa, String fecha, 
                       String hora, int numPersonas, String estado) {
        this.idReservacion = idReservacion;
        this.folioUnico = folioUnico;
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
        this.idMesa = idMesa;
        this.fecha = fecha;
        this.hora = hora;
        this.numPersonas = numPersonas;
        this.estado = estado;
    }

    /**
     * Obtiene la clave primaria incremental de la reservación.
     * @return El ID de la reservación.
     */
    public int getIdReservacion() { return idReservacion; }

    /**
     * Obtiene el código de folio único.
     * @return Cadena alfanumérica del folio.
     */
    public String getFolioUnico() { return folioUnico; }

    /**
     * Obtiene el identificador de cliente del sistema.
     * @return Clave alfanumérica del cliente.
     */
    public String getIdCliente() { return idCliente; }

    /**
     * Obtiene el nombre real completo del cliente.
     * @return Cadena con el nombre comercial del cliente.
     */
    public String getNombreCliente() { return nombreCliente; }

    /**
     * Obtiene la locación física asignada.
     * @return Número entero de la mesa.
     */
    public int getIdMesa() { return idMesa; }

    /**
     * Obtiene el día asignado para la cita.
     * @return Fecha estructurada en texto.
     */
    public String getFecha() { return fecha; }

    /**
     * Obtiene el bloque de tiempo pactado.
     * @return Horario estructurado en texto.
     */
    public String getHora() { return hora; }

    /**
     * Obtiene la cantidad de comensales estipulados para asistir.
     * @return Volumen numérico de personas.
     */
    public int getNumPersonas() { return numPersonas; }

    /**
     * Obtiene la bandera contextual del registro.
     * @return Estado operativo de la reservación.
     */
    public String getEstado() { return estado; }
}