package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.pojo.Asistencia;
import com.mycompany.restaurante.utils.ConexionBD;
import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;

/**
 * Clase de Acceso a Datos (DAO) para la gestión de asistencia del personal.
 * Sostiene las operaciones de persistencia en MySQL para registrar marcas de 
 * entrada, actualizaciones de salida y recopilación del historial diario.
 * * @author Ricardo, Diego, Angel, Stephy
 */
public class AsistenciaDAO {
    
    public AsistenciaDAO() {}

    /**
     * Recupera el listado completo de asistencias registradas hoy.
     * @return Una lista observable cargada con objetos Asistencia.
     */
    public ObservableList<Asistencia> obtenerAsistenciasHoy() {
        ObservableList<Asistencia> lista = FXCollections.observableArrayList();
        String sql = "SELECT a.idAsistencia, e.usuario AS username, "
                + "a.fechaEntrada, a.fechaSalida, a.estado, a.horas_trabajadas "
                + "FROM asistencias a "
                + "INNER JOIN empleados e ON a.idEmpleado = e.idEmpleado "
                + "WHERE DATE(a.fechaEntrada) = CURDATE() "
                + "ORDER BY a.fechaEntrada DESC";
                     
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                String fSalida = rs.getString("fechaSalida") == null 
                        ? "Pendiente" : rs.getString("fechaSalida");
                String est = rs.getString("estado") == null 
                        ? "En turno" : rs.getString("estado");
                String hTrab = rs.getString("horas_trabajadas") == null 
                        ? "00:00 hrs" : rs.getString("horas_trabajadas");

                lista.add(new Asistencia(
                    rs.getInt("idAsistencia"),
                    rs.getString("username"),
                    rs.getString("fechaEntrada"),
                    fSalida,
                    est,
                    hTrab
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error en obtenerAsistenciasHoy: " 
                    + e.getMessage());
        }
        return lista;
    }

    /**
     * Procesa de forma transaccional el registro de entrada o salida.
     * Evalúa las horas totales trabajadas: si el cómputo final es menor 
     * a 8 horas, marca el registro automáticamente como "Incompleto",
     * de lo contrario, le asigna el estado "Cumplió".
     * * @param username Nombre de usuario del empleado.
     * @param nombreRol Nombre del rol seleccionado.
     * @param horaEntrada Hora de inicio del turno (HH:mm).
     * @param horaSalida Hora de cierre del turno (HH:mm).
     * @param horasTexto Cómputo final de horas (Formato: "HH:mm hrs").
     * @return true si la persistencia fue exitosa; false si no.
     */
    public boolean procesarAsistenciaCompleta(String username, String nombreRol, 
            String horaEntrada, String horaSalida, String horasTexto) {
        String fechaHoy = LocalDate.now().toString();
        String estadoFinal = "En turno";

        // LÓGICA DE AUDITORÍA DE JORNADA REGLAMENTARIA (8 HORAS)
        if (horaSalida != null && horasTexto != null && !horasTexto.isEmpty()) {
            try {
                // Extraemos los dos primeros caracteres del formato "HH:mm hrs"
                int horasNumericas = Integer.parseInt(horasTexto.substring(0, 2));
                
                if (horasNumericas >= 8) {
                    estadoFinal = "Cumplió";
                } else {
                    estadoFinal = "Incompleto";
                }
            } catch (Exception e) {
                estadoFinal = "Finalizado"; // Escudo por si el texto viene mal
            }
        }

        try (Connection con = ConexionBD.conectar()) {
            // 1. Validar existencia unificando la tabla 'empleados' y 'rol'
            String sqlValidar = "SELECT e.idEmpleado FROM empleados e "
                    + "INNER JOIN rol r ON e.idRol = r.idRol "
                    + "WHERE e.usuario = ? AND r.nombre = ?";
                    
            int idEmpleado = -1;
            try (PreparedStatement psVal = con.prepareStatement(sqlValidar)) {
                psVal.setString(1, username);
                psVal.setString(2, nombreRol);
                try (ResultSet rsVal = psVal.executeQuery()) {
                    if (rsVal.next()) {
                        idEmpleado = rsVal.getInt("idEmpleado");
                    } else {
                        return false; 
                    }
                }
            }

            // 2. Comprobar si ya fichó entrada el día de hoy
            String sqlCheck = "SELECT idAsistencia FROM asistencias WHERE "
                    + "idEmpleado = ? AND DATE(fechaEntrada) = CURDATE()";
            int idExistente = -1;
            try (PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {
                psCheck.setInt(1, idEmpleado);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) idExistente = rs.getInt("idAsistencia");
                }
            }

            // 3. Ejecutar la acción transaccional (INSERT o UPDATE)
            if (idExistente != -1) {
                String sqlUpdate = "UPDATE asistencias SET fechaSalida = ?, "
                        + "estado = ?, horas_trabajadas = ? "
                        + "WHERE idAsistencia = ?";
                try (PreparedStatement psUp = con.prepareStatement(sqlUpdate)) {
                    psUp.setString(1, fechaHoy + " " + horaSalida + ":00");
                    psUp.setString(2, estadoFinal); // "Cumplió" o "Incompleto"
                    psUp.setString(3, horasTexto);
                    psUp.setInt(4, idExistente);
                    return psUp.executeUpdate() > 0;
                }
            } else {
                String sqlInsert = "INSERT INTO asistencias (idEmpleado, "
                        + "fechaEntrada, fechaSalida, estado, horas_trabajadas) "
                        + "VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement psIn = con.prepareStatement(sqlInsert)) {
                    psIn.setInt(1, idEmpleado);
                    psIn.setString(2, fechaHoy + " " 
                            + (horaEntrada != null ? horaEntrada : "00:00") 
                            + ":00");
                    
                    psIn.setString(3,(horaSalida != null )
                            ? fechaHoy + " " + horaSalida + ":00" : null);
                    psIn.setString(4, estadoFinal);
                    psIn.setString(5, horasTexto);
                    return psIn.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en AsistenciaDAO: " + e.getMessage());
            return false;
        }
    }
}