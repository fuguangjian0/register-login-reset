<script setup>
import {EditPen, Lock, Message, User} from "@element-plus/icons-vue";
import router from "@/router";
import {reactive, ref} from "vue";
import {ElMessage} from "element-plus";
import {post} from "@/net";

//双向绑定数据:  username(用户名),password(密码),password_repeat(重复密码),email(邮箱),code(验证码)
const form = reactive({
  username: '',
  password: '',
  password_repeat: '',
  email: '',
  code: ''
})

//自定义用户名验证规则
const validateUsername = (rule, value, callback) => {
  if (value === '') callback(new Error('请输入用户名'))//判空
  else if(!/^[a-zA-Z0-9\u4e00-\u9fa5]+$/.test(value)) callback(new Error('用户名不能包含特殊字符，只能是中文/英文'))//特殊字符
  else callback()
}

//自定义密码验证规则
const validatePassword = (rule, value, callback) => {
  if (value === '') callback(new Error('请再次输入密码'))//判空
  else if (value !== form.password) callback(new Error("两次输入的密码不一致"))//比较
  else callback()
}

//验证规则,username,password,password_repeat,email,code
const rules = {
  username: [
    { validator: validateUsername, trigger: ['blur', 'change'] },
    { min: 2, max: 8, message: '用户名的长度必须在2-8个字符之间', trigger: ['blur', 'change'] },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 16, message: '密码的长度必须在6-16个字符之间', trigger: ['blur', 'change'] }
  ],
  password_repeat: [
    { validator: validatePassword, trigger: ['blur', 'change'] },
  ],
  email: [
    { required: true, message: '请输入邮件地址', trigger: 'blur' },
    {type: 'email', message: '请输入合法的邮箱', trigger: ['blur', 'change']}
  ],
  code: [
    { required: true, message: '请输入获取的验证码', trigger: 'blur' },
  ]
}

const formRef = ref()
const isEmailValid = ref(false)
const coldTime = ref(0)

//如果邮件填写正确,则满足发送验证码条件
const onValidate = (prop, isValid) => {
  if(prop === 'email') isEmailValid.value = isValid
}

//注册函数
const register = () => {
  formRef.value.validate((isValid) => {
    if(isValid) {//如果前端表单验证通过,发送post请求,传输username password email code
      post('/api/auth/register', {
        username: form.username,
        password: form.password,
        email: form.email,
        code: form.code
      }, (message) => {//接收message数据
        ElMessage.success(message)//弹窗成功信息
        router.push("/")//注册成功,跳转登录页
      })
    } else {//注册失败,弹窗警告
      ElMessage.warning('请完整填写注册表单内容！')
    }
  })
}

//验证电子邮件函数
const validateEmail = () => {
  coldTime.value = 60//初始化冷却时间
  post('/api/auth/valid-register-email', {//发送post请求,把email数据发给后台
    email: form.email
  }, (message) => {
    ElMessage.success(message)//弹窗信息
    setInterval(() => coldTime.value--, 1000)//冷却时间每秒-1
  }, (message) => {
    ElMessage.warning(message)//弹窗警告
    coldTime.value = 0//冷却时间每秒置0
  })
}
</script>


<template>
  <div style="text-align: center;margin: 0 20px">
    <div style="margin-top: 100px">
      <div style="font-size: 25px;font-weight: bold">注册新用户</div>
      <div style="font-size: 14px;color: grey">请在下方填写相关信息</div>
    </div>

    <div style="margin-top: 50px">
      <!--el-form表单用于前端校验,:rules(规则)rules函数,:validate(验证)validate函数,ref引用formRef-->
      <el-form :model="form" :rules="rules" @validate="onValidate" ref="formRef">

        <el-form-item prop="username"><!--prop规则 username,v-model双向绑定,username(用户名)-->
          <el-input v-model="form.username" :maxlength="8" type="text" placeholder="用户名" :prefix-icon="User"/></el-form-item>
        <el-form-item prop="password"><!--prop规则 password,v-model双向绑定,password(密码)-->
          <el-input v-model="form.password" :maxlength="16" type="password" placeholder="密码" :prefix-icon="Lock"/></el-form-item>
        <el-form-item prop="password_repeat"><!--prop规则 password_repeat,v-model双向绑定,password_repeat(重复密码)-->
          <el-input v-model="form.password_repeat" :maxlength="16" type="password" placeholder="重复密码" :prefix-icon="Lock"/></el-form-item>
        <el-form-item prop="email"><!--prop规则 email,v-model双向绑定,email(邮箱)-->
          <el-input v-model="form.email" type="email" placeholder="邮箱" :prefix-icon="Message"/></el-form-item>

        <el-form-item prop="code"><!--prop规则 code,v-model双向绑定,code(验证码)-->
          <el-row :gutter="10" style="width: 100%"><el-col :span="17">
              <el-input v-model="form.code" :maxlength="6" type="text" placeholder="请输入验证码" :prefix-icon="EditPen"/>
            </el-col>
            <el-col :span="5">
              <!-- 点击按钮触发:validateEmail函数; disabled不能点击:1.邮箱格式不对 2.冷却时间未结束 -->
              <el-button type="success" @click="validateEmail" :disabled="!isEmailValid || coldTime > 0">
                <!--三元表达式填充内容-->
                {{coldTime > 0 ? '请稍后 ' + coldTime + ' 秒' : '获取验证码'}}</el-button>
            </el-col>
          </el-row>
        </el-form-item>

      </el-form>
    </div>
    <div style="margin-top: 80px">
      <!--点击按钮触发register(注册)函数-->
      <el-button style="width: 270px" type="warning" @click="register" plain>立即注册</el-button>
    </div>
    <div style="margin-top: 20px">
      <span style="font-size: 14px;line-height: 15px;color: grey">已有账号? </span>
      <!--点击链接跳转到登录页面-->
      <el-link type="primary" style="translate: 0 -2px" @click="router.push('/')">立即登录</el-link>
    </div>
  </div>
</template>



<style scoped>

</style>