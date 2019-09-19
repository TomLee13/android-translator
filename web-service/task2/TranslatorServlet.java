/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package task2;

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
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.ServerAddress;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

import org.bson.Document;
import java.util.Arrays;
import com.mongodb.Block;
import com.mongodb.client.AggregateIterable;

import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import static com.mongodb.client.model.Filters.*;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.DeleteResult;
import static com.mongodb.client.model.Updates.*;
import com.mongodb.client.result.UpdateResult;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.RequestDispatcher;

/**
 *
 * @author limingyang
 */
@WebServlet(name = "TranslatorServlet",
        urlPatterns = {"/Translator/*", "/Dashboard"})
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
        
        //String operationID = ""; // ID given by MongoDB to each document
        long receivedFromApp = 0; // system time when message is received from the app
        String textFromApp = ""; // text to be translated received from the app
        String destLanguage = "";
        long sentToAPI = 0; // system time when the message is sent to the 3rd party API
        long receivedFromAPI = 0; // system time when reply is received from the 3rd party API
        long sentToApp = 0; // system time when reply is sent back to the app
        
        // update time when message is received from the app
        receivedFromApp = System.currentTimeMillis();
        
        // connect to MongoDB
        MongoClientURI uri = new MongoClientURI(
                "mongodb://Ryougi:Tay-nina-13@project4task2-shard-00-00-h5zza.mongodb.net:27017,project4task2-shard-00-01-h5zza.mongodb.net:27017,project4task2-shard-00-02-h5zza.mongodb.net:27017/test?ssl=true&replicaSet=Project4Task2-shard-0&authSource=admin&retryWrites=true");
        MongoClient mongoClient = new MongoClient(uri);
        MongoDatabase database = mongoClient.getDatabase("mydb");
        // get the log collection from the database
        MongoCollection<Document> collection = database.getCollection("log");
        System.out.println("database connected");
        
        // get the servlet path
        String servletPath = request.getServletPath();
        if (servletPath.equals("/Dashboard")) {
            System.out.println("Viewing dashboard");
            System.out.println("total number of documents: " + collection.count());
            
            // get all the documents in the collection "log"
            // convert it to a list an pass it to the request
            // source: http://www.thejavageek.com/2015/08/24/retrieve-documents-from-mongodb-using-java/
            List<Document> documents = (List<Document>) collection.find().into(
                    new ArrayList<>());
            request.setAttribute("documentList", documents);
            
            long sumTime = 0;
            for (Document doc : documents) {
                long timeSpentAPI = doc.getLong("receivedFromAPI") - doc.getLong("sentToAPI");
                sumTime += timeSpentAPI;
            }
            System.out.println("time spent on API: " + sumTime);
            request.setAttribute("timeAPI", sumTime);
            
            // find the most popular text to be translated           
            Document firstText = mostPopular(collection, "text");
            if (firstText != null) {
                System.out.println(firstText.toJson());
                System.out.println("most popular text: " + firstText.get("_id"));
                request.setAttribute("mostPopularText", firstText.get("_id"));
            }
            
            // find the most popular text to be translated
            Document firstLanguage = mostPopular(collection, "destination");
            if (firstLanguage != null) {
                System.out.println("most popular language: " + firstLanguage.get("_id"));
                request.setAttribute("mostPopularLanguage", firstLanguage.get("_id"));
            }
                       
            // Transfer control over the the correct "view"
            String nextView = "dashboard.jsp";
            RequestDispatcher view = request.getRequestDispatcher(nextView);
            view.forward(request, response);
        } else {
            String path = request.getPathInfo().toLowerCase();
            System.out.println(path);
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
            // update text to be translated from the app
            textFromApp = text;
            System.out.println(text);
            String language = data[1];
            // update destination language from the app
            destLanguage = language;
            System.out.println(language);
            
            // call the api translator to get the translation of the user's input
            TranslatorAPIGetter getter = new TranslatorAPIGetter();
            try {
                // update time when message is sent to the API
                sentToAPI = System.currentTimeMillis();
                JSONObject result = getter.translate(text, language);
                // update time when reply from API is received
                receivedFromAPI = System.currentTimeMillis();
                System.out.println(result.toString());
                // Things went well so set the HTTP response code to 200 OK
                response.setStatus(200);
                // tell the client the type of the response
                response.setContentType("text/plain;charset=UTF-8");
                PrintWriter out = response.getWriter();
                out.println(result);
                // update time when translation is sent back to the app
                sentToApp = System.currentTimeMillis();
                
                // insert the full information about the current operation to MongoDB
                Document doc = new Document("text", textFromApp)
                        .append("destination", destLanguage)
                        .append("receivedFromApp", receivedFromApp)
                        .append("sentToAPI", sentToAPI)
                        .append("receivedFromAPI", receivedFromAPI)
                        .append("sentToApp", sentToApp);
                collection.insertOne(doc);
                
            } catch (IOException i){
                i.printStackTrace();
            } catch (JSONException j) {
                j.printStackTrace();
            }
        }
        
    }
    
    // find the most popular text to be translated
    // source: https://www.programcreek.com/java-api-examples/index.php?api=com.mongodb.client.AggregateIterable
    private Document mostPopular(MongoCollection collection, String maxField) {
        AggregateIterable<Document> popular = collection.aggregate(
                Arrays.asList(
                        Aggregates.group("$" + maxField, Accumulators.sum("count", 1)),
                        Aggregates.sort(Sorts.descending("count"))
                )
        );
        return popular.first();
    }
    
}
