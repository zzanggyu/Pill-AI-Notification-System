package com.example.pillapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {

    private List<String> searchResultsList;

    public SearchResultsAdapter(List<String> searchResultsList) {
        this.searchResultsList = searchResultsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String searchResult = searchResultsList.get(position);
        holder.tvSearchResult.setText(searchResult);
    }

    @Override
    public int getItemCount() {
        return searchResultsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvSearchResult;

        public ViewHolder(View itemView) {
            super(itemView);
            tvSearchResult = itemView.findViewById(R.id.tv_search_result);
        }
    }
}
