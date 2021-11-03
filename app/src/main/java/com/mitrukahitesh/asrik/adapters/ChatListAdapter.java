package com.mitrukahitesh.asrik.adapters;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.CustomVH> {

    @NonNull
    @Override
    public CustomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull CustomVH holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class CustomVH extends RecyclerView.ViewHolder {
        public CustomVH(@NonNull View itemView) {
            super(itemView);
        }
    }
}
