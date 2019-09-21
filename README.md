# E-ink-android-application

##### NodeJs 서버와 Google OAuth2 인증방식 활용을 테스트 하기위한 샘플 어플리케이션

This sample application is to test OAuth2 Identification and RestAPI call with backend-server



SERVER URL : http://169.56.98.117/



#### 테스트 목록

---

```Android
- Google Login Process
- Google Login 후 AuthCode 처리
- POST : /loginToken 후 response 받은 Jwt 보관
- GET  : /users 후 유저 정보 처리
- GET  : /calendar/next/:nextCount -> AccessToken 을 활용한 서버에서 구글api 사용가능 확인
- GET  : /calendar/next/10 을 통해 달력 정보 처리
- GET  : /weather/:latitude/:longitude GPS로 위치 수신하고 서버로 날씨 요청
```



