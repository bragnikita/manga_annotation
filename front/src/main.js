import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import vuetify from './plugins/vuetify';
import Fragment from 'vue-fragment';

Vue.use(Fragment.Plugin);

Vue.config.productionTip = false;

console.log("Starting")

new Vue({
  router,
  store,
  vuetify,
  render: h => h(App)
}).$mount('#app')

console.log("Started")