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

public class LoadUser extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONArray usersArray = new JSONArray();

        HttpSession session = request.getSession(false); // Get the current session without creating a new one
        if (session == null || session.getAttribute("username") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"User not logged in.\"}");
            return;
        }

        String username = (String) session.getAttribute("username");

        String query = "SELECT * FROM users WHERE username = '" + username + "'";

        try (Connection conn = DB_Connection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                JSONObject user = new JSONObject();
                user.put("user_id", rs.getInt("user_id"));
                user.put("username", rs.getString("username"));
                user.put("email", rs.getString("email"));
                user.put("password", rs.getString("password"));
                user.put("firstname", rs.getString("firstname"));
                user.put("lastname", rs.getString("lastname"));
                user.put("birthdate", rs.getString("birthdate"));
                user.put("gender", rs.getString("gender"));
                user.put("afm", rs.getString("afm"));
                user.put("country", rs.getString("country"));
                user.put("address", rs.getString("address"));
                user.put("municipality", rs.getString("municipality"));
                user.put("prefecture", rs.getString("prefecture"));
                user.put("job", rs.getString("job"));
                user.put("telephone", rs.getString("telephone"));
                user.put("lat", rs.getDouble("lat"));
                user.put("lon", rs.getDouble("lon"));

                usersArray.put(user);
            }

            response.setContentType("application/json");
            response.getWriter().write(usersArray.toString());
        } catch (SQLException | ClassNotFoundException ex) {
            System.err.println("Database error: " + ex.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        }
    }
}
