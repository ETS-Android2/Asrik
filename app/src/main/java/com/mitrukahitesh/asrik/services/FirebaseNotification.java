package com.mitrukahitesh.asrik.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mitrukahitesh.asrik.App;
import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.activities.Main;
import com.mitrukahitesh.asrik.utility.Constants;

import java.util.HashMap;
import java.util.Map;

public class FirebaseNotification extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> map = remoteMessage.getData();
        SharedPreferences preferences = getSharedPreferences(Constants.USER_DETAILS_SHARED_PREFERENCE, MODE_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (preferences.getBoolean(Constants.ADMIN, false) && Boolean.parseBoolean(map.get("forAdmin"))) {
            Intent intent = new Intent(this, Main.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            Notification notification = new NotificationCompat.Builder(this, App.ADMIN_NEW_REQUEST)
                    .setContentTitle(map.get("title"))
                    .setContentText(map.get("body"))
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_baseline_bloodtype_24)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setColor(getResources().getColor(R.color.theme_color_light, null))
                    .build();
            manager.notify((int) System.currentTimeMillis(), notification);
        } else if (!preferences.getBoolean(Constants.ADMIN, false) && !Boolean.parseBoolean(map.get("forAdmin"))) {

        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        if (FirebaseAuth.getInstance().getUid() == null)
            return;
        Log.i("Asrik: FCM", s);
        Map<String, String> map = new HashMap<>();
        map.put(Constants.TOKEN, s);
        FirebaseFirestore.getInstance()
                .collection(Constants.TOKENS)
                .document(FirebaseAuth.getInstance().getUid())
                .set(map);
    }
}
