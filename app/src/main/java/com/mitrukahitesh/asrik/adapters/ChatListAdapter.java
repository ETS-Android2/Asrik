package com.mitrukahitesh.asrik.adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.models.BloodRequest;
import com.mitrukahitesh.asrik.models.ChatInfo;
import com.mitrukahitesh.asrik.utility.Constants;
import com.mitrukahitesh.asrik.utility.TimeFormatter;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.CustomVH> {

    private final Context context;
    private final List<ChatInfo> chatInfoList;
    private NavController controller;

    public ChatListAdapter(Context context, List<ChatInfo> chatInfoList, NavController controller) {
        this.context = context;
        this.chatInfoList = chatInfoList;
        this.controller = controller;
    }

    @NonNull
    @Override
    public CustomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_chat_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CustomVH holder, int position) {
        holder.setView(chatInfoList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return chatInfoList.size();
    }

    public class CustomVH extends RecyclerView.ViewHolder {

        private final CircleImageView dp;
        private final TextView name, lastMessage, time;
        private final LinearLayout root;

        public CustomVH(@NonNull View itemView) {
            super(itemView);
            dp = itemView.findViewById(R.id.dp);
            name = itemView.findViewById(R.id.name);
            lastMessage = itemView.findViewById(R.id.lastMsg);
            time = itemView.findViewById(R.id.time);
            root = itemView.findViewById(R.id.root);
            setListener();
        }

        private void setListener() {
            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ChatInfo info = chatInfoList.get(getAdapterPosition());
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.PROFILE_PIC_URL, info.getProfileUrl());
                    bundle.putString(Constants.NAME, info.getName());
                    bundle.putString(Constants.UID, info.getUid());
                    bundle.putString(Constants.NUMBER, info.getNumber());
                    controller.navigate(R.id.action_chatList_to_chat2, bundle);
                }
            });
        }

        private void setView(ChatInfo info, int position) {
            Glide.with(context).load(!info.getProfileUrl().equals("") ? info.getProfileUrl() : AppCompatResources.getDrawable(context, R.drawable.ic_usercircle)).into(dp);
            name.setText(info.getName());
            time.setText(getTime(info.getTime()));
            if (info.getMedia()) {
                lastMessage.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(context, R.drawable.ic_baseline_file_present_24), null, null, null);
            } else {
                lastMessage.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            }
            lastMessage.setText(info.getLastMessage());
        }

        private String getTime(Long time) {
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            calendar.setTimeInMillis(time);
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            String month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT_FORMAT, Locale.getDefault());
            int year = calendar.get(Calendar.YEAR);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            return String.format(Locale.getDefault(), "%s %d %s %d", TimeFormatter.formatTime(hour, minute), dayOfMonth, month, year);
        }
    }
}
