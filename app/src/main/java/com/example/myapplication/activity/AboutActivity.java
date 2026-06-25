package com.example.myapplication.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView tvVersion = findViewById(R.id.tv_version);
        tvVersion.setText("版本 1.0.0");

        // 返回按钮
        findViewById(R.id.tv_back).setOnClickListener(v -> finish());

        // 检查更新
        findViewById(R.id.ll_check_update).setOnClickListener(v -> {
            Toast.makeText(this, "当前已是最新版本", Toast.LENGTH_SHORT).show();
        });

        // 使用说明
        findViewById(R.id.ll_user_guide).setOnClickListener(v -> {
            showUserGuide();
        });

        // 反馈与建议
        findViewById(R.id.ll_feedback).setOnClickListener(v -> {
            showFeedbackDialog();
        });
    }

    private void showUserGuide() {
        String guide = "【使用说明】\n\n" +
                "1. 添加联系人：点击主页[添加联系人]按钮，输入姓名、电话并选择分组。\n\n" +
                "2. 搜索联系人：在搜索框输入姓名或电话号码快速查找。\n\n" +
                "3. 分组管理：通过下拉框筛选分组，点击[管理分组]可添加或删除自定义分组。\n\n" +
                "4. 收藏联系人：点击联系人卡片上的星标图标可收藏/取消收藏。\n\n" +
                "5. 编辑/删除：点击联系人卡片可编辑信息，长按可选择删除。\n\n" +
                "6. 批量删除：点击[批量删除]进入多选模式，选择后点击确认删除。\n\n" +
                "7. 下拉刷新：在联系人列表下拉可刷新，收藏联系人会自动置顶。\n\n" +
                "8. 个人中心：点击[我的]可查看个人信息、修改密码、应用设置等。";
    
        new AlertDialog.Builder(this)
                .setTitle("使用说明")
                .setMessage(guide)
                .setPositiveButton("知道了", null)
                .show();
    }

    private void showFeedbackDialog() {
        String[] options = {"发送邮件", "查看GitHub"};

        new AlertDialog.Builder(this)
                .setTitle("反馈与建议")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // 发送邮件
                        try {
                            Intent intent = new Intent(Intent.ACTION_SENDTO);
                            intent.setData(Uri.parse("mailto:"));
                            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"feedback@example.com"});
                            intent.putExtra(Intent.EXTRA_SUBJECT, "个人通讯录 - 反馈与建议");
                            startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(this, "未找到邮件应用", Toast.LENGTH_SHORT).show();
                        }
                    } else if (which == 1) {
                        // 打开GitHub
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("https://github.com"));
                            startActivity(intent);
                        } catch (Exception e) {
                            Toast.makeText(this, "无法打开浏览器", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
