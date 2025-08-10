<template>
  <div class="markdown-renderer" v-html="renderedContent"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  content: string
}

const props = defineProps<Props>()

// 简单的 Markdown 渲染函数
const renderedContent = computed(() => {
  if (!props.content) return ''

  let content = props.content

  // 处理代码块
  content = content.replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')

  // 处理行内代码
  content = content.replace(/`([^`]+)`/g, '<code>$1</code>')

  // 处理标题
  content = content.replace(/^### (.*$)/gim, '<h3>$1</h3>')
  content = content.replace(/^## (.*$)/gim, '<h2>$1</h2>')
  content = content.replace(/^# (.*$)/gim, '<h1>$1</h1>')

  // 处理粗体
  content = content.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')

  // 处理斜体
  content = content.replace(/\*(.*?)\*/g, '<em>$1</em>')

  // 处理链接
  content = content.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>')

  // 处理换行
  content = content.replace(/\n/g, '<br>')

  return content
})
</script>

<style scoped>
.markdown-renderer {
  line-height: 1.6;
  word-wrap: break-word;
}

.markdown-renderer :deep(h1) {
  font-size: 1.5em;
  font-weight: bold;
  margin: 0.5em 0;
}

.markdown-renderer :deep(h2) {
  font-size: 1.3em;
  font-weight: bold;
  margin: 0.4em 0;
}

.markdown-renderer :deep(h3) {
  font-size: 1.1em;
  font-weight: bold;
  margin: 0.3em 0;
}

.markdown-renderer :deep(code) {
  background-color: #f6f8fa;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Monaco', 'Menlo', monospace;
  font-size: 0.9em;
}

.markdown-renderer :deep(pre) {
  background-color: #f6f8fa;
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 8px 0;
}

.markdown-renderer :deep(pre code) {
  background: none;
  padding: 0;
}

.markdown-renderer :deep(strong) {
  font-weight: bold;
}

.markdown-renderer :deep(em) {
  font-style: italic;
}

.markdown-renderer :deep(a) {
  color: #1890ff;
  text-decoration: none;
}

.markdown-renderer :deep(a:hover) {
  text-decoration: underline;
}
</style>
