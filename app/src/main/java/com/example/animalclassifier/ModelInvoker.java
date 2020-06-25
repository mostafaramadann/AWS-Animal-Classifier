package com.example.animalclassifier;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ModelInvoker extends AsyncTask<Void,Void,String> {
    private String body = "{\"name\": \"img.png\"}";
    private String apiURL = "https://kd18k9hoti.execute-api.us-east-1.amazonaws.com/invoke/model";
    private String Method = "POST";
    public static String classificationString="";
    public ModelInvoker() {
        super();
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {

            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(Method);
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {
                byte[] input = body.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine = null;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                System.out.println(response.toString());
                return response.toString();
            }

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onPreExecute() {
        MainActivity.classificationText.setText(ModelInvoker.classificationString);
        super.onPreExecute();
    }


    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }


    @Override
    protected void onCancelled() {
        super.onCancelled();
    }



    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getApiURL() {
        return apiURL;
    }

    public void setApiURL(String apiURL) {
        this.apiURL = apiURL;
    }

    public String getMethod() {
        return Method;
    }

    @Override
    protected void onPostExecute(String s) {
        classificationString=s;
        MainActivity.classificationText.setText(s);
    }

    public void setMethod(String method) {
        Method = method;
    }
}
