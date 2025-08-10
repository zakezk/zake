<template>
  <div class="visual-editor">
    <!-- 编辑模式提示 -->
    <div v-if="isEditMode" class="edit-mode-indicator">
      <div class="indicator-content">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
          <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
        </svg>
        <span>编辑模式 - 点击网页元素进行选择</span>
        <button @click="exitEditMode" class="exit-edit-btn">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>
      </div>
    </div>

    <!-- iframe 容器 -->
    <div class="iframe-container" :class="{ 'edit-mode': localEditMode }">
      <iframe
        v-if="iframeSrc"
        :src="iframeSrc"
        ref="previewIframe"
        class="preview-iframe"
        frameborder="0"
        @load="onIframeLoad"
      ></iframe>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import { VisualEditor, type ElementInfo } from '@/utils/VisualEditor'

interface Props {
  iframeSrc: string
  isEditMode: boolean
}

interface Emits {
  (e: 'element-selected', element: ElementInfo): void
  (e: 'edit-mode-exit'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const previewIframe = ref<HTMLIFrameElement>()
const localEditMode = ref(false)
const visualEditor = ref<VisualEditor>()

// 监听编辑模式变化
watch(
  () => props.isEditMode,
  (newMode) => {
    if (newMode) {
      enterEditMode()
    } else {
      exitEditMode()
    }
  },
)

// iframe 加载完成
const onIframeLoad = () => {
  console.log('iframe加载完成')
  if (previewIframe.value && visualEditor.value) {
    visualEditor.value.init(previewIframe.value)
    visualEditor.value.onIframeLoad()
  }
}

// 进入编辑模式
const enterEditMode = () => {
  if (!visualEditor.value) return

  try {
    console.log('开始进入编辑模式')
    visualEditor.value.enableEditMode()
    localEditMode.value = true
    message.success('已进入编辑模式，点击网页元素进行选择')
  } catch (error) {
    console.error('进入编辑模式失败:', error)
    message.error('进入编辑模式失败')
  }
}

// 退出编辑模式
const exitEditMode = () => {
  if (!visualEditor.value) return

  try {
    console.log('开始退出编辑模式')
    visualEditor.value.disableEditMode()
    localEditMode.value = false
    emit('edit-mode-exit')
    message.info('已退出编辑模式')
  } catch (error) {
    console.error('退出编辑模式失败:', error)
  }
}

// 清除选中的元素
const clearSelectedElement = () => {
  if (visualEditor.value) {
    visualEditor.value.clearSelection()
  }
}

// 获取选中的元素信息
const getSelectedElement = () => {
  return null // 通过事件获取
}

// 暴露方法给父组件
defineExpose({
  getSelectedElement,
  clearSelectedElement,
  enterEditMode,
  exitEditMode,
})

onMounted(() => {
  // 初始化可视化编辑器
  visualEditor.value = new VisualEditor({
    onElementSelected: (elementInfo: ElementInfo) => {
      console.log('元素被选中:', elementInfo)
      emit('element-selected', elementInfo)
    },
    onElementHover: (elementInfo: ElementInfo) => {
      console.log('元素悬浮:', elementInfo)
    },
    onEditModeToggle: (editMode: boolean) => {
      console.log('编辑模式切换:', editMode)
      localEditMode.value = editMode
    },
  })

  // 监听iframe消息
  window.addEventListener('message', (event) => {
    if (visualEditor.value) {
      visualEditor.value.handleIframeMessage(event)
    }
  })
})

onUnmounted(() => {
  if (visualEditor.value) {
    visualEditor.value.disableEditMode()
  }
  window.removeEventListener('message', (event) => {
    if (visualEditor.value) {
      visualEditor.value.handleIframeMessage(event)
    }
  })
})
</script>

<style scoped>
.visual-editor {
  position: relative;
  width: 100%;
  height: 100%;
}

.edit-mode-indicator {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  background: #1890ff;
  color: white;
  padding: 8px 16px;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
}

.indicator-content {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.indicator-content svg {
  width: 16px;
  height: 16px;
}

.exit-edit-btn {
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.exit-edit-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.exit-edit-btn svg {
  width: 16px;
  height: 16px;
}

.iframe-container {
  width: 100%;
  height: 100%;
  position: relative;
}

.iframe-container.edit-mode {
  cursor: crosshair;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
}
</style>
