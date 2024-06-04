<template>
	<view style="padding: 2%;">
		<view class="box" >
			<view style="padding: 3%;">
				<u-toast ref="uToast"></u-toast>
				<u--form
					abelPosition="left"
					>
						<u-form-item
						        labelWidth="25%"
								label="真实姓名:"
								borderBottom
								
								ref="item1"
						>
							<u--input :disabled="disable1"  border="none" v-model="usermsg.user.name"></u--input>
						</u-form-item>
						<u-form-item
						        labelWidth="25%"
								label="身份证号:"
								
								borderBottom
								ref="item1"
						>
							<u--input :disabled="disable2"  border="none" v-model="usermsg.user.idcard" ></u--input>
						</u-form-item>
						<u-form-item
						        labelWidth="15%"
								label="年龄:"
								type="number"
								borderBottom
								ref="item1"
						>
							<u--input :disabled="disable3"  border="none" v-model="usermsg.userinfo.age"></u--input>
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
				<view>
					<u-button style="width: 95%;" type="primary" text="修改确认" @click="edit"></u-button>
				</view>
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
			    disable1:false,
				disable2:false,
				disable3:false,
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
				if(this.usermsg.user.idcard!=''){
					this.disable2=true
				}
				if(this.usermsg.user.name!=''){
					this.disable1=true
				}
				if(this.usermsg.userinfo.age!=''){
					this.disable3=true
				}
			},
			getuser(){//获取用户基本信息
				this.request({
					url:"/User/getUser",
					method:"POST",
					data:{
						uid:this.user.id,
					}
				}).then(res=>{
					if(res.code=='200'){
						this.user=res.data
						uni.setStorageSync("user",this.user)
						this.getusermsg()
					}
				})
			},
			sexSelect(e) {
				this.usermsg.userinfo.marital = e.name
			},
			edit(){
				if(this.usermsg.user.name==''||this.usermsg.user.idcard==''||this.usermsg.user.phone==''||this.usermsg.userinfo.age==''||this.usermsg.userinfo.job==''||this.usermsg.userinfo.marital==''||this.usermsg.userinfo.education==''){
					this.$refs.uToast.show({
										type:'warning',
										duration:'1500',
										message:"存在信息未填写完整",
									})
				}
				else{
					this.request({
						url:"/User/setUserinfoanuser",
						method:"POST",
						data:{
							user:JSON.stringify(this.usermsg.user),
							userinfo:JSON.stringify(this.usermsg.userinfo)
						}
					}).then(res=>{
						if(res.code=='200'){
							
							this.getuser()
							this.$refs.uToast.show({
												type:'success',
												duration:'1500',
												message:"信息修改成功",
											})
						}
						else{
							this.$refs.uToast.show({
												type:'error',
												duration:'1500',
												message:res.msg,
											})
						}
					})
				}
			}
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
