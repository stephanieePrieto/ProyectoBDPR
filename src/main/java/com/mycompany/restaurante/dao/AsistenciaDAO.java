package com.mycompany.restaurante.dao;

import com.mycompany.restaurante.modelo.pojo.Asistencia;
import com.mycompany.restaurante.modelo.sql.OracleConnect;
import java.sql.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;

/**
 * Clase AsistenciaDAO migrada a Oracle Cloud.
 */
public class AsistenciaDAO {
    
    public AsistenciaDAO() {}

    public ObservableList<Asistencia> obtenerAsistenciasHoy() {
        ObservableList<Asistencia> lista = FXCollections.observableArrayList();
        String sql = "SELECT a.idAsistencia, e.usuario AS username, a.tiempo.fecha_entrada AS fechaEntrada, "
                   + "a.tiempo.fecha_salida AS fechaSalida, a.estado, a.horas_trabajadas "
                   + "FROM asistencias a INNER JOIN empleados e ON a.idEmpleado = e.idEmpleado "
                   + "WHERE TRUNC(a.tiempo.fecha_entrada) = TRUNC(SYSDATE) "
                   + "ORDER BY a.tiempo.fecha_entrada DESC";
                   
        try (Connection con = OracleConnect.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                lista.add(new Asistencia(
                    rs.getInt("idAsistencia"), rs.getString("username"),
                    rs.getString("fechaEntrada"), (rs.getString("fechaSalida") == null ? "Pendiente" : rs.getString("fechaSalida")),
                    (rs.getString("estado") == null ? "En turno" : rs.getString("estado")),
                    (rs.getString("horas_trabajadas") == null ? "00:00 hrs" : rs.getString("horas_trabajadas"))
                ));
            }
        } catch (SQLException e) { System.err.println("Error Oracle Asistencias: " + e.getMessage()); }
        return lista;
    }

    public boolean procesarAsistenciaCompleta(String username, String nombreRol, String horaEntrada, String horaSalida, String horasTexto) {
        String estadoFinal = (horaSalida != null && !horasTexto.isEmpty() && Integer.parseInt(horasTexto.substring(0, 2)) >= 8) ? "Cumplió" : "Incompleto";
        
        try (Connection con = OracleConnect.getConexion()) {
            // 1. Validar existencia del empleado
            String sqlValidar = "SELECT e.idEmpleado FROM empleados e JOIN rol r ON e.idRol = r.idRol WHERE e.usuario = ? AND r.nombre = ?";
            int idEmpleado = -1;
            try (PreparedStatement psVal = con.prepareStatement(sqlValidar)) {
                psVal.setString(1, username); psVal.setString(2, nombreRol);
                try (ResultSet rsVal = psVal.executeQuery()) { if (rsVal.next()) idEmpleado = rsVal.getInt("idEmpleado"); else return false; }
            }

            // 2. Verificar si ya tiene registro hoy
            String sqlCheck = "SELECT idAsistencia FROM asistencias WHERE idEmpleado = ? AND TRUNC(tiempo.fecha_entrada) = TRUNC(SYSDATE)";
            int idExistente = -1;
            try (PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {
                psCheck.setInt(1, idEmpleado);
                try (ResultSet rs = psCheck.executeQuery()) { if (rs.next()) idExistente = rs.getInt("idAsistencia"); }
            }

            // 3. Insertar o Actualizar
            if (idExistente != -1) {
                String sqlUpdate = "UPDATE asistencias SET tiempo = periodo_typ(tiempo.fecha_entrada, TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI:SS')), "
                                 + "estado = ?, horas_trabajadas = ? WHERE idAsistencia = ?";
                try (PreparedStatement psUp = con.prepareStatement(sqlUpdate)) {
                    psUp.setString(1, LocalDate.now().toString() + " " + horaSalida + ":00");
                    psUp.setString(2, estadoFinal); psUp.setString(3, horasTexto); psUp.setInt(4, idExistente);
                    return psUp.executeUpdate() > 0;
                }
            } else {
                String sqlInsert = "INSERT INTO asistencias (idEmpleado, tiempo, estado, horas_trabajadas) VALUES (?, periodo_typ(TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI:SS'), NULL), ?, ?)";
                try (PreparedStatement psIn = con.prepareStatement(sqlInsert)) {
                    psIn.setInt(1, idEmpleado);
                    psIn.setString(2, LocalDate.now().toString() + " " + (horaEntrada != null ? horaEntrada : "00:00") + ":00");
                    psIn.setString(3, "En turno"); psIn.setString(4, "00:00 hrs");
                    return psIn.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) { System.err.println("Error en AsistenciaDAO Oracle: " + e.getMessage()); return false; }
    }
}