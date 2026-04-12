package com.sagie.myfirstapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.sagie.myfirstapplication.R;
import java.util.List;

public class MechinaAdapter extends RecyclerView.Adapter<MechinaAdapter.ViewHolder> {

    private List<String> data;

    public MechinaAdapter(List<String> data) { this.data = data; }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // וודא שיצרת את item_mechina_card.xml
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mechina_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.name.setText(data.get(position));
    }

    @Override
    public int getItemCount() { return data.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tvMechinaName);
        }
    }
}