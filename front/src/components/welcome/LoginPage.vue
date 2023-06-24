<script setup>
import {User, Lock} from '@element-plus/icons-vue'
import {reactive} from "vue";
import {ElMessage} from "element-plus";
import {get, post} from "@/net";
import router from "@/router";
import {useStore} from "@/stores";


//导入 store 仓库
const store = useStore()

//双向绑定数据:  username(用户名),password(密码),remember(记住我)
const form = reactive({
  username: '',
  password: '',
  remember: false
})

//登录函数,传输数据username,password,remember
const login = () => {
  //如果没有填用户名或密码,弹窗警告
  if(!form.username || !form.password) {
    ElMessage.warning('请填写用户名和密码！')
  } else {//填写正确,url: localhost:8080/api/auth/login  传入信息:message
    post('/api/auth/login', {
      //把表单内容传到服务器后台
      username: form.username,
      password: form.password,
      remember: form.remember
    }, (message) => {
      //弹窗message(信息)
      ElMessage.success(message)
      //url: localhost:8080/api/auth/me  传入信息:message
      get('/api/user/me', (message) => {
        //仓库内全局变量user改为message
        store.auth.user = message
        //页面跳转到主页
        router.push('/index')
      }, () => {
        //没传信息,仓库内全局变量user置空
        store.auth.user = null
      })
    })
  }
}


</script>

<template>
  <div style="text-align: center;margin: 0 20px">
    <div style="margin-top: 150px">
      <div style="font-size: 25px;font-weight: bold">登录</div>
      <div style="font-size: 14px;color: grey">在进入系统之前请先输入用户名和密码进行登录</div>
    </div>
    <div style="margin-top: 50px">
      <!--v-model双向绑定,username(用户名),password(密码)-->
      <el-input v-model="form.username" type="text" placeholder="用户名/邮箱" :prefix-icon="User"/>
      <el-input v-model="form.password" type="password" style="margin-top: 10px" placeholder="密码" :prefix-icon="Lock"/>
    </div>
    <el-row style="margin-top: 5px">
      <el-col :span="12" style="text-align: left">
        <!--v-model双向绑定,remember(记住我)-->
        <el-checkbox v-model="form.remember" label="记住我"/>
      </el-col>
      <el-col :span="12" style="text-align: right">
        <!--点击链接跳转忘记密码页面-->
        <el-link @click="router.push('/forget')">忘记密码？</el-link>
      </el-col>
    </el-row>
    <div style="margin-top: 40px">
      <!--点击按钮跳转触发login(登录)函数-->
      <el-button @click="login()" style="width: 270px" type="success" plain>立即登录</el-button>
    </div>
    <el-divider>
      <span style="color: grey;font-size: 13px">没有账号</span>
    </el-divider>
    <div>
      <!--点击案列跳转注册页面-->
      <el-button style="width: 270px" @click="router.push('/register')" type="warning" plain>注册账号</el-button>
    </div>
  </div>
</template>


<style scoped>

</style>