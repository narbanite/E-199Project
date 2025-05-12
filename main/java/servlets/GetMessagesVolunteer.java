package servlets;

import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import org.json.JSONArray;
import org.json.JSONObject;
import database.DB_Connection;

/**
 *
 * @author nikol
 */

public class GetMessagesVolunteer extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Volunteer not logged in.\"}");
            return;
        }

        String username = (String) session.getAttribute("username");

        String escapedUsername = escapeSQL(username);

        String query = "SELECT m.incident_id, m.sender, m.recipient, m.message, m.date_time "
                + "FROM messages m "
                + "JOIN incidents i ON m.incident_id = i.incident_id "
                + "WHERE i.status = 'running' "
                + "AND (m.recipient = 'public' OR m.recipient = '" + escapedUsername + "')";

        try (Connection conn = DB_Connection.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(query);

            JSONArray messages = new JSONArray();
            while (rs.next()) {
                JSONObject message = new JSONObject();
                message.put("incident_id", rs.getInt("incident_id"));
                message.put("sender", rs.getString("sender"));
                message.put("recipient", rs.getString("recipient"));
                message.put("message", rs.getString("message"));
                message.put("date_time", rs.getTimestamp("date_time"));
                messages.put(message);
            }

            response.setContentType("application/json");
            response.getWriter().write(messages.toString());
        } catch (SQLException | ClassNotFoundException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred.");
        }
    }

    // Helper method to escape SQL input
    private String escapeSQL(String input) {
        return input != null ? input.replace("'", "''") : null;
    }
}

