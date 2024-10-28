// LegalNoticeActivity.java
package com.example.finalpillapp.legalnotice;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import com.example.finalpillapp.Main.MainActivity;
import com.example.pillapp.R;

public class LegalNoticeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_legal_notice);

        ImageButton btnAgreeStart = findViewById(R.id.agree_button);

        btnAgreeStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 버튼을 누르면 바로 MainActivity로 전환
                startActivity(new Intent(LegalNoticeActivity.this, MainActivity.class));
                finish();
            }
        });
    }
}
