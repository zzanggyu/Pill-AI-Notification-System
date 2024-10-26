package com.example.finalpillapp.Alarm;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pillapp.R;

import java.util.List;

// RecyclerView에 알람목록을 표시하는 데 사용 RecyclerView.Adapter를 상속받아 알람 데이터를 뷰로 변환하는 역할
public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
    private List<Alarm> alarms; // 표시할 알람 목록
    public AlarmAdapter(List<Alarm> alarms) {
        this.alarms = alarms;
    }

    @NonNull
    @Override
    // 이 메서드는 RecyclerView가 새로운 항목을 표시해야 할 때 호출
    public AlarmViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // item_alarm 레이아웃을 인플레이트하여 View 객체 생성
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_alarm, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    // ViewHolder에 데이터를 바인딩
    //  RecyclerView가 특정 위치의 데이터를 표시해야 할 때 호출됨
    public void onBindViewHolder(@NonNull AlarmViewHolder holder, int position) {

        Alarm alarm = alarms.get(position);
        holder.textViewAlarmTime.setText(alarm.getTimeString());
        holder.switchAlarm.setChecked(alarm.isOn());
    }

    @Override
    public int getItemCount() {
        return alarms.size(); // 알람 목록의 크기
    }

    public void removeAlarm(int position) {
        alarms.remove(position); // 지정된 위치의 알람 제거
        notifyItemRemoved(position);
    }

    // 각 알람 항목의 뷰를 보유함
//    RecyclerView.ViewHolder를 상속받아 뷰의 재사용을 최적화
    static class AlarmViewHolder extends RecyclerView.ViewHolder {
        TextView textViewAlarmTime; // 알람 시간 표시
        Switch switchAlarm; // 알람  on/off 제어 스위치

        AlarmViewHolder(View itemView) {
            super(itemView); // itemView 각 알람 항목의 레이아웃 뷰
            textViewAlarmTime = itemView.findViewById(R.id.textViewAlarmTime);
            switchAlarm = itemView.findViewById(R.id.switchAlarm);
        }
    }
}