export const http = {
    baseURL: '/api/app',

    request(options) {
        const locale = uni.getStorageSync('app-locale') || 'zh-CN'
        return new Promise((resolve, reject) => {
            uni.request({
                url: this.baseURL + (options.url || ''),
                method: options.method || 'GET',
                data: options.data,
                header: {
                    'X-App-Locale': locale,
                    ...options.header
                },
                success: (res) => {
                    const data = res.data
                    if (data && data.code === 200) {
                        resolve(data.data)
                    } else {
                        reject(new Error(data?.message || '请求失败'))
                    }
                },
                fail: (err) => reject(err)
            })
        })
    },

    get(url, data) { return this.request({ url, method: 'GET', data }) },
    post(url, data) { return this.request({ url, method: 'POST', data }) },
    put(url, data) { return this.request({ url, method: 'PUT', data }) },
    delete(url) { return this.request({ url, method: 'DELETE' }) }
}
