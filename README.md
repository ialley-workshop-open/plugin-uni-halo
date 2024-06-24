# UniHalo 配置插件
 
### 1.1 插件功能
为 [uni-halo v2.0-beta](https://github.com/ialley-workshop-open/uni-halo/tree/v2.0-beta) 小程序提供动态配置，支持以下配置：

- 基本配置：文章详情版权、评论开关、页面显示、免责声明
- 启动配置：配置启动页面的信息
- 我的配置：配置博主信息、社交信息
- 图片配置：配置默认的图片地址
- 恋爱配置：恋人信息、恋爱清单、恋爱相册、我们的故事
- 插件配置：一些独立支持的插件配置
- 分享配置：页面分享配置
- 广告配置：广告ID相关配置

### 1.2 插件预览
![预览](https://img.925i.cn/file/82dd57626e36b3dc3d86e.png) 

---

## 开发环境

插件开发的详细文档请查阅：<https://docs.halo.run/developer-guide/plugin/introduction>

所需环境：

1. Java 17
2. Node 18
3. pnpm 8
4. Docker (可选)

克隆项目：

```bash
git clone git@github.com:halo-sigs/plugin-starter.git

# 或者当你 fork 之后

git clone git@github.com:{your_github_id}/plugin-starter.git
```

```bash
cd path/to/plugin-starter
```

### 运行方式 1（推荐）

> 此方式需要本地安装 Docker

```bash
# macOS / Linux
./gradlew pnpmInstall

# Windows
./gradlew.bat pnpmInstall
```

```bash
# macOS / Linux
./gradlew haloServer

# Windows
./gradlew.bat haloServer
```

执行此命令后，会自动创建一个 Halo 的 Docker 容器并加载当前的插件，更多文档可查阅：<https://docs.halo.run/developer-guide/plugin/basics/devtools>

### 运行方式 2

> 此方式需要使用源码运行 Halo

编译插件：

```bash
# macOS / Linux
./gradlew build

# Windows
./gradlew.bat build
```

修改 Halo 配置文件：

```yaml
halo:
  plugin:
    runtime-mode: development
    fixedPluginPath:
      - "/path/to/plugin-starter"
```

最后重启 Halo 项目即可。


## 参考项目
[plugin-moments](https://github.com/halo-sigs/plugin-moments)
[halo-dev图标库](https://icon-sets.iconify.design/)
