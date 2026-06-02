package com.mycompany.restaurante.modelo.pojo;

/**
 * Plain Old Java Object (POJO) unificado que representa la entidad Platillo.
 * Encapsula las propiedades de los artículos del menú, mapeando columnas 
 * del esquema relacional y variables auxiliares para interfaces de JavaFX.
 * * @author Ricardo, Diego, Angel, Stephy
 */
public class Platillo {
    
    private int idPlatillo;
    private String nombre;
    private String descripcion;
    private double precio;
    private String categoria;
    private int idCategoria; 
    private String imagen; 
    private boolean esBebida;
    private boolean disponibilidad;
    private int cantidad;
    private String estadoPlatillo;

    /**
     * Constructor predeterminado vacío.
     */
    public Platillo() {
    }

    /**
     * Constructor parametrizado completo para preservar compatibilidad.
     * @param idPlatillo ID único autoincremental.
     * @param nombre Nombre comercial del producto.
     * @param descripcion Detalle de ingredientes o receta.
     * @param precio Costo unitario en valor decimal.
     * @param categoria Identificador textual de la categoría.
     * @param imagen Ruta o nombre físico del archivo de imagen.
     * @param esBebida Clasificador booleano para líquidos.
     * @param disponibilidad Bandera de control de existencias.
     * @param idCategoria ID numérico clave de la categoría en la BD.
     */
    public Platillo(int idPlatillo, String nombre, String descripcion, 
                    double precio, String categoria, String imagen, 
                    boolean esBebida, boolean disponibilidad, int idCategoria) {
        this.idPlatillo = idPlatillo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.imagen = imagen;
        this.esBebida = esBebida;
        this.disponibilidad = disponibilidad;
        this.idCategoria = idCategoria;
        this.estadoPlatillo = "Normal"; 
    }

    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public int getIdPlatillo() { return idPlatillo; }
    public void setIdPlatillo(int idPlatillo) { this.idPlatillo = idPlatillo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public boolean isEsBebida() { return esBebida; }
    public void setEsBebida(boolean esBebida) { this.esBebida = esBebida; }

    public boolean isDisponibilidad() { return disponibilidad; }
    public void setDisponibilidad(boolean disponibilidad) { this.disponibilidad = disponibilidad; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    
    public String getEstadoPlatillo() { return estadoPlatillo; }
    public void setEstadoPlatillo(String estadoPlatillo) { this.estadoPlatillo = estadoPlatillo; }

    /**
     * Devuelve una representación en texto del objeto Platillo.
     * @return Cadena formateada de diagnóstico con los atributos clave.
     */
    @Override
    public String toString() {
        return this.nombre;
    }
    
    private int idInsumoClave;
    private int stockDisponible; // Guardará cuánto queda en el almacén temporalmente

    // Agrega sus respectivos Getters y Setters:
    public int getIdInsumoClave() { return idInsumoClave; }
    public void setIdInsumoClave(int idInsumoClave) { this.idInsumoClave = idInsumoClave; }

    public int getStockDisponible() { return stockDisponible; }
    public void setStockDisponible(int stockDisponible) { this.stockDisponible = stockDisponible; }
   
    
}