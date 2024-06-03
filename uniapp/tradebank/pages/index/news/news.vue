<template>
	<view style="padding: 2%;">
			<view class="u-content">
				<u-parse :content="newcontect.content"></u-parse>
			</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				newid:uni.getStorageSync('newid'),
				newcontect:{}
			};
		},
		onLoad() {
			this.seclectnewbyid()
		},
		onBackPress() {
			uni.removeStorageSync('newid')
		},
		methods:{
			seclectnewbyid(){
				this.request({
						url:"/news/selectbyid",
						method:"POST",
						data:{
							id:this.newid
						}
					}).then(res=>{
						if(res.code==='200'){
							this.newcontect=res.data
						}
					})
			},
			
		},
		
	}
</script>

<style lang="scss">

</style>
