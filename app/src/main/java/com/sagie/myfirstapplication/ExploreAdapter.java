package com.sagie.myfirstapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sagie.myfirstapplication.Activities.ChatActivity;
import com.sagie.myfirstapplication.models.MechinaEvent;

import java.util.ArrayList;
import java.util.List;

public class ExploreAdapter extends RecyclerView.Adapter<ExploreAdapter.ViewHolder> {
    private List<MechinaEvent> mList;
    private OnMechinaClickListener listener;

    public ExploreAdapter(List<MechinaEvent> list, OnMechinaClickListener listener) {
        this.mList = list;
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_explore_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MechinaEvent item = mList.get(position);
        Context context = holder.itemView.getContext();

        holder.tvMechinaName.setText(item.mechinaName);
        holder.tvBranchName.setText("(" + item.branch + ")");
        holder.tvLocation.setText(item.address);

        // כפתור פרטים (הדיאלוג הקיים)
        holder.btnReadMore.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMechinaClick(item);
            }
        });

        // כפתור הצ'אט הגנרי - מעביר את שם המכינה והשלוחה כנתיב ל-Firebase
        holder.btnOpenChat.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatActivity.class);
            intent.putExtra("MECHINA_NAME", item.mechinaName);
            intent.putExtra("BRANCH_NAME", item.branch);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public interface OnMechinaClickListener {
        void onMechinaClick(MechinaEvent event);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMechinaName, tvBranchName, tvLocation;
        Button btnReadMore;
        ImageButton btnOpenChat; // הוספת הכפתור החדש

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMechinaName = itemView.findViewById(R.id.tvMechinaName);
            tvBranchName = itemView.findViewById(R.id.tvBranchName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            btnReadMore = itemView.findViewById(R.id.btnReadMore);
            btnOpenChat = itemView.findViewById(R.id.btnOpenChat); // קישור ל-XML
        }
    }

    public void updateList(List<MechinaEvent> newList) {
        mList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }
}