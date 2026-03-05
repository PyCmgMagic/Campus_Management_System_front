# 💻 教务管理系统前端 Campus Management System Front

<div align="center">

![GitHub commit activity](https://img.shields.io/github/commit-activity/m/PyCmgMagic/Campus_Management_System_front)
![GitHub last commit](https://img.shields.io/github/last-commit/PyCmgMagic/Campus_Management_System_front)
![GitHub top language](https://img.shields.io/github/languages/top/PyCmgMagic/Campus_Management_System_front)
![Repo size](https://img.shields.io/github/repo-size/PyCmgMagic/Campus_Management_System_front)
![License](https://img.shields.io/github/license/PyCmgMagic/Campus_Management_System_front)
![GitHub issues](https://img.shields.io/github/issues/PyCmgMagic/Campus_Management_System_front)
![GitHub issues closed](https://img.shields.io/github/issues-closed/PyCmgMagic/Campus_Management_System_front)

</div>

## 📌 项目仓库一览

<a href="https://github.com/Hanserprpr/Campus_Management_System">
  <img align="center" src="https://github-readme-stats-eight-theta.vercel.app/api/pin/?username=Hanserprpr&repo=Campus_Management_System" />
</a>

<a href="https://github.com/Tans-hub/Campus_Management_System_front">
  <img align="center" src="https://github-readme-stats-eight-theta.vercel.app/api/pin/?username=PycmgMagic&repo=Campus_Management_System_front" />
</a>


本项目是教务管理系统的 **JavaFX 前端客户端**，搭配 Spring Boot 后端服务使用，实现了包括学生信息展示、选课、成绩管理等功能，界面简洁，交互友好。旨在完成Java课设作业。

---

## 项目结构

```
.
├── pom.xml                # Maven 项目配置文件
├── README.md              # 项目说明文档
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── com/work/javafx/
│   │   │   │   ├── MainApplication.java  # 应用程序主入口
│   │   │   │   ├── controller/           # FXML 控制器
│   │   │   │   ├── model/                # 数据模型
│   │   │   │   ├── entity/               # 数据实体类
│   │   │   │   ├── util/                 # 工具类 
│   │   │   │   └── DataResponse/         # API 响应封装(基本弃用)
│   │   │   └── module-info.java      # Java 模块描述文件
│   │   └── resources/
│   │       ├── com/work/javafx/
│   │       │   ├── Login.fxml
│   │       │   ├── admin/                # 管理员相关 FXML 和资源
│   │       │   │   └── AdminBaseView.fxml
│   │       │   ├── student/              # 学生相关 FXML 和资源
│   │       │   │   └── StudentBaseView.fxml
│   │       │   ├── teacher/              # 教师相关 FXML 和资源
│   │       │   │   └── TeacherBaseView.fxml
│   │       │   ├── css/                  # CSS 样式文件
│   │       │   └── images/               # 图片资源
│   │       ├
│   │       │   
│   │       ├── application.properties  # 应用程序配置文件 (服务器URL等)
│   │       └── version.properties      # 版本信息
├── .gitignore             # Git 忽略文件配置
└── target/                # Maven 构建输出目录
```

<br/>

## ✨ 已实现功能（配合后端）

- 🔐 登录
- **用户认证**:
- **登录界面** (`Login.fxml`)
- 基于角色的访问控制 (管理员, 教师, 学生)
- Token 自动刷新
- **管理员模块** (`admin/AdminBaseView.fxml`):
- **教师模块** (`teacher/TeacherBaseView.fxml`):
- **学生模块** (`student/StudentBaseView.fxml`):
- **通用功能**:
    - 自动更新检查 (`AutoUpdater`)
    - Excel 数据导入/导出 (通过 Apache POI)
    - 网络请求工具类 (`NetworkUtils`)
- 🧑‍🎓 学生信息查看与编辑
- 📚 选课与退选（含课程搜索、筛选）
- 📊 成绩查询与导出
- 📅 课表自动生成与展示
- 🔄 学籍状态查看（如休学/复学）

---

## 🧰 技术栈

- Java 21
- JavaFX 21
- Maven

## 💻 项目运行说明

### ✅ 启动准备

1. 请确保后端项目已启动并可访问（默认接口地址需一致）。
2. 配置 `/src/main/resources/config.properties`以指向正确的后端 API 地址。

### ▶️ 配置设置

1. 在 `src/main/resources` 目录下确保 `application.properties` 文件存在。
2. 编辑 `application.properties` 设置实际服务器 URL：
   ```properties
   server.url=**********************
   ```

   (请替换 `**********************` 为实际的后端服务地址)

## 🧪 注意事项

- 确保已正确配置 `application.properties` 中的 `server.url`。

- 运行前请确保已安装 JDK 21 或更高版本。

- 若遇到网络异常，请检查后端地址是否正确，或后端是否正常运行。

---

## 🔗 后端与接口文档

- 后端仓库地址：[Hanserprpr/Campus\_Management\_System](https://github.com/Hanserprpr/Campus_Management_System)
- 接口文档平台：[Apifox 在线文档](https://cmsdoc.orbithy.com)

---

## 🧑‍💻 开发者

[![contributors](https://contrib.rocks/image?repo=PyCmgMagic/Campus_Management_System_front)](https://github.com/PyCmgMagic/Campus_Management_System_front/graphs/contributors)

<img src="http://github-profile-summary-cards.vercel.app/api/cards/profile-details?username=PyCmgMagic&theme=default" />
<img src="http://github-profile-summary-cards.vercel.app/api/cards/profile-details?username=03ln3&theme=default" />
<img src="http://github-profile-summary-cards.vercel.app/api/cards/profile-details?username=x1x-1&theme=default" />
<img src="http://github-profile-summary-cards.vercel.app/api/cards/profile-details?username=Hanserprpr&theme=default" />
---

## 📜 项目声明

本前端项目仅用于学习用途与完成课设作业。
