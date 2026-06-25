package com.example.myapplication.activity;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;
import com.example.myapplication.db.DBHelper;
import java.util.ArrayList;
import java.util.List;

public class GroupManageActivity extends AppCompatActivity {

    LinearLayout layoutGroups;
    Button btnAddGroup;
    DBHelper dbHelper;
    SharedPreferences sharedPreferences;
    int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_manage);

        layoutGroups = findViewById(R.id.layout_groups);
        btnAddGroup = findViewById(R.id.btn_add_group);
        dbHelper = new DBHelper(this);
        sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        currentUserId = sharedPreferences.getInt("userId", -1);

        loadGroups();

        btnAddGroup.setOnClickListener(v -> showAddGroupDialog());
    }

    private void loadGroups() {
        layoutGroups.removeAllViews();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM custom_group WHERE userId=?",
                new String[]{String.valueOf(currentUserId)});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String groupName = cursor.getString(2);
                addGroupView(id, groupName);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void addGroupView(int id, String groupName) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_group, layoutGroups, false);
        TextView tvGroupName = view.findViewById(R.id.tv_group_name);
        Button btnDelete = view.findViewById(R.id.btn_delete_group);

        tvGroupName.setText(groupName);
        btnDelete.setOnClickListener(v -> deleteGroup(id));

        layoutGroups.addView(view);
    }

    private void showAddGroupDialog() {
        EditText input = new EditText(this);
        input.setHint("输入分组名称");

        new AlertDialog.Builder(this)
                .setTitle("添加分组")
                .setView(input)
                .setPositiveButton("确定", (dialog, which) -> {
                    String groupName = input.getText().toString().trim();
                    if (groupName.isEmpty()) {
                        Toast.makeText(this, "分组名称不能为空", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    dbHelper.addCustomGroup(currentUserId, groupName);
                    loadGroups();
                    Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteGroup(int id) {
        new AlertDialog.Builder(this)
                .setTitle("删除分组")
                .setMessage("确定删除该分组？")
                .setPositiveButton("确定", (dialog, which) -> {
                    dbHelper.deleteCustomGroup(id);
                    loadGroups();
                    Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
