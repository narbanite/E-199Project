package servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.sql.*;
import org.json.JSONObject;
import database.DB_Connection;

/**
 *
 * @author nikol
 */

public class SendPublicMessage extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String jsonData = sb.toString();

        try {

            JSONObject json = new JSONObject(jsonData);

            int incidentId = json.getInt("incident_id");
            String sender = json.getString("sender");
            String recipient = json.getString("recipient");
            String messageText = json.getString("message_text");

            String dateTime = new java.sql.Timestamp(System.currentTimeMillis()).toString();

            String query = "INSERT INTO messages (incident_id, sender, recipient, message, date_time) "
                    + "VALUES (" + incidentId + ", '"
                    + escapeSQL(sender) + "', '"
                    + escapeSQL(recipient) + "', '"
                    + escapeSQL(messageText) + "', '"
                    + escapeSQL(dateTime) + "')";

            System.out.println("Executing Query: " + query);

            try (Connection conn = DB_Connection.getConnection(); Statement stmt = conn.createStatement()) {
                int rowsInserted = stmt.executeUpdate(query);
                if (rowsInserted > 0) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("application/json");
                    response.getWriter().write(new JSONObject().put("message", "Message sent successfully").toString());
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write(new JSONObject().put("error", "Failed to send message").toString());
                }
            } catch (SQLException | ClassNotFoundException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write(new JSONObject().put("error", "Database error occurred").toString());
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(new JSONObject().put("error", "Invalid JSON format: " + e.getMessage()).toString());
        }
    }

    /**
     * Escapes single quotes in input strings to prevent SQL syntax errors.
     */
    private String escapeSQL(String input) {
        return input != null ? input.replace("'", "''") : null;
    }
}
