package com.example.finalpillapp.SearchPill;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pillapp.R;

import java.util.List;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {

    private List<String> searchResults;

    public SearchResultsAdapter(List<String> searchResults) {
        this.searchResults = searchResults;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_search_results, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvResult.setText(searchResults.get(position));
    }

    @Override
    public int getItemCount() {
        return searchResults.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvResult;

        public ViewHolder(View itemView) {
            super(itemView);
            tvResult = itemView.findViewById(com.example.pillapp.R.id.tv_result);
        }
    }
}
