package com.sagie.myfirstapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<ChatMessage> messageList;

    public MessageAdapter(List<ChatMessage> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // קישור לקובץ ה-XML של בועת ההודעה
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);

        holder.tvSender.setText(message.senderName);
        holder.tvMessageText.setText(message.text);

        // בדיקה שהזמן תקין לפני המרה
        if (message.timestamp != 0) {
            try {
                // יצירת אובייקט תאריך מה-timestamp (מילי-שניות)
                Date date = new Date(message.timestamp);
                // פורמט של שעות ודקות (למשל 14:30)
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String time = sdf.format(date);
                holder.tvTimestamp.setText(time);
            } catch (Exception e) {
                holder.tvTimestamp.setText("--:--");
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // התיקון נמצא כאן - קישור הרכיבים ל-IDs מה-XML
    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvSender, tvMessageText, tvTimestamp;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            // וודא שה-IDs האלו קיימים בדיוק כך ב-item_chat_message.xml
            tvSender = itemView.findViewById(R.id.tvSender);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}