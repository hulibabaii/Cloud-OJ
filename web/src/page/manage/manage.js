import Vue from 'vue'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import Manage from "./Manage.vue";
import axios from "axios";
import qs from "qs";

Vue.prototype.$axios = axios
Vue.prototype.qs = qs
Vue.prototype.apiUrl = 'http://cloudoj.204.group/'

Vue.use(ElementUI);

new Vue({
    render: h => h(Manage),
}).$mount('#manage')