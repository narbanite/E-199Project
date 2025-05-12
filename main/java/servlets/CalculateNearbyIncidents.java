package servlets;

import jakarta.servlet.http.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author nikol
 */

public class CalculateNearbyIncidents extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String userLat = request.getParameter("userLat");
        String userLon = request.getParameter("userLon");
        String[] incidentCoordinates = request.getParameterValues("incidentCoordinates");

        JSONArray origins = new JSONArray();
        origins.put(userLat + "," + userLon);

        JSONArray destinations = new JSONArray(incidentCoordinates);

        JSONObject payload = new JSONObject();
        payload.put("origins", origins);
        payload.put("destinations", destinations);

        try {
            URL url = new URL("https://trueway-matrix.p.rapidapi.com/CalculateDrivingMatrix");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-RapidAPI-Key", "YOUR_API_KEY_HERE");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int status = conn.getResponseCode();
            if (status == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
                    StringBuilder responseText = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        responseText.append(line.trim());
                    }

                    JSONObject apiResponse = new JSONObject(responseText.toString());
                    JSONArray distances = apiResponse.getJSONArray("distances");

                    JSONArray nearbyIncidents = new JSONArray();
                    for (int i = 0; i < distances.length(); i++) {
                        if (distances.getJSONArray(i).getDouble(0) <= 30.0) {
                            nearbyIncidents.put(incidentCoordinates[i]);
                        }
                    }

                    response.setContentType("application/json");
                    response.getWriter().write(nearbyIncidents.toString());
                }
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Failed to calculate distances.");
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error occurred while calculating distances.");
        }
    }
}
