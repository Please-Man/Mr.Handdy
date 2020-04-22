package kr.ac.dongyang.mr_handdy;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyFirebaseMessasingService extends FirebaseMessagingService {

    private static final String TAG = "FirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {  //data payload로 보내면 실행
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        int flag = -1;
        //여기서 메세지의 두가지 타입(1. data payload 2. notification payload)에 따라 다른 처리를 한다.
        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }

            flag = 0;
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Title: " + remoteMessage.getNotification().getTitle());
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            flag = 1;
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.

        //화면을 깨운다.(이방법은 Deprecated 되었다고 한다. 당장 작동은 되지만 나중에 어떻게 될지 모른다?)
        PowerManager powerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "WAKELOCK");
        wakeLock.acquire(3000);
        String title = "";
        String body = "";
        String message = "";

        if (flag == 0) {
            title = remoteMessage.getData().get("title");
            body = remoteMessage.getData().get("body");

            // 푸시 message 추출
            String patten_message = "\\{\"([a-zA-Z]{7})\":\"(.+)\"\\}";
            Pattern pattern = Pattern.compile(patten_message);
            Matcher macher;

            macher = pattern.matcher(body);
            if (macher.find()) {
                message = String.valueOf(macher.group(2));
            }
        }else if(flag == 1){
            title  = remoteMessage.getNotification().getTitle();
            message = remoteMessage.getNotification().getBody();
        }

        Log.d(TAG, title + " " + message);
        sendNotification(title, message);
    }

    private void scheduleJob() {
        //이건 아직 나중에 알아 볼것.
        Log.d(TAG, "이것에 대해서는 나중에 알아 보자.");
    }

    private void handleNow() {
        Log.d(TAG, "10초이내 처리됨");
    }

    private void sendNotification(String title, String messageBody) {

        if (title == null){
            //제목이 없는 payload이면
            title = "알림"; //기본제목을 적어 주자.
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setVibrate(new long[]{1000, 1000})
                .setLights(Color.BLUE, 1,1)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    @Override
    public void onDeletedMessages() {
        Log.e(TAG, "Messageon DeletedMessages");
    }
}
