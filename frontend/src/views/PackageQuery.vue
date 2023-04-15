<template>
    <div class="container">
        <div class="form-container">
            <h2>Please type your PackageID to start a query</h2>
            <form @submit.prevent="submitPackageQuery">
                <div class="form-group">
                    <label class="label" for="packageId">Package ID:</label>
                    <input class="input" type="text" id="packageId" v-model="packageId">
                </div>
                <button class="submit-btn" type="submit">Search</button>
            </form>
        </div>
    </div>
</template>

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


<script>
export default {
    data() {
        return {
            packageId: ''
        }
    },
    methods: {
        submitPackageQuery() {
            // TODO: Implement package search API call
            console.log('Package ID:', this.packageId);
        }
    }
}
</script>
