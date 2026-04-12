package com.sagie.myfirstapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sagie.myfirstapplication.models.MechinaEvent;

import java.util.ArrayList;
import java.util.List;

public class ExploreAdapter extends RecyclerView.Adapter<ExploreAdapter.ViewHolder> {
    private List<MechinaEvent> mList;
    private OnMechinaClickListener listener;
    public ExploreAdapter(List<MechinaEvent> list,OnMechinaClickListener listener) {
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

        holder.tvMechinaName.setText(item.mechinaName);
        holder.tvBranchName.setText("(" + item.branch + ")");
        holder.tvLocation.setText(item.address);
        holder.btnReadMore.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMechinaClick(item);
            }
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMechinaName = itemView.findViewById(R.id.tvMechinaName);
            tvBranchName = itemView.findViewById(R.id.tvBranchName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            btnReadMore = itemView.findViewById(R.id.btnReadMore);
        }
    }

    public void updateList(List<MechinaEvent> newList) {
        mList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }
}