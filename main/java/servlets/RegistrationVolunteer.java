package servlets;

import database.tables.EditVolunteersTable;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mainClasses.Volunteer;
import org.json.JSONObject;

/**
 *
 * @author nikol
 */
public class RegistrationVolunteer extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("doPost registration");

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Failed to read request data");
            return;
        }

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(sb.toString());
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid JSON format");
            return;
        }

        // Create and populate the User object
        Volunteer volunteer = new Volunteer();
        try {
            volunteer.setUsername(jsonObject.getString("username"));
            volunteer.setPassword(jsonObject.getString("password"));
            volunteer.setFirstname(jsonObject.getString("firstname"));
            volunteer.setLastname(jsonObject.getString("lastname"));
            volunteer.setEmail(jsonObject.getString("email"));
            volunteer.setBirthdate(jsonObject.getString("birthdate"));
            volunteer.setGender(jsonObject.getString("gender"));
            volunteer.setAfm(jsonObject.getString("afm"));
            volunteer.setCountry(jsonObject.getString("country"));
            volunteer.setAddress(jsonObject.getString("address"));
            volunteer.setMunicipality(jsonObject.getString("municipality"));
            volunteer.setPrefecture(jsonObject.getString("prefecture"));
            volunteer.setJob(jsonObject.getString("job"));
            volunteer.setTelephone(jsonObject.getString("telephone"));
            volunteer.setLat(jsonObject.getDouble("lat"));
            volunteer.setLon(jsonObject.getDouble("lon"));
            volunteer.setVolunteer_type((jsonObject.getString("volunteer_type")));
            volunteer.setHeight(jsonObject.getDouble("height"));
            volunteer.setWeight(jsonObject.getDouble("weight"));

        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid fields in request JSON");
            return;
        }

        // Interact with the database through EditVolunteerssTable
        EditVolunteersTable editVolunteersTable = new EditVolunteersTable();
        JSONObject jsonResponse = new JSONObject();

        try {

            if (editVolunteersTable.checkDuplicateVolunteer(volunteer.getUsername(), volunteer.getEmail(), volunteer.getTelephone())) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Volunteer with the provided volunteername, email, or telephone already exists.");
                response.setStatus(HttpServletResponse.SC_CONFLICT);
            } else {

                editVolunteersTable.addNewVolunteer(volunteer);
                System.out.println("Volunteer added successfully");
                jsonResponse.put("status", "success");
                jsonResponse.put("message", "Registration successful.");
                response.setStatus(HttpServletResponse.SC_OK);
            }
        } catch (SQLException ex) {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Database error: " + ex.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Logger.getLogger(RegistrationVolunteer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Class not found error: " + ex.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Logger.getLogger(RegistrationVolunteer.class.getName()).log(Level.SEVERE, null, ex);
        }

        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            out.print(jsonResponse);
            out.flush();
        }
    }
}
