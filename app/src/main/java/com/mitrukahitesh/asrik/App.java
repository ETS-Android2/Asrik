/*
    This class is instantiated before any other class when the process for your application/package is created.
    Responsible for creating notification channels.
 */

package com.mitrukahitesh.asrik;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {

    /**
     * Notification Channel IDs
     */
    public static final String ADMIN_NEW_REQUEST = "ADMIN_NEW_REQUEST";
    public static final String USER_REQUEST_VERIFIED = "USER_REQUEST_VERIFIED";
    public static final String BLOOD_CAMP = "BLOOD_CAMP";
    public static final String NEW_MESSAGE = "NEW_MESSAGE";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    /**
     * Creates notification channels for various purposes
     */
    private void createNotificationChannel() {
        NotificationChannel adminNewRequest = new NotificationChannel(ADMIN_NEW_REQUEST, "Admin New Request", NotificationManager.IMPORTANCE_HIGH);
        adminNewRequest.setShowBadge(true);
        NotificationChannel userRequestVerified = new NotificationChannel(USER_REQUEST_VERIFIED, "User Request Verified", NotificationManager.IMPORTANCE_HIGH);
        userRequestVerified.setShowBadge(true);
        NotificationChannel bloodCamp = new NotificationChannel(BLOOD_CAMP, "Blood Camp", NotificationManager.IMPORTANCE_HIGH);
        bloodCamp.setShowBadge(true);
        NotificationChannel newMessage = new NotificationChannel(NEW_MESSAGE, "New Message", NotificationManager.IMPORTANCE_HIGH);
        newMessage.setShowBadge(true);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.createNotificationChannel(adminNewRequest);
        manager.createNotificationChannel(userRequestVerified);
        manager.createNotificationChannel(bloodCamp);
        manager.createNotificationChannel(newMessage);
    }
}
