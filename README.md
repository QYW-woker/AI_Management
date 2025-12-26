# AI智能生活管理APP

一款集成目标管理、财务统计、日常记账、待办事项、日记、时间统计、习惯打卡于一体的AI智能生活管理应用。

## 功能特点

- **全本地存储**: 所有用户数据存储在本地SQLite数据库，保护隐私
- **AI智能化**: 调用AI大模型实现语音识别、智能分类、数据分析
- **自定义字段**: 财务模块支持用户自由添加统计类别
- **多维数据可视化**: 丰富的图表展示和筛选功能
- **响应式UI**: 适配手机、平板等不同屏幕尺寸

## 模块清单

| 模块 | 说明 |
|------|------|
| 目标管理 | 年/季/月目标、拆解、进度追踪 |
| 月度收支 | 收入/支出记录、自定义字段 |
| 月度资产 | 资产配置、净值追踪 |
| 月度开销 | 固定支出、生活成本 |
| 日常记账 | 日常消费流水 |
| 待办记事 | 任务管理、提醒 |
| 日记 | 生活记录、情绪分析 |
| 时间统计 | 时间追踪、番茄钟 |
| 习惯打卡 | 习惯养成、连续统计 |
| 存钱计划 | 存钱目标、进度追踪 |

## 技术栈

- **UI框架**: Jetpack Compose + Material 3
- **架构模式**: MVVM + Clean Architecture
- **依赖注入**: Hilt
- **数据库**: Room
- **网络请求**: Retrofit + OkHttp
- **异步处理**: Kotlin Coroutines + Flow

## 构建要求

- Android Studio Hedgehog | 2023.1.1 或更高版本
- JDK 17
- Android SDK 34
- Gradle 8.5

## 构建步骤

```bash
# 克隆仓库
git clone https://github.com/QYW-woker/AI_Management.git

# 进入项目目录
cd AI_Management

# 构建Debug APK
./gradlew assembleDebug

# 构建Release APK
./gradlew assembleRelease
```

## 项目结构

```
com.lifemanager.app/
├── LifeManagerApplication.kt     # Application入口
├── MainActivity.kt               # 主Activity
├── core/                         # 核心模块
│   ├── database/                 # Room数据库
│   ├── datastore/                # DataStore偏好设置
│   ├── network/                  # AI API网络层
│   ├── di/                       # Hilt依赖注入
│   └── util/                     # 工具类
├── domain/                       # 领域层
├── data/                         # 数据层
├── feature/                      # 功能模块
│   ├── home/                     # 首页
│   ├── goal/                     # 目标管理
│   ├── finance/                  # 财务模块
│   ├── todo/                     # 待办记事
│   ├── diary/                    # 日记
│   ├── timetrack/                # 时间统计
│   └── habit/                    # 习惯打卡
└── ui/                           # 通用UI组件
    ├── component/                # 可复用组件
    ├── navigation/               # 导航
    └── theme/                    # 主题
```

## 许可证

MIT License

## 作者

QYW-woker
