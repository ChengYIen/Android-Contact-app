package com.example.myapplication.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.myapplication.R;
import com.example.myapplication.db.DBHelper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class EditProfileActivity extends AppCompatActivity {

    ImageView ivAvatar;
    TextView tvUsername;
    EditText etNickname;
    Button btnSave;
    DBHelper dbHelper;
    SharedPreferences sharedPreferences;
    int currentUserId;
    String avatarPath = "";
    Uri selectedImageUri;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        dbHelper = new DBHelper(this);
        sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        currentUserId = sharedPreferences.getInt("userId", -1);

        ivAvatar = findViewById(R.id.iv_avatar);
        tvUsername = findViewById(R.id.tv_username);
        etNickname = findViewById(R.id.et_nickname);
        btnSave = findViewById(R.id.btn_save);

        // 返回按钮
        findViewById(R.id.tv_back).setOnClickListener(v -> finish());

        // 初始化图片选择器
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        ivAvatar.setImageURI(selectedImageUri);
                    }
                }
        );

        // 点击更换头像
        ivAvatar.setOnClickListener(v -> {
            if (checkPermission()) {
                pickImage();
            }
        });

        // 保存按钮
        btnSave.setOnClickListener(v -> saveProfile());

        loadUserInfo();
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 100);
                return false;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
                return false;
            }
        }
        return true;
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void loadUserInfo() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM user WHERE id=?", new String[]{String.valueOf(currentUserId)});

        if (cursor.moveToFirst()) {
            String username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
            String nickname = cursor.getString(cursor.getColumnIndexOrThrow("nickname"));
            String avatar = cursor.getString(cursor.getColumnIndexOrThrow("avatar"));

            tvUsername.setText(username);
            etNickname.setText(nickname);
            avatarPath = avatar;

            if (!avatar.isEmpty()) {
                File avatarFile = new File(avatar);
                if (avatarFile.exists()) {
                    ivAvatar.setImageURI(Uri.fromFile(avatarFile));
                }
            }
        }
        cursor.close();
        db.close();
    }

    private void saveProfile() {
        String nickname = etNickname.getText().toString().trim();

        if (nickname.isEmpty()) {
            Toast.makeText(this, "昵称不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 保存头像
        if (selectedImageUri != null) {
            try {
                File dir = new File(getFilesDir(), "avatars");
                if (!dir.exists()) dir.mkdirs();
                File avatarFile = new File(dir, "user_" + currentUserId + ".jpg");

                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                OutputStream outputStream = new FileOutputStream(avatarFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                inputStream.close();
                outputStream.close();

                avatarPath = avatarFile.getAbsolutePath();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "头像保存失败", Toast.LENGTH_SHORT).show();
            }
        }

        // 更新数据库
        dbHelper.updateUserProfile(currentUserId, nickname, avatarPath);

        // 更新SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("nickname", nickname);
        editor.putString("avatar", avatarPath);
        editor.apply();

        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickImage();
        } else {
            Toast.makeText(this, "需要存储权限才能更换头像", Toast.LENGTH_SHORT).show();
        }
    }
}
