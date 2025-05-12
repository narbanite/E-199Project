package servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.json.JSONObject;
import database.DB_Connection;

/**
 *
 * @author nikol
 */

public class AddIncident extends HttpServlet {

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

            String incidentType = json.optString("incident_type", null);
            String description = json.optString("description", null);
            String userPhone = json.optString("user_phone", null);
            String userType = json.optString("user_type", null);
            String address = json.optString("address", null);
            Double lat = json.optDouble("lat", 0.0);
            Double lon = json.optDouble("lon", 0.0);
            String municipality = json.optString("municipality", null);
            String prefecture = json.optString("prefecture", null);
            String startDatetime = json.optString("start_datetime", null);
            String endDatetime = json.optString("end_datetime", null);
            String danger = json.optString("danger", null);
            String status = json.optString("status", null);
            String finalResult = json.optString("finalResult", null);
            int vehicles = json.optInt("vehicles", 0);
            int firemen = json.optInt("firemen", 0);

            String query = "INSERT INTO incidents (incident_type, description, user_phone, user_type, address, lat, lon, municipality, prefecture, start_datetime, end_datetime, danger, status, finalResult, vehicles, firemen) "
                    + "VALUES ('" + escapeSQL(incidentType) + "', '"
                    + escapeSQL(description) + "', '"
                    + escapeSQL(userPhone) + "', '"
                    + escapeSQL(userType) + "', '"
                    + escapeSQL(address) + "', "
                    + lat + ", " + lon + ", '"
                    + escapeSQL(municipality) + "', '"
                    + escapeSQL(prefecture) + "', '"
                    + escapeSQL(startDatetime) + "', "
                    + (endDatetime != null ? "'" + escapeSQL(endDatetime) + "'" : "NULL") + ", '"
                    + escapeSQL(danger) + "', '"
                    + escapeSQL(status) + "', "
                    + (finalResult != null ? "'" + escapeSQL(finalResult) + "'" : "NULL") + ", "
                    + vehicles + ", " + firemen + ")";

            System.out.println("Executing Query: " + query);

            try (Connection conn = DB_Connection.getConnection(); Statement stmt = conn.createStatement()) {
                int rowsInserted = stmt.executeUpdate(query);
                if (rowsInserted > 0) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("application/json");
                    response.getWriter().write(new JSONObject().put("message", "Incident added successfully").toString());
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write(new JSONObject().put("error", "Failed to add incident").toString());
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
