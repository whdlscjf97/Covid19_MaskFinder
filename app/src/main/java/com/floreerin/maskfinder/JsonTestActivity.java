package com.floreerin.maskfinder;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class JsonTestActivity extends AppCompatActivity {

    private static final String TAG = "JsonTestActivity";
    public static final int LOAD_SUCCESS = 101;

    private String SEARCH_URL = "https://8oi9s0nnth.apigw.ntruss.com/corona19-masks/v1";
    private String STORE = "/stores/json?";
    private String PAGE = "page=1";
    private String PER_PAGE = "&perPage=500";
    private String REQUEST_URL = SEARCH_URL + STORE + PAGE + PER_PAGE;

    private ProgressDialog progressDialog;
    private TextView tv_jsonmask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jsontest);

        tv_jsonmask = findViewById(R.id.tv_jsonmask);

        progressDialog = new ProgressDialog(JsonTestActivity.this);
        progressDialog.setMessage("please wait....");
        progressDialog.show();

        getJSON();

    }

    private final MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private final WeakReference<JsonTestActivity> weakReference;

        public MyHandler(JsonTestActivity jsonTestActivity) {
            weakReference = new WeakReference<JsonTestActivity>(jsonTestActivity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            JsonTestActivity jsonTestActivity = weakReference.get();

            if (jsonTestActivity != null) {
                switch (msg.what) {
                    case LOAD_SUCCESS:
                        jsonTestActivity.progressDialog.dismiss();

                        String jsonString = (String) msg.obj;

                        jsonTestActivity.tv_jsonmask.setText(jsonString);
                        break;
                }
            }
        }
    }

    private void getJSON() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String result;

                try{
                    Log.d(TAG, REQUEST_URL);
                    URL url = new URL(REQUEST_URL);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setReadTimeout(3000);
                    httpURLConnection.setConnectTimeout(3000);
//                    httpURLConnection.setDoOutput(true);
//                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setUseCaches(false);
                    httpURLConnection.connect();

                    int responseStatusCode = httpURLConnection.getResponseCode();

                    InputStream inputStream;
                    if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                        inputStream = httpURLConnection.getInputStream();
                    } else {
                        inputStream = httpURLConnection.getErrorStream();
                    }

                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                    StringBuilder sb = new StringBuilder();
                    String line;


                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }

                    bufferedReader.close();
                    httpURLConnection.disconnect();

                    result = sb.toString().trim();
                } catch (Exception e){
                    result = e.toString();
                }
                if(jsonParser(result)) {
                    Message message = mHandler.obtainMessage(LOAD_SUCCESS, result);
                    mHandler.sendMessage(message);
                }
            }
        });
        thread.start();
    }

    private boolean jsonParser(String jsonString) {
        if(jsonString == null)
            return false;

        try{
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray storeInfos = jsonObject.getJSONArray("storeInfos"); // arraylist에 있는 storeinfos를 가져옴

            for (int i = 0; i < storeInfos.length(); i++){
                JSONObject storeInfosListKey = storeInfos.getJSONObject(i);

                String addr = storeInfosListKey.getString("addr"); // 주소
                String code = storeInfosListKey.getString("code"); // 약국 코드
                String lat = storeInfosListKey.getString("lat"); // 위도
                String lng = storeInfosListKey.getString("lng"); // 경도
                String name = storeInfosListKey.getString("name"); // 약국 이름
                String type = storeInfosListKey.getString("type"); // 업체 타입 (약국, 농협, 우체국 등 공적마스크 제공업체 타입)
            }
            return true;
        } catch (JSONException e){
            Log.d(TAG, e.toString());
        }
        return false;
    }
}
