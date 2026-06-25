# Android Contact Management App

一个功能完善的 Android 通讯录管理应用，支持用户注册登录、联系人管理、分组管理和通话记录等功能。

## 📱 功能特性

### 用户系统
- ✅ 用户注册与登录
- ✅ 个人资料编辑（头像、昵称等）
- ✅ 密码修改
- ✅ 个人信息查看

### 联系人管理
- ✅ 添加新联系人（姓名、电话、分组）
- ✅ 编辑联系人信息
- ✅ 删除联系人
- ✅ 联系人列表展示
- ✅ 按分组筛选联系人

### 分组管理
- ✅ 创建联系人群组
- ✅ 编辑分组名称
- ✅ 删除分组
- ✅ 将联系人分配到不同分组

### 通话功能
- ✅ 一键拨号
- ✅ 通话界面展示

### 其他功能
- ✅ 设置页面
- ✅ 关于页面
- ✅ 数据存储（SQLite 数据库）

## 🛠️ 技术栈

- **开发语言**: Java
- **构建工具**: Gradle 8.2.0
- **Android SDK**: API Level 31+ (Android 12+)
- **UI 框架**: Material Design Components 1.10.0
- **数据库**: SQLite (DBHelper)
- **架构模式**: MVC
- **依赖库**:
  - AndroidX AppCompat 1.6.1
  - ConstraintLayout 2.1.4
  - Activity 1.8.0

## 📦 项目结构

```
MyApplication/
── app/
│   ├── src/main/
│   │   ├── java/com/example/myapplication/
│   │   │   ├── activity/          # Activity 页面
│   │   │   │   ├── LoginActivity.java
│   │   │   │   ├── RegisterActivity.java
│   │   │   │   ├── MainActivity.java
│   │   │   │   ├── AddContactActivity.java
│   │   │   │   ├── EditContactActivity.java
│   │   │   │   ├── CallActivity.java
│   │   │   │   ├── GroupManageActivity.java
│   │   │   │   ├── ProfileActivity.java
│   │   │   │   ├── EditProfileActivity.java
│   │   │   │   ├── ChangePasswordActivity.java
│   │   │   │   ├── SettingsActivity.java
│   │   │   │   └── AboutActivity.java
│   │   │   ├── adapter/           # 适配器
│   │   │   │   └── ContactAdapter.java
│   │   │   ├── db/                # 数据库
│   │   │   │   └── DBHelper.java
│   │   │   └── model/             # 数据模型
│   │   │       └── Contact.java
│   │   ├── res/                   # 资源文件
│   │   │   ├── layout/            # 布局文件
│   │   │   ├── drawable/          # 图片资源
│   │   │   ├── values/            # 字符串、颜色、主题
│   │   │   └── mipmap-*/          # 启动图标
│   │   └── AndroidManifest.xml    # 应用配置清单
│   └── build.gradle               # 模块级构建配置
├── gradle/                        # Gradle 配置
├── build.gradle                   # 项目级构建配置
└── settings.gradle                # 项目设置
```

## 🚀 快速开始

### 环境要求
- **JDK**: 17 或更高版本
- **Android Studio**: Hedgehog (2023.1.1) 或更高版本
- **Gradle**: 8.2+
- **Android SDK**: API Level 31+ (Android 12+)

### 安装步骤

1. **克隆仓库**
   ```bash
   git clone https://github.com/ChengYien/Android-Contact-app.git
   cd Android-Contact-app
   ```

2. **使用 Android Studio 打开项目**
   - 启动 Android Studio
   - 选择 "Open an existing project"
   - 选择项目根目录

3. **同步 Gradle**
   - 等待 Gradle 自动同步完成
   - 如果同步失败，检查网络连接或配置镜像源

4. **运行项目**
   - 连接 Android 设备或启动模拟器
   - 点击 Run 按钮或按 `Shift + F10`

## 📸 应用截图

（此处可以添加应用截图）

## 🔐 权限说明

本应用需要以下权限：
- `READ_EXTERNAL_STORAGE`: 读取外部存储（用于选择头像）
- `WRITE_EXTERNAL_STORAGE`: 写入外部存储（用于保存头像）
- `READ_MEDIA_IMAGES`: 读取媒体图片（Android 13+）
- `CALL_PHONE`: 拨打电话

## 🗄️ 数据库设计

### 用户表 (users)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER | 主键，自增 |
| username | TEXT | 用户名 |
| password | TEXT | 密码 |
| nickname | TEXT | 昵称 |
| avatar | TEXT | 头像路径 |

### 联系人表 (contacts)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER | 主键，自增 |
| name | TEXT | 联系人姓名 |
| phone | TEXT | 电话号码 |
| group_name | TEXT | 所属分组 |
| user_id | INTEGER | 所属用户ID |

### 分组表 (groups)
| 字段 | 类型 | 说明 |
|------|------|------|
| id | INTEGER | 主键，自增 |
| name | TEXT | 分组名称 |
| user_id | INTEGER | 所属用户ID |

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建你的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交你的更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启一个 Pull Request

## 📝 许可证

本项目仅供学习和参考使用。

## 👨‍💻 作者

**ChengYien**

- GitHub: [@ChengYien](https://github.com/ChengYien)

## 🙏 致谢

感谢所有为本项目做出贡献的开发者！

---

⭐ 如果这个项目对你有帮助，请给个 Star 吧！
