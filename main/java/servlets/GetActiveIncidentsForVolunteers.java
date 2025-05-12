package servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.json.JSONArray;
import org.json.JSONObject;
import database.DB_Connection;

/**
 *
 * @author nikol
 */

public class GetActiveIncidentsForVolunteers extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Volunteer not logged in.\"}");
            return;
        }

        String username = (String) session.getAttribute("username");
        String volunteerType = request.getParameter("volunteer_type"); //"simple" or "driver"

        String query = "SELECT DISTINCT i.* FROM incidents i "
                + "JOIN participants p ON i.incident_id = p.incident_id "
                + "WHERE p.volunteer_type = '" + volunteerType + "' "
                + "AND p.status = 'requested' "
                + "AND i.status = 'running'";


        try (Connection conn = DB_Connection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            JSONArray incidentsArray = new JSONArray();
            while (rs.next()) {
                JSONObject incident = new JSONObject();
                incident.put("incident_id", rs.getInt("incident_id"));
                incident.put("incident_type", rs.getString("incident_type"));
                incident.put("description", rs.getString("description"));
                incident.put("start_datetime", rs.getString("start_datetime"));
                incident.put("end_datetime", rs.getString("end_datetime"));
                incident.put("danger", rs.getString("danger"));
                incident.put("status", rs.getString("status"));
                incidentsArray.put(incident);
            }

            response.setContentType("application/json");
            response.getWriter().write(incidentsArray.toString());
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred.");
        }
    }
}
