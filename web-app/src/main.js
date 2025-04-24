import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { library } from '@fortawesome/fontawesome-svg-core'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { fas } from '@fortawesome/free-solid-svg-icons'
import { far } from '@fortawesome/free-regular-svg-icons'
import './style.css'

//Charts
import VueApexCharts from "vue3-apexcharts";

// Add Font Awesome icons to library
library.add(fas, far)

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(VueApexCharts)
app.component('font-awesome-icon', FontAwesomeIcon)
app.component("apexchart", VueApexCharts)

app.mount('#app')
