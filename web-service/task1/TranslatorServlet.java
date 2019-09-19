/*
* This class is a servlet for Project4 Task1
* It receives request from the Android app and pass the message sent by the app
* to the 3rd party API, and reply the message sent back from the API to the app.
*/
package task1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.*;

/**
 *
 * @author limingyang
 */
@WebServlet(name = "TranslatorServlet",
        urlPatterns = {"/Translator/*"})
public class TranslatorServlet extends HttpServlet {
    
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println("doGet hit");
        
        String path = request.getPathInfo().toLowerCase();
        //System.out.println(path);
        // split the path to get the user input
        String[] input = path.split("/");
        // if there's no user input
        if (input.length < 2) {
            response.setStatus(401);
            return;
        }
        // extract the user input
        String message = input[1];
        String[] data = message.split(",");
        // if there's no required input of text and destination language
        if (data.length < 2) {
            response.setStatus(401);
            return;
        }
        String text = data[0];
        //System.out.println(text);
        String language = data[1];
        //System.out.println(language);
        
        // call the api translator to get the translation of the user's input
        TranslatorAPIGetter getter = new TranslatorAPIGetter();
        try {
            JSONObject result = getter.translate(text, language);
            System.out.println(result.toString());
            // Things went well so set the HTTP response code to 200 OK
            response.setStatus(200);
            // tell the client the type of the response
            response.setContentType("text/plain;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println(result);
        } catch (IOException i){
            i.printStackTrace();
        } catch (JSONException j) {
            j.printStackTrace();
        }
        
        
        
    }
    
    
}
