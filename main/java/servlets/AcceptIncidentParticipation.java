package servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import database.DB_Connection;

/**
 *
 * @author nikol
 */

public class AcceptIncidentParticipation extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null || session.getAttribute("volunteerType") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Volunteer not logged in.\"}");
            return;
        }

        String username = (String) session.getAttribute("username");
        String volunteerType = (String) session.getAttribute("volunteerType");
        String incidentId = request.getParameter("incident_id");

        if (incidentId == null || incidentId.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Incident ID is required.\"}");
            return;
        }

        String query = "UPDATE participants "
                + "SET status = 'accepted', volunteer_username = '" + username + "' "
                + "WHERE incident_id = " + incidentId
                + " AND volunteer_type = '" + volunteerType + "'"
                + " AND status = 'requested' "
                + "LIMIT 1";

        try (Connection conn = DB_Connection.getConnection(); Statement stmt = conn.createStatement()) {

            int rowsAffected = stmt.executeUpdate(query);

            if (rowsAffected > 0) {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": true, \"message\": \"Participation accepted.\"}");
            } else {
                response.setContentType("application/json");
                response.getWriter().write("{\"success\": false, \"message\": \"No matching participation request found.\"}");
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred.");
        }
    }
}
