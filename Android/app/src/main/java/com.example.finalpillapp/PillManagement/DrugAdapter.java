package com.example.finalpillapp.PillManagement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DrugAdapter extends RecyclerView.Adapter<DrugAdapter.DrugViewHolder> {

    private List<String> drugList;

    public DrugAdapter(List<String> drugList) {
        this.drugList = drugList;
    }

    @NonNull
    @Override
    public DrugViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(com.example.pillapp.R.layout.item_drug, parent, false);
        return new DrugViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DrugViewHolder holder, int position) {
        String drug = drugList.get(position);
        holder.tvDrugName.setText(drug);
    }

    @Override
    public int getItemCount() {
        return drugList.size();
    }

    public static class DrugViewHolder extends RecyclerView.ViewHolder {
        TextView tvDrugName;

        public DrugViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDrugName = itemView.findViewById(com.example.pillapp.R.id.itemName);
        }
    }
}
