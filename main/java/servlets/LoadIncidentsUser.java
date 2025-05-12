package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import database.DB_Connection;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

/**
 *
 * @author nikol
 */

public class LoadIncidentsUser extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        JSONArray incidentsArray = new JSONArray();

        String query = "SELECT * FROM incidents WHERE status = 'running'";

        try (Connection conn = DB_Connection.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                JSONObject incident = new JSONObject();
                incident.put("incident_id", rs.getInt("incident_id"));
                incident.put("incident_type", rs.getString("incident_type"));
                incident.put("description", rs.getString("description"));
                incident.put("user_phone", rs.getString("user_phone"));
                incident.put("user_type", rs.getString("user_type"));
                incident.put("address", rs.getString("address"));
                incident.put("lat", rs.getDouble("lat"));
                incident.put("lon", rs.getDouble("lon"));
                incident.put("municipality", rs.getString("municipality"));
                incident.put("prefecture", rs.getString("prefecture"));
                incident.put("start_datetime", rs.getString("start_datetime"));
                incident.put("end_datetime", rs.getString("end_datetime"));
                incident.put("danger", rs.getString("danger"));
                incident.put("status", rs.getString("status"));
                incident.put("finalResult", rs.getString("finalResult"));
                incident.put("vehicles", rs.getInt("vehicles"));
                incident.put("firemen", rs.getInt("firemen"));

                incidentsArray.put(incident);
            }

            response.setStatus(HttpServletResponse.SC_OK);
            out.print(incidentsArray.toString());
            out.flush();

        } catch (SQLException | ClassNotFoundException e) {

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JSONObject error = new JSONObject();
            error.put("error", "Failed to fetch incidents: " + e.getMessage());
            out.print(error.toString());
            out.flush();
        }
    }
}
