package servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import database.DB_Connection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author nikol
 */

public class LoadIncidents extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONArray incidentsArray = new JSONArray();
        String query = "SELECT * FROM incidents";

        try (Connection conn = DB_Connection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                JSONObject incident = new JSONObject();
                incident.put("incident_id", rs.getInt("incident_id"));
                incident.put("incident_type", rs.getString("incident_type"));
                incident.put("description", rs.getString("description"));
                incident.put("user_phone", rs.getString("user_phone"));
                incident.put("user_type", rs.getString("user_type"));
                incident.put("address", rs.getString("address"));
                incident.put("lat", rs.getDouble("lat"));
                incident.put("lon", rs.getDouble("lon"));
                incident.put("municipality", rs.getString("municipality"));
                incident.put("prefecture", rs.getString("prefecture"));
                incident.put("start_datetime", rs.getTimestamp("start_datetime") != null ? rs.getTimestamp("start_datetime").toString() : "N/A");
                incident.put("end_datetime", rs.getTimestamp("end_datetime") != null ? rs.getTimestamp("end_datetime").toString() : "N/A");
                incident.put("danger", rs.getString("danger"));
                incident.put("status", rs.getString("status"));
                incident.put("finalResult", rs.getString("finalResult"));
                incident.put("vehicles", rs.getInt("vehicles"));
                incident.put("firemen", rs.getInt("firemen"));

                incidentsArray.put(incident);
            }

            response.setContentType("application/json");
            response.getWriter().write(incidentsArray.toString());
        } catch (SQLException | ClassNotFoundException ex) {
            System.err.println("Database error: " + ex.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        }
    }
}
