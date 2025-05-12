package servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.json.JSONObject;
import database.DB_Connection;

public class GetUserLocation extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\": \"User not logged in.\"}");
            out.flush();
            return;
        }

        String username = (String) session.getAttribute("username");

        try (Connection conn = DB_Connection.getConnection(); Statement stmt = conn.createStatement()) {

            // Construct and execute query
            String query = "SELECT lat, lon FROM users WHERE username = '" + username + "'";
            System.out.println("Executing query: " + query); // Debugging line
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                double lat = rs.getDouble("lat");
                double lon = rs.getDouble("lon");

                JSONObject location = new JSONObject();
                location.put("lat", lat);
                location.put("lon", lon);

                response.setStatus(HttpServletResponse.SC_OK);
                out.print(location.toString());
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"User location not found.\"}");
            }

        } catch (Exception e) {
            e.printStackTrace(); // Log the error to server logs
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Server error occurred: " + e.getMessage() + "\"}");
        } finally {
            out.flush();
        }
    }
}
