package com.jcp.magicapplication.http;

import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import com.jcp.magicapplication.Session.SessionJWT;

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

    로그인 후 JSON 방식으로
    POST 요청을 할 때 이 클래스를 사용한다.
    인증을 위한 토큰은 로그인이 된 상태를 가정하므로
    SessionJWT 에서 호출한다.

*/

public class JSONPost extends AsyncTask<JSONObject, JSONObject, JSONObject> {

    /* 멤버 변수 */
    private String strUrl;              // 서버로 연결할 서버 URL

    /*

        생성자
        서버로 보낼 URL 을 미리 받는다.

    */
    public JSONPost(String strUrl){
        super();
        this.strUrl = strUrl;
    }

    /*

        백그라운드에서 비동기적으로
        Http Request 와 Response 를 수행한다.

    */

    @Override
    protected JSONObject doInBackground(JSONObject... objects) {

        try{

            /*

                Request 에 실어 보낼 요소들을 모아
                JSON 객체를 받아 옴.

            */
            JSONObject jsonObject = objects[0];
            String sJwt = SessionJWT.getInstance().getJwt();

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try{

                /* 2번째 parameter 로 넘어온 URL 값으로 생성한 다음 해당 통신 오픈  */
                URL url = new URL(strUrl);
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
                writer.write(jsonObject.toString());
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
                Log.d("*****MYTAG", "JWT : "+ret.toString());
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

        /*

            TODO : 받은 JWT 로 세션 관리하기.
            관련 스터디 진행 해서 바로 구현 할 것.

        */


    }

}
