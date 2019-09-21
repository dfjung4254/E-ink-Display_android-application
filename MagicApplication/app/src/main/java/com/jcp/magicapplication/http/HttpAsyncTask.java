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

/*

    AsynkTask 분리 작업 -> 각 UI 클래스에서 본 HttpAsyncTask 를 호출한다.
    그리고 본 클래스는 Http 통신 작업을 수행 한 뒤 본 클래스를 호출한 UI 클래스의 UI 를
    바꾼다.

    AsyncTask 클래스 분리에 관한 StackOverflow 참고
    https://stackoverflow.com/questions/12575068/how-to-get-the-result-of-onpostexecute-to-main-activity-because-asynctask-is-a

*/

public class HttpAsyncTask extends AsyncTask<String, JSONObject, JSONObject> {

    // you may separate this or combined to caller class.
    /* UI 클래스에 Callback 함수를 달기 위한 인터페이스 */
    public interface AsyncResponse {
        void processFinish(JSONObject resultData);
    }

    public AsyncResponse delegate = null;

    /* 멤버 변수 */
    private String sJwt;                                                                // 서버로 요청할 JWT 인증값
    private String SERVER_URL = "http://169.56.98.117";                                 // 서버로 연결할 서버 URL

    private int option;                                                                 // 서버로 요청할 메서드
    public static final int GET_USER_INFO = 0;                                          // 유저 정보를 받아오는 절차
    public static final int GET_CALENDAR_NEXT = 1;                                      // 달력 정보를 받아오는 절차
    public static final int GET_WEATHER = 2;                                            // 날씨 정보를 받아오는 절차

    private final String SERVER_DIR[] = {
            "/users/",                                                                  // GET_USER_INFO = 0
            "/calendar/next/",                                                          // GET_CALENDAR_NEXT = 1
            "/weather/",                                                                // GET_WEATHER   = 2
    };

    private final String SERVER_REQ_TYPE[] = {
            "GET",                                                                      // GET_USER_INFO 요청방식
            "GET",                                                                      // GET_CALENDAR 요청방식
            "GET",                                                                      // GET_WEATHER 요청방식
    };

    private JSONObject reqBody;                                                         // 요청 바디 값 POST 요청을 할 때 사용
    private String reqParams;                                                           // 요청 파라미터 값 GET 요청을 할 때 사용


    /*

        생성자
        서버로 보낼 요청정보를 미리 받는다.

    */
    public HttpAsyncTask(int option, JSONObject reqBody, String reqParams, AsyncResponse delegate){
        super();
        this.option = option;
        this.sJwt = SessionJWT.getInstance().getJwt();
        this.reqBody = reqBody;
        this.reqParams = reqParams;
        this.delegate = delegate;
    }

    @Override
    protected JSONObject doInBackground(String... strings) {


        try{

                /*

                    Request 에 실어 보낼 요소들을 모아
                    JSON 객체를 받아 옴.
                    GET 이냐 POST 방식이냐에 따라 다른 HttpURLConnection 을 사용하는 방식이 다름.

                */

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try{

                if(SERVER_REQ_TYPE[option].equals("POST")){

                    /* 요청 방식이 POST 일 때 */

                    /* 2번째 parameter 로 넘어온 URL 값으로 생성한 다음 해당 통신 오픈  */
                    String strUrl = SERVER_URL + SERVER_DIR[option];
                    URL url = new URL(strUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod(SERVER_REQ_TYPE[option]);               // 전송 방식 : POST or GET
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

                    Log.d("SERVER REQUEST : ", "\n" +
                            "URL : " + strUrl + "\n" +
                            "METHOD : " + SERVER_REQ_TYPE[option] + "\n" +
                            "BODY : " + reqBody);

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

                }else if(SERVER_REQ_TYPE[option].equals("GET")){

                    /* 요청방식이 GET 일 때 */
                    String strUrl = SERVER_URL + SERVER_DIR[option];
                    if(reqParams != null){
                        strUrl += reqParams;
                    }
                    URL url = new URL(strUrl);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod(SERVER_REQ_TYPE[option]);               // 전송 방식 : POST or GET
                    connection.setRequestProperty("Cache-Control", "no-cache");         // 캐시 설정
                    connection.setRequestProperty("jwt", sJwt);                         // 헤더에 JWT 를 실어서 보냄(인증)
                    //connection.setDoOutput(true);                                     // Outputstream 으로 request
                    connection.setDoInput(true);                                        // Inputstream 으로 response
                    connection.connect();

                    /* 서버로 부터 데이터 수신을 위한 스트림 생성 */
                    InputStream inStream = connection.getInputStream();

                    Log.d("SERVER REQUEST : ", "\n" +
                            "URL : " + strUrl + "\n" +
                            "METHOD : " + SERVER_REQ_TYPE[option] + "\n" +
                            "BODY : " + reqBody);

                    /* read 버퍼생성 및 내용 읽기 */
                    reader = new BufferedReader(new InputStreamReader(inStream));
                    StringBuffer ret = new StringBuffer();
                    String line = "";
                    while((line = reader.readLine()) != null){
                        ret.append(line);
                    }

                    /* 읽은 버퍼 값을 JSONObject 로 변환하여 리턴 */
                    Log.d("SERVER RESPONSE : ", ret.toString());
                    return new JSONObject(ret.toString());

                }

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
            response 받은 JSONObject 를 다시 callback 으로 보낸다.

    */
    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        delegate.processFinish(jsonObject);
    }

}
