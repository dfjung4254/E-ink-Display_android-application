package com.jcp.magicapplication.Session;

/* TODO Author : 정근화 */

/*

    Class : SessionJWT
    본 클래스는 싱글톤 클래스로
    세션관리에 관한 여러 변수들을 저장하는 클래스이다.

    현재는 Node 서버에서 받은 JWT 토큰을 저장하고 관리

    SessionJWT.getInstance()

    로 호출하여 사용한다.

*/

public class SessionJWT {

    /* 멤버 변수 - 세션에 관련한 변수들을 저장한다. */
    private String jwt;                     // Node 서버에서 받은 JWT 토큰.

    /* 생성자 - private 처리되어 외부에서 생성할 수 없음 */
    private SessionJWT() {
        jwt = new String();
    }

    /* 내부 클래스(LazyHolder)에서 SessionJWT 클래스를 단 한번 생성 */
    private static class LazyHolder {
        public static final SessionJWT INSTANCE = new SessionJWT();
    }

    /*

        static 함수로 SessionJWT는 LazyHolder 가
        가지고 있는 SessionJWT 하나만 getInstance()로 호출한다.

    */
    public static SessionJWT getInstance() {
        return LazyHolder.INSTANCE;
    }


    /* Getter & Setter */
    public void setJwt(String jwt){
        this.jwt = jwt;
    }
    public String getJwt(){
        return jwt;
    }

}
