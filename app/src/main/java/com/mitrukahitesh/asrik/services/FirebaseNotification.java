package com.mitrukahitesh.asrik.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import java.util.Locale;
import java.util.Map;

public class FirebaseNotification extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> map = remoteMessage.getData();
        Log.i("Asrik: FCM", map.toString());
        SharedPreferences preferences = getSharedPreferences(Constants.USER_DETAILS_SHARED_PREFERENCE, MODE_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, Main.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        if (Boolean.parseBoolean(map.get("newMessage"))) {
            Intent broadcast = new Intent(Constants.NEW_MESSAGE_INTENT_FILTER);
            broadcast.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            broadcast.putExtra(Constants.NUMBER, map.get(Constants.NUMBER.toLowerCase(Locale.ROOT)));
            broadcast.putExtra(Constants.NAME, map.get("title"));
            broadcast.putExtra(Constants.MESSAGE, map.get("body"));
            broadcast.putExtra(Constants.CHAT_ID, map.get(Constants.CHAT_ID.toLowerCase(Locale.ROOT)));
            sendOrderedBroadcast(broadcast, null, new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (getResultExtras(true).getBoolean(Constants.CANCELLED))
                        return;
                    Notification notification = new NotificationCompat.Builder(context, App.NEW_MESSAGE)
                            .setContentTitle(intent.getStringExtra(Constants.NAME))
                            .setContentText(intent.getStringExtra(Constants.MESSAGE))
                            .setSmallIcon(R.drawable.ic_baseline_bloodtype_24)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setCategory(NotificationCompat.CATEGORY_ALARM)
                            .setColor(context.getResources().getColor(R.color.theme_color_light, null))
                            .build();
                    manager.notify((int) Long.parseLong(intent.getStringExtra(Constants.NUMBER)), notification);
                }
            }, null, 1, null, null);
        } else if (Boolean.parseBoolean(map.get("bloodCamp")) && preferences.getBoolean(Constants.ENABLE_NOTIFICATION_BLOOD_CAMP, true)) {
            Notification notification = new NotificationCompat.Builder(this, App.BLOOD_CAMP)
                    .setContentTitle(map.get("title"))
                    .setContentText(map.get("date"))
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.drawable.ic_baseline_bloodtype_24)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(map.get("body")))
                    .setColor(getResources().getColor(R.color.theme_color_light, null))
                    .build();
            manager.notify((int) System.currentTimeMillis(), notification);
        } else {
            if (preferences.getBoolean(Constants.ADMIN, false) && Boolean.parseBoolean(map.get("forAdmin"))) {
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
                Notification notification = new NotificationCompat.Builder(this, App.USER_REQUEST_VERIFIED)
                        .setContentTitle(map.get("title"))
                        .setContentText(map.get("body"))
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(R.drawable.ic_baseline_bloodtype_24)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setColor(getResources().getColor(R.color.theme_color_light, null))
                        .build();
                manager.notify((int) System.currentTimeMillis(), notification);
            }
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
