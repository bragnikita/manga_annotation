import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import vuetify from './plugins/vuetify';
import Fragment from 'vue-fragment';
import  {setAccessToken} from "./store/api_client";

Vue.use(Fragment.Plugin);

Vue.config.productionTip = false;

console.log("Starting");

let token = window.localStorage.getItem("ma:access_token");
if (token) {
  setAccessToken(token);
  store.commit('setUser', { username: "admin" });
}


new Vue({
  router,
  store,
  vuetify,
  render: h => h(App)
}).$mount('#app')

console.log("Started")