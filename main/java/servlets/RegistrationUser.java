package servlets;

import database.tables.EditUsersTable;
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
import mainClasses.User;
import org.json.JSONObject;

/**
 *
 * @author nikol
 */
public class RegistrationUser extends HttpServlet {

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

        User user = new User();
        try {
            user.setUsername(jsonObject.getString("username"));
            user.setPassword(jsonObject.getString("password"));
            user.setFirstname(jsonObject.getString("firstname"));
            user.setLastname(jsonObject.getString("lastname"));
            user.setEmail(jsonObject.getString("email"));
            user.setBirthdate(jsonObject.getString("birthdate"));
            user.setGender(jsonObject.getString("gender"));
            user.setAfm(jsonObject.getString("afm"));
            user.setCountry(jsonObject.getString("country"));
            user.setAddress(jsonObject.getString("address"));
            user.setMunicipality(jsonObject.getString("municipality"));
            user.setPrefecture(jsonObject.getString("prefecture"));
            user.setJob(jsonObject.getString("job"));
            user.setTelephone(jsonObject.getString("telephone"));
            user.setLat(jsonObject.getDouble("lat"));
            user.setLon(jsonObject.getDouble("lon"));
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or invalid fields in request JSON");
            return;
        }

        EditUsersTable editUserTable = new EditUsersTable();
        JSONObject jsonResponse = new JSONObject();

        try {

            if (editUserTable.checkDuplicateUser(user.getUsername(), user.getEmail(), user.getTelephone())) {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "User with the provided username, email, or telephone already exists.");
                response.setStatus(HttpServletResponse.SC_CONFLICT);
            } else {

                editUserTable.addNewUser(user);
                System.out.println("User added successfully");
                jsonResponse.put("status", "success");
                jsonResponse.put("message", "Registration successful.");
                response.setStatus(HttpServletResponse.SC_OK);
            }
        } catch (SQLException ex) {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Database error: " + ex.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Logger.getLogger(RegistrationUser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Class not found error: " + ex.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Logger.getLogger(RegistrationUser.class.getName()).log(Level.SEVERE, null, ex);
        }

        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            out.print(jsonResponse);
            out.flush();
        }
    }
}
