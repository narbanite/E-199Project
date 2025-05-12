package servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import database.DB_Connection;
import java.io.BufferedReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.json.JSONObject;

/**
 *
 * @author nikol
 */

public class GetIncidentDetails extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String jsonString = sb.toString();
        JSONObject json = new JSONObject(jsonString);
        int incidentId = json.getInt("incident_id");

        String query = "SELECT * FROM incidents WHERE incident_id = " + incidentId;

        try (Connection conn = DB_Connection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                JSONObject incident = new JSONObject();
                incident.put("incident_id", rs.getInt("incident_id"));
                incident.put("incident_type", rs.getString("incident_type"));
                incident.put("description", rs.getString("description"));
                incident.put("user_phone", rs.getString("user_phone"));
                incident.put("address", rs.getString("address"));
                incident.put("lat", rs.getDouble("lat"));
                incident.put("lon", rs.getDouble("lon"));
                incident.put("municipality", rs.getString("municipality"));
                incident.put("status", rs.getString("status"));

                response.setContentType("application/json");
                response.getWriter().write(incident.toString());
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Incident not found");
            }
        } catch (SQLException | ClassNotFoundException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        }
    }
}
