package com.example.myapplication.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.myapplication.db.DBHelper;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class AddContactActivity extends AppCompatActivity {

    EditText et_name, et_phone;
    Spinner spinner_group;
    Button btn_save, btn_choose_avatar;
    ImageView iv_avatar;
    DBHelper dbHelper;
    int userId;
    String selectedAvatarPath = "";

    private ActivityResultLauncher<Intent> galleryLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);

        et_name = findViewById(R.id.et_name);
        et_phone = findViewById(R.id.et_phone);
        spinner_group = findViewById(R.id.spinner_group);
        btn_save = findViewById(R.id.btn_save);
        btn_choose_avatar = findViewById(R.id.btn_choose_avatar);
        iv_avatar = findViewById(R.id.iv_avatar);
        dbHelper = new DBHelper(this);

        userId = getIntent().getIntExtra("userId", -1);

        loadGroups();

        btn_save.setOnClickListener(v -> saveContact());
        btn_choose_avatar.setOnClickListener(v -> openGallery());

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        String imagePath = saveImageToPublicStorage(imageUri);
                        if (imagePath != null) {
                            selectedAvatarPath = imagePath;
                            iv_avatar.setImageURI(Uri.parse(imagePath));
                            Toast.makeText(this, "头像已保存到相册", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void loadGroups() {
        Set<String> groups = new HashSet<>();
        groups.add("家人");
        groups.add("朋友");
        groups.add("同事");
        groups.add("其他");

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT groupName FROM custom_group WHERE userId=?",
                new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                groups.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();

        List<String> groupList = new ArrayList<>(groups);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, groupList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_group.setAdapter(adapter);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private String saveImageToPublicStorage(Uri imageUri) {
        OutputStream outputStream = null;
        InputStream inputStream = null;

        try {
            inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) return null;

            String fileName = "ContactAvatar_" + System.currentTimeMillis() + ".jpg";

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MyContacts");

                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri == null) return null;

                outputStream = getContentResolver().openOutputStream(uri);
                if (outputStream == null) return null;

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                return uri.toString();
            } else {
                File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                File myContactsDir = new File(picturesDir, "MyContacts");

                if (!myContactsDir.exists()) {
                    myContactsDir.mkdirs();
                }

                File file = new File(myContactsDir, fileName);
                outputStream = new FileOutputStream(file);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                return file.getAbsolutePath();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "保存失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        } finally {
            try {
                if (outputStream != null) outputStream.close();
                if (inputStream != null) inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
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

    private void saveContact() {
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

        try {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.execSQL("INSERT INTO contact(userId, name, phone, groupname, avatar, isFavorite) VALUES(?,?,?,?,?,?)",
                    new Object[]{userId, name, phone, group, selectedAvatarPath, 0});

            Toast.makeText(this, "添加成功！分组：" + group, Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "保存失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
