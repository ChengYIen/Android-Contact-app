package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.activity.CallActivity;
import com.example.myapplication.activity.EditContactActivity;
import com.example.myapplication.activity.MainActivity;
import com.example.myapplication.db.DBHelper;
import com.example.myapplication.model.Contact;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private Context context;
    private List<Contact> list;
    private DBHelper dbHelper;
    private OnDeleteListener listener;
    private boolean isBatchMode = false;
    private List<Integer> selectedIds = new ArrayList<>();

    public interface OnDeleteListener {
        void onDeleteSuccess();
    }

    public ContactAdapter(Context context, List<Contact> list, DBHelper dbHelper, boolean isBatchMode, OnDeleteListener listener) {
        this.context = context;
        this.list = list;
        this.dbHelper = dbHelper;
        this.isBatchMode = isBatchMode;
        this.listener = listener;
    }

    public void setBatchMode(boolean batchMode) {
        isBatchMode = batchMode;
        if (!isBatchMode) {
            selectedIds.clear();
        }
    }

    public void clearSelection() {
        selectedIds.clear();
    }

    public List<Integer> getSelectedIds() {
        return new ArrayList<>(selectedIds);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact c = list.get(position);
        holder.tvName.setText(c.getName());
        holder.tvPhone.setText(c.getPhone());
        holder.tvGroup.setText("分组：" + c.getGroupName());

        String avatarPath = c.getAvatarPath();
        if (avatarPath != null && !avatarPath.isEmpty()) {
            if (avatarPath.startsWith("content://")) {
                Glide.with(context)
                        .load(Uri.parse(avatarPath))
                        .placeholder(R.mipmap.ic_default_avatar)
                        .error(R.mipmap.ic_default_avatar)
                        .circleCrop()
                        .into(holder.ivAvatar);
            } else {
                File avatarFile = new File(avatarPath);
                if (avatarFile.exists()) {
                    Glide.with(context)
                            .load(avatarFile)
                            .placeholder(R.mipmap.ic_default_avatar)
                            .error(R.mipmap.ic_default_avatar)
                            .circleCrop()
                            .into(holder.ivAvatar);
                } else {
                    holder.ivAvatar.setImageResource(R.mipmap.ic_default_avatar);
                }
            }
        } else {
            holder.ivAvatar.setImageResource(R.mipmap.ic_default_avatar);
        }

        if (c.getIsFavorite() == 1) {
            holder.ivFavorite.setVisibility(View.VISIBLE);
            holder.ivFavorite.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            holder.ivFavorite.setVisibility(View.VISIBLE);
            holder.ivFavorite.setImageResource(android.R.drawable.btn_star_big_off);
        }

        holder.cbSelect.setVisibility(isBatchMode ? View.VISIBLE : View.GONE);
        if (isBatchMode) {
            holder.cbSelect.setChecked(selectedIds.contains(c.getId()));
        }

        holder.itemView.setOnClickListener(v -> {
            if (isBatchMode) {
                toggleSelection(c, holder, position);
            } else {
                Intent callIntent = new Intent(context, CallActivity.class);
                callIntent.putExtra("phone", c.getPhone());
                context.startActivity(callIntent);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (!isBatchMode) {
                Intent editIntent = new Intent(context, EditContactActivity.class);
                editIntent.putExtra("id", c.getId());
                context.startActivity(editIntent);
                return true;
            }
            return false;
        });

        holder.ivFavorite.setOnClickListener(v -> {
            if (isBatchMode) return;

            int newStatus = c.getIsFavorite() == 1 ? 0 : 1;
            dbHelper.toggleFavorite(c.getId(), newStatus);
            c.setIsFavorite(newStatus);
            notifyItemChanged(position);
            Toast.makeText(context, newStatus == 1 ? "已收藏" : "已取消收藏", Toast.LENGTH_SHORT).show();
        });

        holder.ivDelete.setOnClickListener(v -> {
            if (isBatchMode) return;

            new android.app.AlertDialog.Builder(context)
                    .setTitle("确认删除")
                    .setMessage("确定删除该联系人？")
                    .setPositiveButton("确定", (dialog, which) -> {
                        dbHelper.deleteContact(c.getId());
                        list.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, list.size());
                        Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
                        listener.onDeleteSuccess();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });
    }

    private void toggleSelection(Contact contact, ViewHolder holder, int position) {
        if (selectedIds.contains(contact.getId())) {
            selectedIds.remove(Integer.valueOf(contact.getId()));
            holder.cbSelect.setChecked(false);
        } else {
            selectedIds.add(contact.getId());
            holder.cbSelect.setChecked(true);
        }

        if (context instanceof MainActivity) {
            ((MainActivity) context).updateBatchDeleteButton(selectedIds.size());
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvPhone, tvGroup;
        ImageView ivAvatar, ivDelete, ivFavorite;
        CheckBox cbSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            tvPhone = itemView.findViewById(R.id.tv_phone);
            tvGroup = itemView.findViewById(R.id.tv_group);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            ivDelete = itemView.findViewById(R.id.iv_delete);
            ivFavorite = itemView.findViewById(R.id.iv_favorite);
            cbSelect = itemView.findViewById(R.id.cb_select);
        }
    }
}
