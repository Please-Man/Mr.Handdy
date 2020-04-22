package kr.ac.dongyang.mr_handdy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private String token = "mobile Test"; // 안드로이드 고유 번호
    private static final String SERVER_URL = "http://joon16.iptime.org:8080/MrHanddy/";
    private String order = "";
    private SharedPreferences sp;

    ImageButton imageButton1;
    ImageButton imageButton2;
    Button button_hot;
    Button button_cold;
    TextView textView_menu;
    TextView textView_ame_hot;
    TextView textView_ame_cold;
    TextView textView_eso_hot;
    Intent intent;
    Button button_how;


    //XML관련
    int menu_choice = -1;
    int ame_hot=0 , ame_cold=0, eso_hot=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseMessaging.getInstance().subscribeToTopic("Alarm");

        imageButton1 = findViewById(R.id.imageButton1);
        imageButton2 = findViewById(R.id.imageButton2);
        textView_ame_cold = findViewById(R.id.textView_ame_cold);
        textView_ame_hot = findViewById(R.id.textView_ame_hot);
        textView_eso_hot = findViewById(R.id.textView_eso_hot);
//        button_order = findViewById(R.id.button3);
        button_cold = findViewById(R.id.button_cold);
        button_hot = findViewById(R.id.button_hot);
        textView_menu = findViewById(R.id.textView_menu);
        button_how = findViewById(R.id.button_how);

        /*
        //라디오 버튼 클릭 리스너 (중복 가능)
        radiobutton1.setOnClickListener(radioButtonClickListener);
        radiobutton2.setOnClickListener(radioButtonClickListener);
        */
    }
    @Override
    protected void onStart() {
        super.onStart();
        sp = getSharedPreferences("TOKENDATA", Context.MODE_PRIVATE);
        token = sp.getString("TOKENDATA", "");
        Log.d(TAG,"Token : " +  token);
    }

    public void button_hot_click(View view) {

        switch (menu_choice) {
            case 0: // 아메리카노
                ame_hot++;
                textView_ame_hot.setText("" + ame_hot);
            break;

            case 1 :  // 에소프레소
            eso_hot++;
            textView_eso_hot.setText("" + eso_hot);
            break;

            default:
                Log.e(TAG, "[ERROR] 없는 메뉴 선택");
        }
    }

    public void button_cold_click(View view) {
        switch (menu_choice) {
            case 0: // 아메리카노
                ame_cold++;
                textView_ame_cold.setText(""+ame_cold);
                break;
            case 1 :  // 에소프레소
            default:
                Log.e(TAG, "[ERROR] 없는 메뉴 선택");
        }
    }

    public void button_ame_click(View view) {
        button_cold.setEnabled(true);
        button_hot.setEnabled(true);
        menu_choice = 0;
        Toast.makeText(MainActivity.this, "아메리카노를 선택하셨습니다.", Toast.LENGTH_SHORT).show();
    }

    public void button_eso_click(View view) {
        button_cold.setEnabled(false);
        button_hot.setEnabled(true);
        menu_choice = 1;
        Toast.makeText(MainActivity.this, "에소프레소를 선택하셨습니다.", Toast.LENGTH_SHORT).show();
    }


    public void button_defult_click(View view) {
        ame_hot=0;
        ame_cold=0;
        eso_hot=0;
        order = "";
        textView_menu.setText("주문하세요");
        textView_eso_hot.setText(String.valueOf(eso_hot));
        textView_ame_cold.setText(String.valueOf(ame_cold));
        textView_ame_hot.setText(String.valueOf(eso_hot));
    }

    public void button_how_click(View view) {
        intent = new Intent(getApplicationContext(),Howto_OrderActivity.class);
        startActivity(intent);
    }

    public void button_send_order(View view) {

        int sum = eso_hot + ame_hot + ame_cold;
        if(sum > 2){
            button_defult_click(view);

            Toast.makeText(MainActivity.this, "현재 테스트 서비스로는 3잔 이상 주문을 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
        }
        else if(sum < 3 && sum > 0){

            boolean separator = false; // 다른 메뉴가 있는 경우
            if (eso_hot > 0){
                order += "에소프레소 Hot " +String.valueOf(eso_hot)+ "잔";
                separator = true;
            }
            if (ame_hot > 0 ){
                if (separator){
                    order += ", ";
                }
                order += "아메리카노 Hot " +String.valueOf(ame_hot)+ "잔";
                separator = true;
            }
            if (ame_cold > 0 ){
                if (separator){
                    order += ", ";
                }
                order += "아메리카노 Cold " +String.valueOf(ame_cold)+ "잔";
            }
            //DB 연결 코드 추가
            insertToDatabase(token, textView_eso_hot.getText().toString(),textView_ame_hot.getText().toString(),textView_ame_cold.getText().toString());

            // 클릭시 생성됨
            Toast.makeText(MainActivity.this, order + "를 주문했습니다.", Toast.LENGTH_SHORT).show();

            button_defult_click(view);

        }
        else {
            Toast.makeText(MainActivity.this, "주문해주세요.", Toast.LENGTH_SHORT).show();
        }
    }

    private void insertToDatabase(String id , String eso_hot, String ame_hot, String ame_cold){

        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(MainActivity.this, "Please Wait", null, true, true); //다이어로그 생성
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
            }

            @Override // 데이터베이스로 데이터를 옮겨주는 메소드
            protected String doInBackground(String... params) {

                try{
                    String id = (String)params[0];
                    String eso_hot = (String)params[1];
                    String ame_hot = (String)params[2];
                    String ame_cold = (String)params[3];

                    // 현재시간
                    long now = System.currentTimeMillis();
                    Date date = new Date(now);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    String getTime = sdf.format(date);

                    Log.d(TAG,id + ", " + getTime + ", " +eso_hot + ", " +ame_hot + ", " +ame_cold);

                    String data  = URLEncoder.encode("id", "UTF-8") + "=" + URLEncoder.encode(id, "UTF-8"); // name필드에 있는 값 가지고 오기
                    data += "&" + URLEncoder.encode("time", "UTF-8") + "=" + URLEncoder.encode(getTime, "UTF-8");
                    data += "&" + URLEncoder.encode("eso_hot", "UTF-8") + "=" + URLEncoder.encode(eso_hot, "UTF-8");
                    data += "&" + URLEncoder.encode("ame_hot", "UTF-8") + "=" + URLEncoder.encode(ame_hot, "UTF-8");
                    data += "&" + URLEncoder.encode("ame_cold", "UTF-8") + "=" + URLEncoder.encode(ame_cold, "UTF-8");

                    Log.d(TAG, SERVER_URL+"take_order.php");
                    URL url = new URL(SERVER_URL+"take_order.php"); // URL에 내가 지정해준 주소로 간다.
                    URLConnection conn = url.openConnection(); //주소를 통해서 열어준다.

                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream()); //데이터 쓰기

                    wr.write( data );
                    wr.flush();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    // Read Server Response
                    while((line = reader.readLine()) != null)
                    {
                        sb.append(line);
                        break;
                    }
                    return sb.toString();
                }
                catch(Exception e){
                    return new String("Exception: " + e.getMessage());
                }

            }
        }

        InsertData task = new InsertData();
        task.execute(id , eso_hot, ame_hot, ame_cold); // 데이터를 데이터 베이스에 삽입
    }


}
