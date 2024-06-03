<template>
	<view>
		<view class="box" >
			<view >
				<u--text style="padding-left:  5%;padding-top: 3%;" bold text="贷款选择"></u--text>
				<u--form  style="padding-left: 5%;;width: 85%;"	labelPosition="left" :model="model1" :rules="rules" ref="uForm">
							<u-form-item
							        labelWidth="25%"
									label="贷款类型: "
									prop="lender.name"
									borderBottom
									@click="showTypechange(); hideKeyboard()"
									ref="item1"
							>
								<u--input
										v-model="model1.lender.name"
										disabled
										disabledColor="#ffffff"
										placeholder="请选择贷款类型"
										border="none"
								></u--input>
								<u-icon
										slot="right"
										name="arrow-right"
								></u-icon>
							</u-form-item>
							<u-form-item
							        v-if="model1.lender.id!=''"
							        labelWidth="30%"
									label="年利率(%):"
									borderBottom
									ref="item1"
							>
								<u--text type="error" :text="model1.lender.rate"></u--text>
							</u-form-item>
							<u-form-item
							        v-if="model1.lender.id!=''"
							        labelWidth="25%"
									label="贷款信息:"
									borderBottom
									ref="item1"
							>
								<u--text type="info" :text="model1.lender.description"></u--text>
							</u-form-item>
							<u-form-item
							        labelWidth="25%"
									label="付款账号:"
									borderBottom
									ref="item1"
							>
								<u--input
								        style="width: 50%;"
									    suffixIcon="coupon"
										v-model="carddetil.cardnumber"
										border="none"
										disabled="true"									
								></u--input>
							</u-form-item>
						</u--form>
						<u-action-sheet
								:show="showType"
								:actions="Lenders"
								title="请选择贷款类型"
								@close="showType = false"
								@select="TypeSelect"
						>
						</u-action-sheet>
			</view>
		</view>
		
		<view class="box"  style="margin-top: 3%;" >
			<view >
				<u--text style="padding-left:  5%;padding-top: 3%;" bold text="信息填写"></u--text>
				<u--form  style="padding-left: 5%;;width: 85%;"	labelPosition="left" :model="model1" :rules="rules" ref="uForm">
							<u-form-item
							        labelWidth="50%"
									label="贷款金额(单位万元): "
									prop="userInfo.cost"
									borderBottom
									
									ref="item1"
							>
								<u--input
								        type="number"
										v-model="model1.userInfo.cost"
										border="none"
								></u--input>
							</u-form-item>
				</u--form>
				<u--form  style="padding-left: 5%;;width: 85%;"	labelPosition="left" :model="model1" :rules="rules" ref="uForm">
							<u-form-item
							        labelWidth="45%"
									label="贷款期限(单位月): "
									prop="userInfo.timelimit"
									borderBottom
									
									ref="item1"
							>
								<u--input
								        type="number"
										v-model="model1.userInfo.timelimit"
										border="none"
								></u--input>
							</u-form-item>
							<u-form-item
							        labelWidth="25%"
									label="还款方式: "
									borderBottom
									@click="showreturn = true; hideKeyboard()"
									ref="item1"
							>
								<u--input
										v-model="model1.userInfo.returnname"
										disabled
										disabledColor="#ffffff"
										placeholder="请选择还款方式"
										border="none"
										color="rgb(232,110,48)"
								></u--input>
								<u-icon
										slot="right"
										name="arrow-right"
								></u-icon>
							</u-form-item>
				</u--form>		
						
				<u-action-sheet
						:show="showreturn"
						:actions="returnWays"
						title="请选择还款方式"
						@close="showreturn = false"
						@select="returnSelect"
				>
				</u-action-sheet>
			</view>
		</view>
		
		<view class="box"  style="margin-top: 3%;" >
			<view >
				<u--text style="padding-left:  5%;padding-top: 3%;" bold text="个人信息"></u--text>
				<u--form  style="padding-left: 5%;;width: 85%;"	labelPosition="left" :model="model1" :rules="rules" ref="uForm">
							<u-form-item
							        labelWidth="35%"
									label="月收入(税后): "
									prop="userInfo.salary"
									borderBottom
									ref="item1"
							>
								<u--input
								        type="number"
										v-model="model1.userInfo.salary"
										border="none"
								></u--input>
							</u-form-item>
							<u--text text="证明材料"></u--text>
							<u-upload
									:fileList="fileList1"
									@afterRead="afterRead"
									@delete="deletePic"
									name="1"
									multiple
									:maxCount="10"
									:previewFullImage="true"
								
							></u-upload>		
				</u--form>		
				
			</view>
		</view>
		<view>
			<u-button style="width: 95%;" type="success" :text="loanstext" @click="opensure"></u-button>
		</view>
		<u-toast ref="uToast"></u-toast>
		
		<view >
			<u-modal 
			  showCancelButton="true" 
			  :closeOnClickOverlay="true" 
			  :show="show" 
			  title="申请确认" 
			  :content="'您正使用账号：'+carddetil.cardnumber+'的银行卡申请办理贷款，贷款金额'+model1.userInfo.cost+'万元，请确认信息后继续'"
			  @cancel="show=false"
			  @confirm="confirm"
			  @close="show=false"
			  >
			</u-modal>
		</view>
		
		<view >
			<u-modal 
			  showCancelButton="true" 
			  :closeOnClickOverlay="true" 
			  :show="show2" 
			  title="身份验证" 
			  @cancel="show2=false"
			  @confirm="sendLoans"
			  @close="show2=false"
			  >
			  <u--form  style="padding-left: 5%;;width: 95%;"	labelPosition="left" ref="uForm">
			  			<u-form-item
			  			        labelWidth="30%"
			  					label="用户姓名: "
			  					borderBottom
			  					ref="item1"
			  			>
			  				<u--input
			  						v-model="model1.userInfo.name"
			  						border="none"
			  				></u--input>
			  			</u-form-item>
			  			<u-form-item
			  			        labelWidth="30%"
			  					label="账号密码: "
			  					borderBottom
			  					ref="item1"
			  			>
			  				<u--input
							        type="password"
			  						v-model="model1.userInfo.password"
			  						border="none"
			  				></u--input>
			  			</u-form-item>
			  </u--form>
			  <u-toast ref="uToast2"></u-toast>
			</u-modal>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				showType: false,
				showreturn:false,
				show:false,
				show2:false,
				Lenders:[],
				loanstext:'贷款申请',
				model1: {
					lender: {
						name: '',
						id:'',
						description:'',
						rate:'',
					},
					userInfo:{
						name:'',
						password:'',
						cost:'',
						timelimit:48,
						returnType:1,
						returnname:'等额本息',
						salary:''
					}
				},	
				returnWays:[{type:1,name:'等额本息'},{type:2,name:'等额本金'}],
				carddetil:uni.getStorageSync('bankdetail'),
				fileList1:[],
				myloans:{},
				type:0,//0是新增申请，1是编辑申请
				src:"",
				returnfiles:[]
			};
		},
		onLoad() {
			this.getLenders()
			this.check()
		},
		methods:{
			confirm(){//关闭面板
				this.show=false
				this.show2=true
			},
			TypeSelect(e){//选择贷款类型
				this.model1.lender=e
				
			},
			showTypechange(){
				if(this.type==0){
					this.showType=true
				}
				else{
					this.$refs.uToast.show({
										type:'warning',
										duration:'1500',
										message:"贷款申请已经提交过，无法更改贷款类型！",
									})
				}
			},
			check(){//检查是编辑还是新增
				let flag=0
				uni.getStorageInfo({
					success: function(res){
						if(res.keys.includes('myloans')){
							flag=1
						}
						else{
							console.log('没有')
						}
					}
				})
				if(flag==1){//是编辑
				this.myloans=uni.getStorageSync('myloans')
				    this.request({
				    	url:"/file/filesselectByIds",
				    	method:"POST",
				    	data:{
				    		ids:this.myloans.materials
				    	}
				    }).then(res=>{
				    	if(res.code=='200'){
							this.returnfiles=res.data
				    		this.gethistory()
				    	}
				    })
					
				}
			},
			gethistory(){//设置编辑的历史信息
				this.type=1
				this.model1.lender=this.myloans.type
				this.model1.userInfo.cost=this.myloans.cost
				this.model1.userInfo.salary=this.myloans.salary
				this.model1.userInfo.returnType=this.myloans.returnType
				this.model1.userInfo.timelimit=this.myloans.timelimit
				for(let i=0;i<this.returnfiles.length;i++){
					let fileson={id:this.returnfiles[i].id , message:'' , name:this.returnfiles[i].name , size:(this.returnfiles[i].size)*1024 , status:'success' , thumb:this.returnfiles[i].url , type:'image' , url:this.returnfiles[i].url}
					this.fileList1.push(fileson)
				}
				this.loanstext='申请修改'
			},
			sendLoans(){//提交贷款申请
				let user=uni.getStorageSync('user')
				console.log(this.fileList1)
				if(this.model1.userInfo.name==user.name&&this.model1.userInfo.password==user.password){
					let fileListids=[]
					for(let i=0;i<this.fileList1.length;i++){
						fileListids.push(Number(this.fileList1[i].id))
					}
					console.log(fileListids)
					this.request({
						url:"/UserLoans/sentLoans",
						method:"POST",
						data:{
							uid:Number(user.id),
						    cid:Number(this.carddetil.id),
							lid:Number(this.model1.lender.id),
							cost:this.model1.userInfo.cost,
							timelimit:this.model1.userInfo.timelimit,
							returnType:this.model1.userInfo.returnType,
							salary:this.model1.userInfo.salary,
							fileList:fileListids
						}
					}).then(res=>{
						if(res.code=='200'){
							this.Lenders=res.data
							this.$refs.uToast2.show({
												type:'success',
												duration:'1500',
												message:"提交申请成功！请耐心等待审核，页面即将返回到银行卡界面",
												complete() {
													uni.navigateBack({
															url: "pages/bankcards/bankcards"
													})
												}
											})
						}
						else{
							this.$refs.uToast2.show({
												type:'error',
												duration:'1500',
												message:res.msg,
											})
						}
					})
				}
				else{
					this.$refs.uToast2.show({
										type:'error',
										duration:'1500',
										message:"姓名或密码错误！",
									})
				    this.model1.userInfo.password=''
				}
			},
			returnSelect(e){//选择还款方式
				this.model1.userInfo.returnType=e.type
				this.model1.userInfo.returnname=e.name
			},
			
			deletePic(event) {//删除图片
				this.request({
					url:"/file/filesDeletById",
					method:"POST",
					data:{
						id:Number(event.file.id)
					}
				}).then(res=>{
					if(res.code=='200'){
						this[`fileList${event.name}`].splice(event.index, 1)
					}
				})
				
				
			},
			async afterRead(event) {// 新增图片
				// 当设置 multiple 为 true 时, file 为数组格式，否则为对象格式
				let lists = [].concat(event.file)
				let fileListLen = this[`fileList${event.name}`].length
				lists.map((item) => {
					this[`fileList${event.name}`].push({
						...item,
						status: 'uploading',
						message: '上传中'
					})
				})
				for (let i = 0; i < lists.length; i++) {
					const result = JSON.parse(await this.uploadFilePromise(lists[i].url))
					console.log(result)
					let item = this[`fileList${event.name}`][fileListLen]
					this[`fileList${event.name}`].splice(fileListLen, 1, Object.assign(item, {
						status: 'success',
						message: '',
						url: result.data.url,
						id:result.data.id
					}))
					fileListLen++
				}
			},
			uploadFilePromise(url) {//文件上传
				return new Promise((resolve, reject) => {
					let a = uni.uploadFile({
						url: 'http://localhost:9090/file/upload', // 仅为示例，非真实的接口地址
						filePath: url,
						name: 'file',
						formData: {
							user: 'test'
						},
						success: (res) => {
							setTimeout(() => {
								resolve(res.data)
								console.log("上传成功")
								console.log(this.fileList1)
							}, 1000)
						}
					});
				})			
			},
			getLenders(){//获取贷款类型
				this.request({
					url:"/Lender/getLenders",
					method:"POST",
					data:{
					}
				}).then(res=>{
					if(res.code=='200'){
						this.Lenders=res.data
					}
				})
			},
			opensure(){//确认贷款
				if(this.model1.lender.id==''){
					this.$refs.uToast.show({
										type:'warning',
										duration:'1500',
										message:"请选择贷款类型！",
									})
				}
				else{
					if(this.model1.userInfo.cost>0){
						if(this.model1.userInfo.timelimit>0){
							if(this.model1.userInfo.salary!=''){//&&this.fileList1.length>0
								this.show=true
							}
							else{
								this.$refs.uToast.show({
													type:'warning',
													duration:'1500',
													message:"个人信息必须填写完整，包括个人月收入和证明材料",
												})
							}
						}
						else{
							this.$refs.uToast.show({
												type:'warning',
												duration:'1500',
												message:"贷款期限必须大于1个月！",
											})
						}
					}
					else{
						this.$refs.uToast.show({
											type:'warning',
											duration:'1500',
											message:"贷款金额必须大于1万元！",
										})
					}
				}
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
