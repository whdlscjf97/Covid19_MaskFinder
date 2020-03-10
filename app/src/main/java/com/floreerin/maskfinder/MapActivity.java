package com.floreerin.maskfinder;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

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

        try{
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
}
