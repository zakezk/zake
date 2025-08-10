<template>
  <a-modal
    :open="open"
    title="部署成功"
    width="500px"
    :footer="null"
    @cancel="handleCancel"
    @update:open="(value) => $emit('update:open', value)"
  >
    <div class="deploy-success">
      <div class="success-icon">
        <CheckCircleOutlined />
      </div>
      <div class="success-message">
        <h3>恭喜！应用部署成功</h3>
        <p>您的网站已经成功部署到云端，可以通过以下链接访问：</p>
      </div>

      <div class="deploy-url">
        <a-input :value="deployUrl" readonly size="large" style="margin-bottom: 16px">
          <template #addonAfter>
            <a-button type="link" @click="copyUrl" style="padding: 0; height: auto">
              复制
            </a-button>
          </template>
        </a-input>
      </div>

      <div class="deploy-actions">
        <a-button type="primary" size="large" @click="handleOpenSite">
          <template #icon>
            <ExportOutlined />
          </template>
          打开网站
        </a-button>
        <a-button size="large" @click="handleCancel" style="margin-left: 12px"> 关闭 </a-button>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue'
import { CheckCircleOutlined, ExportOutlined } from '@ant-design/icons-vue'

interface Props {
  open: boolean
  deployUrl: string
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'open-site'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 处理取消
const handleCancel = () => {
  emit('update:open', false)
}

// 处理打开网站
const handleOpenSite = () => {
  emit('open-site')
}

// 复制链接
const copyUrl = async () => {
  try {
    await navigator.clipboard.writeText(props.deployUrl)
    message.success('链接已复制到剪贴板')
  } catch (error) {
    // 降级方案
    const textArea = document.createElement('textarea')
    textArea.value = props.deployUrl
    document.body.appendChild(textArea)
    textArea.select()
    document.execCommand('copy')
    document.body.removeChild(textArea)
    message.success('链接已复制到剪贴板')
  }
}
</script>

<style scoped>
.deploy-success {
  text-align: center;
  padding: 20px 0;
}

.success-icon {
  font-size: 64px;
  color: #52c41a;
  margin-bottom: 16px;
}

.success-message h3 {
  color: #52c41a;
  margin-bottom: 8px;
  font-size: 18px;
}

.success-message p {
  color: #666;
  margin-bottom: 24px;
}

.deploy-url {
  margin-bottom: 24px;
}

.deploy-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
}
</style>
