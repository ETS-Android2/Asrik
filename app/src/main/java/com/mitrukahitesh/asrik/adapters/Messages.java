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
import com.mitrukahitesh.asrik.models.Message;
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

        private final LinearLayout boxSent, boxReceived, fileSent, fileReceived;
        private final TextView msgSent, timeSent, msgReceived, timeReceived, urlSent, urlReceived;
        private final CircularProgressIndicator progressSent, progressReceived;
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

        private void setListener() {
            urlReceived.setOnClickListener(urlClick);
            urlSent.setOnClickListener(urlClick);
        }

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

        private String getTime(Long time) {
            Calendar c = Calendar.getInstance(TimeZone.getDefault());
            c.setTimeInMillis(time);
            return TimeFormatter.formatTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
        }
    }
}
