package com.example.myapplication.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.example.myapplication.R;

public class CallActivity extends AppCompatActivity {

    TextView tv_phone;
    Button btn_call, btn_back;
    String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        tv_phone = findViewById(R.id.tv_phone);
        btn_call = findViewById(R.id.btn_call);
        btn_back = findViewById(R.id.btn_back);

        // 获取传递过来的电话号码
        phoneNumber = getIntent().getStringExtra("phone");
        tv_phone.setText("拨打：" + phoneNumber);

        // 拨号按钮
        btn_call.setOnClickListener(v -> {
            makeCall();
        });

        // 返回按钮
        btn_back.setOnClickListener(v -> {
            finish();
        });
    }

    // 拨号方法
    private void makeCall() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            // 申请权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, 100);
            return;
        }

        // 有权限 → 直接拨号
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    // 权限申请结果
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeCall();
            } else {
                Toast.makeText(this, "拨号权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }
}