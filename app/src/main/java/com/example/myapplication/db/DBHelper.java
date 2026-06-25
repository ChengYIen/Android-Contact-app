package com.example.myapplication.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "contact.db";
    private static final int VERSION = 4;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE user(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT, pwd TEXT, nickname TEXT DEFAULT '', avatar TEXT DEFAULT '', registerTime TEXT DEFAULT '')");
        db.execSQL("CREATE TABLE contact(id INTEGER PRIMARY KEY AUTOINCREMENT, userId INTEGER, name TEXT, phone TEXT, groupname TEXT, avatar TEXT, isFavorite INTEGER DEFAULT 0)");
        db.execSQL("CREATE TABLE custom_group(id INTEGER PRIMARY KEY AUTOINCREMENT, userId INTEGER, groupName TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE contact ADD COLUMN userId INTEGER DEFAULT 0");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE contact ADD COLUMN avatar TEXT DEFAULT ''");
            db.execSQL("ALTER TABLE contact ADD COLUMN isFavorite INTEGER DEFAULT 0");
            db.execSQL("CREATE TABLE IF NOT EXISTS custom_group(id INTEGER PRIMARY KEY AUTOINCREMENT, userId INTEGER, groupName TEXT)");
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE user ADD COLUMN nickname TEXT DEFAULT ''");
            db.execSQL("ALTER TABLE user ADD COLUMN avatar TEXT DEFAULT ''");
            db.execSQL("ALTER TABLE user ADD COLUMN registerTime TEXT DEFAULT ''");
        }
    }

    public void updateContact(int id, String name, String phone, String groupname) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("phone", phone);
        cv.put("groupname", groupname);
        db.update("contact", cv, "id=?", new String[]{id + ""});
        db.close();
    }

    public void updateContactWithAvatar(int id, String name, String phone, String groupname, String avatarPath, int isFavorite) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("phone", phone);
        cv.put("groupname", groupname);
        cv.put("avatar", avatarPath);
        cv.put("isFavorite", isFavorite);
        db.update("contact", cv, "id=?", new String[]{id + ""});
        db.close();
    }

    public void deleteContact(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("contact", "id=?", new String[]{id + ""});
        db.close();
    }

    public void deleteContacts(int[] ids) {
        SQLiteDatabase db = getWritableDatabase();
        StringBuilder whereClause = new StringBuilder("id IN (");
        for (int i = 0; i < ids.length; i++) {
            whereClause.append("?");
            if (i < ids.length - 1) {
                whereClause.append(",");
            }
        }
        whereClause.append(")");

        String[] args = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            args[i] = String.valueOf(ids[i]);
        }

        db.delete("contact", whereClause.toString(), args);
        db.close();
    }

    public void toggleFavorite(int id, int isFavorite) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("isFavorite", isFavorite);
        db.update("contact", cv, "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void addCustomGroup(int userId, String groupName) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("userId", userId);
        cv.put("groupName", groupName);
        db.insert("custom_group", null, cv);
        db.close();
    }

    public void deleteCustomGroup(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("custom_group", "id=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void updateUserProfile(int userId, String nickname, String avatar) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("nickname", nickname);
        cv.put("avatar", avatar);
        db.update("user", cv, "id=?", new String[]{String.valueOf(userId)});
        db.close();
    }

    public void updateUserPassword(int userId, String newPassword) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("pwd", newPassword);
        db.update("user", cv, "id=?", new String[]{String.valueOf(userId)});
        db.close();
    }

    public int getContactCount(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM contact WHERE userId=?", new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }

    public int getFavoriteCount(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM contact WHERE userId=? AND isFavorite=1", new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }

    public int getGroupCount(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM custom_group WHERE userId=?", new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst()) count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }

    public boolean verifyPassword(int userId, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT id FROM user WHERE id=? AND pwd=?", new String[]{String.valueOf(userId), password});
        boolean result = cursor.moveToFirst();
        cursor.close();
        db.close();
        return result;
    }
}
