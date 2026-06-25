package com.example.myapplication.model;

public class Contact {
    private int id;
    private int userId;
    private String name;
    private String phone;
    private String groupName;
    private String avatarPath;
    private int isFavorite;

    public Contact(int id, String name, String phone, String groupName, String avatarPath, int isFavorite) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.groupName = groupName;
        this.avatarPath = avatarPath;
        this.isFavorite = isFavorite;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getGroupName() { return groupName; }
    public String getAvatarPath() { return avatarPath; }
    public int getIsFavorite() { return isFavorite; }

    public void setIsFavorite(int isFavorite) {
        this.isFavorite = isFavorite;
    }
}
