<script setup>
import {get} from "@/net";
import {useStore} from "@/stores";
import router from "@/router";

const store = useStore()

//一旦user置空了,应该检查一下,向localhost:8080/api/user/me接口要一下message信息,如果user要来了(redis中含user),可以重定向到主页
// if(store.auth.user == null) {
//   get('/api/user/me', (message) => {
//     store.auth.user = message
//     router.push('/index')
//   }, () => {
//     store.auth.user = null
//   })
// }
</script>

<template>
  <!--此处交给路由即可,不用写内容-->
  <router-view/>
</template>

<style scoped>

</style>
