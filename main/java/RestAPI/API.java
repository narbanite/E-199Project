package RestAPI;

import mainClasses.Incident;
import com.google.gson.Gson;
import database.DB_Connection;
import database.tables.EditIncidentsTable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static spark.Spark.*;

/**
 *
 * @author nikol
 */
public class API {

    static String apiPath = "incidentsAPI/";

    public static void main(String[] args) {
        post(apiPath + "/newIncident", (request, response) -> {
            response.type("application/json");
            EditIncidentsTable editIncidentsTable = new EditIncidentsTable();

            Incident newIncident = new Gson().fromJson(request.body(), Incident.class);
            // Add default values if null

            if (newIncident.getPrefecture() == null) {
                newIncident.setPrefecture("?");
            }

            if (newIncident.getMunicipality() == null) {
                newIncident.setMunicipality("?");
            }
            if (newIncident.getStart_datetime() == null) {
                newIncident.setStart_datetime();
            }
            if (newIncident.getDanger() == null) {
                newIncident.setDanger("unknown");
            }
            if (newIncident.getStatus() == null) {
                newIncident.setStatus("submitted");
            }
            try {
                editIncidentsTable.createNewIncident(newIncident);
                response.status(200);
                return new Gson().toJson(new StandardResponse(new Gson().toJson("Success: Incident Added")));
            } catch (ClassNotFoundException e) {
                response.status(500);
                return new Gson().toJson(new StandardResponse(new Gson().toJson("Error: Internal Server Error")));
            }
        });

        // Endpoint to retrieve incidents based on type and status
        get(apiPath + "/incidents/:type/:status", (request, response) -> {
            response.type("application/json");

            String type = request.params(":type");
            String status = request.params(":status");
            String municipality = request.queryParams("municipality");

            Map<String, Object> responseData = new HashMap<>();

            // Validate `type`
            if (!type.equalsIgnoreCase("fire")
                    && !type.equalsIgnoreCase("accident")
                    && !type.equalsIgnoreCase("all")) {
                response.status(400);
                responseData.put("error", "Invalid type. Allowed values: 'fire', 'accident', 'all'.");
                return new Gson().toJson(responseData);
            }

            // Validate `status`
            if (!status.equalsIgnoreCase("submitted")
                    && !status.equalsIgnoreCase("running")
                    && !status.equalsIgnoreCase("completed")
                    && !status.equalsIgnoreCase("all")) {
                response.status(400);
                responseData.put("error", "Invalid status. Allowed values: 'submitted', 'running', 'completed', 'all'.");
                return new Gson().toJson(responseData);
            }

            // Build SQL Query
            StringBuilder query = new StringBuilder("SELECT * FROM incidents WHERE 1=1");

            // Add conditions for `type`
            if (!type.equalsIgnoreCase("all")) {
                query.append(" AND incident_type = '").append(type).append("'");
            }

            // Add conditions for `status`
            if (!status.equalsIgnoreCase("all")) {
                query.append(" AND status = '").append(status).append("'");
            }

            // Add conditions for `municipality` if provided
            if (municipality != null && !municipality.isEmpty()) {
                query.append(" AND municipality = '").append(municipality).append("'");
            }

            try (Connection con = DB_Connection.getConnection(); Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(query.toString())) {

                List<Incident> incidents = new ArrayList<>();
                while (rs.next()) {
                    Incident incident = new Incident();
                    incident.setIncident_id(rs.getInt("incident_id"));
                    incident.setIncident_type(rs.getString("incident_type"));
                    incident.setDescription(rs.getString("description"));
                    incident.setUser_phone(rs.getString("user_phone"));
                    incident.setUser_type(rs.getString("user_type"));
                    incident.setAddress(rs.getString("address"));
                    incident.setLat(rs.getDouble("lat"));
                    incident.setLon(rs.getDouble("lon"));
                    incident.setMunicipality(rs.getString("municipality"));
                    incident.setPrefecture(rs.getString("prefecture"));
                    incident.setStart_datetime();
                    incident.setEnd_datetime(rs.getString("end_datetime"));
                    incident.setDanger(rs.getString("danger"));
                    incident.setStatus(rs.getString("status"));
                    incident.setFinalResult(rs.getString("finalResult"));
                    incident.setVehicles(rs.getInt("vehicles"));
                    incident.setFiremen(rs.getInt("firemen"));

                    incidents.add(incident);
                }

                response.status(200);
                return new Gson().toJson(incidents);

            } catch (Exception e) {
                response.status(500);
                responseData.put("error", "Internal server error: " + e.getMessage());
                return new Gson().toJson(responseData);
            }
        });

        // PUT endpoint for updating incident status
        put(apiPath + "/incidentStatus/:incident_id/:status", (request, response) -> {
            response.type("application/json");

            String incidentIdParam = request.params(":incident_id");
            String newStatus = request.params(":status");

            Map<String, Object> responseData = new HashMap<>();

            try {
                int incidentId = Integer.parseInt(incidentIdParam);

                // Validate status
                if (!newStatus.equalsIgnoreCase("fake")
                        && !newStatus.equalsIgnoreCase("running")
                        && !newStatus.equalsIgnoreCase("finished")) {
                    response.status(400);
                    responseData.put("error", "Invalid status. Allowed values: 'fake', 'running', 'finished'.");
                    return new Gson().toJson(responseData);
                }

                // Connect to the database and update the status
                try (Connection con = DB_Connection.getConnection(); Statement stmt = con.createStatement()) {

                    StringBuilder query = new StringBuilder();
                    query.append("UPDATE incidents SET status = '").append(newStatus).append("'");

                    // Add current datetime for 'finished' status
                    if (newStatus.equalsIgnoreCase("finished")) {
                        query.append(", end_datetime = NOW()");
                    }

                    query.append(" WHERE incident_id = ").append(incidentId);
                    query.append(" AND (status = 'submitted' OR status = 'running')");

                    int rowsAffected = stmt.executeUpdate(query.toString());

                    if (rowsAffected > 0) {
                        response.status(200);
                        responseData.put("message", "Incident status updated successfully.");
                    } else {
                        response.status(400);
                        responseData.put("error", "Failed to update status. Incident may not exist or status change is not allowed.");
                    }
                }

            } catch (NumberFormatException e) {
                response.status(400);
                responseData.put("error", "Invalid incident_id. Must be an integer.");
            } catch (Exception e) {
                response.status(500);
                responseData.put("error", "Internal server error: " + e.getMessage());
            }

            return new Gson().toJson(responseData);
        });

        // DELETE endpoint for deleting an incident
        delete(apiPath + "/incidentDeletion/:incident_id", (request, response) -> {
            response.type("application/json");

            String incidentIdParam = request.params(":incident_id");
            Map<String, Object> responseData = new HashMap<>();

            try {
                int incidentId = Integer.parseInt(incidentIdParam);

                try (Connection con = DB_Connection.getConnection(); Statement stmt = con.createStatement()) {

                    String query = "DELETE FROM incidents WHERE incident_id = " + incidentId;
                    int rowsAffected = stmt.executeUpdate(query);

                    if (rowsAffected > 0) {
                        response.status(200);
                        responseData.put("message", "Incident deleted successfully.");
                    } else {
                        response.status(404);
                        responseData.put("error", "Incident not found with the provided ID.");
                    }
                }

            } catch (NumberFormatException e) {
                response.status(400);
                responseData.put("error", "Invalid incident_id. Must be an integer.");
            } catch (Exception e) {
                response.status(500);
                responseData.put("error", "Internal server error: " + e.getMessage());
            }

            return new Gson().toJson(responseData);
        });
    }
}
