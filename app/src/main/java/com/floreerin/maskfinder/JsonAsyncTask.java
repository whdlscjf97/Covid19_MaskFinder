package com.floreerin.maskfinder;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class JsonAsyncTask extends AsyncTask<Void, Void, String> {
    private static final String TAG = "JsonAsyncTask" ;

    String url;
    JsonAsyncTask(String url){
        this.url = url;
    }

    @Override
    protected String doInBackground(Void... strings) {
        String result;
        try{

            URL URLObject = new URL(url);
            HttpURLConnection Conn = (HttpURLConnection) URLObject.openConnection();

            Conn.setReadTimeout(100000);
            Conn.setConnectTimeout(15000);
            Conn.setRequestMethod("GET");
            Conn.setUseCaches(false);
            Conn.connect();

            int responseStatusCode = Conn.getResponseCode();

            InputStream inputStream;
            if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                inputStream = Conn.getInputStream();
            } else {
                inputStream = Conn.getErrorStream();
            }

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line);
            }

            bufferedReader.close();
            Conn.disconnect();

            result = sb.toString();
            return result; // json 내용 결과값 돌려줌

        } catch (Exception e){
            e.getStackTrace();
            return e.toString();
        }
    }
}
