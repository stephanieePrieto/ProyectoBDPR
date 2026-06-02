package com.mycompany.restaurante.modelo.pojo;

/**
 * Clase de modelo (POJO) que representa a un usuario del sistema (empleado).
 * Encapsula las credenciales de acceso y la información de perfil, además de 
 * vincular al usuario con su rol operativo para el control de permisos.
 * * @author Ricardo, Diego, Angel, Stephy
 */
public class Usuario {
    
    private int id;
    private String nombre;
    private String username;
    private String password;
    private int idRol;
    private String rol;

    /**
     * Constructor vacío por defecto.
     */
    public Usuario() {}

    /**
     * Constructor simplificado para validación de login.
     * * @param username Nombre de usuario.
     * @param password Contraseña.
     * @param rol Nombre del rol asociado.
     */
    public Usuario(String username, String password, String rol) {
        this.username = username;
        this.password = password;
        this.rol = rol;
    }

    /**
     * Constructor completo para instanciar un usuario con todos sus privilegios.
     * * @param id Identificador único del empleado.
     * @param nombre Nombre completo.
     * @param username Nombre de usuario para login.
     * @param password Contraseña.
     * @param idRol Identificador numérico del rol.
     * @param rol Nombre legible del rol (Ej. "Mesero", "Admin").
     */
    public Usuario(int id, String nombre, String username, String password, int idRol, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.username = username;
        this.password = password;
        this.idRol = idRol;
        this.rol = rol;
    }

    // --- MÉTODOS ACCESORES Y MUTADORES ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public int getIdRol() { return idRol; }
    public void setIdRol(int idRol) { this.idRol = idRol; }
    
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    
    public String getNombreRol() { return rol; }
    public void setNombreRol(String rol) { this.rol = rol; }
}