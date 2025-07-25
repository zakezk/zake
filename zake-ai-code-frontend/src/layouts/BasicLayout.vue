<template>
  <a-layout class="layout">
    <!-- 头部导航 -->
    <GlobalHeader />

    <!-- 内容区域 -->
    <a-layout-content class="content-container">
      <router-view />
    </a-layout-content>

    <!-- 底部版权 -->
    <GlobalFooter />
  </a-layout>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.content-container {
  margin: 24px 16px;
  padding: 24px;
  background: #fff;
  min-height: 280px;
}
</style>
