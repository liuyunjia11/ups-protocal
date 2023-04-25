import { createApp } from 'vue';
import App from './App.vue';
import router from './router';
import './assets/styles/main.css';

import Socket from "./socket";
const socket = new Socket("ws://localhost:8080");

const app = createApp(App);
app.provide('$socket', socket);
app.use(router).mount('#app');
