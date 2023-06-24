import {reactive} from 'vue'
import { defineStore } from 'pinia'


//创建useStore仓库,auth-user的全局对象,可以改变它的状态并全局使用它
export const useStore = defineStore('store', () => {
    const auth = reactive({
        user: null
    })
    return { auth }
})
