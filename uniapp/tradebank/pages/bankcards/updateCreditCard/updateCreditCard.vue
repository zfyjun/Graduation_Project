<template>
	<view>
		<view class="box" >
			<view >
				<view style="display: flex;padding-left:  5%;padding-top: 3%;">
					<u-icon name="integral-fill" color="rgba(255,231,41)" size="28"></u-icon>
					<u--text  v-if="CreditCardupdate==''" style=" " bold :text="'信用卡等级:'+rang"></u--text>
					<u--text v-if="CreditCardupdate!=''" bold :text="'申请信用卡等级:'+rang"></u--text>
				</view>
				<u--form style="padding-left: 5%;width: 85%;" labelPosition="left" ref="uForm">
							<u-form-item
							        labelWidth="30%"
									label="信用卡卡号:"
									borderBottom
									ref="item1"
							>
								<u--text :text="carddetil.cardnumber"></u--text>
							</u-form-item>
							<u-form-item
							        labelWidth="35%"
									label="信用卡额度:"
									borderBottom
									ref="item1"
							>
								<u--text :text="CreditCard.limits+'元'"></u--text>
							</u-form-item>
				</u--form>
			</view>
		</view>
		<view>
			<u-button :disabled="types1" style="width: 95%;" type="success" text=" 额度提升" @click="increase"></u-button>
		</view>
		<view class="box" >
			<view >
				<u-collapse
				    :value="['1']"
				  >
				    <u-collapse-item
				      title="信用卡升级规则:"
					  name="1"
				    >
				      <text style="color: rgba(164,165,167);" class="u-collapse-content"> 信用卡升级需要持卡人按时还款，没有或少有欠款的记录，提交信用卡升级申请后等待银行审核通过即可开始使用。</text>
				      <u--text style="margin-top: 2%;" text="普通卡额度范围(元) : 1000--5000"></u--text>
					  <u--text style="margin-top: 2%;" text="金卡额度范围(元) : 5000--20000"></u--text>
					  <u--text style="margin-top: 2%;" text="白金卡额度范围(元) : 20000--50000"></u--text>
					  <u--text style="margin-top: 2%;" text="钻石卡额度范围(元) : 50000--200000"></u--text>
					  <u--text style="margin-top: 2%;" text="黑卡额度范围(元) : 200000--500000"></u--text>
					</u-collapse-item>
				  </u-collapse>
			</view>
		</view>
		<view>
			<u-button :disabled="types2" style="width: 95%;" type="primary" text=" 信用卡升级" @click="update"></u-button>
		</view>
		
		<view >
			<u-modal @close="cancel" @cancel="cancel" @confirm="confirm(1)" :show="show1" :title="'额度提升'" :closeOnClickOverlay="true" showCancelButton="true" >
				<view style="display: inline-block;width: 110%;">
					<view style="display: flex;">
						<u--text :text="'当前额度:'+CreditCard.limits+'元'"></u--text>
						<u--text :text="'额度上限:'+limits+'元'"></u--text>
					</view>
					<view style="margin-top: 2%;">
						<u--input placeholder="请输入额度" v-model="cost" type="number"></u--input>
					</view>
				</view>
				 <u-toast ref="uToast2"></u-toast>
			</u-modal>
		</view>
		
		<view >
			<u-modal @close="cancel" @cancel="cancel" @confirm="confirm(2)" :show="show2" :title="'银行卡升级'" :closeOnClickOverlay="true" showCancelButton="true" >
				<view style="display: inline-block;width: 110%;">
					<div style="display: flex;">
						<u--text text="申请等级:"></u--text>
						<u--input
							v-model="sentrank.rankanme"
							disabled
							disabledColor="#ffffff"
							placeholder="请选择等级"
							border="none"
							@click="showSex=true"
						></u--input>
						<u-icon
							slot="right"
							name="arrow-right"
							@click="showSex=true"
						></u-icon>
					</div>
					<u-divider ></u-divider>
					<view v-if="rank!=sentrank.rank" style="display: flex;">
						<u--text :text="'最低额度:'+showlimits.low+'元'"></u--text>
						<u--text :text="'额度上限:'+showlimits.high+'元'"></u--text>
					</view>
					<view style="margin-top: 2%;">
						<u--input placeholder="请输入额度(可填,默认最低额度)" v-model="cost" type="number"></u--input>
					</view>
				</view>
				 <u-toast ref="uToast2"></u-toast>
				 <u-action-sheet
				 				:show="showSex"
				 				:actions="ranks"
				 				title="请选择申请的信用卡等级"
				 				@close="showSex = false"
				 				@select="sexSelect"
				 		>
				</u-action-sheet>
			</u-modal>
			 <u-toast ref="uToast "></u-toast>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				carddetil:uni.getStorageSync('bankdetail'),
				user:uni.getStorageSync('user'),
				CreditCard:{},
				rang:'',
				rank:0,
				show1:false,
				show2:false,
				showSex:false,
				limits:10000,
				cost:'',
				sentrank:{rankanme:'',rank:0},
				showlimits:{low:0,high:0},
				ranks:[{rank:1,name:'普通卡',disabled:false},{rank:2,name:'金卡',disabled:false},{rank:3,name:'白金卡',disabled:false},{rank:4,name:'钻石卡',disabled:false},{rank:5,name:'黑金卡',disabled:false}],
				types2:false,
				types1:false,
				CreditCardupdate:uni.getStorageSync('CreditCardupdate')
			
			};
		},
		onLoad() {
			this.getcreditcard()
			this.editsure()
		},
		methods:{
			update(){//升级界面打开
				this.show2=true
				this.cost=''
			},
			increase(){//额度提升界面打开
				this.show1=true
				
			},
			editsure(){//设定编辑板块
				if(this.CreditCardupdate!=''){//进入编辑界面
					if(this.CreditCardupdate.updatemsg.type==1){//提升额度
						this.types2=true
					}
					else{//等级提升
						this.types1=true
					}
				}
			},
			confirm(type){//提交验证
			    if(type==1){//额度
					if(this.cost<=this.CreditCard.limits){
						this.$refs.uToast2.show({
											type:'error',
											duration:'1500',
											message:'申请额度必须大于当前额度！',
										})
					}
					else if(this.cost>this.limits){
						this.$refs.uToast2.show({
											type:'error',
											duration:'1500',
											message:'申请额度必须小于当前信用卡等级的最大额度：'+this.limits+'元',
										})
					}
					else{
						if(this.CreditCardupdate!=null){
							this.resentmsg()
						}
						else{
							this.sentmsg(type)
						}
					}
				}
				else if(type==2){//升级
					if(this.rank==this.sentrank.rank){
						this.$refs.uToast2.show({
											type:'error',
											duration:'1500',
											message:'申请等级必须大于当前信用卡等级',
										})
					}
					else{
						if(this.cost<this.showlimits.low||this.cost>this.showlimits.high){
							this.$refs.uToast2.show({
												type:'error',
												duration:'1500',
												message:'信用卡额度必须在该等级限制额度之间',
											})
						}
						else{
							if(this.CreditCardupdate!=null){
								this.resentmsg()
							}
							else{
								this.sentmsg(type)
							}
						}
					}
					
				}
			},
			resentmsg(){//重新提交
				this.request({
					url:"/UserUpdate/resent",
					method:"POST",
					data:{
						id:this.CreditCardupdate.updatemsg.id,
						cost:this.cost,
						rank:this.sentrank.rank
					}
				}).then(res=>{
					if(res.code=="200"){
						this.$refs.uToast2.show({
											type:'success',
											duration:'1500',
											message:'申请成功！请耐心等待审核，即将跳转至银行卡审核信息界面',
											complete() {
												uni.removeStorageSync("CreditCardupdate")
												uni.reLaunch({
														url: "/pages/person/person"
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
			},
			sentmsg(type){//提交申请
				this.request({
					url:"/UserUpdate/sentupdate",
					method:"POST",
					data:{
						uid:this.user.id,
						cid:this.carddetil.id,
						type:type,
						cost:this.cost,
						rank:this.sentrank.rank
					}
				}).then(res=>{
					if(res.code=="200"){
						this.$refs.uToast2.show({
											type:'success',
											duration:'1500',
											message:'申请成功！请耐心等待审核，即将跳转至银行卡界面',
											complete() {
												uni.navigateBack({
														url: "/pages/bankcards/bankcards"
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
			},
			cancel(){
				this.show1=false
				this.show2=false
				this.cost=''
			},
			sexSelect(e) {
				this.sentrank.rankanme = e.name
				this.sentrank.rank=e.rank
				if(e.rank==2){
					this.showlimits.high=20000
					this.showlimits.low=5000
				}
				else if(e.rank==3){
					this.showlimits.high=50000
					this.showlimits.low=20000
				}
				else if(e.rank==4){
					this.showlimits.high=200000
					this.showlimits.low=50000
				}
				else if(e.rank==5){
					this.showlimits.high=500000
					this.showlimits.low=200000
				}
				this.cost=this.showlimits.low
			},
			getcreditcard(){
				let cid=0
				if(this.CreditCardupdate!=''){
					cid=this.CreditCardupdate.updatemsg.cid
				}
				else{
					cid=this.carddetil.id
				}
				this.request({
					url:"/CreditCard/getCreditCard",
					method:"POST",
					data:{
						cid:cid
					}
				}).then(res=>{
					if(res.code=='200'){
						this.CreditCard=res.data
						this.rank=this.CreditCard.rank
						this.sentrank.rank=this.rank
						if(this.CreditCard.rank==1){
							this.limits=5000
							this.rang=this.ranks[this.CreditCard.rank-1].name
						}
						else if(this.CreditCard.rank==2){
							this.limits=20000
							this.rang=this.ranks[this.CreditCard.rank-1].name
						}
						else if(this.CreditCard.rank==3){
							this.limits=50000
							this.rang=this.ranks[this.CreditCard.rank-1].name
						}
						else if(this.CreditCard.rank==4){
							this.limits=200000
							this.rang=this.ranks[this.CreditCard.rank-1].name
						}
						else if(this.CreditCard.rank==5){
							this.limits=500000
							this.rang=this.ranks[this.CreditCard.rank-1].name
						}
						this.sentrank.rankanme=this.rang
						for(let i=0;i<this.ranks.length;i++){
							if(this.ranks[i].rank<=this.rank){
								this.ranks[i].disabled=true
							}
							else if(this.ranks[i].rank>this.rank+2){
								this.ranks[i].disabled=true
							}
						}
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
