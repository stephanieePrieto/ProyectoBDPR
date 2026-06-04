package com.mycompany.restaurante.controller;

import com.mycompany.restaurante.modelo.sql.OracleConnect; // Conexión a Oracle Cloud
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 * Controlador de componente autónomo correspondiente a la Tarjeta de Pedido.
 * Actúa como un fragmento gráfico (Widget) inyectado dinámicamente en el 
 * Sistema de Visualización de Cocina (KDS).
 * Migrado a arquitectura Oracle Cloud, soportando manipulación de tipos de datos de objeto.
 */
public class TarjetaPedidoController {

    // --- Componentes Gráficos de la Tarjeta ---
    @FXML private Label lblMesa;
    @FXML private Label lblHora;
    @FXML private TextArea txtContenido;
    @FXML private Button btnListo;

    // --- Estado Interno y Referencias ---
    private int idPedido;
    
    // Referencia al controlador principal del Chef para solicitar recargas de la UI
    private ChefController pantallaPadre;

    /**
     * Acopla la información del pedido sobre los controles visuales de la tarjeta
     * en el momento de su instanciación en memoria.
     * @param idPedido Identificador de la comanda en la base de datos.
     * @param idMesa Número físico de la locación que solicita el consumo.
     * @param hora Cadena pre-formateada con la hora de captura del registro.
     * @param textoPlatillos Bloque textual consolidado con el desglose de alimentos.
     * @param padre Instancia de referencia del controlador principal (ChefController) para callbacks.
     */
    public void configurarTarjeta(int idPedido, int idMesa, String hora, 
            String textoPlatillos, ChefController padre) {
        this.idPedido = idPedido;
        this.pantallaPadre = padre; 

        this.lblMesa.setText("MESA " + idMesa);
        this.lblHora.setText("Pedido #" + idPedido + " - " + hora);
        this.txtContenido.setText(textoPlatillos);
    }

    /**
     * Marca el pedido como "Listo" y actualiza la base de datos Oracle.
     * Este método implementa lógica Objeto-Relacional avanzada, extrayendo una 
     * estructura anidada (STRUCT) para preservar la integridad de la fecha original 
     * al momento de alterar únicamente el estado de la auditoría.
     * @param event Evento de acción disparado por el botón "Listo".
     */
    @FXML
    void clicDespachar(ActionEvent event) {
        // 1. SELECT: Obtenemos el objeto 'info' completo.
        // Se utiliza el alias 'p' por normativas estrictas del parser Objeto-Relacional de Oracle.
        String sqlSelect = "SELECT p.info FROM pedidos p WHERE p.idPedido = ?";
        
        // 2. UPDATE: Reconstruye el objeto invocando su constructor nativo en PL/SQL.
        String sqlUpdate = "UPDATE pedidos p SET p.info = auditoria_pedido_typ(?, ?) WHERE p.idPedido = ?";
        
        try (Connection con = OracleConnect.getConexion()) {
            java.sql.Timestamp fechaOriginal = null;
            
            // FASE DE EXTRACCIÓN (READ): 
            // Obtenemos el objeto info para no perder la fecha original en la que el mesero tomó el pedido.
            try (PreparedStatement psSel = con.prepareStatement(sqlSelect)) {
                psSel.setInt(1, idPedido);
                try (ResultSet rs = psSel.executeQuery()) {
                    if (rs.next()) {
                        /* * MAGIA JDBC AVANZADA: 
                         * Cast explícito a java.sql.Struct para interpretar un Objeto propio de Oracle.
                         * Se desglosan sus atributos internos a un arreglo de objetos.
                         */
                        java.sql.Struct struct = (java.sql.Struct) rs.getObject(1); // <--- Índice 1 (Columna info)
                        Object[] attrs = struct.getAttributes();
                        fechaOriginal = (java.sql.Timestamp) attrs[0]; // <--- Índice 0 (Propiedad FECHA_HORA)
                    }
                }
            }
            
            // FASE DE ACTUALIZACIÓN (WRITE):
            // Si la extracción fue exitosa, reescribimos la base de datos con el nuevo estado.
            if (fechaOriginal != null) {
                try (PreparedStatement psUp = con.prepareStatement(sqlUpdate)) {
                    psUp.setTimestamp(1, fechaOriginal); // Mantenemos la fecha inalterada
                    psUp.setString(2, "Listo");          // Alteramos el estado
                    psUp.setInt(3, idPedido);
                    psUp.executeUpdate();
                }
            }
            
            // Callback: Le solicita al controlador del Dashboard del Chef que vuelva a consultar 
            // la base de datos. Como esta tarjeta ya no es "Pendiente", desaparecerá de la pantalla mágicamente.
            if (pantallaPadre != null) {
                pantallaPadre.cargarComandasActivas();
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error al despachar pedido de KDS a BD: " + e.getMessage());
            e.printStackTrace();
        }
    }
}