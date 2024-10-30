package com.example.finalpillapp.RecognizePill;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.BaseAdapter;

import com.bumptech.glide.Glide;
import com.example.pillapp.R;

import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {

    private final Context mContext;
    private final ArrayList<String> imagePaths;

    public ImageAdapter(Context context, ArrayList<String> imagePaths) {
        this.mContext = context;
        this.imagePaths = imagePaths;
    }

    @Override
    public int getCount() {
        return imagePaths.size();
    }

    @Override
    public Object getItem(int position) {
        return imagePaths.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.grid_item, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.grid_image);
        String imagePath = imagePaths.get(position);

        // Glide를 사용해 이미지 로드
        Glide.with(mContext).load("file://" + imagePath).into(imageView);

        return convertView;
    }
}
