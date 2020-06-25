package com.example.animalclassifier;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Database extends AsyncTask<Void,Void,String> {
    private static String name="mostafa";
    private static String password="123456";
    private static int operation=1;
    private String Method = "POST";
    private static Database db = new Database();
    private static String body =  "{\"name\":\""+name+"\","+
            "\"password\":\""+password+"\","+
            "\"operation\":"+operation+
            "}";
    private String apiURL = "https://t93nh3mk4k.execute-api.us-east-1.amazonaws.com/call/database";
private Database()
{}

    @Override
    protected void onPostExecute(String s) {
        Login.login = Boolean.parseBoolean(s);
    }

    public static void setBody(String name, String password, int operation)
    {
        Database.name=name;
        Database.password=password;
        Database.operation=operation;
        Database.body =  "{\"name\":\""+name+"\","+
                "\"password\":\""+password+"\","+
                "\"operation\":"+operation+
                "}";
    }

    public static Database getInstance() {
        return db;
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


}
