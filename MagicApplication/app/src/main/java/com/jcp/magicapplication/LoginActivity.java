package com.jcp.magicapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

    앱이 실행되고 로그인이 되었나 확인하는 Activity
    만약 로그인이 된 상태이면 MainActivity 로 넘어가고
    로그인이 안되었다면 현재의 로그인 화면을 보여준다.

*/

public class LoginActivity extends AppCompatActivity {

    /* 멤버변수 */
    private final int RC_SIGN_IN = 100;                         // 구글 로그인 result 식별값(OnActivityResult)
    private GoogleSignInClient mGoogleSignInClient;             // 구글 로그인 클라이언트
    private GoogleSignInAccount account;                        // 구글 계정 정보를 담고있는 객체

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /*

            DEFAULT_SIGN_IN : 접속 시 자동 로그인 상태
            GoogleSignInOptions : 구글 로그인 옵션을 지정한다.
            .requestIdToken 옵션을 넣으면
            GoogleSignInAccount account 에서 getIdToken() 을 통해 토큰을 추출할 수 있다.

            이메일정보와 기본 프로필을 추가로 요청한다.
            또한 accessToken 을 받을때 서버에서 api를 사용할 수 있는 Scope의 범위에 프로젝트에서
            사용할 구글 api scopes 를 입력한다. (ex: google calendar api)

            --> requestIdToken 이 아니라 서버에서 대신 api 요청까지 필요한 accessToken을 받아야 한다.
            getIdToken 대신 requestServerAuthCode 과 requestProfile 로 요청할 시
                1. accessToken
                2. refreshToken
                3. idToken
            을 받게된다.

            idToken 은 노드서버에서 로그인 인증용도로 사용하고
            실제 DB에 값을 보관해야하는 토큰은 accessToken과 refreshToken 으로 추정된다.

        */
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        // Request only the user's ID token, which can be used to identify the
        // user securely to your backend. This will contain the user's basic
        // profile (name, profile picture URL, etc) so you should not need to
        // make an additional call to personalize your application.

        // Configure sign-in to request offline access to the user's ID, basic
        // profile, and Google Drive. The first time you request a code you will
        // be able to exchange it for an access token and refresh token, which
        // you should store. In subsequent calls, the code will only result in
        // an access token. By asking for profile access (through
        // DEFAULT_SIGN_IN) you will also get an ID Token as a result of the
        // code exchange.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestServerAuthCode(getResources().getString(R.string.server_client_id), true)
                .requestProfile()
                .requestEmail()
                .requestScopes(new Scope(getResources().getString(R.string.google_calendar_scopes)))
                .build();

        /*

            현재 엑티비티에서 gso 옵션을 담아 client 를 가져온다.

        */
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        /*

            중복로그인(2개 이상 앱에서 로그인)이나 외부에 의한 계정상태 변경을 감지하려면 해당 옵션을 준다.
            Note: If you need to detect changes to a user's auth state that happen outside your app,
            such as access token or ID token revocation, or to perform cross-device sign-in,
            you might also call GoogleSignInClient.silentSignIn when your app starts.

            만약 이 디바이스나 다른 디바이스에서 이미 로그인 되어있다면 조용히 로그인을 한다.
            바로 handleSignInResult 로 들어감.
            When your app starts, check if the user has already signed in to your app using Google,
            on this device or another device, by calling silentSignIn

        */
        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this, new OnCompleteListener<GoogleSignInAccount>() {
            @Override
            public void onComplete(@NonNull Task<GoogleSignInAccount> task) {

                Log.d("**MYTAG", "사일런트 로그인 addOnCompleteListener");

                handleSignInResult(task);
            }
        });

        /*

            현재 구글계정이 로그인되어있는지 체크한다.

            GoogleSignIn.getLastSignedInAccount(this) == null
            --> 현재 구글 로그인 여부를 null 로 체크할 수 있다.

            만약 이미 로그인 되어있다면 account는 null이 아닌 상태이다.

        */
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        account = GoogleSignIn.getLastSignedInAccount(this);

        /* 현재 로그인된 계정을 사용해서 UI를 그린다. */
        updateUI(account);

    }

    /*

        현재 로그인된 계정을 사용해서 화면 UI를 그힌다.

    */
    private void updateUI(GoogleSignInAccount account) {

        if(SessionJWT.getInstance().getJwt().equals("")){
            /*

                비로그인 상태
                비로그인 상태의 시작 UI를 그린다.
                    ex:)구글 로그인 하기 버튼을 보여줌.

            */

            /*

                @Optional
                굳이 구글 로그인 버튼을 커스터마이징 하고 싶을 때
                기본 사이즈의 구글 로그인 버튼 view 와 연동한다음 setSize 해준다.

            */
            //SignInButton signInButton = findViewById(R.id.LoginActivity_SignInButton_googleLogin);
            //signInButton.setSize(SignInButton.SIZE_STANDARD);

            /* 연동 안하고 바로 OnClickListener 달아줌 */
            findViewById(R.id.LoginActivity_SignInButton_googleLogin).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    /*

                        로그인 인텐트를 시작하고 결과를 Result로 받아온다.

                    */

                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);

                }
            });

        }else{
            /*

                로그인 상태
                이미 로그인 되었으므로 로그인된 상태에 맞게
                다음 화면으로 intent를 넘기거나 해당 로그인 계정
                유저의 정보를 보여주든지 한다.

            */

            startActivity(new Intent(this, MainActivity.class));
            finish();

        }

    }

    /*

        다른 activity 를 갔다가 값을 가지고 다시 return 한 경우
        onActivityResult 에서 받는다.

    */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {

            /*

                구글 로그인 결과
                data 값을 Task 로 추출하고 handleSignInResult 함수에서 task 를 처리한다.

            */
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);

        }

    }

    /*

        구글 로그인 결과를 직접 처리한다.
        account 에 로그인된 Task 의 결과를 담고 이를 토대로 UI를 그린다.

    */
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {

        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            /*

                TODO: account 로 부터 authCode 를 추출한다.

            */
            String authCode = account.getServerAuthCode();

            Log.d("**MYTAG -- ", "GoogleAuthCode : "  + authCode);

            /* TODO : 여기서 authCode를 서버로 보낸다. */
            // send ID Token to server and validate
            /* HTTP request, 나중에 코드 리팩토링 할 것! */
            new JSONLoginPost(getResources().getString(R.string.server_url)+"/loginToken").execute(authCode);
            /*-------------------------------------------------*/

            /* 로그인 완료된 UI를 보여줌 */
            // Signed in successfully, show authenticated UI.
            //updateUI(account);
        } catch (ApiException e) {
            /* 예외 발생시 다시 로그인 화면 보여줌 */
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("MYTAG", "signInResult:failed code=" + e.getStatusCode());
            Log.w("MYTAG", e.getMessage());
            updateUI(null);
        }

    }

    /*

        Login 통신과 UI 제어를 위한 내부 클래스

    */
    private class JSONLoginPost extends AsyncTask<String, String, String> {

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
                jsonObject.accumulate("authCode", tokens[0]);

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

            return "";
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

            /* 로그인이 완료되었으므로 UI를 업데이트 한다. */
            updateUI(account);

        }
    }


}
