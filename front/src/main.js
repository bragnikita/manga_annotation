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

const token = window.localStorage.getItem("ma:access_token");
const expireAt = parseInt(window.localStorage.getItem("ma:token_expire_at") || "0", 10);
if (token && expireAt-3*60*60*1000 > Date.now()) {
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