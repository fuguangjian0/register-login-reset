<script setup>
import {reactive, ref} from "vue";
import {EditPen, Lock, Message} from "@element-plus/icons-vue";
import {post} from "@/net";
import {ElMessage} from "element-plus";
import router from "@/router";

const active = ref(0)

//双向绑定数据:  email(邮箱),code(验证码),password(密码),password_repeat(重复密码)
const form = reactive({
  email: '',
  code: '',
  password: '',
  password_repeat: '',
})

//自定义密码验证规则
const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== form.password) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}

//验证规则email,code,password,password_repeat
const rules = {
  email: [
    { required: true, message: '请输入邮件地址', trigger: 'blur' },
    {type: 'email', message: '请输入合法的电子邮件地址', trigger: ['blur', 'change']}
  ],
  code: [
    { required: true, message: '请输入获取的验证码', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 16, message: '密码的长度必须在6-16个字符之间', trigger: ['blur', 'change'] }
  ],
  password_repeat: [
    { validator: validatePassword, trigger: ['blur', 'change'] },
  ],
}



const formRef = ref()
const isEmailValid = ref(false)
const coldTime = ref(0)

//如果邮件填写正确,则满足发送验证码条件
const onValidate = (prop, isValid) => {
  if(prop === 'email')
    isEmailValid.value = isValid
}

//验证电子邮件函数
const validateEmail = () => {
  coldTime.value = 60//初始化冷却时间
  post('/api/auth/valid-reset-email', {//发送post请求,把email数据发给后台
    email: form.email
  }, (message) => {
    ElMessage.success(message)//弹窗信息
    setInterval(() => coldTime.value--, 1000)//冷却时间每秒-1
  }, (message) => {
    ElMessage.warning(message)//弹窗警告
    coldTime.value = 0//冷却时间每秒置0
  })
}

//开始重置密码函数
const startReset = () => {
  //满足重置条件
  formRef.value.validate((isValid) => {
    if(isValid) {//发送post请求,把email和code发给后台
      post('/api/auth/start-reset', {
        email: form.email,
        code: form.code
      }, () => {
        active.value++//跳转重新设定密码页面
      })
    } else {
      //不满足重置密码条件,弹窗警告
      ElMessage.warning('请填写电子邮件地址和验证码')
    }
  })
}

//进行重置密码函数
const doReset = () => {
  //满足重置条件
  formRef.value.validate((isValid) => {
    if(isValid) {
      post('/api/auth/do-reset', {
        password: form.password//发送post请求,把password发给后台
      }, (message) => {
        ElMessage.success(message)//接收数据(重置密码成功)弹窗
        router.push('/')//页面跳转主页
      })
    } else {
      ElMessage.warning('请填写新的密码')
    }
  })
}

</script>


<template>
  <div>

    <!--两步大纲-->
    <div style="margin: 30px 20px">
      <el-steps :active="active" finish-status="success" align-center>
        <el-step title="验证电子邮件" /><!--第一步-->
        <el-step title="重新设定密码" /><!--第二步-->
      </el-steps>
    </div>


    <transition name="el-fade-in-linear" mode="out-in"><!--套一个淡入淡出动画-->
      <div style="text-align: center;margin: 0 20px;height: 100%" v-if="active === 0"><!--0验证邮件页面-->
        <div style="margin-top: 80px">
          <div style="font-size: 25px;font-weight: bold">重置密码</div>
          <div style="font-size: 14px;color: grey">请输入需要重置密码的电子邮件地址</div>
        </div>

        <div style="margin-top: 50px">
          <el-form :model="form" :rules="rules" @validate="onValidate" ref="formRef"><!--表单验证套用-->
            <el-form-item prop="email"><!--prop规则 email,v-model双向绑定,email(邮箱)-->
              <el-input v-model="form.email" type="email" placeholder="电子邮件地址" :prefix-icon="Message"/>
            </el-form-item>
            <el-form-item prop="code"><!--prop规则 code,v-model双向绑定,code(验证码)-->
              <el-row :gutter="10" style="width: 100%">
                <el-col :span="17">
                  <el-input v-model="form.code" :maxlength="6" type="text" placeholder="请输入验证码" :prefix-icon="EditPen"/>
                </el-col>
                <el-col :span="5">
                  <!-- 点击按钮触发:validateEmail函数; disabled不能点击:1.邮箱格式不对 2.冷却时间未结束 -->
                  <el-button type="success" @click="validateEmail" :disabled="!isEmailValid || coldTime > 0">
                    <!--三元表达式填充内容-->
                    {{coldTime > 0 ? '请稍后 ' + coldTime + ' 秒' : '获取验证码'}}
                  </el-button>
                </el-col>
              </el-row>
            </el-form-item>
          </el-form>
        </div>
        <div style="margin-top: 70px">
          <!--点击按钮触发startReset函数,开始重置密码-->
          <el-button @click="startReset()" style="width: 270px;" type="danger" plain>开始重置密码</el-button>
        </div>
      </div>
    </transition>



    <transition name="el-fade-in-linear" mode="out-in">
      <div style="text-align: center;margin: 0 20px;height: 100%" v-if="active === 1"><!--1重设密码页面-->
        <div style="margin-top: 80px">
          <div style="font-size: 25px;font-weight: bold">重置密码</div>
          <div style="font-size: 14px;color: grey">请填写您的新密码，务必牢记，防止丢失</div>
        </div>


        <div style="margin-top: 50px">
          <!--表单校验-->
          <el-form :model="form" :rules="rules" @validate="onValidate" ref="formRef">
            <el-form-item prop="password"><!--prop规则 password,v-model双向绑定,password(密码)-->
              <el-input v-model="form.password" :maxlength="16" type="password" placeholder="新密码" :prefix-icon="Lock"/>
            </el-form-item>
            <el-form-item prop="password_repeat"><!--prop规则 password_repeat,v-model双向绑定,password_repeat(重复密码)-->
              <el-input v-model="form.password_repeat" :maxlength="16" type="password" placeholder="重复新密码" :prefix-icon="Lock"/>
            </el-form-item>
          </el-form>
        </div>
        <div style="margin-top: 70px">
          <!--点击触发doReset函数,重置密码-->
          <el-button @click="doReset()" style="width: 270px;" type="danger" plain>立即重置密码</el-button>
        </div>
      </div>
    </transition>
  </div>



</template>



<style scoped>

</style>