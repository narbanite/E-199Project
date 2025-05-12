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

public class LoadVolunteer extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        JSONArray volunteersArray = new JSONArray();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Volunteer not logged in.\"}");
            return;
        }

        String username = (String) session.getAttribute("username");

        String query = "SELECT * FROM volunteers WHERE username = '" + username + "'";

        try (Connection conn = DB_Connection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                JSONObject volunteer = new JSONObject();
                volunteer.put("volunteer_id", rs.getInt("volunteer_id"));
                volunteer.put("username", rs.getString("username"));
                volunteer.put("email", rs.getString("email"));
                volunteer.put("password", rs.getString("password"));
                volunteer.put("firstname", rs.getString("firstname"));
                volunteer.put("lastname", rs.getString("lastname"));
                volunteer.put("birthdate", rs.getString("birthdate"));
                volunteer.put("gender", rs.getString("gender"));
                volunteer.put("afm", rs.getString("afm"));
                volunteer.put("country", rs.getString("country"));
                volunteer.put("address", rs.getString("address"));
                volunteer.put("municipality", rs.getString("municipality"));
                volunteer.put("prefecture", rs.getString("prefecture"));
                volunteer.put("job", rs.getString("job"));
                volunteer.put("telephone", rs.getString("telephone"));
                volunteer.put("lat", rs.getDouble("lat"));
                volunteer.put("lon", rs.getDouble("lon"));
                volunteer.put("volunteer_type", rs.getString("volunteer_type"));
                volunteer.put("height", rs.getDouble("height"));
                volunteer.put("weight", rs.getDouble("weight"));

                volunteersArray.put(volunteer);
            }

            response.setContentType("application/json");
            response.getWriter().write(volunteersArray.toString());
        } catch (SQLException | ClassNotFoundException ex) {
            System.err.println("Database error: " + ex.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error occurred");
        }
    }
}
