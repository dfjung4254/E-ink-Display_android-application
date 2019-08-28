package com.jcp.magicapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.jcp.magicapplication.Session.SessionJWT;

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

/* TODO Author : 정근화 */

/*

    로그인 후 앱의 메인 액티비티이다.

    본 액티비티는 샘플로 로그인정보를 끌어와 로그인이 성공했는지 테스트 하고
    로그아웃의 여부를 테스트 하는 임시 액티비티이다.

*/

public class MainActivity extends AppCompatActivity {

    private TextView txtName;
    private TextView txtCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtName = findViewById(R.id.MainActivity_TextView_Name);
        txtCalendar = findViewById(R.id.MainActivity_TextView_Calendar);

        new JSONPost(JSONPost.GET_USER_INFO, null).execute();
        new JSONPost(JSONPost.GET_CALENDAR, null).execute();


    }

    /*

        로그인 후 JSON 방식으로
        POST 요청을 할 때 이 클래스를 사용한다.
        인증을 위한 토큰은 로그인이 된 상태를 가정하므로
        SessionJWT 에서 호출한다.

    */

    private class JSONPost extends AsyncTask<String, JSONObject, JSONObject> {

        /* 멤버 변수 */
        private String sJwt;                                                                // 서버로 요청할 JWT 인증값
        private final String SERVER_URL = getResources().getString(R.string.server_url);                           // 서버로 연결할 서버 URL
        private final String SERVER_DIR[] = {
            "/users/info",                                                                  // GET_USER_INFO = 0
            "/calendar/next"                                                                // GET_CALENDAR  = 1
        };
        private int option;                                                                 // 서버로 요청할 메서드
        private JSONObject reqBody;
        public static final int GET_USER_INFO = 0;                                          // 유저를 받아옴
        public static final int GET_CALENDAR = 1;                                           // 달력 10일을 받아옴.

        /*

            생성자
            서버로 보낼 요청정보를 미리 받는다.

        */
        public JSONPost(int option, JSONObject reqBody){
            super();
            this.option = option;
            this.sJwt = SessionJWT.getInstance().getJwt();
            this.reqBody = reqBody;
        }

        /*

            백그라운드에서 비동기적으로
            Http Request 와 Response 를 수행한다.

        */

        @Override
        protected JSONObject doInBackground(String... objects) {

            try{

                /*

                    Request 에 실어 보낼 요소들을 모아
                    JSON 객체를 받아 옴.

                */

                HttpURLConnection connection = null;
                BufferedReader reader = null;

                try{

                    /* 2번째 parameter 로 넘어온 URL 값으로 생성한 다음 해당 통신 오픈  */
                    URL url = new URL(SERVER_URL + SERVER_DIR[option]);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");                                // 전송 방식 : POST
                    connection.setRequestProperty("Cache-Control", "no-cache");         // 캐시 설정
                    connection.setRequestProperty("Content-Type", "application/json");  // JSON 형식으로 전송
                    connection.setRequestProperty("jwt", sJwt);                         // 헤더에 JWT 를 실어서 보냄(인증)
                    connection.setDoOutput(true);                                       // Outputstream 으로 request
                    connection.setDoInput(true);                                        // Inputstream 으로 response
                    connection.connect();

                    /* 서버에 전송하기 위한 스트림 생성 */
                    OutputStream outStream = connection.getOutputStream();

                    /* write 버퍼생성 및 내용 쓰기 */
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                    if(reqBody != null){
                        writer.write(reqBody.toString());
                    }
                    writer.flush();
                    writer.close();

                    /* 서버로 부터 데이터 수신을 위한 스트림 생성 */
                    InputStream inStream = connection.getInputStream();

                    /* read 버퍼생성 및 내용 읽기 */
                    reader = new BufferedReader(new InputStreamReader(inStream));
                    StringBuffer ret = new StringBuffer();
                    String line = "";
                    while((line = reader.readLine()) != null){
                        ret.append(line);
                    }

                    /* 읽은 버퍼 값을 JSONObject 로 변환하여 리턴 */
                    Log.d("*****MYTAG", "RESPONSE : "+ret.toString());
                    return new JSONObject(ret.toString());

                }catch (MalformedURLException e){
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                }finally {

                    /* 연결 후 종료 */
                    if(connection != null){
                        connection.disconnect();
                    }

                    /* read 버퍼도 닫음 */
                    try {

                        if(reader != null){
                            reader.close();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            }catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }

        /*

            doInBackground 에서 서버로 부터
            response 받은 JSONObject 를 사용해 UI 처리를 한다.

        */
        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            switch (option){
                case GET_USER_INFO:
                    /* UI 구성에 사용할 value 값 */
                    String txtChangeName = "no changed";

                    /* jsonObject 에서 value 값을 추출한다. */
                    try {

                        txtChangeName = jsonObject.getString("email");

                        Log.d("****txtChangeName1 : ", txtChangeName);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    txtName.setText(txtChangeName);
                    break;
                case GET_CALENDAR:
                    Log.d("txtCalendarChange : ", jsonObject.toString());
                    String txtCalendarChange = "no changed calendar";
                    try{
                        JSONArray arr = jsonObject.getJSONArray("days");
                        txtCalendarChange = "";
                        for(int i = 0; i < arr.length(); i++){
                            txtCalendarChange += arr.getJSONObject(i).getString("summary");
                            txtCalendarChange += '\n';
                        }

                    }catch(Exception e){
                        e.printStackTrace();
                    }

                    txtCalendar.setText(txtCalendarChange);
                    break;

            }

        }

    }

}
