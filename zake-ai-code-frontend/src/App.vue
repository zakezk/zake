<script setup lang="ts">
import { RouterView } from 'vue-router'
import BasicLayout from './layouts/BasicLayout.vue'

import { healthy } from "@/api/healthyController.ts";

healthy().then((res) => {
  console.log(res)
})
</script>

<template>
  <BasicLayout>
    <RouterView />
  </BasicLayout>
</template>

<style scoped>
/* 可删除原有样式或根据需要保留 */
</style>
