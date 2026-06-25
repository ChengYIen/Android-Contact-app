package com.example.myapplication.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.myapplication.db.DBHelper;

public class ChangePasswordActivity extends AppCompatActivity {

    EditText etOldPassword, etNewPassword, etConfirmPassword;
    Button btnChangePassword;
    DBHelper dbHelper;
    SharedPreferences sharedPreferences;
    int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        dbHelper = new DBHelper(this);
        sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        currentUserId = sharedPreferences.getInt("userId", -1);

        etOldPassword = findViewById(R.id.et_old_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnChangePassword = findViewById(R.id.btn_change_password);

        // 返回按钮
        findViewById(R.id.tv_back).setOnClickListener(v -> finish());

        // 修改密码
        btnChangePassword.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String oldPassword = etOldPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (oldPassword.isEmpty()) {
            Toast.makeText(this, "请输入当前密码", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.isEmpty()) {
            Toast.makeText(this, "请输入新密码", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "新密码长度不能少于6位", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        if (oldPassword.equals(newPassword)) {
            Toast.makeText(this, "新密码不能与当前密码相同", Toast.LENGTH_SHORT).show();
            return;
        }

        // 验证当前密码
        if (!dbHelper.verifyPassword(currentUserId, oldPassword)) {
            Toast.makeText(this, "当前密码错误", Toast.LENGTH_SHORT).show();
            return;
        }

        // 更新密码
        dbHelper.updateUserPassword(currentUserId, newPassword);
        Toast.makeText(this, "密码修改成功", Toast.LENGTH_SHORT).show();
        finish();
    }
}
