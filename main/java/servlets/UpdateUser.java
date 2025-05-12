package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import database.tables.EditUsersTable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nikol
 */

public class UpdateUser extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuilder sb = new StringBuilder();
        String line;

        // Read JSON data from the request
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request data");
            return;
        }

        // Get username from session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User is not logged in.");
            return;
        }
        String username = (String) session.getAttribute("username");

        // Parse JSON data
        JSONObject jsonObject = new JSONObject(sb.toString());
        EditUsersTable editUsersTable = new EditUsersTable();
        JSONObject jsonResponse = new JSONObject();

        // Define fields that cannot be updated
        String[] readOnlyFields = {"username", "telephone", "afm", "email"};
        // Update each key-value pair in the database
        try {
            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = jsonObject.optString(key);

                // Skip empty or null values
                if (value == null || value.trim().isEmpty() || isReadOnlyField(key, readOnlyFields)) {
                    continue;
                }

                // Call the updateUser function for each key-value pair
                editUsersTable.updateUser(username, key, value);
            }

            // Prepare success response
            jsonResponse.put("status", "success");
            jsonResponse.put("message", "User information updated successfully.");
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (SQLException | ClassNotFoundException ex) {
            // Prepare error response
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Error updating user information: " + ex.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Logger.getLogger(UpdateUser.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Send JSON response
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(jsonResponse);
        out.flush();
    }

    // Helper method to check if a field is read-only
    private boolean isReadOnlyField(String key, String[] readOnlyFields) {
        for (String field : readOnlyFields) {
            if (field.equals(key)) {
                return true;
            }
        }
        return false;
    }
}
