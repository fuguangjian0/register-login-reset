import { createRouter, createWebHistory } from 'vue-router'
import {useStore} from "@/stores";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {path: '/', name: 'welcome', component: ()=>import('@/views/WelcomeView.vue'),
      //父路由welcome  子路由welcome-login(登录),welcome-register(注册),welcome-forget(忘记密码)
      children: [
        {path:'',name: 'welcome-login',component:()=>import('@/components/welcome/LoginPage.vue')},
        {path:'register',name: 'welcome-register',component:()=>import('@/components/welcome/RegisterPage.vue')},
        {path:'forget',name: 'welcome-forget',component:()=>import('@/components/welcome/ForgetPage.vue')},
      ]
    },
    {path:'/index', name:'index', component: ()=> import('@/views/IndexView.vue')}
      //主页只有这一个路由,没有子路由
  ]
})

router.beforeEach((to, from, next) => {
  const store = useStore()
  //如果用户不存在且路由名称以welcome-开头,跳转到首页
  if(store.auth.user != null && to.name.startsWith('welcome-')) {
    next('/index')
  } //如果用户不存在且url包含index,跳转到登录页
  else if(store.auth.user == null && to.fullPath.startsWith('/index')) {
    next('/')
  } //如果没匹配到字符,跳转到首页
  else if(to.matched.length === 0){
    next('/index')
  } else {
    next()
  }
})

//导出路由
export default router
