package com.example.myapplication.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.myapplication.db.DBHelper;

public class LoginActivity extends AppCompatActivity {
    EditText etUser, etPwd;
    Button btnLogin, btnRegister;
    DBHelper dbHelper;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etUser = findViewById(R.id.et_username);
        etPwd = findViewById(R.id.et_pwd);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        dbHelper = new DBHelper(this);
        sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);

        btnLogin.setOnClickListener(v -> login());
        btnRegister.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void login() {
        String user = etUser.getText().toString().trim();
        String pwd = etPwd.getText().toString().trim();
        if (user.isEmpty() || pwd.isEmpty()) {
            Toast.makeText(this, "账号密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from user where username=? and pwd=?", new String[]{user, pwd});
        if (cursor.moveToFirst()) {
            int userId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("userId", userId);
            editor.putString("username", username);
            editor.apply();

            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "账号或密码错误", Toast.LENGTH_SHORT).show();
        }
        cursor.close();
        db.close();
    }
}
