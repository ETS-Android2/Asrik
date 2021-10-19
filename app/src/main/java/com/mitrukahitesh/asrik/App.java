package com.mitrukahitesh.asrik;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {

    public static final String ADMIN_NEW_REQUEST = "ADMIN_NEW_REQUEST";
    public static final String USER_REQUEST_VERIFIED = "USER_REQUEST_VERIFIED";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel adminNewRequest = new NotificationChannel(ADMIN_NEW_REQUEST, "Admin New Request", NotificationManager.IMPORTANCE_HIGH);
            adminNewRequest.setShowBadge(true);
            NotificationChannel userRequestVerified = new NotificationChannel(USER_REQUEST_VERIFIED, "User Request Verified", NotificationManager.IMPORTANCE_HIGH);
            userRequestVerified.setShowBadge(true);

            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(adminNewRequest);
            manager.createNotificationChannel(userRequestVerified);
        }
    }
}
