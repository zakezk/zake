<script setup lang="ts">
import { RouterView } from 'vue-router'
import BasicLayout from './layouts/BasicLayout.vue'

import { useLoginUserStore } from '@/stores/loginUser.ts'

const loginUserStore = useLoginUserStore()
loginUserStore.fetchLoginUser()


</script>

<template>
  <BasicLayout>
    <RouterView />
  </BasicLayout>
</template>

<style scoped>
/* 可删除原有样式或根据需要保留 */
</style>
