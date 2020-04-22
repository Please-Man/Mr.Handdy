package kr.ac.dongyang.mr_handdy;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";
    /**
     * 만약 onTokenRefresh()이 호출되지 않는다면 설치된 앱을 지우고 사용자 데이터 삭제 후 다시 시도
     */
    // [START refresh_token]
    @Override
   public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token : " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

        // 생성등록된 토큰을 개인 앱서버에 보내 저장해 두었다가 추가 뭔가를 하고 싶으면 할 수 있도록 한다.
        sendRegistration(refreshedToken);
    }
    // [END refresh_token]

    public void sendRegistration(String token){
        SharedPreferences sp = getSharedPreferences("TOKENDATA", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("TOKENDATA", token); // idData의 값을 LOGINDATA.xml에 저장
        editor.commit(); // 실행한다.
    }

}