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

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Constants.NEW_MESSAGE_INTENT_FILTER.equals(intent.getAction())) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(Constants.CANCELLED, chatId.equals(intent.getStringExtra(Constants.CHAT_ID)));
            setResultExtras(bundle);
        }
    }
}
