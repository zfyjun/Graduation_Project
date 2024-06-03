<template>
	<view>
		<view class="box" >
			<u--text style="padding-left:  5%;padding-top: 3%;" bold text="转给"></u--text>
			<u--form style="padding-left: 5%;width: 85%;" labelPosition="left" :model="model1" ref="uForm">
						<u-form-item
						        labelWidth="20%"
								label="收款人:"
								prop="userInfo.name"
								borderBottom
								ref="item1"
						>
							<u--input
							        style="width: 50%;"
								    suffixIcon="account"
									v-model="model1.userInfo.name"
									border="none"
							></u--input>
						</u-form-item>
						<u-form-item
						        labelWidth="25%"
								label="收款账号:"
								prop="userInfo.cardnumber"
								borderBottom
								ref="item1"
						>
							<u--input
							        type="number"
							        style="width: 50%;"
								    suffixIcon="coupon"
									v-model="model1.userInfo.cardnumber"
									border="none"
							></u--input>
						</u-form-item>
			</u--form>
		</view>
		
		<view class="box" style="margin-top: 3%;" >
			<view style="padding-top:3%; display: flex;" >
				<u--text style="padding-left:  5%;flex: 1;" bold text="转账金额"></u--text>
				<u--text style="flex: 5;" size="12" type="info" :text="'（余额：'+carddetil.balance+'）'"></u--text>
			</view>
			<u--form style="padding-left: 5%;width: 85%;" labelPosition="left" :model="model2" ref="uForm">
						<u-form-item						
								prop="userInfo.name"
								
								borderBottom
								ref="item1"
						>
							<u--input
							        placeholder="请输入转账金额"
							        style="width: 80%;"
								    prefixIcon="rmb"
									type="number"
									v-model="model2.userInfo.cost"
									border="none"
							></u--input>
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
		</view>
		
		<view class="box" style="margin-top: 10%;" >
			<u--text style="padding-left:  5%;padding-top:3%;" bold text="转账用途"></u--text>
			<u--form style="padding-left: 5%;width: 85%;" labelPosition="left" :model="model2" ref="uForm">
						<u-form-item						
								borderBottom
								ref="item1"
						>
							<u--input
							        placeholder="请输入转账用途(选填)"
							        style="width: 80%;"
									v-model="model2.userInfo.describe"
									border="none"
							></u--input>
						</u-form-item>
			</u--form>
		</view>
		<view>
			<u-button style="width: 95%;" type="primary" text="确定" @click="opensure"></u-button>
		</view>
		<view style="width: 95%;margin: 5% auto;">
			<u--text size="12" text="温馨提示"></u--text>
			<u--text  size="12" type="info" text="请勿轻易向陌生人转账,请勿相信以兼职投资为借口或假冒客服公检法熟人等骗局.请认真核实收款方信息,谨防电信诈骗,切勿上当受骗,如有疑问请电话咨询"></u--text>
		</view>
		
		<u-toast ref="uToast"></u-toast>
		
		<view >
			<u-modal 
			  showCancelButton="true" 
			  :closeOnClickOverlay="true" 
			  :show="show" 
			  title="转账确认" 
			  @cancel="cancel"
			  @confirm="confirm"
			  @close="show=false"
			  >
			  <view>
				  <view>
					  <u--text :text="'您正在向账号：'+model1.userInfo.cardnumber+'转账，金额为：'+model2.userInfo.cost+'元，是否确认？'"></u--text>
				  </view>
				  <view>
					    <u--input v-model="password" placeholder="请输入银行卡密码" type="password" ></u--input>
				  </view>
			  </view>
			</u-modal>
		</view>
		<view>
			<u-modal :show="showflag1"  title="警告提示" @close="showflag1=false"  :closeOnClickOverlay="true"  showCancelButto="true" @confirm="tureconfirm" @cancel="showflag1=false" >
						<view style="padding: 2%;" class="slot-content">
							<u--text type="warning" :text="'您在24小时内已向账号：'+model1.userInfo.cardnumber+'账户转账次数达到'+tsum+'次，是否确认继续转账？'"></u--text>
						</view>
			</u-modal>
		</view>
		<view>
			<u-popup mode="center" :show="showflag" @close="showflag=false" closeable="true">
		        <view style="padding: 5%;">
					<u-toast ref="uToast33"></u-toast>
		            <u--text type="warning" :text="message"></u--text>
		        </view>
				
			</u-popup>
		</view>
		
	</view>
</template>

<script>
	export default {
		data() {
			return {
				carddetil:uni.getStorageSync('bankdetail'),
				user:uni.getStorageSync('user'),
				model1: {
								userInfo: {
									name: '',
									cardnumber: '',
								},
							},
				model2: {
								userInfo: {
									cost: '',
									describe: '',
								},
							},
				show:false,
				password:'',
				tsum:0,
				showflag1:false,
				showflag:false       
			};
		},
		onLoad() {//获取转账次数
			
		},
		methods:{
			opensure(){
				if(this.model1.userInfo.name!=''&&this.model1.userInfo.cardnumber!=''&&this.model2.userInfo.cost!=''){
					if(this.model1.userInfo.cardnumber==this.carddetil.cardnumber){
						this.$refs.uToast.show({
											type:'warning',
											duration:'1500',
											message:"转账账户不能为本账户",
										})
					}
					else if(this.model1.userInfo.cardnumber.length<19){
						this.$refs.uToast.show({
											type:'warning',
											duration:'1500',
											message:"收款账户不能为信用卡账户",
										})
					}
					else{
						if(this.model2.userInfo.cost>this.carddetil.balance){
							this.$refs.uToast.show({
												type:'warning',
												duration:'1500',
												message:"转账金额不能大于该卡的剩余金额",
											})
						}
						else if(this.model2.userInfo.cost<=0){
							this.$refs.uToast.show({
												type:'warning',
												duration:'1500',
												message:"转账金额必须大于0",
											})
						}
						else{
							this.show=true
						}
					}
				}
				else{
					this.$refs.uToast.show({
										type:'warning',
										duration:'1500',
										message:"您有信息尚未填写完整！请继续填写对应信息",
									})
				}
			},
			confirm(){//确认转账
			    this.gettimes()
				if(this.password==this.user.password){//密码正确
					if(this.gettransfertimes()>=5){//次数够了
					    this.show=false
						this.showflag1=true
					}
					else {//次数还不够
						this.tureconfirm()
					}
				}
				else{
					this.password=''
					this.$refs.uToast.show({
										type:'error',
										duration:'2000',
										message:'密码错误',
									})
				}
			},
			tureconfirm(){//真实确认转账
				this.request({
					url:"/Pay/transfer",
					method:"POST",
					data:{
						payname:this.model1.userInfo.name,
						paycard:this.model1.userInfo.cardnumber,
						cardid:this.carddetil.id,
						cost:this.model2.userInfo.cost,
						describe:this.model2.userInfo.describe
					}
				}).then(res=>{
					this.show=false
					this.showflag1=false
					if(res.code==='200'){
						this.model2.userInfo.cost=''
						this.password=''
						this.getbankcardone()
						//日流水安全
						this.testsave()
					}
					else{
						this.$refs.uToast.show({
											type:'error',
											duration:'2000',
											message:res.msg,
										})
					}
				})
			},
			gettimes(){//获取转账次数
				this.request({
					url:"/BankCardSave/getttime",
					method:"POST",
					data:{
						payname:this.model1.userInfo.name,
						paycard:this.model1.userInfo.cardnumber,
						cardid:this.carddetil.id,
					}
				}).then(res=>{
					if(res.code==='200'){
						this.tsum=res.data
					}
				})
			},
			testsave(){//日流水检擦
				this.request({
					url:"/BankCardSave/costsave",
					method:"POST",
					data:{
						cid:this.carddetil.id,
					}
				}).then(res=>{
					if(res.code==='200'){
						this.message="日流水达到"+(res.data).toFixed(2)+"元，已经超过安全阈值2万元，操作记录将会被计入异常操作中"
						this.showflag=true
						this.$refs.uToast33.show({
											type:'success',
											duration:'1500',
											message:"转账成功",
										})
					}
					else{
						this.$refs.uToast.show({
											type:'success',
											duration:'1500',
											message:"转账成功！",
										})
					}
					
				})
			},
			cancel(){
				this.show=false
			},
			gettransfertimes(){//获取转账次数
				this.request({
					url:"/BankCardSave/transfertest",
					method:"POST",
					data:{
						payname:this.model1.userInfo.name,
						paycard:this.model1.userInfo.cardnumber,
						cardid:this.carddetil.id,
					}
				}).then(res=>{
					if(res.code==='200'){
						this.tsum=res.data
						return this.tsum
					}
				})
				return this.tsum
			},
			getbankcardone(){
				this.request({
					url:"/BankCard/getCard",
					method:"POST",
					data:{
						cardid:this.carddetil.id,
					}
				}).then(res=>{
					if(res.code==='200'){
						uni.setStorageSync('bankdetail',res.data)
						this.carddetil=res.data
					}
					else{
						this.$refs.uToast.show({
											type:'warning',
											duration:'2000',
											message:res.msg,
										})
					}
				})
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
