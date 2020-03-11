package com.floreerin.maskfinder;

import android.app.ProgressDialog;
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


public class JsonGeoTestActivity extends AppCompatActivity {

    private static final String TAG = "JsonGeoTestActivity";
    public static final int LOAD_SUCCESS = 101;

    private String SEARCH_URL = "https://8oi9s0nnth.apigw.ntruss.com/corona19-masks/v1";
    private String STORESBYGEO = "/storesByGeo/json?";
    private String LAT = "lat="; // 테스트를 위한 고정
    private String LNG = "&lng="; // 테스트를 위한 고정
    private String METER = "&m=";

    private ProgressDialog progressDialog;
    private TextView tv_jsonmask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jsontest2);

        tv_jsonmask = findViewById(R.id.tv_jsonmask);

        progressDialog = new ProgressDialog(JsonGeoTestActivity.this);
        progressDialog.setMessage("please wait....");
        progressDialog.show();

        String url = SEARCH_URL + STORESBYGEO + LAT + "37.6565184" + LNG + "126.6760681" + METER + "3000"; // 테스트를 위한 고정 값 지정
        try{
            JsonAsyncTask jsonAsyncTask = new JsonAsyncTask(url);
            String result = jsonAsyncTask.execute().get(); // jsonAsyncTask 실행
            if(jsonParser(result)) {
                    Message message = mHandler.obtainMessage(LOAD_SUCCESS, result);
                    mHandler.sendMessage(message);
                }
        } catch (Exception e){
            e.getStackTrace();
        }

    }

    private final MyHandler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private final WeakReference<JsonGeoTestActivity> weakReference;

        public MyHandler(JsonGeoTestActivity jsonTestActivity) {
            weakReference = new WeakReference<JsonGeoTestActivity>(jsonTestActivity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            JsonGeoTestActivity jsonTestActivity = weakReference.get();

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


    private boolean jsonParser(String jsonString) {
        if(jsonString == null)
            return false;

        try{
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray storeInfos = jsonObject.getJSONArray("stores"); // arraylist에 있는 stores를 가져옴
            int list_cnt = storeInfos.length();

            String[] getAddr = new String[list_cnt];
            String[] getcode = new String[list_cnt];
            String[] getcreated_at  = new String[list_cnt];
            String[] getlat = new String[list_cnt];
            String[] getlng = new String[list_cnt];
            String[] getname = new String[list_cnt];
            String[] getremain_stat = new String[list_cnt];
            String[] getstock_at = new String[list_cnt];
            String[] gettype = new String[list_cnt];

            for (int i = 0; i < list_cnt; i++){
                JSONObject storeInfosListKey = storeInfos.getJSONObject(i);

                getAddr[i] = storeInfosListKey.getString("addr");
                getcode[i] = storeInfosListKey.getString("code"); // 약국 코드 (Type : String)
                getcreated_at[i] = storeInfosListKey.getString("created_at"); // 데이터 생성 일자 (업데이트 일자) (Type : String)
                getlat[i] = storeInfosListKey.getString("lat"); // 위도 (Type : float)
                getlng[i] = storeInfosListKey.getString("lng"); // 경도 (Type : float)
                getname[i] = storeInfosListKey.getString("name"); // 약국 이름 (Type : String)
                getremain_stat[i] = storeInfosListKey.getString("remain_stat"); // 재고 상태 (Type : String)
                // [100개 이상(녹색): 'plenty' / 30개 이상 100개미만(노랑색): 'some' / 2개 이상 30개 미만(빨강색): 'few' / 1개 이하(회색): 'empty']
                // 가장 중요!! 정부 공식 메뉴얼
                getstock_at[i] = storeInfosListKey.getString("stock_at"); // 마스크 입고 시간 (Type : String)
                gettype[i] = storeInfosListKey.getString("type"); // 업체 타입  (Type : String) (약국(01), 농협(02), 우체국(03) 공적마스크 제공업체 타입)

                System.out.println("업데이트 시간 : " + getcreated_at[i]);
            }
            return true;
        } catch (JSONException e){
            Log.d(TAG, e.toString());
        }
        return false;
    }
}
