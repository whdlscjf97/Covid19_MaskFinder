package com.floreerin.maskfinder;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";
    public static final int LOAD_SUCCESS = 101;

    private String SEARCH_URL = "https://8oi9s0nnth.apigw.ntruss.com/corona19-masks/v1";
    private String STORESBYGEO = "/storesByGeo/json?";
    private String LAT = "lat="; // 테스트를 위한 고정
    private String LNG = "&lng="; // 테스트를 위한 고정
    private String METER = "&m="; // 테스트를 위한 고정

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Intent mapIntent = getIntent();
        String address = mapIntent.getStringExtra("postAddress"); // get postAddress from MainActivity
        Double lat = mapIntent.getDoubleExtra("lat",0); // get lat from MainActivity
        Double lng = mapIntent.getDoubleExtra("lng", 0); // get lng from MainActivity
        int meter = mapIntent.getIntExtra("postFindMeter",0);  // get postFindMeter from MainActivity

        mMap = googleMap;

        String request_url = SEARCH_URL + STORESBYGEO + LAT + lat + LNG + lng + METER + meter; // 인텐트로 받은 정보로 json url 만들기
        getJSON(request_url);

        try{ // 자신의 위치를 마커로 표시
            LatLng latLng = new LatLng(lat,lng);

            MarkerOptions findMakerOptions = new MarkerOptions(); // 사용자가 입력한 주소의 마커를 찍는다
            findMakerOptions.title(address);
            findMakerOptions.snippet(address);
            findMakerOptions.position(latLng);

            CircleOptions circleMeter = new CircleOptions();
            circleMeter.center(latLng);
            circleMeter.radius(meter).strokeWidth(0f).fillColor(Color.parseColor("#880000ff"));

            mMap.addMarker(findMakerOptions);
            mMap.addCircle(circleMeter);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));

        } catch (Exception e){
            e.getStackTrace();
        }
    }

    private void getJSON(final String request_url) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String result;
                try{
                    Log.d(TAG, request_url);
                    URL url = new URL(request_url);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setReadTimeout(3000);
                    httpURLConnection.setConnectTimeout(3000);
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
                    Log.d("TAG,","JsonParser OK");
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
            JSONArray storeInfos = jsonObject.getJSONArray("stores"); // arraylist에 있는 stores를 가져옴

            for (int i = 0; i < storeInfos.length(); i++){
                JSONObject storeInfosListKey = storeInfos.getJSONObject(i);

                String addr = storeInfosListKey.getString("addr"); // 주소 (Type : String)
                String code = storeInfosListKey.getString("code"); // 약국 코드 (Type : String)
                String created_at = storeInfosListKey.getString("created_at"); // 데이터 생성 일자 (업데이트 일자) (Type : String)
                String lat = storeInfosListKey.getString("lat"); // 위도 (Type : float)
                String lng = storeInfosListKey.getString("lng"); // 경도 (Type : float)
                String name = storeInfosListKey.getString("name"); // 약국 이름 (Type : String)
                String remain_stat = storeInfosListKey.getString("remain_stat"); // 재고 상태 (Type : String)
                // [100개 이상(녹색): 'plenty' / 30개 이상 100개미만(노랑색): 'some' / 2개 이상 30개 미만(빨강색): 'few' / 1개 이하(회색): 'empty']
                // 가장 중요!! 정부 공식 메뉴얼
                String stock_at = storeInfosListKey.getString("stock_at"); // 마스크 입고 시간 (Type : String)
                String type = storeInfosListKey.getString("type"); // 업체 타입  (Type : String) (약국(01), 농협(02), 우체국(03) 공적마스크 제공업체 타입)

                System.out.println("업데이트 시간 : " + created_at + " 약국 주소 : " + addr + " 약국 코드 : " + code + " 약국 위도 : " + lat +
                        " 약국 경도 : " + lng + " 약국 이름 : " + name + " 마스크 재고 상태 : " + remain_stat + " 마스크 입고 시간 " + stock_at +
                        " 업체 타입 : " + type  );
            }
            return true;
        } catch (JSONException e){
            Log.d(TAG, e.toString());
        }
        return false;
    }

}
