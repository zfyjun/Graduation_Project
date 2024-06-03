const BASE_URL = 'http://localhost:9090'
const headers = {
		    token: uni.getStorageSync('user') ? uni.getStorageSync('user').token : ''
}

export const request = (options => {
	if(options.url=="/file/uploadAudio"){
		headers['Content-Type'] = 'multipart/form-data';
	}
	return new Promise((resolve, reject) => {
		uni.request({
			url: BASE_URL + options.url,
			method: options.method || 'GET',
			header: headers,
			data: options.data || {},
			success: (res) => {
				const data = res.data
				resolve(data)
			},
			fail:(error)=>{
				uni.showToast({
					icon:'error',
					title:'系统错误'
				})
				reject(error)
			}
		})
	})
})