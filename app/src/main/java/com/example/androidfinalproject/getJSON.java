package com.example.androidfinalproject;

//Code lay JSON file tham khao tai https://www.simplifiedcoding.net/json-parsing-in-android/

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

class GetJSON extends AsyncTask<Void, Void, String> {
    String urlWebService;
    public String rawJSON;

    public GetJSON(String x) {
        urlWebService=x;

    }

    public String getRawJSON(){
        return rawJSON;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);



//        Log.d("raw json",rawJSON);
        //From string to Obj
        try {

            JSONObject obj = new JSONObject(rawJSON);

            //Log.d("My App", obj.toString());

            String status = obj.getString("status");

            Log.d("letmeseethestatus:",status);



        } catch (Throwable t) {
            Log.e("My App", "Could not parse malformed JSON: \"" + rawJSON + "\"");
        }


    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            URL url = new URL(urlWebService);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            StringBuilder sb = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String json;


            while ((json = bufferedReader.readLine()) != null) {
                sb.append(json + "\n");
            }


            rawJSON= sb.toString().trim();

            return sb.toString().trim();



        }


        catch (Exception e) {
            return null;
        }
    }


}


