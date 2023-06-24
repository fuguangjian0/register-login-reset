<script setup>
//登录后页面
import {get} from "@/net";
import {ElMessage} from "element-plus";
import router from "@/router";
import {useStore} from "@/stores";

//导入 store 仓库
const store = useStore()

//登出函数
const logout = () => {
  //url: localhost:8080/api/auth/logout  传入信息:message
  get('/api/auth/logout', (message) => {
    ElMessage.success(message)//页面信息弹出一下
    store.auth.user = null//仓库内user全局变量置空
    router.push('/')//路由跳转到登录页
  })
}

</script>

<template>
  <div>
    <!--双向绑定仓库内全局变量 user 的 username (用户名)-->
    欢迎{{store.auth.user.username}}进入到学习平台
  </div>
  <div>
    <!--退出登录按钮,点击触发上面的 logout 函数,设为 danger&&plain 样式-->
    <el-button @click="logout()" type="danger" plain>退出登录</el-button>
  </div>
</template>

<style scoped>

</style>
