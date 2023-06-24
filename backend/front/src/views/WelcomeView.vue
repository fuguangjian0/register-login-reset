<script setup>

</script>

<template>
  <div style="width: 100vw;height: 100vh;overflow: hidden;display: flex">
    <!--左边div,是一张图片-->
    <div style="flex: 1">
      <el-image style="width: 100%;height: 100%" fit="cover" src="../public/bg.png"/>
    </div>

    <!--右边div-->
    <div style="width: 400px;background-color: white;z-index: 1">
      <!--走路由-->
      <router-view v-slot="{Component}">
        <!--动画:淡入淡出-->
        <transition name="el-fade-in-linear" mode="out-in">
          <component :is="Component" style="height: 100%"/>
        </transition>
      </router-view>
    </div>

  </div>
</template>

<style scoped>

</style>
