package com.example.myapplication.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.db.DBHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.regex.Pattern;

public class EditContactActivity extends AppCompatActivity {

    EditText et_name, et_phone;
    Spinner spinner_group;
    Button btn_save, btn_delete, btn_change_avatar;
    ImageView iv_avatar;
    DBHelper dbHelper;
    int contactId;
    int currentUserId;
    String currentAvatarPath = "";
    int currentIsFavorite = 0;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);

        et_name = findViewById(R.id.et_name);
        et_phone = findViewById(R.id.et_phone);
        spinner_group = findViewById(R.id.spinner_group);
        btn_save = findViewById(R.id.btn_save);
        btn_delete = findViewById(R.id.btn_delete);
        btn_change_avatar = findViewById(R.id.btn_change_avatar);
        iv_avatar = findViewById(R.id.iv_avatar);

        dbHelper = new DBHelper(this);
        contactId = getIntent().getIntExtra("id", -1);

        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        currentUserId = sharedPreferences.getInt("userId", -1);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            saveAvatarToInternalStorage(selectedImageUri);
                        }
                    }
                }
        );

        loadContactData();

        btn_change_avatar.setOnClickListener(v -> checkPermissionsAndPickImage());

        btn_save.setOnClickListener(v -> saveEdit());

        btn_delete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("确认删除")
                    .setMessage("确定删除？")
                    .setPositiveButton("确定", (d, w) -> {
                        if (!currentAvatarPath.isEmpty()) {
                            deleteAvatarFile(currentAvatarPath);
                        }
                        dbHelper.deleteContact(contactId);
                        Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });
    }

    private void checkPermissionsAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, 100);
                return;
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
                return;
            }
        }
        openImagePicker();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                Toast.makeText(this, "需要权限才能选择图片", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void saveAvatarToInternalStorage(Uri imageUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return;

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            String fileName = "avatar_" + contactId + "_" + System.currentTimeMillis() + ".jpg";
            File avatarDir = new File(getFilesDir(), "avatars");
            if (!avatarDir.exists()) {
                avatarDir.mkdirs();
            }

            File avatarFile = new File(avatarDir, fileName);
            FileOutputStream fos = new FileOutputStream(avatarFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            fos.close();

            String newPath = avatarFile.getAbsolutePath();

            if (!currentAvatarPath.isEmpty()) {
                deleteAvatarFile(currentAvatarPath);
            }

            currentAvatarPath = newPath;
            iv_avatar.setImageBitmap(bitmap);

            Toast.makeText(this, "头像已更新", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "保存头像失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteAvatarFile(String path) {
        if (path != null && !path.isEmpty()) {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    private boolean validatePhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }

        if (phone.length() != 11) {
            return false;
        }

        String phonePattern = "^\\d{11}$";
        return Pattern.matches(phonePattern, phone);
    }

    private void loadContactData() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM contact WHERE id=? AND userId=?",
                new String[]{String.valueOf(contactId), String.valueOf(currentUserId)});

        if (cursor.moveToFirst()) {
            String name = cursor.getString(2);
            String phone = cursor.getString(3);
            String group = cursor.getString(4);
            String avatarPath = cursor.getString(5);
            currentIsFavorite = cursor.getInt(6);

            if (avatarPath == null) avatarPath = "";

            et_name.setText(name);
            et_phone.setText(phone);
            currentAvatarPath = avatarPath;

            if (!avatarPath.isEmpty()) {
                if (avatarPath.startsWith("content://")) {
                    Glide.with(this)
                            .load(Uri.parse(avatarPath))
                            .placeholder(R.mipmap.ic_default_avatar)
                            .error(R.mipmap.ic_default_avatar)
                            .circleCrop()
                            .into(iv_avatar);
                } else {
                    File avatarFile = new File(avatarPath);
                    if (avatarFile.exists()) {
                        Glide.with(this)
                                .load(avatarFile)
                                .placeholder(R.mipmap.ic_default_avatar)
                                .error(R.mipmap.ic_default_avatar)
                                .circleCrop()
                                .into(iv_avatar);
                    }
                }
            }

            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner_group.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).equals(group)) {
                    spinner_group.setSelection(i);
                    break;
                }
            }
        }
        cursor.close();
        db.close();
    }

    private void saveEdit() {
        String name = et_name.getText().toString().trim();
        String phone = et_phone.getText().toString().trim();
        String group = spinner_group.getSelectedItem().toString();

        if (name.isEmpty()) {
            Toast.makeText(this, "姓名不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phone.isEmpty()) {
            Toast.makeText(this, "电话不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validatePhone(phone)) {
            if (phone.length() != 11) {
                Toast.makeText(this, "电话号码输入错误：必须是11位数字", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "电话号码输入错误：只能包含数字", Toast.LENGTH_LONG).show();
            }
            return;
        }

        dbHelper.updateContactWithAvatar(contactId, name, phone, group, currentAvatarPath, currentIsFavorite);
        Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
