package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 *
 * @author nikol
 */
public class Logout extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("username") != null) {
            session.invalidate();
            response.setStatus(HttpServletResponse.SC_OK); // 200 status code
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 status code
        }
    }

    @Override
    public String getServletInfo() {
        return "Logout servlet for handling user logout requests.";
    }
}
