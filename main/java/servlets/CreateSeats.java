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

public class CreateSeats extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        try {
            JSONObject json = new JSONObject(sb.toString());
            int incidentId = json.getInt("incident_id");
            int seatCount = json.getInt("seatCount");
            String volunteerType = json.getString("volunteer_type");

            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("INSERT INTO participants (incident_id, volunteer_type, status) VALUES ");

            for (int i = 0; i < seatCount; i++) {
                queryBuilder.append("(")
                        .append(incidentId).append(", '")
                        .append(volunteerType.replace("'", "''")).append("', 'requested')");
                if (i < seatCount - 1) {
                    queryBuilder.append(", ");
                }
            }

            try (Connection conn = DB_Connection.getConnection(); Statement stmt = conn.createStatement()) {

                int rowsInserted = stmt.executeUpdate(queryBuilder.toString());
                if (rowsInserted > 0) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("{\"message\":\"Seats created successfully\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\":\"Failed to create seats\"}");
                }
            } catch (SQLException | ClassNotFoundException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\":\"Database error occurred\"}");
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Invalid request data\"}");
        }
    }
}
