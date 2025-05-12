package servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import org.json.JSONArray;
import org.json.JSONObject;
import database.DB_Connection;

/**
 *
 * @author nikol
 */

public class GetVolunteerHistory extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Volunteer not logged in.\"}");
            return;
        }

        String volunteerUsername = (String) session.getAttribute("username");

        // SQL Query
        String query = "SELECT i.incident_id, i.incident_type, i.description, i.start_datetime, i.end_datetime, i.danger, p.status "
                + "FROM participants p "
                + "JOIN incidents i ON p.incident_id = i.incident_id "
                + "WHERE p.volunteer_username = '" + volunteerUsername + "' "
                + "ORDER BY i.start_datetime DESC";

        try (Connection conn = DB_Connection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            JSONArray historyArray = new JSONArray();
            while (rs.next()) {
                JSONObject incident = new JSONObject();
                incident.put("incident_id", rs.getInt("incident_id"));
                incident.put("incident_type", rs.getString("incident_type"));
                incident.put("description", rs.getString("description"));
                incident.put("start_datetime", rs.getString("start_datetime"));
                incident.put("end_datetime", rs.getString("end_datetime"));
                incident.put("danger", rs.getString("danger"));
                incident.put("status", rs.getString("status"));
                historyArray.put(incident);
            }

            response.setContentType("application/json");
            response.getWriter().write(historyArray.toString());

        } catch (SQLException | ClassNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Database error occurred.\"}");
        }
    }
}
