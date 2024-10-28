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
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_drug, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        PillInfo pill = getItem(position);

        if (pill != null) {
            viewHolder.itemName.setText(pill.getItemName());
            viewHolder.efcyQesitm.setText(pill.getEfcyQesitm());
            Picasso.get().load(pill.getImageUrl()).placeholder(R.drawable.sample_pill_image).into(viewHolder.itemImage);
        }

        convertView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(pill);
            }
        });

        return convertView;
    }

    static class ViewHolder {
        TextView itemName;
        TextView efcyQesitm;
        ImageView itemImage;

        ViewHolder(View view) {
            itemName = view.findViewById(R.id.itemName);
            efcyQesitm = view.findViewById(R.id.efcyQesitm);
            itemImage = view.findViewById(R.id.itemImage);
        }
    }
}
