<template>
    <div class="container">
        <div class="form-container">
            <h2>Register</h2>
            <form @submit.prevent="submitRegistration">
                <div class="form-group">
                    <label class="label" for="username">Username:</label>
                    <input class="input" type="text" id="username" v-model="username">
                </div>
                <div class="form-group">
                    <label class="label" for="password">Password:</label>
                    <input class="input" type="password" id="password" v-model="password">
                </div>
                <button class="submit-btn" type="submit">Register</button>
            </form>
        </div>
    </div>
</template>


<script>
import { Register_UserController, UserController_Register } from "@/user_ups_pb";


export default {
    data() {
        return {
            username: "",
            password: "",
        };
    },
    methods: {
        async submitRegistration() {
            console.log("submitRegistration called");

            const registerRequest = new Register_UserController();
            registerRequest.setUpsuserid(this.username);
            registerRequest.setUpspassword(this.password);

            await this.$socket.connect();
            this.$socket.send("register", registerRequest);

            this.$socket.onMessage((data) => {
                console.log("Received response from server");
                if (data.action === "registerResponse") {
                    const registerResponse = UserController_Register.deserializeBinary(
                        new Uint8Array(data.payload)
                    );
                    const ack = registerResponse.getAcks();

                    if (ack === "success") {
                        console.log("Registration successful, navigating to login");
                        this.$router.push("/login");
                    } else {
                        alert("Registration failed, please try again");
                    }
                    this.$socket.close();
                }
            });
        },

    },
};
</script>


<style scoped>
.container {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 100vh;
    background: linear-gradient(135deg, #1e5799, #7db9e8);
}

.form-container {
    background-color: rgba(255, 255, 255, 0.8);
    padding: 2rem;
    border-radius: 1rem;
    width: 400px;
}

.form-group {
    display: flex;
    flex-direction: column;
    margin-bottom: 1.5rem;
}

.label {
    font-weight: bold;
    margin-bottom: 0.5rem;
}

.input {
    padding: 0.5rem;
    font-size: 1rem;
    border: 1px solid #ccc;
    border-radius: 5px;
}

.submit-btn {
    background-color: #1e5799;
    color: white;
    padding: 0.5rem;
    font-size: 1rem;
    border: none;
    border-radius: 5px;
    cursor: pointer;
}

.submit-btn:hover {
    background-color: #7db9e8;
}

</style>