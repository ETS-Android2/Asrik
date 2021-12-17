package com.mitrukahitesh.asrik.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.helpers.TimeFormatter;
import com.mitrukahitesh.asrik.models.chat.Message;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class ChatBotAdapter extends RecyclerView.Adapter<ChatBotAdapter.CustomVH> {

    private List<Message> messages;

    public ChatBotAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public CustomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_message, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CustomVH holder, int position) {
        holder.setView(messages.get(position), position);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public class CustomVH extends RecyclerView.ViewHolder {

        private final LinearLayout boxSent, boxReceived;
        private final TextView msgSent, timeSent, msgReceived, timeReceived;

        public CustomVH(@NonNull View itemView) {
            super(itemView);
            boxSent = itemView.findViewById(R.id.boxSent);
            msgSent = itemView.findViewById(R.id.messageSent);
            timeSent = itemView.findViewById(R.id.timeSent);
            boxReceived = itemView.findViewById(R.id.boxReceived);
            msgReceived = itemView.findViewById(R.id.messageReceived);
            timeReceived = itemView.findViewById(R.id.timeReceived);
            itemView.findViewById(R.id.file_ll_received).setVisibility(View.GONE);
            itemView.findViewById(R.id.file_ll_sent).setVisibility(View.GONE);
        }

        private void setView(Message message, int position) {
            TextView msg, time;
            if (message.getSender().equals(FirebaseAuth.getInstance().getUid())) {
                boxReceived.setVisibility(View.GONE);
                boxSent.setVisibility(View.VISIBLE);
                msg = msgSent;
                time = timeSent;
            } else {
                boxSent.setVisibility(View.GONE);
                boxReceived.setVisibility(View.VISIBLE);
                msg = msgReceived;
                time = timeReceived;
            }
            msg.setText(messages.get(position).getMessage());
            time.setText(getTime(message.getTime()));
        }

        private String getTime(Long time) {
            Calendar c = Calendar.getInstance(TimeZone.getDefault());
            c.setTimeInMillis(time);
            return TimeFormatter.formatTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
        }
    }
}
