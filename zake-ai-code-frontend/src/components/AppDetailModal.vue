<template>
  <a-modal
    :open="open"
    title="应用详情"
    width="600px"
    :footer="null"
    @cancel="handleCancel"
    @update:open="(value) => $emit('update:open', value)"
  >
    <div v-if="app" class="app-detail">
      <div class="detail-item">
        <label>应用名称：</label>
        <span>{{ app.appName || '未设置' }}</span>
      </div>
      <div class="detail-item">
        <label>代码生成类型：</label>
        <span>{{ formatCodeGenType(app.codeGenType) }}</span>
      </div>
      <div class="detail-item">
        <label>创建时间：</label>
        <span>{{ formatDate(app.createTime) }}</span>
      </div>
      <div class="detail-item">
        <label>更新时间：</label>
        <span>{{ formatDate(app.updateTime) }}</span>
      </div>
      <div v-if="app.initPrompt" class="detail-item">
        <label>初始提示词：</label>
        <div class="prompt-content">{{ app.initPrompt }}</div>
      </div>
      <div v-if="app.deployedTime" class="detail-item">
        <label>部署时间：</label>
        <span>{{ formatDate(app.deployedTime) }}</span>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div v-if="showActions" class="modal-actions">
      <a-button type="primary" @click="handleEdit" style="margin-right: 8px"> 编辑应用 </a-button>
      <a-button type="primary" danger @click="handleDelete"> 删除应用 </a-button>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  open: boolean
  app?: API.AppVO
  showActions?: boolean
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'edit'): void
  (e: 'delete'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

// 格式化代码生成类型
const formatCodeGenType = (type?: string) => {
  if (!type) return '未知'
  switch (type) {
    case 'HTML':
      return 'HTML'
    case 'VUE':
      return 'Vue'
    case 'REACT':
      return 'React'
    default:
      return type
  }
}

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '未知'
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 处理取消
const handleCancel = () => {
  emit('update:open', false)
}

// 处理编辑
const handleEdit = () => {
  emit('edit')
}

// 处理删除
const handleDelete = () => {
  emit('delete')
}
</script>

<style scoped>
.app-detail {
  margin-bottom: 20px;
}

.detail-item {
  margin-bottom: 12px;
  display: flex;
  align-items: flex-start;
}

.detail-item label {
  font-weight: 600;
  color: #333;
  min-width: 100px;
  margin-right: 8px;
}

.detail-item span {
  color: #666;
  flex: 1;
}

.prompt-content {
  background: #f5f5f5;
  padding: 8px 12px;
  border-radius: 4px;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

.modal-actions {
  text-align: right;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e8e8e8;
}
</style>
