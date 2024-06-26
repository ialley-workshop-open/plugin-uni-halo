# UniHalo 配置插件

> 为免费开源的 `uni-halo v2.0` 微信小程序提供配套的配置插件， 

* 小程序文档：https://uni-halo.925i.cn/
* 小程序仓库：https://github.com/ialley-workshop-open/uni-halo
* 插件仓库：https://github.com/ialley-workshop-open/plugin-uni-halo

### 😘 1、功能说明
该插件仅为 `uni-halo v2.0` 提供配套的配置，目前支持以下配置：

- 基本配置：文章详情版权、评论开关、页面显示、免责声明
- 应用配置：应用信息、启动页面配置
- 我的配置：配置博主信息、社交信息
- 图片配置：配置默认的图片地址
- 恋爱配置：恋人信息、恋爱清单、恋爱相册、我们的故事
- 插件配置：一些独立支持的插件配置
- 分享配置：页面分享配置
- 广告配置：广告ID相关配置

### 😘 2、配置预览

![预览](https://img.925i.cn/file/0a550a70a42b62a92e042.png) 

![基本配置](https://img.925i.cn/file/a2753f4614e257bb4ceb8.png)

![应用配置](https://img.925i.cn/file/ebfa18e67ebd105bf250a.png)

![我的配置](https://img.925i.cn/file/f2baabe2590db060e69e0.png)

![图片配置](https://img.925i.cn/file/39977d2f8e213be449990.png)

![恋爱配置](https://img.925i.cn/file/4923977f46d484a2fee71.png)

![插件配置](https://img.925i.cn/file/65903d5ae26f09410ddd3.png)

![分享配置](https://img.925i.cn/file/c10ae8a6fbb7bb65e3521.png)

![广告配置](https://img.925i.cn/file/0acd12f2b430d3026540a.png)


### 😘 3、使用方式

- 1、下载 `uni-halo v2.0` 小程序源码，参考：https://uni-halo.925i.cn/payload/introduction.html 部署指南将项目在本地运行起来。
- 2、在 Halo 插件市场搜索 `UniHalo 配置` 插件下载安装，或者通过 `github` 仓库 [点这里](https://github.com/ialley-workshop-open/plugin-uni-halo/releases) 找到发布包下载安装。
- 3、安装完成并且启动插件，进入插件配置页面，配置相关参数即可。


### 😘 4、赞赏和支持

如果您觉得这个项目对您有帮助，可以帮作者买杯饮料鼓励鼓励，同时为了项目能够持续发展，可以根据您的喜好支持一下本项目哦，非常感谢您的支持，作者也会更有动力持续维护和更新新的功能哦~

|                支付宝												                 |                微信												                 |                QQ												                 |
|:----------------------------------------------:|:---------------------------------------------:|:---------------------------------------------:|
| ![支付宝赞赏](https://b.925i.cn/skm/zf_zfb_skm.png) | ![微信赞赏](https://b.925i.cn/skm/zf_wx_zsm.png)	 | ![QQ赞赏](https://b.925i.cn/skm/zf_qq_skm.png)	 |



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
