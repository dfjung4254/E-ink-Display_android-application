package com.jcp.magicapplication;

import androidx.annotation.LongDef;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jcp.magicapplication.Session.SessionJWT;
import com.jcp.magicapplication.http.HttpAsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class WeatherActivity extends AppCompatActivity {

    private Button btnBack;
    private TextView txtLatitude;
    private TextView txtLongitude;
    private TextView txtCity;
    private TextView txtWeather;
    private TextView txtWeatherDesc;
    private TextView txtTemp;
    private TextView txtTempMax;
    private TextView txtTempMin;
    private TextView txtPressure;
    private TextView txtHumidity;
    private TextView txtWindSpeed;
    private TextView txtClouds;

    private final int REQUEST_CODE_GPS_PERMISSION = 3;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double latitude = 0;
    private double longitude = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        /* view 연결 */
        btnBack = findViewById(R.id.WeatherActivity_Button_goBack);
        txtLatitude = findViewById(R.id.WeatherActivity_TextView_latitude);
        txtLongitude = findViewById(R.id.WeatherActivity_TextView_longitude);
        txtCity = findViewById(R.id.WeatherActivity_TextView_city);
        txtWeather = findViewById(R.id.WeatherActivity_TextView_weather);
        txtWeatherDesc = findViewById(R.id.WeatherActivity_TextView_weatherDescription);
        txtTemp = findViewById(R.id.WeatherActivity_TextView_temperature);
        txtTempMax = findViewById(R.id.WeatherActivity_TextView_temperatureMax);
        txtTempMin = findViewById(R.id.WeatherActivity_TextView_temperatureMin);
        txtPressure = findViewById(R.id.WeatherActivity_TextView_pressure);
        txtHumidity = findViewById(R.id.WeatherActivity_TextView_humidity);
        txtWindSpeed = findViewById(R.id.WeatherActivity_TextView_windSpeed);
        txtClouds = findViewById(R.id.WeatherActivity_TextView_clouds);

        /* 뒤로가기 */
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        /* 현재 위치 구함 */
        Location userLocation = getMyLocation();
        if( userLocation != null ) {
            latitude = userLocation.getLatitude();
            longitude = userLocation.getLongitude();
        }

        /* 요청 파라미터 설정 */
        String requestParameter = latitude + "/" + longitude;

        /* http 연결 및 UI 수정 */
        new HttpAsyncTask(HttpAsyncTask.GET_WEATHER, null, requestParameter, new HttpAsyncTask.AsyncResponse() {

            /*

                요청에 대한 json 응답을 받았을 때
                이 함수 내부에서 관련 UI 를 처리 한다.

            */

            @Override
            public void processFinish(JSONObject resultData) {

                try{

                    txtLatitude.setText("latitude : " + latitude);
                    txtLongitude.setText("longitude : " + longitude);
                    txtCity.setText("city : " + resultData.getString("city"));
                    txtWeather.setText("weather : " + resultData.getString("weather"));
                    txtWeatherDesc.setText("weatherDescription : " + resultData.getString("weather_description"));
                    txtTemp.setText("temperature : " + resultData.getString("temperature"));
                    txtTempMax.setText("temperatureMax : "+resultData.getString("temperature_max"));
                    txtTempMin.setText("temperatureMin : " +resultData.getString("temperature_min"));
                    txtPressure.setText("pressure : " + resultData.getString("pressure"));
                    txtHumidity.setText("humidity : " + resultData.getString("humidity"));
                    txtWindSpeed.setText("windSpeed : " + resultData.getString("wind_speed"));
                    txtClouds.setText("clouds : " + resultData.getString("clouds"));

                }catch(Exception e){
                    e.printStackTrace();
                }

            }

        }).execute();

    }


    private Location getMyLocation() {

        setGPS();

        Location currentLocation = null;
        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 사용자 권한 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_GPS_PERMISSION);
        }
        else {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            // 수동으로 위치 구하기
            String locationProvider = LocationManager.GPS_PROVIDER;
            currentLocation = locationManager.getLastKnownLocation(locationProvider);
            if (currentLocation != null) {
                double lng = currentLocation.getLongitude();
                double lat = currentLocation.getLatitude();
            }
        }
        return currentLocation;
    }

    private void setGPS() {
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
    }

    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {
        if (requestCode == REQUEST_CODE_GPS_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // success!
                Location userLocation = getMyLocation();
                if( userLocation != null ) {
                    latitude = userLocation.getLatitude();
                    longitude = userLocation.getLongitude();
                }
            } else {
                // Permission was denied or request was cancelled

            }
        }
    }

}
