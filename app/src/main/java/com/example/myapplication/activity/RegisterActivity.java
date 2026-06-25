package com.example.myapplication.activity;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.myapplication.db.DBHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {
    EditText etUser, etPwd;
    Button btnReg;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        etUser = findViewById(R.id.et_reg_user);
        etPwd = findViewById(R.id.et_reg_pwd);
        btnReg = findViewById(R.id.btn_reg_ok);
        dbHelper = new DBHelper(this);

        btnReg.setOnClickListener(v -> register());
    }

    private void register() {
        String user = etUser.getText().toString().trim();
        String pwd = etPwd.getText().toString().trim();
        if (user.isEmpty() || pwd.isEmpty()) {
            Toast.makeText(this, "不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String registerTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        db.execSQL("insert into user(username,pwd,registerTime) values(?,?,?)", new Object[]{user, pwd, registerTime});
        Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show();
        finish();
    }
}
