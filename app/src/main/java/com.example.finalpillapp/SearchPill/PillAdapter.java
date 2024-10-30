package com.example.finalpillapp.SearchPill;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.finalpillapp.PillInfo.PillInfo;
import com.example.pillapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PillAdapter extends ArrayAdapter<PillInfo> {

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(PillInfo pill);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public PillAdapter(Context context, List<PillInfo> pillList) {
        super(context, 0, pillList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_drug, parent, false);
        }

        PillInfo pill = getItem(position);

        TextView itemName = convertView.findViewById(R.id.itemName);
        TextView efcyQesitm = convertView.findViewById(R.id.efcyQesitm);
        ImageView itemImage = convertView.findViewById(R.id.itemImage);

        itemName.setText(pill.getItemName());
        efcyQesitm.setText(pill.getEfcyQesitm());
        Picasso.get().load(pill.getImageUrl()).into(itemImage);

        convertView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(pill);
            }
        });

        return convertView;
    }
}
