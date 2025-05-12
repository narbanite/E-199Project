package servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import database.DB_Connection;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author nikol
 */

public class GetMessages extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (Connection conn = DB_Connection.getConnection(); Statement stmt = conn.createStatement()) {

            String query = "SELECT * FROM messages";
            ResultSet rs = stmt.executeQuery(query);

            JSONArray messagesArray = new JSONArray();

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

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(messagesArray.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(new JSONObject().put("error", "Failed to fetch messages: " + e.getMessage()).toString());
        }
    }
}
