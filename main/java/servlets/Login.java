package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import database.tables.EditUsersTable;
import database.tables.EditVolunteersTable;
import mainClasses.User;
import mainClasses.Volunteer;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nikol
 */
public class Login extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        HttpSession session = request.getSession();
        EditUsersTable userTable = new EditUsersTable();
        EditVolunteersTable volunteerTable = new EditVolunteersTable();

        JSONObject jsonResponse = new JSONObject();
        response.setContentType("application/json");

        try {

            User user = userTable.databaseToUsers(username, password);
            if (user != null) {

                session.setAttribute("username", user.getUsername());
                session.setAttribute("password", user.getPassword());
                session.setMaxInactiveInterval(120); // Set session timeout to 2 minutes

                jsonResponse.put("success", true);
                jsonResponse.put("userType", "user");
                response.getWriter().write(jsonResponse.toString());
                return;
            }

            Volunteer volunteer = volunteerTable.databaseToVolunteer(username, password);
            if (volunteer != null) {

                session.setAttribute("username", volunteer.getUsername());
                session.setAttribute("password", volunteer.getPassword());
                session.setAttribute("volunteerType", volunteer.getVolunteer_type());
                session.setMaxInactiveInterval(120);

                jsonResponse.put("success", true);
                jsonResponse.put("userType", "volunteer");
                jsonResponse.put("volunteerType", volunteer.getVolunteer_type());
                response.getWriter().write(jsonResponse.toString());
                return;
            }

            jsonResponse.put("success", false);
            jsonResponse.put("message", "Invalid username or password");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(jsonResponse.toString());
        } catch (SQLException | ClassNotFoundException ex) {

            Logger.getLogger(Login.class.getName()).log(Level.SEVERE, null, ex);
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Internal server error. Please try again later.");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(jsonResponse.toString());
        }
    }
}
