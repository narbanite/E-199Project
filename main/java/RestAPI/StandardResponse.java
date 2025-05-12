/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package RestAPI;

import com.google.gson.JsonElement;

/**
 *
 * @author nikol
 */
public class StandardResponse {
    private String message;
    private JsonElement data;

  
    public StandardResponse(String message) {
        this.message = message;
    }

    public StandardResponse(JsonElement data) {
        this.data = data;
    }

   

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public JsonElement getData() {
        return data;
    }

    public void setData(JsonElement data) {
        this.data = data;
    }
}
