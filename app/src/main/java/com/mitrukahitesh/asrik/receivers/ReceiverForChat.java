/*
    Broadcast receiver for receiving broadcast broad-casted by
    firebase messaging service when it receives new message notification
 */

package com.mitrukahitesh.asrik.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.mitrukahitesh.asrik.helpers.Constants;

public class ReceiverForChat extends BroadcastReceiver {

    private String chatId = "";

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    /**
        Handles the broadcast
        This is set dynamically in chat fragment
        Since FCM sends ordered broadcast, this broadcast is first received by Chat fragment
        If the notification contains message from the user whose chat is currently opened,
        the broadcast is forwarded with CANCELLED value as TRUE
        If CANCELLED is FALSE, notification is displayed else not
        FCM ---> Chat Fragment (ReceiverForChat) ---> FCM ---> Notify if CANCELLED == FALSE
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Constants.NEW_MESSAGE_INTENT_FILTER.equals(intent.getAction())) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(Constants.CANCELLED, chatId.equals(intent.getStringExtra(Constants.CHAT_ID)));
            setResultExtras(bundle);
        }
    }
}
