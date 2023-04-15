import { createRouter, createWebHistory } from 'vue-router';
import Home from '../views/Home.vue';
import Login from '../views/Login.vue';
import Register from '../views/Register.vue';
import PackageQuery from '../views/PackageQuery.vue';
import UserPackages from "../views/UserPackages.vue";

const routes = [
    {
        path: '/',
        name: 'Home',
        component: Home,
    },
    {
        path: '/login',
        name: 'Login',
        component: Login,
    },
    {
        path: '/register',
        name: 'Register',
        component: Register,
    },
    {
        path: '/package-query',
        name: 'PackageQuery',
        component: PackageQuery,
    },
    {
        path: "/user-packages",
        name: "UserPackages",
        component: UserPackages,
    }
];

const router = createRouter({
    history: createWebHistory(process.env.BASE_URL),
    routes,
});

export default router;
