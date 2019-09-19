package edu.cmu.mingyan2.project4android;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A simple wrapper class
 * Idea is from the lab
 */
class Result {
    String value;

    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
}

/**
 * This class this very similar to the GetPicture class in the Android lab.
 * It is a client to the web-service.
 * It acts as the backend model to process the data returned from the web-service.
 */
public class GetTranslation {
    Translator t = null;

    public void translate(String text, String language, Translator t) {
        System.out.println("translating");
        // form the message to be sent to the web-service
        String message = text + "," + language;
        System.out.println(message);
        this.t = t;
        new AsyncTranslator().execute(message);
    }

    private class AsyncTranslator extends AsyncTask<String, Void, String> {

        @Override
        // inherited from the parent class
        protected String doInBackground(String... urls) {
            System.out.println("doInBackground hit");
            return translate(urls[0]);
        }

        @Override
        // inherited from the parent class
        protected void onPostExecute (String result) {
            System.out.println("result is " + result);
            t.translationReady(result);
        }

        private String translate(String message) {
            // heroku url for task 1
            //String webservice = "https://stormy-headland-79242.herokuapp.com/Translator" + "//" + message;
            // heroku url for task 2
            String webservice = "https://arcane-cove-71574.herokuapp.com/Translator" + "//" + message;
            System.out.println(webservice);
            Result r = new Result();
            try {
                // receive message back from the web-service and store it in the r
                int status = doGET(webservice, r);
                if (status != 200) {
                    return "Error from server "+ status;
                }
                // get the JSON value from the web-service
                JSONObject result = new JSONObject(r.getValue());
                System.out.println(result.toString());
                System.out.println(result.get("status"));
                if ((int)(result.get("status")) == 1) {
                    // if things go well
                    // parse the JSON to get the translation
                    return result.getJSONObject("content").getString("out");
                } else {
                    // if something bad happens
                    // return an error code to the Translator object
                    return "N";
                }
            } catch (JSONException j) {
                j.printStackTrace();
            }

            return null;
        }

        protected int doGET(String webservice, Result r) throws JSONException {
            r.setValue("");
            String response = "";
            HttpURLConnection conn;
            int status = 0;

            try {
                // pass the name on the URL line
                URL url = new URL(webservice);
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

                while ((output = br.readLine()) != null) {
                    response += output;
                }
                conn.disconnect();
                System.out.println("response: " + response);
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }   catch (IOException e) {
                e.printStackTrace();
            }

            r.setValue(response);
            return status;
        }
    }
}
