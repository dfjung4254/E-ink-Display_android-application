package com.jcp.magicapplication.http;

import android.os.AsyncTask;
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

public class JSONLoginPost extends AsyncTask<String, String, String> {

    /* 멤버 변수 */
    private String strUrl;              // 서버로 연결할 서버 URL

    /*

        생성자
        서버로 보낼 URL 을 미리 받는다.

    */
    public JSONLoginPost(String strUrl){
        super();
        this.strUrl = strUrl;
    }

    /*

        백그라운드에서 비동기적으로
        Http Request 와 Response 를 수행한다.

    */

    @Override
    protected String doInBackground(String... tokens) {

        try{

            /*

                Request 에 실어 보낼 요소들을 모아
                JSON 객체를 생성

            */
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("id_token", tokens[0]);

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try{

                /* 2번째 parameter 로 넘어온 URL 값으로 생성한 다음 해당 통신 오픈  */
                URL url = new URL(strUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");                                // 전송 방식 : POST
                connection.setRequestProperty("Cache-Control", "no-cache");         // 캐시 설정
                connection.setRequestProperty("Content-Type", "application/json");  // JSON 형식으로 전송
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
                StringBuffer jwt = new StringBuffer();
                String line = "";
                while((line = reader.readLine()) != null){
                    jwt.append(line);
                }

                /* 읽은 버퍼 값(JWT 토큰 값)을 리턴 */
                Log.d("*****MYTAG", "JWT : "+jwt.toString());
                return jwt.toString();

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
        response 받은 JWT 토큰을 이용해 로그인 처리한다.

    */
    @Override
    protected void onPostExecute(String jwt) {
        super.onPostExecute(jwt);

        /*

            TODO : 받은 JWT 로 세션 관리하기.
            관련 스터디 진행 해서 바로 구현 할 것.

        */

        /* SessionJWT 에 서버에서 전송받은 jwt를 보관한다. */
        SessionJWT.getInstance().setJwt(jwt);

    }
}

/* TODO Author : 정근화 */

/*

    구글 로그인 시 id_token 을
    Node 서버로 전송하고 JWT 를 받는 통신

    new JSONLoginPost(서버 URL).execute(id_token);

    으로 사용

*/
