package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import database.DB_Connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nikol
 */

public class UpdateVolunteer extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        StringBuilder sb = new StringBuilder();
        String line;

        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid request data");
            return;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Volunteer is not logged in.");
            return;
        }
        String username = (String) session.getAttribute("username");

        JSONObject jsonObject = new JSONObject(sb.toString());
        JSONObject jsonResponse = new JSONObject();

        String[] readOnlyFields = {"username", "telephone", "afm", "email"};

        try (Connection con = DB_Connection.getConnection(); Statement stmt = con.createStatement()) {

            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                String value = jsonObject.optString(key);

                if (value == null || value.trim().isEmpty() || isReadOnlyField(key, readOnlyFields)) {
                    continue;
                }

                String updateQuery = "UPDATE volunteers SET " + key + " = '" + value + "' WHERE username = '" + username + "'";
                stmt.executeUpdate(updateQuery);
            }

            jsonResponse.put("status", "success");
            jsonResponse.put("message", "Volunteer information updated successfully.");
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (SQLException | ClassNotFoundException ex) {

            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Error updating volunteer information: " + ex.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Logger.getLogger(UpdateVolunteer.class.getName()).log(Level.SEVERE, null, ex);
        }

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print(jsonResponse);
        out.flush();
    }

    private boolean isReadOnlyField(String key, String[] readOnlyFields) {
        for (String field : readOnlyFields) {
            if (field.equals(key)) {
                return true;
            }
        }
        return false;
    }
}
