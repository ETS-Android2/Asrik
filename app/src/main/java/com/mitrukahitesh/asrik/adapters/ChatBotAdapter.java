/*
    It is the adapter that is used with recycler view
    which renders conversation between user and bot
 */

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

    /**
     * Called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item
     */
    @NonNull
    @Override
    public CustomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_message, parent, false));
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method should update the contents of the RecyclerView.ViewHolder.itemView
     * to reflect the item at the given position.
     */
    @Override
    public void onBindViewHolder(@NonNull CustomVH holder, int position) {
        holder.setView(messages.get(position), position);
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     */
    @Override
    public int getItemCount() {
        return messages.size();
    }

    /**
     * Extend the abstract class RecyclerView.ViewHolder
     * to create ViewHolder objects and write custom
     * implementation
     */
    public class CustomVH extends RecyclerView.ViewHolder {

        private final LinearLayout boxSent, boxReceived;
        private final TextView msgSent, timeSent, msgReceived, timeReceived;

        /**
         * Call super constructor, and
         * Set references and listeners to views
         */
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

        /**
         * Renders message data into view holder
         */
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

        /**
         * Get formatted time (String) from time in millis
         */
        private String getTime(Long time) {
            Calendar c = Calendar.getInstance(TimeZone.getDefault());
            c.setTimeInMillis(time);
            return TimeFormatter.formatTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
        }
    }
}
