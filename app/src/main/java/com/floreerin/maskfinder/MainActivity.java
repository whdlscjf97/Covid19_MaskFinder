package com.floreerin.maskfinder;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText edit_address, edit_setmeter;
    private Button btn_maskfind;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edit_address = findViewById(R.id.edit_address);
        edit_setmeter = findViewById(R.id.edit_setmeter);
        btn_maskfind = findViewById(R.id.btn_maskfind);

        final Geocoder geocoder = new Geocoder(this);

        btn_maskfind.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String meter = edit_setmeter.getText().toString();
                String postAddress = edit_address.getText().toString();
                try {
                    int postFindMeter = Integer.parseInt(meter);

                    if (postAddress.equals("") || (postFindMeter >= 10000 || postFindMeter <= 0)) { // 잘못된 주소 null 또는 meter 값 입력 시 오류 로직
                        Toast.makeText(getApplicationContext(), "빈값을 입력하거나 잘못된 영역이 있습니다.", Toast.LENGTH_LONG).show();
                    } else { // 정상적 로직
                        addressGeocoder(postAddress);
                        }
                    } catch (NumberFormatException e){ // 반경거리에 빈 값을 넣었을 경우
                        Toast.makeText(getApplicationContext(),"잘못된 값을 입력했습니다" , Toast.LENGTH_LONG).show();
                    }
                }

            private void addressGeocoder (String postAddress) {
                List<Address> list = null;

                try {
                    list = geocoder.getFromLocationName(postAddress,10);
                } catch (IOException e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"Geocoder 오류 발생" , Toast.LENGTH_LONG).show();
                    Log.e("Error : ", "Geocoder 오류 발생");
                }

                if(list != null){
                    if (list.size() == 0){
                        Toast.makeText(getApplicationContext(),"해당 주소는 없습니다" , Toast.LENGTH_LONG).show();
                    } else {
                      Address addr = list.get(0);
                      double lat = addr.getLatitude();
                      double lng = addr.getLongitude();

                      String location = String.format("geo:%f, %f",lat,lng);
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(location));
                        startActivity(intent);
                    }
                }
            }
        });
    }
}
