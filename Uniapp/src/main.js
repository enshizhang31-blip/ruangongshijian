import { createSSRApp } from "vue";
import App from "./App.vue";
import i18nPlugin from "@/composables/useI18n.js";
import requireLoginMixin from "@/mixins/requireLogin.js";

export function createApp() {
	const app = createSSRApp(App);
	app.use(i18nPlugin);
	// 全局 mixin：受保护页面在 onShow 时检查登录态。
	// 允许页面用 __requireLogin: false 关闭。
	app.mixin(requireLoginMixin);
	return { app };
}
