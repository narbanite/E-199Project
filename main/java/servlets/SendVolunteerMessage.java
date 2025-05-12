package servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Statement;
import org.json.JSONObject;
import database.DB_Connection;

/**
 *
 * @author nikol
 */

public class SendVolunteerMessage extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        StringBuilder sb = new StringBuilder();
        String line;
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        String requestData = sb.toString();

        try {
            JSONObject json = new JSONObject(requestData);

            int incidentId = json.getInt("incident_id");
            String message = json.getString("message");
            String recipient = json.getString("recipient");
            String sender = json.getString("sender");

            String query = "INSERT INTO messages (incident_id, message, recipient, sender, date_time) "
                    + "VALUES (" + incidentId + ", '" + escapeSQL(message) + "', '"
                    + escapeSQL(recipient) + "', '" + escapeSQL(sender) + "', NOW())";

            System.out.println("Executing Query: " + query);

            try (Connection conn = DB_Connection.getConnection(); Statement stmt = conn.createStatement()) {

                int rowsAffected = stmt.executeUpdate(query);

                if (rowsAffected > 0) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.write("{\"message\":\"Message added successfully\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.write("{\"error\":\"Failed to add message\"}");
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"error\":\"An error occurred: " + e.getMessage() + "\"}");
        }
    }

    private String escapeSQL(String input) {
        return input != null ? input.replace("'", "''") : null;
    }
}
