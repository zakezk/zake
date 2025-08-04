<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.push('/')
}
</script>

<template>
  <div class="about-page">
    <div class="page-header">
      <div class="header-content">
        <div>
          <h1>关于我们</h1>
          <p>了解 Zake AI 应用生成平台</p>
        </div>
        <button @click="goBack" class="back-btn">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="19" y1="12" x2="5" y2="12"></line>
            <polyline points="12,19 5,12 12,5"></polyline>
          </svg>
          返回首页
        </button>
      </div>
    </div>

    <div class="about-content">
      <div class="about-section">
        <h2>平台介绍</h2>
        <p>
          Zake AI 应用生成平台是一个基于人工智能的创新工具，旨在让每个人都能轻松创建网站和应用。
          通过简单的自然语言描述，AI 就能为您生成完整的应用程序。
        </p>
      </div>

      <div class="about-section">
        <h2>核心功能</h2>
        <div class="features-grid">
          <div class="feature-card">
            <div class="feature-icon">🤖</div>
            <h3>AI 对话生成</h3>
            <p>通过自然语言与 AI 对话，描述您的需求，AI 将为您生成完整的应用代码。</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">👁️</div>
            <h3>实时预览</h3>
            <p>在生成过程中实时查看应用效果，确保生成结果符合您的预期。</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">🚀</div>
            <h3>一键部署</h3>
            <p>生成完成后，一键部署应用到可访问的 URL，立即分享给他人使用。</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">📱</div>
            <h3>应用管理</h3>
            <p>方便地管理您创建的所有应用，支持编辑、删除和分享。</p>
          </div>
        </div>
      </div>

      <div class="about-section">
        <h2>技术特点</h2>
        <ul class="tech-list">
          <li><strong>前端技术</strong>：Vue 3 + TypeScript + Ant Design Vue</li>
          <li><strong>AI 集成</strong>：支持 SSE 流式对话，实时生成代码</li>
          <li><strong>用户体验</strong>：现代化的界面设计，响应式布局</li>
          <li><strong>权限管理</strong>：完善的用户权限控制系统</li>
        </ul>
      </div>

      <div class="about-section">
        <h2>使用指南</h2>
        <div class="guide-steps">
          <div class="step">
            <div class="step-number">1</div>
            <div class="step-content">
              <h3>描述需求</h3>
              <p>在主页输入框中详细描述您想要创建的应用，描述越详细，生成效果越好。</p>
            </div>
          </div>
          <div class="step">
            <div class="step-number">2</div>
            <div class="step-content">
              <h3>AI 生成</h3>
              <p>AI 将根据您的描述生成应用代码，您可以实时查看生成过程。</p>
            </div>
          </div>
          <div class="step">
            <div class="step-number">3</div>
            <div class="step-content">
              <h3>预览效果</h3>
              <p>生成完成后，在右侧预览区域查看应用效果，确认是否符合预期。</p>
            </div>
          </div>
          <div class="step">
            <div class="step-number">4</div>
            <div class="step-content">
              <h3>部署分享</h3>
              <p>点击部署按钮，获得可访问的 URL，分享给他人使用。</p>
            </div>
          </div>
        </div>
      </div>

      <div class="about-section">
        <h2>联系我们</h2>
        <p>
          如果您有任何问题或建议，欢迎联系我们：
        </p>
        <div class="contact-info">
          <p>📧 邮箱：support@zake-ai.com</p>
          <p>🌐 网站：<a href="https://codefather.cn" target="_blank">codefather.cn</a></p>
          <p>💬 社区：编程导航</p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.about-page {
  padding: 24px;
  background: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 32px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 28px;
  color: #333;
}

.page-header p {
  margin: 0;
  color: #666;
  font-size: 16px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  background: white;
  color: #666;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.back-btn:hover {
  background: #f5f5f5;
  border-color: #1890ff;
  color: #1890ff;
}

.back-btn svg {
  width: 16px;
  height: 16px;
}

.about-content {
  max-width: 1000px;
  margin: 0 auto;
}

.about-section {
  background: white;
  border-radius: 12px;
  padding: 32px;
  margin-bottom: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.about-section h2 {
  margin: 0 0 20px 0;
  font-size: 24px;
  color: #333;
  border-bottom: 2px solid #1890ff;
  padding-bottom: 8px;
}

.about-section p {
  margin: 0 0 16px 0;
  line-height: 1.6;
  color: #666;
  font-size: 16px;
}

.about-section p:last-child {
  margin-bottom: 0;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
  margin-top: 24px;
}

.feature-card {
  text-align: center;
  padding: 24px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  background: #fafafa;
  transition: all 0.2s;
}

.feature-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.feature-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.feature-card h3 {
  margin: 0 0 12px 0;
  font-size: 18px;
  color: #333;
}

.feature-card p {
  margin: 0;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.tech-list {
  margin: 0;
  padding-left: 20px;
}

.tech-list li {
  margin-bottom: 12px;
  line-height: 1.6;
  color: #666;
}

.guide-steps {
  display: grid;
  gap: 24px;
}

.step {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.step-number {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #1890ff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 18px;
  flex-shrink: 0;
}

.step-content h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #333;
}

.step-content p {
  margin: 0;
  color: #666;
  line-height: 1.6;
}

.contact-info {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  border-left: 4px solid #1890ff;
}

.contact-info p {
  margin: 0 0 8px 0;
  color: #666;
}

.contact-info p:last-child {
  margin-bottom: 0;
}

.contact-info a {
  color: #1890ff;
  text-decoration: none;
}

.contact-info a:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 16px;
  }
  
  .features-grid {
    grid-template-columns: 1fr;
  }
  
  .step {
    flex-direction: column;
    text-align: center;
  }
  
  .about-section {
    padding: 20px;
  }
}
</style> 