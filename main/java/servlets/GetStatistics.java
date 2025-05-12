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
public class GetStatistics extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try (Connection conn = DB_Connection.getConnection(); Statement stmt = conn.createStatement()) {

            JSONObject statistics = new JSONObject();

            //number of incidents by type
            System.out.println("Fetching incidents statistics...");
            ResultSet rs = stmt.executeQuery("SELECT incident_type, COUNT(*) AS count FROM incidents GROUP BY incident_type");
            JSONArray incidentStats = new JSONArray();
            while (rs.next()) {
                JSONObject data = new JSONObject();
                data.put("type", rs.getString("incident_type"));
                data.put("count", rs.getInt("count"));
                incidentStats.put(data);
            }
            statistics.put("incidents", incidentStats);

            //number of users and volunteers
            System.out.println("Fetching users and volunteers statistics...");
            rs = stmt.executeQuery(
                    "SELECT 'Users' AS category, COUNT(*) AS count FROM users "
                    + "UNION ALL "
                    + "SELECT 'Volunteers' AS category, COUNT(*) AS count FROM volunteers"
            );
            JSONArray userVolunteerStats = new JSONArray();
            while (rs.next()) {
                JSONObject data = new JSONObject();
                data.put("category", rs.getString("category"));
                data.put("count", rs.getInt("count"));
                userVolunteerStats.put(data);
            }
            statistics.put("user_volunteers", userVolunteerStats);

            //number of vehicles and firemen
            System.out.println("Fetching participant statistics...");
            rs = stmt.executeQuery(
                    "SELECT volunteer_type, COUNT(*) AS count FROM participants "
                    + "WHERE volunteer_type IN ('simple', 'driver') "
                    + "GROUP BY volunteer_type"
            );
            JSONArray participantStats = new JSONArray();
            while (rs.next()) {
                JSONObject data = new JSONObject();
                data.put("type", rs.getString("volunteer_type"));
                data.put("count", rs.getInt("count"));
                participantStats.put(data);
            }
            statistics.put("participants", participantStats);

            response.setContentType("application/json");
            response.getWriter().write(statistics.toString());
            System.out.println("Statistics successfully fetched and sent.");

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(new JSONObject().put("error", "Error: " + e.getMessage()).toString());
        }
    }
}
