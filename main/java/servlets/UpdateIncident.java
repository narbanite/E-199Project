package servlets;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Statement;
import org.json.JSONObject;
import database.DB_Connection;

/**
 *
 * @author nikol
 */

public class UpdateIncident extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        String jsonString = sb.toString();

        try {
            JSONObject json = new JSONObject(jsonString);

            int incidentId = json.getInt("incident_id");

            StringBuilder queryBuilder = new StringBuilder("UPDATE incidents SET ");

            //optional fields
            if (json.has("incident_type") && !json.isNull("incident_type")) {
                queryBuilder.append("incident_type = '").append(escapeSQL(json.getString("incident_type"))).append("', ");
            }
            if (json.has("description") && !json.isNull("description")) {
                queryBuilder.append("description = '").append(escapeSQL(json.getString("description"))).append("', ");
            }
            if (json.has("user_phone") && !json.isNull("user_phone")) {
                queryBuilder.append("user_phone = '").append(escapeSQL(json.getString("user_phone"))).append("', ");
            }
            if (json.has("user_type") && !json.isNull("user_type")) {
                queryBuilder.append("user_type = '").append(escapeSQL(json.getString("user_type"))).append("', ");
            }
            if (json.has("address") && !json.isNull("address")) {
                queryBuilder.append("address = '").append(escapeSQL(json.getString("address"))).append("', ");
            }
            if (json.has("lat") && !json.isNull("lat")) {
                queryBuilder.append("lat = ").append(json.getDouble("lat")).append(", ");
            }
            if (json.has("lon") && !json.isNull("lon")) {
                queryBuilder.append("lon = ").append(json.getDouble("lon")).append(", ");
            }
            if (json.has("municipality") && !json.isNull("municipality")) {
                queryBuilder.append("municipality = '").append(escapeSQL(json.getString("municipality"))).append("', ");
            }
            if (json.has("prefecture") && !json.isNull("prefecture")) {
                queryBuilder.append("prefecture = '").append(escapeSQL(json.getString("prefecture"))).append("', ");
            }
            if (json.has("start_datetime") && !json.isNull("start_datetime")) {
                queryBuilder.append("start_datetime = '").append(escapeSQL(json.getString("start_datetime"))).append("', ");
            }
            if (json.has("end_datetime") && !json.isNull("end_datetime")) {
                queryBuilder.append("end_datetime = '").append(escapeSQL(json.getString("end_datetime"))).append("', ");
            }
            if (json.has("danger") && !json.isNull("danger")) {
                queryBuilder.append("danger = '").append(escapeSQL(json.getString("danger"))).append("', ");
            }
            if (json.has("status") && !json.isNull("status")) {
                queryBuilder.append("status = '").append(escapeSQL(json.getString("status"))).append("', ");
            }
            if (json.has("finalResult") && !json.isNull("finalResult")) {
                queryBuilder.append("finalResult = '").append(escapeSQL(json.getString("finalResult"))).append("', ");
            }
            if (json.has("vehicles") && !json.isNull("vehicles")) {
                queryBuilder.append("vehicles = ").append(json.getInt("vehicles")).append(", ");
            }
            if (json.has("firemen") && !json.isNull("firemen")) {
                queryBuilder.append("firemen = ").append(json.getInt("firemen")).append(", ");
            }

            //remove the trailing comma and space
            String query = queryBuilder.toString().replaceAll(",\\s*$", " ");

            //add the WHERE clause
            query += " WHERE incident_id = " + incidentId;

            System.out.println("Generated SQL Query: " + query);

            try (Connection conn = DB_Connection.getConnection(); Statement stmt = conn.createStatement()) {
                int rowsUpdated = stmt.executeUpdate(query);
                if (rowsUpdated > 0) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().write("{\"message\": \"Incident updated successfully\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\": \"No incident found with the given ID\"}");
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"An internal server error occurred: " + e.getMessage() + "\"}");
        }
    }

    private String escapeSQL(String input) {
        return input != null ? input.replace("'", "''") : null;
    }
}
