
<template>
  <a-layout-header class="header">
    <div class="logo-container">
      <img src="/favicon.ico" alt="Logo" class="logo" />
      <h1 class="title">zake应用生成</h1>
    </div>

    <a-menu
  mode="horizontal"
  :items="menuItems"
  class="menu"
  :selectedKeys="[selectedKey]"
  @select="(e) => handleMenuSelect(e.key)"
/>

    <div class="user-container">
      <a-button type="primary">登录</a-button>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const selectedKey = ref<string>(route.path)

// 监听路由变化更新选中状态
watch(
  () => route.path,
  (newPath) => {
    selectedKey.value = newPath
  },
  { immediate: true }
)

// 菜单配置项
const menuItems = [
  { key: '/', label: '首页' },
  { key: '/about', label: '关于' },
  // 可根据需要添加更多菜单项
]

const handleMenuSelect = (key: string) => {
  selectedKey.value = key
  router.push(key)
}
</script>

<style scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  padding: 0 24px;
}

.logo-container {
  display: flex;
  align-items: center;
}

.logo {
  height: 32px;
  margin-right: 12px;
}

.title {
  font-size: 18px;
  margin: 0;
}

.menu {
  flex: 1;
  margin: 0 24px;
}

.user-container {
  display: flex;
  align-items: center;
}
</style>
