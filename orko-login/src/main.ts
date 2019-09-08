import Vue from "vue";
import App from "./App.vue";
// @ts-ignore
import SuiVue from "semantic-ui-vue";
import "@orko-semantic/semantic.min.css";

Vue.config.productionTip = false;

// @ts-ignore
Vue.use(SuiVue);

new Vue({
  render: h => h(App)
}).$mount("#app");
