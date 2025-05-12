package servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.json.JSONArray;
import org.json.JSONObject;
import database.DB_Connection;

/**
 *
 * @author nikol
 */

public class GetMessagesUser extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        JSONArray messagesArray = new JSONArray();

        String query = "SELECT messages.message_id, messages.incident_id, messages.message, messages.sender, "
                + "messages.recipient, messages.date_time "
                + "FROM messages "
                + "JOIN incidents ON messages.incident_id = incidents.incident_id "
                + "WHERE LOWER(messages.recipient) = 'public' AND LOWER(incidents.status) = 'running'";

        try (Connection conn = DB_Connection.getConnection(); PreparedStatement stmt = conn.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                JSONObject message = new JSONObject();
                message.put("message_id", rs.getInt("message_id"));
                message.put("incident_id", rs.getInt("incident_id"));
                message.put("message", rs.getString("message"));
                message.put("sender", rs.getString("sender"));
                message.put("recipient", rs.getString("recipient"));
                message.put("date_time", rs.getString("date_time"));

                messagesArray.put(message);
            }

            response.setStatus(HttpServletResponse.SC_OK);
            out.print(messagesArray.toString());
            out.flush();

        } catch (SQLException | ClassNotFoundException e) {

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject error = new JSONObject();
            error.put("error", "Failed to fetch messages: " + e.getMessage());
            out.print(error.toString());
            out.flush();
        }
    }
}
