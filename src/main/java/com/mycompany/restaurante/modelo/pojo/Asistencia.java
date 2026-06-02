package com.mycompany.restaurante.modelo.pojo;

/**
 * Clase Plain Old Java Object (POJO) que representa la entidad Asistencia.
 * Encapsula de forma atómica los atributos correspondientes a la jornada de un 
 * trabajador, proveyendo los métodos constructores, accesores y mutadores requeridos.
 * * @author Ricardo, Diego, Angel, Stephy
 */
public class Asistencia {
    
    /** Identificador único auto-incremental de la asistencia en MySQL. */
    private int id; 
    
    /** Nombre de usuario correspondiente al empleado vinculado. */
    private String usuario;
    
    /** Estampa de tiempo formal que define la apertura del turno. */
    private String entrada;
    
    /** Estampa de tiempo formal que define el cierre de la jornada laboral. */
    private String salida;
    
    /** Estado contextual descriptivo de la asistencia ("En turno", "Finalizado", "Cumplió"). */
    private String estado;
    
    /** Cómputo totalizado de horas laboradas formateado en texto. */
    private String horasTrabajadas;

    /**
     * Construye una instancia parametrizada de la clase Asistencia.
     * Diseñado principalmente para mapear y estructurar las celdas dinámicas de la TableView.
     * @param id Clave primaria numérica de la fila.
     * @param usuario Identificador de cuenta del personal staff.
     * @param entrada Fecha e inicio del turno.
     * @param salida Fecha y finalización de actividades.
     * @param estado Bandera de control de estado operativo.
     * @param horasTrabajadas Texto descriptivo con la sumatoria del tiempo cubierto.
     */
    public Asistencia(int id, String usuario, String entrada, String salida, 
            String estado, String horasTrabajadas) {
        this.id = id;
        this.usuario = usuario;
        this.entrada = entrada;
        this.salida = salida;
        this.estado = estado;
        this.horasTrabajadas = horasTrabajadas;
    }

    /**
     * Obtiene el identificador único del registro.
     * @return El ID numérico de la asistencia.
     */
    public int getId() {
        return id;
    }

    /**
     * Establece el identificador único del registro.
     * @param id El nuevo ID numérico a asignar.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Obtiene el nombre de usuario asociado al turno.
     * @return El nombre de usuario en cadena de texto.
     */
    public String getUsuario() {
        return usuario;
    }

    /**
     * Establece el nombre de usuario asociado al turno.
     * @param usuario El nuevo nombre de usuario a asignar.
     */
    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    /**
     * Obtiene la marca de tiempo de entrada.
     * @return La fecha y hora de entrada como texto.
     */
    public String getEntrada() {
        return entrada;
    }

    /**
     * Establece la marca de tiempo de entrada.
     * @param entrada El nuevo string de fecha y hora de entrada.
     */
    public void setEntrada(String entrada) {
        this.entrada = entrada;
    }

    /**
     * Obtiene la marca de tiempo de salida.
     * @return La fecha y hora de salida como texto.
     */
    public String getSalida() {
        return salida;
    }

    /**
     * Establece la marca de tiempo de salida.
     * @param salida El nuevo string de fecha y hora de salida.
     */
    public void setSalida(String salida) {
        this.salida = salida;
    }

    /**
     * Obtiene el estado operativo actual de la comanda de asistencia.
     * @return El estado operativo actual.
     */
    public String getEstado() {
        return estado;
    }

    /**
     * Establece el estado operativo actual de la comanda de asistencia.
     * @param estado El nuevo estado operativo a asignar.
     */
    public void setEstado(String estado) {
        this.estado = estado;
    }

    /**
     * Obtiene el acumulado finalizado de horas laboradas.
     * @return Las horas trabajadas formateadas en texto.
     */
    public String getHorasTrabajadas() {
        return horasTrabajadas;
    }

    /**
     * Establece el acumulado finalizado de horas laboradas.
     * @param horasTrabajadas El nuevo string formateado de tiempo laborado.
     */
    public void setHorasTrabajadas(String horasTrabajadas) {
        this.horasTrabajadas = horasTrabajadas;
    }
}