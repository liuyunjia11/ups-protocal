module.exports = {
    parserOptions: {
        parser: 'babel-eslint',
        sourceType: 'module',
        ecmaVersion: 2018,
        ecmaFeatures: {
            jsx: true
        }
    },
    env: {
        browser: true,
        es6: true,
        'node': true,
    },
    extends: [
        'plugin:vue/essential',
        'eslint:recommended'
    ],
    rules: {
        "vue/multi-word-component-names": "off",
    },
    plugins: [
        'vue'
    ]
};
