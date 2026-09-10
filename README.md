# UniHalo v3.x 配置插件

> 为免费开源的 `uni-halo v3.x` 微信小程序提供配套的配置插件。

* 官方文档：https://uni-halo.925i.cn/
* 作者主页：https://www.xiaoxiaomo.cn/
* 作者博客：https://blog.xiaoxiaomo.cn/
* 源码仓库：https://github.com/ialley-workshop-open/uni-halo
* 插件源码：https://github.com/ialley-workshop-open/plugin-uni-halo
* 插件市场：https://www.halo.run/store/apps/app-ryemX


### 支持我

如果您觉得这个项目对您有帮助，可以帮作者买杯饮料鼓励鼓励，同时为了项目能够持续发展，可以根据您的喜好支持一下本项目哦，非常感谢您的支持，作者也会更有动力持续维护和更新新的功能哦~

|                                                 支付宝                                                 |                                                微信                                                 |                                                QQ                                                 |
|:---------------------------------------------------------------------------------------------------:|:-------------------------------------------------------------------------------------------------:|:-------------------------------------------------------------------------------------------------:|
| ![支付宝赞助](https://gcore.jsdelivr.net/gh/uni-halo/uni-halo-static-resources/author/ZFBRewardCode.png) | ![微信赞助](https://gcore.jsdelivr.net/gh/uni-halo/uni-halo-static-resources/author/WXRewardCode.png) | ![QQ赞助](https://gcore.jsdelivr.net/gh/uni-halo/uni-halo-static-resources/author/QQRewardCode.png) |


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

### 2、页面截图

|首页|分类|我的|
|:--:|:--:|:--:|
|![首页](https://gcore.jsdelivr.net/gh/uni-halo/uni-halo-static-resources/screenshots/v2/首页.png)|![分类](https://gcore.jsdelivr.net/gh/uni-halo/uni-halo-static-resources/screenshots/v2/分类.jpg)|![我的](https://gcore.jsdelivr.net/gh/uni-halo/uni-halo-static-resources/screenshots/v2/关于.jpg)|


### 恋爱日记

- 在 uni-halo 中，我们为您准备了一个恋爱日记的模块，您可以在其中记录您的恋爱故事，分享给您的朋友们，请扫示例小程序，在线体验恋爱日记的功能。

![恋爱日记](https://gcore.jsdelivr.net/gh/uni-halo/uni-halo-static-resources/screenshots/v2/恋爱日记.jpg)

<br/>

## 二、uni-halo 配置插件

###  1、关于插件
该插件仅为 `uni-halo v3.x` 提供配套的配置，目前支持以下配置：

- 基本配置：文章详情版权、评论开关、页面显示、免责声明
- 应用配置：应用信息、启动页面配置
- 页面配置：特定页面信息和展示内容配置
- 我的配置：配置博主信息、社交信息
- 图片配置：配置默认的图片地址
- 恋爱配置：恋人信息、恋爱清单、恋爱相册、我们的故事
- 插件配置：一些独立支持的插件配置
 
### 2、使用方式

- 1、下载 `uni-halo v3.x` 小程序源码，参考：https://uni-halo.925i.cn/payload/introduction.html 部署指南将项目在本地运行起来。
- 2、在 Halo 插件市场搜索 `UniHalo 配置` 插件下载安装，或者通过 `github` 仓库 [点这里](https://github.com/ialley-workshop-open/plugin-uni-halo/releases) 找到发布包下载安装。
- 3、安装完成并且启动插件，进入插件配置页面，配置相关参数即可。


---

## 开发环境

- Java 21+
- Node.js ^20.19.0 or >=22.12.0
- pnpm

## 开发

```bash
# 启用插件
./gradlew haloServer
# 开发前端
cd ui
pnpm install
pnpm dev
```

## 构建

```bash
./gradlew build
```

构建完成后，可以在 `build/libs` 目录找到插件 jar 文件。

## 许可证

[GPL-3.0](./LICENSE) © 小莫唐尼
