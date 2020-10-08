module.exports = {
    devServer: {
        disableHostCheck: true,
        proxy: {
            '^/api': {
                target: 'http://localhost:8081',
                ws: true,
                changeOrigin: true,
                pathRewrite: {
                    '^/api': '/', // rewrite path
                    '^/api/remove/path': '/path', // remove base path
                },
            },
            '^/foo': {
                target: '<other_url>'
            }
        }
    }
}
