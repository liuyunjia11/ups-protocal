<template>
    <div class="container">
        <h2>Your Packages</h2>
        <table class="packages-table">
            <thead>
            <tr>
                <th>ID</th>
                <th>Item</th>
                <th>Quantity</th>
                <th>Status</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(pkg, index) in packages" :key="index">
                <td>{{ pkg.id }}</td>
                <td>{{ pkg.item }}</td>
                <td>{{ pkg.quantity }}</td>
                <td>{{ pkg.status }}</td>
            </tr>
            </tbody>
        </table>
    </div>
</template>

<script>
export default {
    data() {
        return {
            packages: [] // 这里我们将存储从后端获取的包裹信息
        };
    },
    created() {
        // 假设这是一个从后端获取包裹信息的函数
        this.fetchPackages();
    },
    methods: {
        fetchPackages() {
            // 在这里调用API以获取包裹信息，并将其存储在packages数据属性中
            // 以下为示例数据
            this.packages = [
                {
                    id: 1,
                    item: "Laptop",
                    quantity: 2,
                    status: "Delivered"
                },
                {
                    id: 2,
                    item: "Phone",
                    quantity: 1,
                    status: "In Transit"
                }
            ];
        }
    }
};
</script>

<style scoped>
.container {
    max-width: 800px;
    margin: 0 auto;
    padding: 2rem;
    background-color: white;
    border-radius: 1rem;
}

h2 {
    text-align: center;
    margin-bottom: 1.5rem;
}

.packages-table {
    width: 100%;
    border-collapse: collapse;
}

.packages-table th,
.packages-table td {
    border: 1px solid #ccc;
    padding: 0.5rem;
    text-align: left;
}

.packages-table th {
    background-color: #f2f2f2;
    font-weight: bold;
}
</style>
