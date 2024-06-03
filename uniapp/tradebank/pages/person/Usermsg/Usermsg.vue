<template>
	<view style="padding: 2%;">
		<view class="box" >
			<view style="padding: 3%;">
				<u--form
					abelPosition="left"
					>
						<u-form-item
						        labelWidth="25%"
								label="真实姓名:"
								borderBottom
								ref="item1"
						>
							<u--input  border="none" v-model="usermsg.user.name"></u--input>
						</u-form-item>
						<u-form-item
						        labelWidth="25%"
								label="身份证号:"
								borderBottom
								ref="item1"
						>
							<u--input  border="none" v-model="usermsg.user.idcard"></u--input>
						</u-form-item>
						<u-form-item
						        labelWidth="25%"
								label="电话号码:"
								borderBottom
								ref="item1"
						>
							<u--input  border="none" v-model="usermsg.user.phone"></u--input>
						</u-form-item>
						<u-form-item
						        labelWidth="15%"
								label="年龄:"
								borderBottom
								ref="item1"
						>
							<u--input  border="none" v-model="usermsg.userinfo.age"></u--input>
						</u-form-item>
						<u-form-item
						        labelWidth="15%"
								label="工作:"
								borderBottom
								ref="item1"
						>
							<u--input  border="none" v-model="usermsg.userinfo.job"></u--input>
						</u-form-item>
						<u-form-item
						        labelWidth="25%"
								label="婚姻状况:"
								borderBottom
								@click="showSex = true; hideKeyboard()"
								ref="item1"
							>
								<u--input
									v-model="usermsg.userinfo.marital"
									disabled
									disabledColor="#ffffff"
									placeholder="请选择婚姻状况"
									border="none"
								></u--input>
									<u-icon
										slot="right"
										name="arrow-right"
									></u-icon>
						</u-form-item>
						<u-form-item
						        labelWidth="25%"
								label="教育程度:"
								borderBottom
								ref="item1"
						>
							<u--input  border="none" v-model="usermsg.userinfo.education"></u--input>
						</u-form-item>
				</u--form>
			</view>
			<u-action-sheet
							:show="showSex"
							:actions="actions"
							title="请选择婚姻状况"
							@close="showSex = false"
							@select="sexSelect"
					>
			</u-action-sheet>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				user:uni.getStorageSync('user'),
				userinfo:{},
				showSex: false,
				usermsg: {
				   user:{},
				   userinfo:{}
				},
				actions: [{
						name: 'single',
						subname:"单身",
					},
					{
						name: 'divorced',
						subname:"已离异",
					},
					{
						name: 'married',
						subname:"已结婚",
					},
				],
			}
		},
		onLoad() {
			this.getusermsg()
		},
		methods:{
			getusermsg(){
				this.request({
					url:"/User/getUserinfo",
					method:"POST",
					data:{
						uid:this.user.id
					}
				}).then(res=>{
					if(res.code=='200'){
						this.userinfo=res.data	
						this.setmsg()
					}
				})
			},
			setmsg(){
				this.usermsg.user=this.user
				this.usermsg.userinfo=this.userinfo
			},
			sexSelect(e) {
				this.usermsg.userinfo.marital = e.name
					},
		}
	}
</script>

<style lang="scss">
.box{
	margin: 5% auto;
	width: 95%;
	background-color: #fff;
	border: 1px solid #ddd;
	border-radius: 5px;
	box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}
</style>
