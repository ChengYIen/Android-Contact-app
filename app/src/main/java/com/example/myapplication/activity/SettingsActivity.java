package com.example.myapplication.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import com.example.myapplication.R;
import java.io.File;

public class SettingsActivity extends AppCompatActivity {

    SwitchCompat switchDarkMode, switchNotification;
    TextView tvCacheSize;
    SharedPreferences settingsPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        settingsPrefs = getSharedPreferences("AppSettings", MODE_PRIVATE);

        switchDarkMode = findViewById(R.id.switch_dark_mode);
        switchNotification = findViewById(R.id.switch_notification);
        tvCacheSize = findViewById(R.id.tv_cache_size);

        // 返回按钮
        findViewById(R.id.tv_back).setOnClickListener(v -> finish());

        // 导出数据
        findViewById(R.id.ll_export_data).setOnClickListener(v -> {
            Toast.makeText(this, "数据导出功能开发中...", Toast.LENGTH_SHORT).show();
        });

        // 清除缓存
        findViewById(R.id.ll_clear_cache).setOnClickListener(v -> {
            showClearCacheDialog();
        });

        // 加载设置状态
        loadSettings();

        // 深色模式切换
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsPrefs.edit().putBoolean("darkMode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(isChecked ?
                    AppCompatDelegate.MODE_NIGHT_YES :
                    AppCompatDelegate.MODE_NIGHT_NO);
        });

        // 通知开关
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsPrefs.edit().putBoolean("notification", isChecked).apply();
            Toast.makeText(this, isChecked ? "通知已开启" : "通知已关闭", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadSettings() {
        boolean isDarkMode = settingsPrefs.getBoolean("darkMode", false);
        boolean isNotification = settingsPrefs.getBoolean("notification", true);

        switchDarkMode.setChecked(isDarkMode);
        switchNotification.setChecked(isNotification);

        calculateCacheSize();
    }

    private void calculateCacheSize() {
        long cacheSize = 0;

        // 计算应用缓存大小
        File cacheDir = getCacheDir();
        if (cacheDir != null && cacheDir.exists()) {
            File[] files = cacheDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    cacheSize += file.length();
                }
            }
        }

        // 计算头像文件夹大小
        File avatarDir = new File(getFilesDir(), "avatars");
        if (avatarDir.exists()) {
            File[] files = avatarDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    cacheSize += file.length();
                }
            }
        }

        tvCacheSize.setText(formatSize(cacheSize));
    }

    private String formatSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else {
            return String.format("%.1f MB", size / (1024.0 * 1024));
        }
    }

    private void showClearCacheDialog() {
        new AlertDialog.Builder(this)
                .setTitle("清除缓存")
                .setMessage("确定要清除应用缓存吗？此操作不会删除联系人数据。")
                .setPositiveButton("确定", (dialog, which) -> clearCache())
                .setNegativeButton("取消", null)
                .show();
    }

    private void clearCache() {
        // 清除缓存目录
        File cacheDir = getCacheDir();
        if (cacheDir != null && cacheDir.exists()) {
            deleteDir(cacheDir);
        }

        Toast.makeText(this, "缓存已清除", Toast.LENGTH_SHORT).show();
        calculateCacheSize();
    }

    private void deleteDir(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDir(file);
                } else {
                    file.delete();
                }
            }
        }
    }
}
