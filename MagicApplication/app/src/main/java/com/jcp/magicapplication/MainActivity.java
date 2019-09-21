package com.jcp.magicapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jcp.magicapplication.Session.SessionJWT;
import com.jcp.magicapplication.http.HttpAsyncTask;

import org.json.JSONArray;
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

/* TODO Author : 정근화 */

/*

    로그인 후 앱의 메인 액티비티이다.

    본 액티비티는 샘플로 로그인정보를 끌어와 로그인이 성공했는지 테스트 하고
    로그아웃의 여부를 테스트 하는 임시 액티비티이다.

*/

public class MainActivity extends AppCompatActivity {

    private TextView txtName;
    private TextView txtCalendar;
    private Button btnWeather;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtName = findViewById(R.id.MainActivity_TextView_Name);
        txtCalendar = findViewById(R.id.MainActivity_TextView_Calendar);
        btnWeather = findViewById(R.id.MainActivity_Button_weatherTest);



        btnWeather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, WeatherActivity.class));
            }
        });

        new HttpAsyncTask(HttpAsyncTask.GET_USER_INFO, null, null, new HttpAsyncTask.AsyncResponse() {
            @Override
            public void processFinish(JSONObject resultData) {
                /* UI 구성에 사용할 value 값 */
                String txtChangeName = "no changed";

                /* jsonObject 에서 value 값을 추출한다. */
                try {

                    txtChangeName = resultData.getString("email");

                    Log.d("****txtChangeName1 : ", txtChangeName);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                txtName.setText(txtChangeName);
            }
        }).execute();

        new HttpAsyncTask(HttpAsyncTask.GET_CALENDAR_NEXT, null, "10", new HttpAsyncTask.AsyncResponse() {
            @Override
            public void processFinish(JSONObject resultData) {
                Log.d("txtCalendarChange : ", resultData.toString());
                String txtCalendarChange = "no changed calendar";
                try{
                    JSONArray arr = resultData.getJSONArray("days");
                    txtCalendarChange = "";
                    for(int i = 0; i < arr.length(); i++){
                        JSONObject startObj = arr.getJSONObject(i).getJSONObject("start");
                        txtCalendarChange += startObj.getString("date");
                        txtCalendarChange += " [ ";
                        txtCalendarChange += arr.getJSONObject(i).getString("summary");
                        txtCalendarChange += " ] ";
                        txtCalendarChange += '\n';
                    }

                }catch(Exception e){
                    e.printStackTrace();
                }

                txtCalendar.setText(txtCalendarChange);
            }
        }).execute();

    }

}
