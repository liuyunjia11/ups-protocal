<template>
    <div>
        <div class="button-container">
            <button @click="goToLogin">Login</button>
            <button @click="goToRegister">Register</button>
            <button @click="goToPackageQuery">Track Package</button>
        </div>
    </div>
</template>

<style scoped>
.button-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20px;
}

button {
    font-size: 1.2rem;
    padding: 10px 20px;
    border-radius: 50px;
    border: 2px solid #555;
    background-color: #fff;
    cursor: pointer;
    transition: 0.3s;
}

button:hover {
    background-color: #555;
    color: #fff;
}
</style>

<script>
export default {
    methods: {
        goToLogin() {
            this.$router.push('/login');
        },
        goToRegister() {
            this.$router.push('/register');
        },
        goToPackageQuery() {
            this.$router.push('/package-query');
        }
    }
}
</script>
