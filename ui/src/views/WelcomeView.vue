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
import RiCheckLine from '~icons/ri/check-line'
import RiRocketLine from '~icons/ri/rocket-line'
import RiSparklingLine from '~icons/ri/sparkling-line'
import RiDownloadLine from '~icons/ri/download-line'
import RiInstallLine from '~icons/ri/install-line'
import RiSettings5Line from '~icons/ri/settings-5-line'
import RiPlayCircleLine from '~icons/ri/play-circle-line'
import RiMoneyCnyCircleLine from '~icons/ri/money-cny-circle-line'
import RiWechatPayLine from '~icons/ri/wechat-pay-line'
import RiAlipayLine from '~icons/ri/alipay-line'
import RiQqLine from '~icons/ri/qq-line'
import RiLinksLine from '~icons/ri/links-line'
import RiStoreLine from '~icons/ri/store-line'
import RiCodeBoxLine from '~icons/ri/code-box-line'

const isLoaded = ref(false)

onMounted(() => {
  setTimeout(() => {
    isLoaded.value = true
  }, 100)

  confetti({
    particleCount: 150,
    spread: 100,
    origin: { y: 0.4, x: 0.5 },
    colors: ['#3b82f6', '#8b5cf6', '#06b6d4', '#10b981'],
  })
})

const features = [
  {
    icon: RiSettingsLine,
    title: '基本配置',
    description: '文章版权、评论开关、页面显示、免责声明等基础设置',
    color: 'blue',
  },
  {
    icon: RiSmartphoneLine,
    title: '应用配置',
    description: '应用信息、启动页面、小程序专属配置',
    color: 'purple',
  },
  {
    icon: RiLayoutLine,
    title: '页面配置',
    description: '特定页面信息和展示内容的个性化配置',
    color: 'cyan',
  },
  {
    icon: RiUserLine,
    title: '我的配置',
    description: '博主信息、社交链接、个人主页定制',
    color: 'green',
  },
  {
    icon: RiImageLine,
    title: '图片配置',
    description: '默认图片地址、封面图、Logo 设置',
    color: 'orange',
  },
  {
    icon: RiHeartLine,
    title: '恋爱配置',
    description: '恋人信息、恋爱清单、恋爱相册、我们的故事',
    color: 'pink',
  },
]

const steps = [
  {
    icon: RiDownloadLine,
    title: '下载源码',
    desc: '从 GitHub 下载 uni-halo 小程序源码',
  },
  {
    icon: RiInstallLine,
    title: '安装插件',
    desc: '在 Halo 插件市场搜索安装 UniHalo 配置',
  },
  {
    icon: RiSettings5Line,
    title: '配置参数',
    desc: '根据需求填写应用、页面等配置项',
  },
  {
    icon: RiPlayCircleLine,
    title: '启动运行',
    desc: '运行 uni-halo 小程序，预览效果',
  },
]

const colorMap: Record<string, string> = {
  blue: 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)',
  purple: 'linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)',
  cyan: 'linear-gradient(135deg, #06b6d4 0%, #0891b2 100%)',
  green: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
  orange: 'linear-gradient(135deg, #f97316 0%, #ea580c 100%)',
  pink: 'linear-gradient(135deg, #ec4899 0%, #db2777 100%)',
}
</script>

<template>
  <div class="welcome-page" :class="{ loaded: isLoaded }">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-bg">
        <div class="hero-gradient"></div>
        <div class="hero-pattern"></div>
      </div>

      <div class="hero-content">
        <div class="badge">
          <RiSparklingLine />
          <span>Halo 插件</span>
        </div>

        <h1 class="hero-title">
          <span class="title-line">UniHalo</span>
          <span class="title-highlight">配置插件</span>
        </h1>

        <p class="hero-subtitle">
          为免费开源的 uni-halo v2.0 微信小程序提供配套的配置插件，轻松配置你的小程序
        </p>

        <div class="hero-actions">
          <a
            href="https://uni-halo.925i.cn/"
            class="btn btn-primary"
            target="_blank"
          >
            <RiBookOpenLine />
            <span>官方文档</span>
          </a>
          <a
            href="https://github.com/ialley-workshop-open/plugin-uni-halo"
            class="btn btn-secondary"
            target="_blank"
          >
            <RiGithubLine />
            <span>GitHub</span>
          </a>
          <a
            href="https://www.halo.run/store/apps/app-ryemX"
            class="btn btn-secondary"
            target="_blank"
          >
            <RiStoreLine />
            <span>插件市场</span>
          </a>
        </div>

        <div class="hero-stats">
          <div class="stat-item">
            <span class="stat-value">6+</span>
            <span class="stat-label">配置模块</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-value">100%</span>
            <span class="stat-label">开源免费</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-value">v2.0</span>
            <span class="stat-label">支持版本</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Author Section -->
    <section class="author-section">
      <div class="container">
        <div class="author-card">
          <div class="author-avatar">
            <img
              src="https://avatars.githubusercontent.com/u/ialley-workshop-open"
              alt="作者头像"
              @error="($event.target as HTMLImageElement).style.display = 'none'"
            />
            <div class="avatar-fallback">
              <RiUserLine />
            </div>
          </div>
          <div class="author-info">
            <h3 class="author-name">小莫唐尼</h3>
            <p class="author-bio">uni-halo 项目作者，专注于 Halo 生态开发</p>
            <div class="author-links">
              <a
                href="https://www.xiaoxiaomo.cn/"
                class="author-link"
                target="_blank"
              >
                <RiBookOpenLine />
                <span>博客</span>
              </a>
              <a
                href="https://github.com/ialley-workshop-open"
                class="author-link"
                target="_blank"
              >
                <RiGithubLine />
                <span>GitHub</span>
              </a>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Features Section -->
    <section class="features-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">强大功能，灵活配置</h2>
          <p class="section-desc">全方位满足你的小程序配置需求</p>
        </div>

        <div class="features-grid">
          <div
            v-for="feature in features"
            :key="feature.title"
            class="feature-card"
          >
            <div
              class="feature-icon"
              :style="{ background: colorMap[feature.color] }"
            >
              <component :is="feature.icon" />
            </div>
            <h3 class="feature-title">{{ feature.title }}</h3>
            <p class="feature-desc">{{ feature.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Steps Section -->
    <section class="steps-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">使用流程</h2>
          <p class="section-desc">从下载到上线，轻松四步</p>
        </div>

        <div class="steps-timeline">
          <div
            v-for="(step, index) in steps"
            :key="index"
            class="step-item"
          >
            <div class="step-marker">
              <div class="step-icon">
                <component :is="step.icon" />
              </div>
              <div v-if="index < steps.length - 1" class="step-line"></div>
            </div>
            <div class="step-content">
              <h3 class="step-title">{{ step.title }}</h3>
              <p class="step-desc">{{ step.desc }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Links Section -->
    <section class="links-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">相关链接</h2>
          <p class="section-desc">官方资源与社区支持</p>
        </div>

        <div class="links-grid">
          <a
            href="https://uni-halo.925i.cn/"
            class="link-card"
            target="_blank"
          >
            <div class="link-icon blue">
              <RiBookOpenLine />
            </div>
            <div class="link-content">
              <h4 class="link-title">官方文档</h4>
              <p class="link-desc">uni-halo 小程序使用指南</p>
            </div>
            <RiArrowRightSLine class="link-arrow" />
          </a>

          <a
            href="https://github.com/ialley-workshop-open/plugin-uni-halo"
            class="link-card"
            target="_blank"
          >
            <div class="link-icon dark">
              <RiGithubLine />
            </div>
            <div class="link-content">
              <h4 class="link-title">插件源码</h4>
              <p class="link-desc">GitHub 仓库，欢迎 Star</p>
            </div>
            <RiArrowRightSLine class="link-arrow" />
          </a>

          <a
            href="https://github.com/ialley-workshop-open/uni-halo"
            class="link-card"
            target="_blank"
          >
            <div class="link-icon purple">
              <RiCodeBoxLine />
            </div>
            <div class="link-content">
              <h4 class="link-title">小程序源码</h4>
              <p class="link-desc">uni-halo v2.0 开源代码</p>
            </div>
            <RiArrowRightSLine class="link-arrow" />
          </a>

          <a
            href="https://www.halo.run/store/apps/app-ryemX"
            class="link-card"
            target="_blank"
          >
            <div class="link-icon green">
              <RiStoreLine />
            </div>
            <div class="link-content">
              <h4 class="link-title">Halo 插件市场</h4>
              <p class="link-desc">在线安装 UniHalo 配置插件</p>
            </div>
            <RiArrowRightSLine class="link-arrow" />
          </a>

          <a
            href="https://docs.halo.run/developer-guide/plugin/introduction"
            class="link-card"
            target="_blank"
          >
            <div class="link-icon orange">
              <RiFileCodeLine />
            </div>
            <div class="link-content">
              <h4 class="link-title">Halo 开发者文档</h4>
              <p class="link-desc">插件开发指南与 API 参考</p>
            </div>
            <RiArrowRightSLine class="link-arrow" />
          </a>
        </div>
      </div>
    </section>

    <!-- Sponsor Section -->
    <section class="sponsor-section">
      <div class="container">
        <div class="section-header">
          <h2 class="section-title">赞助支持</h2>
          <p class="section-desc">
            如果这个项目对你有帮助，可以请作者喝杯饮料
          </p>
        </div>

        <div class="sponsor-cards">
          <div class="sponsor-card">
            <div class="sponsor-icon wechat">
              <RiWechatPayLine />
            </div>
            <h4 class="sponsor-title">微信赞助</h4>
            <p class="sponsor-desc">扫码请作者喝咖啡</p>
          </div>

          <div class="sponsor-card">
            <div class="sponsor-icon alipay">
              <RiAlipayLine />
            </div>
            <h4 class="sponsor-title">支付宝赞助</h4>
            <p class="sponsor-desc">扫码请作者喝奶茶</p>
          </div>

          <div class="sponsor-card">
            <div class="sponsor-icon qq">
              <RiQqLine />
            </div>
            <h4 class="sponsor-title">QQ 赞助</h4>
            <p class="sponsor-desc">扫码请作者吃零食</p>
          </div>
        </div>

        <div class="sponsor-note">
          <RiMoneyCnyCircleLine />
          <span>您的支持是作者持续维护的动力</span>
        </div>
      </div>
    </section>

    <!-- CTA Section -->
    <section class="cta-section">
      <div class="container">
        <div class="cta-card">
          <div class="cta-icon">
            <RiRocketLine />
          </div>
          <h2 class="cta-title">准备好了吗？</h2>
          <p class="cta-desc">立即开始配置你的微信小程序</p>
          <div class="cta-actions">
            <a
              href="https://uni-halo.925i.cn/"
              class="btn btn-primary btn-lg"
              target="_blank"
            >
              <RiBookOpenLine />
              <span>查看文档</span>
            </a>
            <a
              href="https://github.com/ialley-workshop-open/plugin-uni-halo"
              class="btn btn-outline btn-lg"
              target="_blank"
            >
              <RiGithubLine />
              <span>源码仓库</span>
            </a>
          </div>
        </div>
      </div>
    </section>

    <!-- Footer -->
    <footer class="footer">
      <div class="container">
        <div class="footer-content">
          <div class="footer-brand">
            <span class="footer-logo">UniHalo</span>
            <span class="footer-copyright">© 2024 ialley-workshop-open</span>
          </div>
          <div class="footer-links">
            <a
              href="https://uni-halo.925i.cn/"
              class="footer-link"
              target="_blank"
            >
              官方文档
            </a>
            <a
              href="https://github.com/ialley-workshop-open/plugin-uni-halo"
              class="footer-link"
              target="_blank"
            >
              GitHub
            </a>
            <a
              href="https://docs.halo.run/developer-guide/plugin/introduction"
              class="footer-link"
              target="_blank"
            >
              Halo 开发者
            </a>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<style lang="scss" scoped>
.welcome-page {
  min-height: 100vh;
  background-color: #f8fafc;
  opacity: 0;
  transition: opacity 0.5s ease;

  &.loaded {
    opacity: 1;
  }
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 2rem;
}

// Hero Section
.hero-section {
  position: relative;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  inset: 0;
  z-index: 0;
}

.hero-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    135deg,
    #1e3a5f 0%,
    #2563eb 50%,
    #7c3aed 100%
  );
}

.hero-pattern {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(
    circle at 25% 25%,
    rgba(255, 255, 255, 0.1) 0%,
    transparent 50%
  );
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 2rem;
  max-width: 800px;
}

.badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 9999px;
  padding: 0.5rem 1rem;
  color: #fff;
  font-size: 0.875rem;
  font-weight: 500;
  margin-bottom: 2rem;
}

.hero-title {
  font-size: 4rem;
  font-weight: 800;
  line-height: 1.1;
  margin: 0 0 1.5rem 0;
  color: #fff;

  .title-line {
    display: block;
  }

  .title-highlight {
    display: block;
    background: linear-gradient(90deg, #60a5fa, #a78bfa, #34d399);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

.hero-subtitle {
  font-size: 1.25rem;
  line-height: 1.75;
  color: rgba(255, 255, 255, 0.85);
  margin: 0 0 2.5rem 0;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.hero-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  margin-bottom: 3rem;
  flex-wrap: wrap;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.875rem 1.5rem;
  border-radius: 0.5rem;
  font-size: 1rem;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.2s ease;
  cursor: pointer;

  &.btn-primary {
    background: #fff;
    color: #2563eb;

    &:hover {
      background: #f0f9ff;
      transform: translateY(-2px);
      box-shadow: 0 10px 25px rgba(0, 0, 0, 0.2);
    }
  }

  &.btn-secondary {
    background: rgba(255, 255, 255, 0.15);
    color: #fff;
    border: 1px solid rgba(255, 255, 255, 0.3);

    &:hover {
      background: rgba(255, 255, 255, 0.25);
      transform: translateY(-2px);
    }
  }

  &.btn-outline {
    background: transparent;
    color: #2563eb;
    border: 2px solid #2563eb;

    &:hover {
      background: #2563eb;
      color: #fff;
    }
  }

  &.btn-lg {
    padding: 1rem 2rem;
    font-size: 1.125rem;
  }
}

.hero-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 2rem;
}

.stat-item {
  text-align: center;

  .stat-value {
    display: block;
    font-size: 2rem;
    font-weight: 700;
    color: #fff;
  }

  .stat-label {
    display: block;
    font-size: 0.875rem;
    color: rgba(255, 255, 255, 0.7);
    margin-top: 0.25rem;
  }
}

.stat-divider {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.3);
}

// Author Section
.author-section {
  padding: 4rem 0;
  background: #fff;
}

.author-card {
  display: flex;
  align-items: center;
  gap: 2rem;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  border-radius: 1rem;
  padding: 2rem;
  max-width: 600px;
  margin: 0 auto;
}

.author-avatar {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .avatar-fallback {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 2rem;
  }
}

.author-info {
  flex: 1;
}

.author-name {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 0.5rem 0;
}

.author-bio {
  font-size: 0.875rem;
  color: #64748b;
  margin: 0 0 1rem 0;
}

.author-links {
  display: flex;
  gap: 1rem;
}

.author-link {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
  font-size: 0.875rem;
  color: #2563eb;
  text-decoration: none;
  transition: color 0.2s ease;

  &:hover {
    color: #1d4ed8;
  }
}

// Section Common
.section-header {
  text-align: center;
  margin-bottom: 4rem;
}

.section-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 1rem 0;
}

.section-desc {
  font-size: 1.125rem;
  color: #64748b;
  margin: 0;
}

// Features Section
.features-section {
  padding: 6rem 0;
  background: #fff;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  gap: 2rem;

  @media (min-width: 640px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (min-width: 1024px) {
    grid-template-columns: repeat(3, 1fr);
  }
}

.feature-card {
  background: #f8fafc;
  border-radius: 1rem;
  padding: 2rem;
  transition: all 0.3s ease;
  cursor: pointer;

  &:hover {
    background: #fff;
    transform: translateY(-8px);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  }
}

.feature-icon {
  width: 3.5rem;
  height: 3.5rem;
  border-radius: 0.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 1.5rem;
  margin-bottom: 1.5rem;
}

.feature-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 0.75rem 0;
}

.feature-desc {
  font-size: 0.9375rem;
  color: #64748b;
  line-height: 1.6;
  margin: 0;
}

// Steps Section
.steps-section {
  padding: 6rem 0;
  background: linear-gradient(180deg, #f8fafc 0%, #fff 100%);
}

.steps-timeline {
  max-width: 800px;
  margin: 0 auto;
}

.step-item {
  display: flex;
  gap: 1.5rem;
  position: relative;
}

.step-marker {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.step-icon {
  width: 3rem;
  height: 3rem;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 1.25rem;
  flex-shrink: 0;
}

.step-line {
  width: 2px;
  flex: 1;
  background: linear-gradient(180deg, #3b82f6 0%, #e2e8f0 100%);
  margin: 0.5rem 0;
}

.step-content {
  padding-bottom: 2rem;

  .step-title {
    font-size: 1.125rem;
    font-weight: 600;
    color: #1e293b;
    margin: 0 0 0.5rem 0;
  }

  .step-desc {
    font-size: 0.9375rem;
    color: #64748b;
    margin: 0;
  }
}

// Links Section
.links-section {
  padding: 6rem 0;
  background: #fff;
}

.links-grid {
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  gap: 1rem;

  @media (min-width: 640px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (min-width: 1024px) {
    grid-template-columns: repeat(3, 1fr);
  }
}

.link-card {
  display: flex;
  align-items: center;
  gap: 1rem;
  background: #f8fafc;
  border-radius: 0.75rem;
  padding: 1.25rem;
  text-decoration: none;
  transition: all 0.2s ease;

  &:hover {
    background: #fff;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  }
}

.link-icon {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 1.25rem;
  flex-shrink: 0;

  &.blue {
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  }

  &.dark {
    background: linear-gradient(135deg, #1e293b 0%, #0f172a 100%);
  }

  &.purple {
    background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  }

  &.green {
    background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  }

  &.orange {
    background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
  }
}

.link-content {
  flex: 1;
  min-width: 0;
}

.link-title {
  font-size: 0.9375rem;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 0.25rem 0;
}

.link-desc {
  font-size: 0.8125rem;
  color: #64748b;
  margin: 0;
}

.link-arrow {
  color: #cbd5e1;
  flex-shrink: 0;
}

// Sponsor Section
.sponsor-section {
  padding: 6rem 0;
  background: linear-gradient(180deg, #f8fafc 0%, #fff 100%);
}

.sponsor-cards {
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  gap: 1.5rem;
  max-width: 900px;
  margin: 0 auto;

  @media (min-width: 640px) {
    grid-template-columns: repeat(3, 1fr);
  }
}

.sponsor-card {
  background: #fff;
  border-radius: 1rem;
  padding: 2rem;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
  }
}

.sponsor-icon {
  width: 4rem;
  height: 4rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 2rem;
  margin: 0 auto 1rem;

  &.wechat {
    background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  }

  &.alipay {
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  }

  &.qq {
    background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  }
}

.sponsor-title {
  font-size: 1rem;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 0.5rem 0;
}

.sponsor-desc {
  font-size: 0.875rem;
  color: #64748b;
  margin: 0;
}

.sponsor-note {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-top: 2rem;
  color: #64748b;
  font-size: 0.875rem;
}

// CTA Section
.cta-section {
  padding: 6rem 0;
  background: #fff;
}

.cta-card {
  text-align: center;
  background: linear-gradient(135deg, #1e3a5f 0%, #2563eb 50%, #7c3aed 100%);
  border-radius: 1.5rem;
  padding: 4rem 2rem;
}

.cta-icon {
  width: 4rem;
  height: 4rem;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 2rem;
  margin: 0 auto 1.5rem;
}

.cta-title {
  font-size: 2rem;
  font-weight: 700;
  color: #fff;
  margin: 0 0 0.75rem 0;
}

.cta-desc {
  font-size: 1.125rem;
  color: rgba(255, 255, 255, 0.85);
  margin: 0 0 2rem 0;
}

.cta-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

// Footer
.footer {
  background: #1e293b;
  padding: 2rem 0;
}

.footer-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.5rem;

  @media (min-width: 640px) {
    flex-direction: row;
    justify-content: space-between;
  }
}

.footer-brand {
  text-align: center;

  @media (min-width: 640px) {
    text-align: left;
  }
}

.footer-logo {
  display: block;
  font-size: 1.25rem;
  font-weight: 700;
  color: #fff;
  margin-bottom: 0.25rem;
}

.footer-copyright {
  font-size: 0.875rem;
  color: #94a3b8;
}

.footer-links {
  display: flex;
  gap: 1.5rem;
}

.footer-link {
  color: #94a3b8;
  text-decoration: none;
  font-size: 0.875rem;
  transition: color 0.2s ease;

  &:hover {
    color: #fff;
  }
}
</style>
