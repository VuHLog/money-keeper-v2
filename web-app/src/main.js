import 'element-plus/dist/index.css'
import ElementPlus from 'element-plus'
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { library } from '@fortawesome/fontawesome-svg-core'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { fas } from '@fortawesome/free-solid-svg-icons'
import { far } from '@fortawesome/free-regular-svg-icons'
import './style.css'

//api
import { base } from "./apis/ApiService.js";

//Charts
import VueApexCharts from "vue3-apexcharts";

// Add Font Awesome icons to library
library.add(fas, far)

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(VueApexCharts)
app.use(ElementPlus)
app.component('font-awesome-icon', FontAwesomeIcon)
app.component("apexchart", VueApexCharts)

const config = app.config;
config.globalProperties.$api = base;

app.mount('#app')
