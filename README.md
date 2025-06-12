# UniHalo 配置插件

> 为免费开源的 `uni-halo v2.0` 微信小程序提供配套的配置插件。

* 官方文档：https://uni-halo.925i.cn/
* 作者主页：https://www.xiaoxiaomo.cn/
* 作者博客：https://blog.xiaoxiaomo.cn/
* 源码仓库：https://github.com/ialley-workshop-open/uni-halo
* 插件源码：https://github.com/ialley-workshop-open/plugin-uni-halo
* 插件市场：https://www.halo.run/store/apps/app-ryemX


### 支持我

如果您觉得这个项目对您有帮助，可以帮作者买杯饮料鼓励鼓励，同时为了项目能够持续发展，可以根据您的喜好支持一下本项目哦，非常感谢您的支持，作者也会更有动力持续维护和更新新的功能哦~

|                支付宝												                 |                微信												                 |                QQ												                 |
|:----------------------------------------------:|:---------------------------------------------:|:---------------------------------------------:|
| ![支付宝赞赏](https://blog.xiaoxiaomo.cn/upload/zf_zfb_skm.png) | ![微信赞赏](https://blog.xiaoxiaomo.cn/upload/zf_wx_zsm.png)	 | ![QQ赞赏](https://blog.xiaoxiaomo.cn/upload/zf_qq_skm.png)	 |


### 交流群

![QQ交流群](https://blog.xiaoxiaomo.cn/upload/qun.png)


## 一、uni-halo 小程序

### 1、应用简介

基于 Halo 2.x 提供的 API 接口，为微信小程序提供的一套开源的博客应用。

- 完全免费开源，包括程序源码、插件源码
- 页面支持插件配置
- 使用最新流行的技术栈 
- 支持特色功能，恋爱日记
- 理论支持编译为 小程序（推荐）、APP

### 2、在线预览

微信内容扫描下方二维码，即可在线预览小程序。

![在线体验](https://blog.xiaoxiaomo.cn/upload/xiaochengxu.gif)


### 3、页面截图

以下为部分页面截图，更多功能页面请扫码预览体验。

|首页|分类|图库|
|:--:|:--:|:--:|
|![首页](https://blog.xiaoxiaomo.cn/upload/39789CF4434C9CD6A6289D7209AF6EEF.jpg)|![分类](https://blog.xiaoxiaomo.cn/upload/19CB6B66F40200045B6F572A9C28C5E8.jpg)|![图库](https://blog.xiaoxiaomo.cn/upload/464F22FDB216CE802653A5F03BE34351.jpg)|

|瞬间|我的|
|:--:|:--:|
|![瞬间](https://blog.xiaoxiaomo.cn/upload/41EE8ADBFAE709A483A6E5F814C6A6E4.jpg)|![我的](https://blog.xiaoxiaomo.cn/upload/9AEFE8DA4671A3C7F20F76FF3F9D15C9.jpg)|


##### 📱 恋爱日记

说明：以下仅为部分截图

|主页|恋爱清单|
|:--:|:--:|
|![主页](https://uni-halo.925i.cn/assets/love_001.6bf8b4e9.jpg)|![恋爱清单](https://uni-halo.925i.cn/assets/love_002.a08bd8d6.jpg)|
<br/>

## 二、uni-halo 配置插件

###  1、关于插件
该插件仅为 `uni-halo v2.0` 提供配套的配置，目前支持以下配置：

- 基本配置：文章详情版权、评论开关、页面显示、免责声明
- 应用配置：应用信息、启动页面配置
- 页面配置：特定页面信息和展示内容配置
- 我的配置：配置博主信息、社交信息
- 图片配置：配置默认的图片地址
- 恋爱配置：恋人信息、恋爱清单、恋爱相册、我们的故事
- 插件配置：一些独立支持的插件配置

### 2、插件预览

#### 2.1 插件预览

![预览](https://blog.xiaoxiaomo.cn/upload/uni-halo-p-1.png)

#### 2.2 基本配置

![基本配置](https://blog.xiaoxiaomo.cn/upload/uni-halo-p-2.png)

#### 2.3 应用配置

![应用配置](https://blog.xiaoxiaomo.cn/upload/uni-halo-p-3.png)

#### 2.4 页面配置

![页面配置](https://blog.xiaoxiaomo.cn/upload/uni-halo-p-4.png)

#### 2.5 我的配置

![我的配置](https://blog.xiaoxiaomo.cn/upload/uni-halo-p-5.png)

#### 2.6 图片配置

![图片配置](https://blog.xiaoxiaomo.cn/upload/uni-halo-p-6.png)

#### 2.7 恋爱配置

![恋爱配置](https://blog.xiaoxiaomo.cn/upload/uni-halo-p-7.png)

#### 2.8 插件配置

![插件配置](https://blog.xiaoxiaomo.cn/upload/uni-halo-p-8.png)

### 3、使用方式

- 1、下载 `uni-halo v2.0` 小程序源码，参考：https://uni-halo.925i.cn/payload/introduction.html 部署指南将项目在本地运行起来。
- 2、在 Halo 插件市场搜索 `UniHalo 配置` 插件下载安装，或者通过 `github` 仓库 [点这里](https://github.com/ialley-workshop-open/plugin-uni-halo/releases) 找到发布包下载安装。
- 3、安装完成并且启动插件，进入插件配置页面，配置相关参数即可。


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
