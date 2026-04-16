# 号外号外 - 华为推送服务客户端

这是一个基于华为推送服务（HUAWEI Push Kit）开发的Android应用，包名为 `evilcode.notification.hwpush`。

## 功能特性

### 1. 基础推送能力
- 获取和注销华为推送Token
- 接收推送消息（通知栏消息和透传消息）
- 设置通知栏消息显示开关
- 自定义点击消息动作

### 2. 消息记录管理
- 查看推送消息记录，包括消息内容、接收时间等信息
- 支持复制消息记录内容（点击消息卡片即可复制）
- 支持删除选定的消息记录（长按消息卡片删除）
- 支持清空所有消息记录

### 3. 自动化构建
- 配置了GitHub Action workflow，支持自动编译APK文件
- 支持Release和Debug两种构建类型
- 构建产物可直接下载使用

## 项目结构

```
haowai/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/evilcode/notification/hwpush/
│   │       │   ├── MainActivity.java          # 主界面
│   │       │   ├── MyPushService.java         # 推送服务
│   │       │   ├── MessageRecord.java         # 消息记录数据模型
│   │       │   ├── MessageAdapter.java        # 消息列表适配器
│   │       │   └── DatabaseHelper.java        # 数据库帮助类
│   │       ├── res/
│   │       │   ├── layout/                    # 布局文件
│   │       │   └── values/                    # 资源文件
│   │       └── AndroidManifest.xml
│   ├── agconnect-services.json                # 华为AGC配置文件
│   └── build.gradle
├── .github/
│   └── workflows/
│       └── build.yml                           # GitHub Action配置
├── build.gradle
├── gradle.properties
├── settings.gradle
└── README.md
```

## 使用说明

### 1. 环境要求
- Android Studio 2021.2.1 及以上
- JDK 1.8 及以上
- Android SDK API 24 及以上
- 华为HMS Core 4.0 及以上

### 2. 编译步骤

#### 本地编译
1. 克隆项目到本地
2. 使用Android Studio打开项目
3. 等待Gradle同步完成
4. 连接华为设备或启动模拟器
5. 点击运行按钮或执行 `./gradlew assembleRelease` 命令

#### GitHub Action 自动编译
1. 将代码推送到GitHub仓库
2. GitHub Action会自动触发构建
3. 构建完成后，在Actions页面下载生成的APK文件

### 3. 功能使用

#### 获取推送Token
- 打开应用后会自动尝试获取Token
- 也可以点击"获取Token"按钮手动获取
- Token会显示在界面上，支持长按复制

#### 注销推送Token
- 点击"注销Token"按钮可注销当前Token

#### 通知栏消息开关
- 使用开关控件可以开启或关闭通知栏消息的显示

#### 查看消息记录
- 收到的推送消息会自动保存并显示在消息列表中
- 点击消息卡片可以复制完整消息内容
- 长按消息卡片可以删除该条记录
- 点击"清空"按钮可以清空所有消息记录

## 签名信息

本项目已配置签名信息：
- 密钥文件：`../doc/evilcode.jks`
- KeyAlias：`evilcode`
- KeyPassword：`@evilcode1024`
- StorePassword：`@evilcode1024`

## 注意事项

1. 确保设备已安装华为HMS Core
2. 确保网络连接正常，以便获取Token和接收推送
3. 在Android 13及以上版本，需要授予通知权限
4. agconnect-services.json文件已配置好，无需修改

## 技术支持

- 华为推送服务官方文档：请参考上级目录中的 `华为推送服务文档.md`
- 华为开发者论坛：https://developer.huawei.com/consumer/cn/forum/
- Stack Overflow：https://stackoverflow.com/questions/tagged/huawei-mobile-services

## 许可证

本项目基于示例代码开发，遵循Apache-2.0许可证。
