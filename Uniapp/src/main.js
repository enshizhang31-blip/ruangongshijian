import { createSSRApp } from "vue";
import App from "./App.vue";
import i18nPlugin from "@/composables/useI18n.js";

export function createApp() {
	const app = createSSRApp(App);
	app.use(i18nPlugin);
	return { app };
}
