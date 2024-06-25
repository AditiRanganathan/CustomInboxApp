package com.example.custominboxapp;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.custominboxapp.CustomInboxMessage;

import java.util.List;

public class CustomInboxAdapter extends RecyclerView.Adapter<CustomInboxAdapter.InboxViewHolder> {
    private List<CustomInboxMessage> inboxMessages;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(CustomInboxMessage message);
    }

    public CustomInboxAdapter(List<CustomInboxMessage> inboxMessages, OnItemClickListener listener) {
        this.inboxMessages = inboxMessages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InboxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.inbox_message_item, parent, false);
        return new InboxViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InboxViewHolder holder, int position) {
        final CustomInboxMessage message = inboxMessages.get(position);
        holder.titleTextView.setText(message.getTitle());
        holder.messageTextView.setText(message.getMessage());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(message);
            }
        });
    }

    @Override
    public int getItemCount() {
        return inboxMessages.size();
    }

    static class InboxViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView messageTextView;

        public InboxViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title);
            messageTextView = itemView.findViewById(R.id.message);
        }
    }
}
