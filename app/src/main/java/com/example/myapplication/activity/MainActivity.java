package com.example.myapplication.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.myapplication.R;
import com.example.myapplication.adapter.ContactAdapter;
import com.example.myapplication.db.DBHelper;
import com.example.myapplication.model.Contact;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    Button btnAdd, btnLogout, btnBatchDelete, btnConfirmBatchDelete, btnGroupManage, btnProfile;
    Spinner spinnerFilter;
    EditText etSearch;
    DBHelper dbHelper;
    ContactAdapter adapter;
    List<Contact> contactList;
    List<Contact> allContacts;
    SharedPreferences sharedPreferences;
    int currentUserId;
    String currentFilter = "全部联系人";
    String searchKeyword = "";
    boolean isBatchMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recyclerView);
        btnAdd = findViewById(R.id.btn_add);
        btnLogout = findViewById(R.id.btn_logout);
        btnBatchDelete = findViewById(R.id.btn_batch_delete);
        btnConfirmBatchDelete = findViewById(R.id.btn_confirm_batch_delete);
        btnGroupManage = findViewById(R.id.btn_group_manage);
        btnProfile = findViewById(R.id.btn_profile);
        spinnerFilter = findViewById(R.id.spinner_filter);
        etSearch = findViewById(R.id.et_search);

        dbHelper = new DBHelper(this);
        contactList = new ArrayList<>();
        allContacts = new ArrayList<>();

        sharedPreferences = getSharedPreferences("UserInfo", MODE_PRIVATE);
        currentUserId = sharedPreferences.getInt("userId", -1);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnConfirmBatchDelete.setVisibility(View.GONE);

        setupSwipeRefresh();
        loadGroups();
        loadContacts();

        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddContactActivity.class);
            intent.putExtra("userId", currentUserId);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());

        btnBatchDelete.setOnClickListener(v -> toggleBatchMode());

        btnConfirmBatchDelete.setOnClickListener(v -> performBatchDelete());

        btnGroupManage.setOnClickListener(v -> {
            startActivity(new Intent(this, GroupManageActivity.class));
        });

        btnProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = parent.getItemAtPosition(position).toString();
                filterAndDisplayContacts();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchKeyword = s.toString().trim();
                filterAndDisplayContacts();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGroups();
        loadContacts();
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );

        swipeRefreshLayout.setOnRefreshListener(() -> {
            refreshContacts();
        });
    }

    private void refreshContacts() {
        new Thread(() -> {
            try {
                Thread.sleep(800);

                runOnUiThread(() -> {
                    loadContacts();
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(this, "刷新成功，收藏联系人已置顶", Toast.LENGTH_SHORT).show();
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(this, "刷新失败", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void loadGroups() {
        List<String> groups = new ArrayList<>();
        groups.add("全部联系人");
        groups.add("收藏联系人");

        Set<String> customGroups = new HashSet<>();
        customGroups.add("家人");
        customGroups.add("朋友");
        customGroups.add("同事");
        customGroups.add("其他");

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT groupName FROM custom_group WHERE userId=?",
                new String[]{String.valueOf(currentUserId)});

        if (cursor.moveToFirst()) {
            do {
                customGroups.add(cursor.getString(0));
            } while (cursor.moveToNext());
        }
        cursor.close();

        groups.addAll(customGroups);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, groups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);
    }

    private void loadContacts() {
        allContacts.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM contact WHERE userId=? ORDER BY isFavorite DESC, groupname, name",
                new String[]{String.valueOf(currentUserId)});

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String name = cursor.getString(2);
                String phone = cursor.getString(3);
                String group = cursor.getString(4);
                String avatar = cursor.getString(5);
                int isFavorite = cursor.getInt(6);
                allContacts.add(new Contact(id, name, phone, group, avatar, isFavorite));
            } while (cursor.moveToNext());
        }
        cursor.close();

        filterAndDisplayContacts();
    }

    private void filterAndDisplayContacts() {
        contactList.clear();

        for (Contact contact : allContacts) {
            boolean matchGroup = false;

            if (currentFilter.equals("全部联系人")) {
                matchGroup = true;
            } else if (currentFilter.equals("收藏联系人")) {
                matchGroup = contact.getIsFavorite() == 1;
            } else {
                matchGroup = contact.getGroupName().equals(currentFilter);
            }

            boolean matchSearch = searchKeyword.isEmpty() ||
                    contact.getName().toLowerCase().contains(searchKeyword.toLowerCase()) ||
                    contact.getPhone().contains(searchKeyword);

            if (matchGroup && matchSearch) {
                contactList.add(contact);
            }
        }

        if (adapter == null) {
            adapter = new ContactAdapter(this, contactList, dbHelper, isBatchMode, () -> loadContacts());
            recyclerView.setAdapter(adapter);
        } else {
            adapter.setBatchMode(isBatchMode);
            adapter.notifyDataSetChanged();
        }
    }

    private void toggleBatchMode() {
        isBatchMode = !isBatchMode;

        if (isBatchMode) {
            btnBatchDelete.setText("取消批量");
            btnConfirmBatchDelete.setVisibility(View.VISIBLE);
            btnConfirmBatchDelete.setText("删除选中联系人 (0)");
            Toast.makeText(this, "请选择要删除的联系人", Toast.LENGTH_SHORT).show();
        } else {
            btnBatchDelete.setText("批量删除");
            btnConfirmBatchDelete.setVisibility(View.GONE);
        }

        if (adapter != null) {
            adapter.setBatchMode(isBatchMode);
            adapter.clearSelection();
            adapter.notifyDataSetChanged();
        }
    }

    public void updateBatchDeleteButton(int count) {
        if (count > 0) {
            btnConfirmBatchDelete.setText("删除选中联系人 (" + count + ")");
            btnConfirmBatchDelete.setEnabled(true);
        } else {
            btnConfirmBatchDelete.setText("删除选中联系人 (0)");
            btnConfirmBatchDelete.setEnabled(false);
        }
    }

    private void performBatchDelete() {
        if (adapter == null) return;

        List<Integer> selectedIds = adapter.getSelectedIds();
        if (selectedIds.isEmpty()) {
            Toast.makeText(this, "请先选择联系人", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("批量删除")
                .setMessage("确定删除选中的 " + selectedIds.size() + " 个联系人？此操作不可恢复！")
                .setPositiveButton("确定删除", (dialog, which) -> {
                    int[] ids = new int[selectedIds.size()];
                    for (int i = 0; i < selectedIds.size(); i++) {
                        ids[i] = selectedIds.get(i);
                    }
                    dbHelper.deleteContacts(ids);
                    loadContacts();
                    toggleBatchMode();
                    Toast.makeText(this, "成功删除 " + ids.length + " 个联系人", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
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
