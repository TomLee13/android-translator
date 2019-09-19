package task1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.json.*;

/**
 *
 * @author limingyang
 */

// A simple class to wrap a result. Code is from lab.
class Result {
    String value;
    
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
}

// a class that get translation based on the user input from a dictionary api.
public class TranslatorAPIGetter {
    
    // read a value associated with a name from the server
    // return either the value read or an error message
    public static JSONObject translate(String text, String language) throws IOException, JSONException {
        System.out.println("translating");
        Result r = new Result();
        int status = 0;
        if((status = doGet(text, language, r)) != 200) {
            System.out.println("Error from server "+ status);
            return null;
        }
        return new JSONObject(r.getValue());
    }
    
    public static int doGet(String text, String language, Result r) throws IOException, JSONException {
        // Make an HTTP GET passing the name on the URL line
        //JSONObject json = new JSONObject("");
        r.setValue("");
        String response = "";       
        HttpURLConnection conn;
        int status = 0;
        
        String endpoint = "http://fy.iciba.com/ajax.php?a=fy";
        String f = "en"; // language to be translated
        String t = "";
        if (language.equals("spanish")) {
            t = "es";
        } else if (language.equals("french")){
            t = "fr";
        }
        // form the url string as the API requires
        String urlStr = endpoint + "&f=" + f + "&t=" + t + "&w=" + text;
        System.out.println(urlStr);
        
        try {           
            // pass the name on the URL line
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // tell the server what format we want back
            conn.setRequestProperty("Accept", "text/plain");
            
            // wait for response
            status = conn.getResponseCode();
            
            // If things went poorly, don't try to read any response, just return.
            if (status != 200) {
                // not using msg
                String msg = conn.getResponseMessage();
                return conn.getResponseCode();
            }
            String output = "";
            // things went well so let's read the response
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));
            // write the reponse
            while ((output = br.readLine()) != null) {
                response += output;
            }
            
            // set the response object
            r.setValue(response);
            conn.disconnect();
            
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }   catch (IOException e) {
            e.printStackTrace();
        }
        
        // return value from server        
        // return HTTP status to caller
        return status;
    }
}
