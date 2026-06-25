package com.example.myapplication.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.myapplication.db.DBHelper;
import java.io.File;

public class ProfileActivity extends AppCompatActivity {

    ImageView ivAvatar;
    TextView tvUsername, tvNickname, tvRegisterTime;
    TextView tvContactCount, tvFavoriteCount;
    Button btnLogout;
    DBHelper dbHelper;
    SharedPreferences sharedPreferences;
    int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper = new DBHelper(this);
        sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        currentUserId = sharedPreferences.getInt("userId", -1);

        ivAvatar = findViewById(R.id.iv_avatar);
        tvUsername = findViewById(R.id.tv_username);
        tvNickname = findViewById(R.id.tv_nickname);
        tvRegisterTime = findViewById(R.id.tv_register_time);
        tvContactCount = findViewById(R.id.tv_contact_count);
        tvFavoriteCount = findViewById(R.id.tv_favorite_count);
        btnLogout = findViewById(R.id.btn_logout);

        // 返回按钮
        findViewById(R.id.tv_back).setOnClickListener(v -> finish());

        // 编辑资料
        findViewById(R.id.tv_edit).setOnClickListener(v -> {
            startActivity(new Intent(this, EditProfileActivity.class));
        });

        // 修改密码
        findViewById(R.id.ll_change_password).setOnClickListener(v -> {
            startActivity(new Intent(this, ChangePasswordActivity.class));
        });

        // 应用设置
        findViewById(R.id.ll_settings).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        // 关于应用
        findViewById(R.id.ll_about).setOnClickListener(v -> {
            startActivity(new Intent(this, AboutActivity.class));
        });

        // 退出登录
        btnLogout.setOnClickListener(v -> showLogoutDialog());

        loadUserInfo();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserInfo();
    }

    private void loadUserInfo() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM user WHERE id=?", new String[]{String.valueOf(currentUserId)});

        if (cursor.moveToFirst()) {
            String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
            String nickname = cursor.getString(cursor.getColumnIndexOrThrow("nickname"));
            String avatar = cursor.getString(cursor.getColumnIndexOrThrow("avatar"));
            String registerTime = cursor.getString(cursor.getColumnIndexOrThrow("registerTime"));

            tvUsername.setText(username);
            tvNickname.setText(nickname.isEmpty() ? "未设置昵称" : nickname);
            tvRegisterTime.setText("注册时间: " + (registerTime.isEmpty() ? "未知" : registerTime));

            // 加载头像
            if (!avatar.isEmpty()) {
                File avatarFile = new File(avatar);
                if (avatarFile.exists()) {
                    ivAvatar.setImageURI(Uri.fromFile(avatarFile));
                }
            }
        }
        cursor.close();
        db.close();

        // 加载统计数据
        tvContactCount.setText(String.valueOf(dbHelper.getContactCount(currentUserId)));
        tvFavoriteCount.setText(String.valueOf(dbHelper.getFavoriteCount(currentUserId)));
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("退出登录")
                .setMessage("确定要退出登录吗？")
                .setPositiveButton("确定", (dialog, which) -> logout())
                .setNegativeButton("取消", null)
                .show();
    }

    private void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        Toast.makeText(this, "已退出登录", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
