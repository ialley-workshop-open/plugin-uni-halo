<script setup lang="ts">
import confetti from 'canvas-confetti'
import { onMounted, ref } from 'vue'
import RiSettingsLine from '~icons/ri/settings-line'
import RiSmartphoneLine from '~icons/ri/smartphone-line'
import RiLayoutLine from '~icons/ri/layout-line'
import RiUserLine from '~icons/ri/user-line'
import RiImageLine from '~icons/ri/image-line'
import RiHeartLine from '~icons/ri/heart-line'
import RiArrowRightSLine from '~icons/ri/arrow-right-s-line'
import RiGithubLine from '~icons/ri/github-line'
import RiBookOpenLine from '~icons/ri/book-open-line'
import RiFileCodeLine from '~icons/ri/file-code-line'
import RiMoneyCnyCircleLine from '~icons/ri/money-cny-circle-line'
import RiWechatPayLine from '~icons/ri/wechat-pay-line'
import RiAlipayLine from '~icons/ri/alipay-line'
import RiQqLine from '~icons/ri/qq-line'
import RiStoreLine from '~icons/ri/store-line'
import RiCodeBoxLine from '~icons/ri/code-box-line'
import RiSparklingLine from '~icons/ri/sparkling-line'
import RiMailLine from '~icons/ri/mail-line'
import RiLinksLine from '~icons/ri/links-line'
import RiMessage2Line from '~icons/ri/message-2-line'
import RiGlobalLine from '~icons/ri/global-line'
import RiMapPinLine from '~icons/ri/map-pin-line'
import RiLightbulbLine from '~icons/ri/lightbulb-line'
import RiHomeLine from '~icons/ri/home-line'

import mpPng from '@/assets/mp.png'
import wxqunPng from '@/assets/wxqun.png'
import qqqunPng from '@/assets/qqqun.png'
import wxPng from '@/assets/wx.png'
import zfbPng from '@/assets/zfb.png'
import qqPng from '@/assets/qq.png'
import logoPng from '@/assets/logo.png'

const isLoaded = ref(false)

onMounted(() => {
  setTimeout(() => {
    isLoaded.value = true
  }, 100)

  confetti({
    particleCount: 150,
    spread: 100,
    origin: { y: 0.4, x: 0.5 },
    colors: ['#B9E424', '#5BA4F5', '#2D7AD6'],
  })
})

const features = [
  {
    icon: RiSettingsLine,
    title: '基本配置',
    description: '文章版权、评论开关、页面显示、免责声明等基础设置',
    color: '#368FEF',
  },
  {
    icon: RiSmartphoneLine,
    title: '应用配置',
    description: '应用信息、启动页面、小程序专属配置',
    color: '#8B5CF6',
  },
  {
    icon: RiLayoutLine,
    title: '页面配置',
    description: '特定页面信息和展示内容的个性化配置',
    color: '#06B6D4',
  },
  {
    icon: RiUserLine,
    title: '我的配置',
    description: '博主信息、社交链接、个人主页定制',
    color: '#10B981',
  },
  {
    icon: RiImageLine,
    title: '图片配置',
    description: '默认图片地址、封面图、Logo 设置',
    color: '#F59E0B',
  },
  {
    icon: RiHeartLine,
    title: '恋爱配置',
    description: '恋人信息、恋爱清单、恋爱相册、恋爱故事',
    color: '#EC4899',
  },
]

const steps = [
  {
    title: '下载源码',
    desc: '从 GitHub 下载 UniHalo v3.x 小程序源码到本地，解压后导入微信开发者工具即可运行',
  },
  {
    title: '安装插件',
    desc: '在 Halo 后台插件市场搜索「UniHalo 配置」，一键安装并启用插件',
  },
  {
    title: '配置参数',
    desc: '进入插件设置页面，根据实际需求配置应用信息、页面内容、博主资料等参数',
  },
  {
    title: '启动运行',
    desc: '启动微信小程序预览配置效果，确认无误后发布上线',
  },
]

const contacts = [
  { icon: RiGithubLine, label: 'GitHub 主页', value: 'github.com/okay-xiaomo', href: 'https://github.com/okay-xiaomo' },
  { icon: RiBookOpenLine, label: '个人博客', value: 'blog.xiaoxiaomo.cn', href: 'https://blog.xiaoxiaomo.cn/' },
  { icon: RiGlobalLine, label: '个人主页', value: 'www.xiaoxiaomo.cn', href: 'https://www.xiaoxiaomo.cn/' },
  { icon: RiMailLine, label: '联系邮箱', value: '1431128779@qq.com', href: 'mailto:1431128779@qq.com' },
  { icon: RiMessage2Line, label: 'QQ 交流群', value: '加入开发者社群', href: 'https://qm.qq.com/q/Kh9QPnL6gO' },
  { icon: RiMapPinLine, label: '所在地', value: '中国·深圳', href: 'javascript:void(0)' },
]
</script>

<template>
  <div
    class="min-h-screen bg-slate-50 transition-opacity duration-500"
    :class="isLoaded ? 'opacity-100' : 'opacity-0'"
  >
    <!-- Hero Section -->
    <section class="relative h-screen flex items-center justify-center overflow-hidden">
      <!-- Background Decorations - 仅使用主题色 #B9E424 淡化 -->
      <div class="absolute inset-0 overflow-hidden pointer-events-none">
        <div
          class="absolute -top-40 -right-40 w-[500px] h-[500px] rounded-full blur-3xl animate-float"
          style="background: radial-gradient(circle, rgb(185 228 36 / 0.33) 0%, rgba(54,143,239,0) 70%)"
        ></div>
        <div
          class="absolute -bottom-40 -left-40 w-[500px] h-[500px] rounded-full blur-3xl animate-float-delayed"
          style="background: radial-gradient(circle, rgb(185 228 36 / 0.33) 0%, rgba(54,143,239,0) 70%)"
        ></div>
        <div
          class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[600px] h-[600px] rounded-full blur-3xl animate-pulse-slow"
          style="background: radial-gradient(circle, rgb(185 228 36 / 0.33) 0%, rgba(54,143,239,0) 70%)"
        ></div>
        <!-- Floating Shapes -->
        <div class="absolute top-20 left-[10%] w-3 h-3 rounded-full animate-bounce-slow" style="background: rgb(185 228 36 / 0.47)"></div>
        <div class="absolute top-[30%] right-[15%] w-4 h-4 rounded-lg rotate-45 animate-bounce-slow-delayed" style="background: rgb(185 228 36 / 0.33)"></div>
        <div class="absolute bottom-[25%] left-[20%] w-2.5 h-2.5 rounded-full animate-bounce-slow" style="background: rgb(185 228 36 / 0.47)"></div>
        <div class="absolute top-[40%] right-[25%] w-2 h-2 rounded-full animate-bounce-slow-delayed" style="background: rgb(185 228 36 / 0.33)"></div>
        <div class="absolute bottom-[15%] right-[10%] w-3.5 h-3.5 rounded-full animate-bounce-slow" style="background: rgb(185 228 36 / 0.33)"></div>
      </div>

      <div class="relative z-10 max-w-5xl mx-auto px-4 text-center">
        <img :src="logoPng" alt="UniHalo Logo" class="w-20 h-20 mx-auto mb-6 drop-shadow-lg" />

        <div class="inline-flex items-center gap-2 bg-white/80 backdrop-blur-sm border rounded-full px-4 py-2 mb-8 shadow-sm" style="border-color: rgb(185 228 36 / 0.47)">
          <RiSparklingLine class="w-4 h-4" style="color: #B9E424" />
          <span class="text-sm font-medium" style="color: #B9E424">v3.x AGPL3.0</span>
        </div>

        <h1 class="text-5xl sm:text-6xl lg:text-7xl font-extrabold mb-6">
          <span style="color: #B9E424">UniHalo</span>
          <span class="block text-3xl sm:text-4xl lg:text-5xl mt-3 font-bold text-slate-700">配置插件</span>
        </h1>

        <p class="text-lg sm:text-xl text-slate-500 max-w-2xl mx-auto mb-10 leading-relaxed">
          为免费开源的 UniHalo v3.x 微信小程序/App提供配套的配置插件
        </p>

        <div class="flex flex-wrap justify-center gap-4 mb-10">
          <a
            href="https://uni-halo.925i.cn/"
            class="inline-flex items-center gap-2 text-white font-semibold px-7 py-3.5 rounded-xl transition-all duration-200 hover:shadow-lg hover:-translate-y-0.5"
            style="background-color: #B9E424"
            target="_blank"
          >
            <RiBookOpenLine class="w-5 h-5" />
            <span>官方文档</span>
          </a>
          <a
            href="https://github.com/ialley-workshop-open/plugin-uni-halo"
            class="inline-flex items-center gap-2 bg-slate-800 hover:bg-slate-900 text-white font-semibold px-7 py-3.5 rounded-xl transition-all duration-200 hover:shadow-lg hover:-translate-y-0.5"
            target="_blank"
          >
            <RiGithubLine class="w-5 h-5" />
            <span>GitHub</span>
          </a>
          <a
            href="https://www.halo.run/store/apps/app-ryemX"
            class="inline-flex items-center gap-2 border-slate-300 bg-white hover:bg-slate-50 text-slate-700 font-semibold px-7 py-3.5 rounded-xl border  transition-all duration-200 hover:shadow-lg hover:-translate-y-0.5"
            target="_blank"
          >
            <RiStoreLine class="w-5 h-5" />
            <span>插件市场</span>
          </a>
          <a
            href="https://github.com/ialley-workshop-open/uni-halo/issues/23"
            class="inline-flex items-center gap-2 text-[#b9e424] border-[#b9e424]/50 bg-white hover:bg-slate-50 font-semibold px-7 py-3.5 rounded-xl border transition-all duration-200 hover:shadow-lg hover:-translate-y-0.5"
            target="_blank"
          >
            <RiLightbulbLine class="w-5 h-5" />
            <span>提交案例</span>
          </a>
        </div>

        <!-- 统计数据 -->
        <div class="flex justify-center gap-10 sm:gap-16 mb-10">
          <div class="text-center">
            <div class="text-3xl sm:text-4xl font-bold" style="color: #B9E424">6+</div>
            <div class="text-sm text-slate-500 mt-1">配置模块</div>
          </div>
          <div class="text-center">
            <div class="text-3xl sm:text-4xl font-bold" style="color: #B9E424">100%</div>
            <div class="text-sm text-slate-500 mt-1">开源免费</div>
          </div>
          <div class="text-center">
            <div class="text-3xl sm:text-4xl font-bold" style="color: #B9E424">v3.x</div>
            <div class="text-sm text-slate-500 mt-1">支持版本</div>
          </div>
        </div>

        <!-- 三个横向截图占位 -->
        <div class="grid grid-cols-3 gap-4 max-w-4xl mx-auto">
          <div class="rounded-2xl overflow-hidden bg-white shadow-sm border border-slate-100 flex items-center justify-center">
            <img :src="mpPng" alt="小程序二维码" class="w-full h-full object-contain p-3" />
          </div>
          <div class="rounded-2xl overflow-hidden bg-white shadow-sm border border-slate-100 flex items-center justify-center">
            <img :src="wxqunPng" alt="微信交流群" class="w-full h-full object-contain p-3" />
          </div>
          <div class="rounded-2xl overflow-hidden bg-white shadow-sm border border-slate-100 flex items-center justify-center">
            <img :src="qqqunPng" alt="QQ 交流群" class="w-full h-full object-contain p-3" />
          </div>
        </div>
      </div>

      <!-- 底部滚动提示 -->
      <div class="absolute bottom-8 left-1/2 -translate-x-1/2 flex flex-col items-center gap-2 animate-bounce-slow">
        <span class="text-xs text-slate-400">向下滚动</span>
        <div class="w-5 h-8 rounded-full border-2 border-slate-300 flex justify-center pt-1.5">
          <div class="w-1 h-2 rounded-full bg-slate-300"></div>
        </div>
      </div>
    </section>

    <!-- Sponsor Section -->
    <section class="py-16 sm:py-20 bg-white">
      <div class="max-w-5xl mx-auto px-4">
        <div class="text-center mb-12">
          <h2 class="text-3xl sm:text-4xl font-bold text-slate-800 mb-4">赞助支持</h2>
          <p class="text-lg text-slate-500">如果这个项目对你有帮助，可以请作者喝杯饮料</p>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-3 gap-6 max-w-5xl mx-auto">
          <div class="bg-slate-50 rounded-2xl p-6 text-center hover:shadow-md transition-all duration-300">
            <div
              class="bg-green-500 w-14 h-14 rounded-full flex items-center justify-center text-white mx-auto mb-4"
            >
              <RiWechatPayLine class="w-7 h-7" />
            </div>
            <h4 class="font-semibold text-slate-800 mb-3">微信赞助</h4>
            <div class="w-full rounded-xl mx-auto mb-3 overflow-hidden bg-white flex items-center justify-center">
              <img :src="wxPng" alt="微信赞赏码" class="w-full h-full object-contain" />
            </div>
            <p class="text-sm text-slate-500">扫码请作者喝咖啡</p>
          </div>

          <div class="bg-slate-50 rounded-2xl p-6 text-center hover:shadow-md transition-all duration-300">
            <div
              class="bg-blue-500 w-14 h-14 rounded-full flex items-center justify-center text-white mx-auto mb-4"
            >
              <RiAlipayLine class="w-7 h-7" />
            </div>
            <h4 class="font-semibold text-slate-800 mb-3">支付宝赞助</h4>
            <div class="w-full rounded-xl mx-auto mb-3 overflow-hidden bg-white flex items-center justify-center">
              <img :src="zfbPng" alt="支付宝赞赏码" class="w-full h-full object-contain" />
            </div>
            <p class="text-sm text-slate-500">扫码请作者喝奶茶</p>
          </div>

          <div class="bg-slate-50 rounded-2xl p-6 text-center hover:shadow-md transition-all duration-300">
            <div
              class="bg-yellow w-14 h-14 rounded-full flex items-center justify-center text-white mx-auto mb-4"
            >
              <RiQqLine class="w-7 h-7" />
            </div>
            <h4 class="font-semibold text-slate-800 mb-3">QQ 赞助</h4>
            <div class="w-full rounded-xl mx-auto mb-3 overflow-hidden bg-white flex items-center justify-center">
              <img :src="qqPng" alt="QQ 赞赏码" class="w-full h-full object-contain" />
            </div>
            <p class="text-sm text-slate-500">扫码请作者吃零食</p>
          </div>
        </div>

        <div class="flex items-center justify-center gap-2 mt-8 text-slate-400 text-sm">
          <RiMoneyCnyCircleLine class="w-4 h-4" />
          <span>您的支持是作者持续维护的动力</span>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section class="py-16 sm:py-20">
      <div class="max-w-5xl mx-auto px-4">
        <div class="text-center mb-12">
          <h2 class="text-3xl sm:text-4xl font-bold text-slate-800 mb-4">功能模块</h2>
          <p class="text-lg text-slate-500">全方位满足你的小程序配置需求</p>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
          <div
            v-for="feature in features"
            :key="feature.title"
            class="group bg-white rounded-2xl p-6 shadow-sm border border-slate-100 hover:shadow-md transition-all duration-300 cursor-pointer"
          >
            <div
              class="w-11 h-11 rounded-xl flex items-center justify-center text-white mb-4 group-hover:scale-110 transition-transform duration-300"
              :style="{ background: `linear-gradient(135deg, ${feature.color} 0%, ${feature.color}cc 100%)` }"
            >
              <component :is="feature.icon" class="w-5.5 h-5.5" />
            </div>
            <h3 class="text-lg font-semibold text-slate-800 mb-2">{{ feature.title }}</h3>
            <p class="text-slate-500 text-sm leading-relaxed">{{ feature.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Steps Section - 左右布局 -->
    <section class="py-16 sm:py-20 bg-white">
      <div class="max-w-4xl mx-auto px-4">
        <div class="text-center mb-12">
          <h2 class="text-3xl sm:text-4xl font-bold text-slate-800 mb-4">使用流程</h2>
          <p class="text-lg text-slate-500">从下载到上线，轻松四步</p>
        </div>

        <div class="space-y-4">
          <div
            v-for="(step, index) in steps"
            :key="index"
            class="flex items-start gap-5 bg-slate-50 rounded-2xl p-6 hover:bg-white hover:shadow-md transition-all duration-300"
          >
            <div
              class="flex-shrink-0 w-12 h-12 rounded-xl flex items-center justify-center text-white font-bold text-lg"
              :style="{ background: index === 0 ? 'linear-gradient(135deg, #B9E424 0%, #2D7AD6 100%)' : index === 1 ? 'linear-gradient(135deg, #8B5CF6 0%, #7C3AED 100%)' : index === 2 ? 'linear-gradient(135deg, #10B981 0%, #059669 100%)' : 'linear-gradient(135deg, #F59E0B 0%, #D97706 100%)' }"
            >
              {{ index + 1 }}
            </div>
            <div class="flex-1 min-w-0">
              <h3 class="text-lg font-semibold text-slate-800 mb-1.5">{{ step.title }}</h3>
              <p class="text-slate-500 text-sm leading-relaxed">{{ step.desc }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Links Section -->
    <section class="py-16 sm:py-20">
      <div class="max-w-5xl mx-auto px-4">
        <div class="text-center mb-12">
          <h2 class="text-3xl sm:text-4xl font-bold text-slate-800 mb-4">相关链接</h2>
          <p class="text-lg text-slate-500">官方资源与社区支持</p>
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          <a
            href="https://uni-halo.925i.cn/"
            class="flex items-center gap-4 bg-white rounded-xl p-4 border border-slate-100 hover:shadow-md transition-all duration-200"
            target="_blank"
          >
            <div
              class="w-10 h-10 rounded-lg flex items-center justify-center text-white flex-shrink-0"
              style="background: linear-gradient(135deg, #B9E424 0%, #2D7AD6 100%)"
            >
              <RiBookOpenLine class="w-5 h-5" />
            </div>
            <div class="flex-1 min-w-0">
              <div class="font-semibold text-slate-800 text-sm">官方文档</div>
              <div class="text-xs text-slate-400 truncate">UniHalo 使用指南</div>
            </div>
            <RiArrowRightSLine class="w-4 h-4 text-slate-300 flex-shrink-0" />
          </a>

          <a
            href="https://github.com/ialley-workshop-open/plugin-uni-halo"
            class="flex items-center gap-4 bg-white rounded-xl p-4 border border-slate-100 hover:shadow-md transition-all duration-200"
            target="_blank"
          >
            <div class="w-10 h-10 rounded-lg flex items-center justify-center text-white flex-shrink-0" style="background: linear-gradient(135deg, #8B5CF6 0%, #7C3AED 100%)">
              <RiGithubLine class="w-5 h-5" />
            </div>
            <div class="flex-1 min-w-0">
              <div class="font-semibold text-slate-800 text-sm">插件源码</div>
              <div class="text-xs text-slate-400 truncate">GitHub 仓库，欢迎 Star</div>
            </div>
            <RiArrowRightSLine class="w-4 h-4 text-slate-300 flex-shrink-0" />
          </a>

          <a
            href="https://github.com/ialley-workshop-open/uni-halo"
            class="flex items-center gap-4 bg-white rounded-xl p-4 border border-slate-100 hover:shadow-md transition-all duration-200"
            target="_blank"
          >
            <div
              class="w-10 h-10 rounded-lg flex items-center justify-center text-white flex-shrink-0"
              style="background: linear-gradient(135deg, #06B6D4 0%, #0891B2 100%)"
            >
              <RiCodeBoxLine class="w-5 h-5" />
            </div>
            <div class="flex-1 min-w-0">
              <div class="font-semibold text-slate-800 text-sm">小程序源码</div>
              <div class="text-xs text-slate-400 truncate">UniHalo v3.x 开源代码</div>
            </div>
            <RiArrowRightSLine class="w-4 h-4 text-slate-300 flex-shrink-0" />
          </a>

          <a
            href="https://www.halo.run/store/apps/app-ryemX"
            class="flex items-center gap-4 bg-white rounded-xl p-4 border border-slate-100 hover:shadow-md transition-all duration-200"
            target="_blank"
          >
            <div
              class="w-10 h-10 rounded-lg flex items-center justify-center text-white flex-shrink-0"
              style="background: linear-gradient(135deg, #10B981 0%, #059669 100%)"
            >
              <RiStoreLine class="w-5 h-5" />
            </div>
            <div class="flex-1 min-w-0">
              <div class="font-semibold text-slate-800 text-sm">Halo 插件市场</div>
              <div class="text-xs text-slate-400 truncate">在线安装 UniHalo 配置插件</div>
            </div>
            <RiArrowRightSLine class="w-4 h-4 text-slate-300 flex-shrink-0" />
          </a>

          <a
            href="https://docs.halo.run/developer-guide/plugin/introduction"
            class="flex items-center gap-4 bg-white rounded-xl p-4 border border-slate-100 hover:shadow-md transition-all duration-200"
            target="_blank"
          >
            <div class="w-10 h-10 rounded-lg flex items-center justify-center text-white flex-shrink-0" style="background: linear-gradient(135deg, #F59E0B 0%, #D97706 100%)">
              <RiFileCodeLine class="w-5 h-5" />
            </div>
            <div class="flex-1 min-w-0">
              <div class="font-semibold text-slate-800 text-sm">Halo 开发者文档</div>
              <div class="text-xs text-slate-400 truncate">插件开发指南与 API 参考</div>
            </div>
            <RiArrowRightSLine class="w-4 h-4 text-slate-300 flex-shrink-0" />
          </a>

          <a
            href="https://blog.xiaoxiaomo.cn/"
            class="flex items-center gap-4 bg-white rounded-xl p-4 border border-slate-100 hover:shadow-md transition-all duration-200"
            target="_blank"
          >
            <div
              class="w-10 h-10 rounded-lg flex items-center justify-center text-white flex-shrink-0"
              style="background: linear-gradient(135deg, #EC4899 0%, #DB2777 100%)"
            >
              <RiLinksLine class="w-5 h-5" />
            </div>
            <div class="flex-1 min-w-0">
              <div class="font-semibold text-slate-800 text-sm">作者博客</div>
              <div class="text-xs text-slate-400 truncate">更多技术分享与经验</div>
            </div>
            <RiArrowRightSLine class="w-4 h-4 text-slate-300 flex-shrink-0" />
          </a>
        </div>
      </div>
    </section>

    <!-- Author Section -->
    <section class="py-16 sm:py-20 bg-white">
      <div class="max-w-5xl mx-auto px-4">
        <div class="text-center mb-12">
          <h2 class="text-3xl sm:text-4xl font-bold text-slate-800 mb-4">关于作者</h2>
          <p class="text-lg text-slate-500">项目的开发者与维护者</p>
        </div>

        <div class="bg-white rounded-3xl p-8 shadow-sm sm:p-10 border border-slate-100">
          <div class="flex flex-col sm:flex-row items-center gap-8">
            <!-- 头像 -->
            <div class="flex-shrink-0">
              <img
                src="https://www.xiaoxiaomo.cn/logo.jpg"
                alt="小莫唐尼"
                class="w-28 h-28 rounded-full object-cover"
              />
            </div>

            <!-- 信息 -->
            <div class="flex-1 text-center sm:text-left">
              <h3 class="text-2xl font-bold text-slate-800 mb-2">小莫唐尼</h3>
              <p class="text-slate-500 mb-1">全栈开发者 / 开源爱好者</p>
              <p class="text-sm text-slate-400">一个爱凑热闹，喜欢捣鼓前端的程序员</p>
            </div>

            <!-- 按钮 -->
            <div class="flex gap-3">
              <a
                href="https://www.xiaoxiaomo.cn/"
                class="inline-flex items-center gap-1.5 text-[#B9E424] bg-slate-100 text-sm font-medium px-3 py-1.5 rounded-lg transition-colors"
                target="_blank"
              >
                <RiHomeLine class="w-3.5 h-3.5" />
                <span>主页</span>
              </a>
              <a
                href="https://blog.xiaoxiaomo.cn/"
                class="inline-flex items-center gap-1.5 text-[#B9E424] bg-slate-100 text-sm font-medium px-3 py-1.5 rounded-lg transition-colors"
                target="_blank"
              >
                <RiBookOpenLine class="w-3.5 h-3.5" />
                <span>博客</span>
              </a>
              <a
                href="https://github.com/okay-xiaomo"
                class="inline-flex items-center gap-1.5 text-sm font-medium px-3 py-1.5 rounded-lg bg-slate-100 text-slate-600 hover:bg-slate-200 transition-colors"
                target="_blank"
              >
                <RiGithubLine class="w-3.5 h-3.5" />
                <span>GitHub</span>
              </a>
              <a
                href="mailto:1431128779@qq.com"
                class="inline-flex items-center gap-1.5 text-sm font-medium px-3 py-1.5 rounded-lg bg-slate-100 text-slate-600 hover:bg-slate-200 transition-colors"
              >
                <RiMailLine class="w-3.5 h-3.5" />
                <span>邮箱</span>
              </a>
            </div>
          </div>

          <!-- 联系方式网格 -->
          <div class="mt-8 pt-8 border-t border-slate-100">
            <div class="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-4">
              <a
                v-for="item in contacts"
                :key="item.label"
                :href="item.href"
                class="flex flex-col items-center gap-2 p-4 rounded-xl bg-slate-50 border border-slate-100 hover:border-slate-200 hover:shadow-sm transition-all duration-200"
                :target="item.href.startsWith('http') ? '_blank' : undefined"
              >
                <component :is="item.icon" class="w-5 h-5" style="color: #B9E424" />
                <span class="text-xs text-slate-400">{{ item.label }}</span>
                <span class="text-xs font-medium text-slate-600 text-center leading-tight">{{ item.value }}</span>
              </a>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
@keyframes float {
  0%,
  100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-20px) rotate(5deg);
  }
}

@keyframes float-delayed {
  0%,
  100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-30px) rotate(-5deg);
  }
}

@keyframes pulse-slow {
  0%,
  100% {
    opacity: 0.3;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.05);
  }
}

@keyframes bounce-slow {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

@keyframes bounce-slow-delayed {
  0%,
  100% {
    transform: translateY(0) rotate(45deg);
  }
  50% {
    transform: translateY(-15px) rotate(45deg);
  }
}

.animate-float {
  animation: float 6s ease-in-out infinite;
}

.animate-float-delayed {
  animation: float-delayed 8s ease-in-out infinite;
}

.animate-pulse-slow {
  animation: pulse-slow 4s ease-in-out infinite;
}

.animate-bounce-slow {
  animation: bounce-slow 3s ease-in-out infinite;
}

.animate-bounce-slow-delayed {
  animation: bounce-slow-delayed 4s ease-in-out infinite;
}
</style>
