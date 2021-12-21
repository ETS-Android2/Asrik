/*
    It is the adapter that is used with recycler view
    which renders conversation between two users
 */

package com.mitrukahitesh.asrik.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.models.chat.Message;
import com.mitrukahitesh.asrik.helpers.TimeFormatter;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class Messages extends RecyclerView.Adapter<Messages.CustomVH> {

    private Context context;
    private List<Message> messages;

    public Messages(Context context, List<Message> messages) {
        this.context = context;
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

        private final LinearLayout boxSent, boxReceived, fileSent, fileReceived;
        private final TextView msgSent, timeSent, msgReceived, timeReceived, urlSent, urlReceived;
        private final CircularProgressIndicator progressSent, progressReceived;
        // OnClickListener for opening media links
        private final View.OnClickListener urlClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message message = messages.get(getAdapterPosition());
                if (!message.getUrl().equals("")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(message.getUrl()));
                    context.startActivity(intent);
                }
            }
        };

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
            fileSent = itemView.findViewById(R.id.file_ll_sent);
            fileReceived = itemView.findViewById(R.id.file_ll_received);
            urlSent = itemView.findViewById(R.id.url_sent);
            urlReceived = itemView.findViewById(R.id.url_received);
            progressSent = itemView.findViewById(R.id.progress_sent);
            progressReceived = itemView.findViewById(R.id.progress_received);
            setListener();
        }

        /**
         * Set listeners to views in view holder
         */
        private void setListener() {
            urlReceived.setOnClickListener(urlClick);
            urlSent.setOnClickListener(urlClick);
        }

        /**
         * Renders message data into view holder
         */
        private void setView(Message message, int position) {
            TextView msg, time, url;
            CircularProgressIndicator progress;
            LinearLayout fileLl;
            if (message.getSender().equals(FirebaseAuth.getInstance().getUid())) {
                boxReceived.setVisibility(View.GONE);
                boxSent.setVisibility(View.VISIBLE);
                msg = msgSent;
                time = timeSent;
                url = urlSent;
                progress = progressSent;
                fileLl = fileSent;
            } else {
                boxSent.setVisibility(View.GONE);
                boxReceived.setVisibility(View.VISIBLE);
                msg = msgReceived;
                time = timeReceived;
                url = urlReceived;
                progress = progressReceived;
                fileLl = fileReceived;
            }
            msg.setText(messages.get(position).getMessage());
            time.setText(getTime(message.getTime()));
            if (message.getMedia()) {
                fileLl.setVisibility(View.VISIBLE);
                if (message.getUrl().equals("")) {
                    progress.setVisibility(View.VISIBLE);
                    progress.setProgress(message.getProgress());
                } else {
                    url.setText(message.getFileName());
                    progress.setVisibility(View.GONE);
                }
            } else {
                fileLl.setVisibility(View.GONE);
            }
        }

        /**
         * Get formatted time from time in millis
         */
        private String getTime(Long time) {
            Calendar c = Calendar.getInstance(TimeZone.getDefault());
            c.setTimeInMillis(time);
            return TimeFormatter.formatTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
        }
    }
}
