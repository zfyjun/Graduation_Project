<template>
	<view>
		<u-toast ref="uToast1"></u-toast>
		<u-sticky>
			<view class="box" >
				<view style="padding: 2%;">
					<view style="display: flex">
						<u--text style="flex: 1;"  :prefixIcon="returnloans.icontype" iconStyle="font-size: 19px" bold :text="returnloans.type.name"></u--text>
					    <u--text style="flex:1" type="success" size="13" :text="'生效时间：'+returnloans.worktime"></u--text>
					</view>
					<u--text size="12" type='info' :text="'年利率：'+returnloans.type.rate+'%'"></u--text>
					<view style="display: flex;">
						<u--text type="error" :text="'欠款￥：'+returnloans.needreturncost" ></u--text>
						<u--text type="primary" :text="'剩余期限（月）：'+returnloans.lasttime" ></u--text>
					</view>
					<u--text size="13" type='info' :text="'已归还￥：'+(returnloans.cost-returnloans.needreturncost).toFixed(2)"></u--text>
					<u-divider ></u-divider>
					<view style="display: flex;">
						<u--text size="13" :text="'贷款金额：'+(returnloans.cost/10000).toFixed(2)+'万元'"></u--text>
						<u--text size="13" type='info' :text="'贷款期限：'+(returnloans.timelimit)+'月'"></u--text>
					</view>
				</view>
			</view>
			<view style="width: 90%;margin: 0 auto;">
				<view class="box">
					<view style="padding: 3%;">
						<view style="display: flex;">
							<u--text bold  prefixIcon="clock-fill" iconStyle="font-size: 19px"  size="14" style="flex: 3;" :text="'还款日期：'+nowpays.time" ></u--text>
							<u--text style="flex: 1;" type="primary" text="未逾期" v-if="nowpays.state==0" ></u--text>
							<u--text style="flex: 1;"  type="success" text="已还款" v-if="nowpays.state==1" ></u--text>
							<u--text style="flex: 1;"  type="error" text="已逾期未还款" v-if="nowpays.state==2" ></u--text>
							<u--text style="flex: 1;"  type="warning" text="逾期还款" v-if="nowpays.state==3" ></u--text>
						</view>
						<view>
							<u--text type="success" :text="'需还金额￥：'+nowpays.cost"></u--text>
							<u--text size="13" type="info" :text="'还款方式：'+returnloans.returnname" ></u--text>
						</view>
					</view>
				</view>
			</view>
			
		</u-sticky>
		<view style="width: 90%;margin: 0 auto;padding-top: 0%;" >	
		<u-empty v-if="historypays.length==0" mode="search" text="暂无历史数据" ></u-empty>
			<view v-for="( item , index ) in historypays" >
				<u-divider ></u-divider>
				<view >
					<u--text size="13" :text="'账单时间：'+item.time"></u--text>
					<u--text size="13" :text="'还款时间：'+item.returntime" v-if="item.returntime!=null"></u--text>
					<view style="display: flex;">
						<u--text style="flex: 1;" :text="'欠款金额￥：'+item.cost"></u--text>
						<u--text style="flex: 1;" type="success" :text="'状态：按时还款'" v-if="item.state==1"></u--text>
						<u--text style="flex: 1;" type="warning" :text="'状态：逾期还款'" v-if="item.state==3"></u--text>
						<u--text style="flex: 1;" type="error" :text="'状态：逾期未还款'" v-if="item.state==2"></u--text>
					</view>
				</view>
				<u-divider ></u-divider>
			</view>
		</view>
		<view style="bottom: 0;position: fixed;width: 100%;">
			<u-button type="success" text="还款" @click="opnereturnmoney" ></u-button>
		</view>
		<u-popup :show="returnflag" @close="close" >
			<u-toast ref="uToast"></u-toast>
		     <view style="padding-top: 5%;width: 90%;margin: 0 auto;">
		        <view>
		        		<u--form
		        				labelPosition="left"
		        				ref="uForm"
		        		>
		        			<u-form-item
							        labelWidth="30%"
		        					label="还款金额(元):"
		        					borderBottom
		        			>
		        				<u--input
								        disabled
		        						v-model="nowpays.cost"
		        						border="none"
		        				></u--input>
								<u-icon
										slot="right"
										name="rmb"
								></u-icon>
		        			</u-form-item>
		        			<u-form-item
							        labelWidth="25%"
		        					label="还款账户:"
		        					borderBottom
		        			>
		        				<u--input
		        						v-model="card.cardnumber"
		        						disabled
		        						disabledColor="#ffffff"
		        						border="none"
		        				></u--input>
		        				<u-icon
		        						slot="right"
		        						name="coupon-fill"
		        				></u-icon>
		        			</u-form-item>
							<u-form-item
							        labelWidth="30%"
									label="账户余额(元):"
									borderBottom
							>
								<u--input
										v-model="balance"
										disabled
										disabledColor="#ffffff"
										border="none"
								></u--input>
								<u-icon
								        @click="seebalance"
										slot="right"
										:name="seeicon"
								></u-icon>
							</u-form-item>
		        		</u--form>
		        	</view>
		     </view>
			 <view style="bottom: 0;position: fixed;width: 100%;">
			 	<u-button type="success" text="还款" @click="returnmoney" ></u-button>
			 </view>
		</u-popup>
		
	</view>
</template>

<script>
	export default {
		data() {
			return {
				returnloans:uni.getStorageSync('returnloans'),
				state:'未到归还时间',
				historypays:[],
				nowpays:{},
				returnflag:false,
				card:{},
				balance:'******',
				seeicon:'eye-fill',
				
			};
		},
		onBackPress() {
			uni.removeStorageSync('returnloans')
		},
		onLoad() {
			this.getpays()
		},
		methods:{
			getpays(){
				this.request({
					url:"/Lender/gethistorypays",
					method:"POST",
					data:{
						id:this.returnloans.id
					}
				}).then(res=>{
					if(res.code=='200'){
						this.nowpays=res.data[0]
						this.historypays=res.data.slice(1,res.length)
					}
				})
			},
			close(){//关闭遮罩层
				this.returnflag=false
			},
			opnereturnmoney(){//还款
				this.getcardone()
			},
			seebalance(){//查看余额
			    if(this.balance=='******'){
					this.balance=this.card.balance
					this.seeicon='eye-off'
				}
				else{
					this.balance='******'
					this.seeicon='eye-fill'
				}
				
			},
		    returnmoney(){//还款
				if(this.card.balance<this.returnloans.cost){
					this.$refs.uToast.show({
										type:'error',
										duration:'1500',
										message:"指定还款账户余额不足！",
									})
				}
				else{
					this.request({
						url:"/BankCard/returnloans",
						method:"POST",
						data:{
							lid:this.returnloans.id,
							rid:this.nowpays.id
						}
					}).then(res=>{
						if(res.code=='200'){
							this.returnflag=false
							this.$refs.uToast1.show({
												type:'success',
												duration:'2000',
												message:'还款成功',
											})
							this.getpays()
						}
						else if(res.code=='500'){
							this.$refs.uToast.show({
												type:'warning',
												duration:'2000',
												message:res.msg,
											})
						}
						else{
							this.$refs.uToast.show({
												type:'error',
												duration:'2000',
												message:res.msg,
											})
						}
					})
				}
			},
			getcardone(){
				this.request({
					url:"/BankCard/getCard",
					method:"POST",
					data:{
						cardid:this.returnloans.cid
					}
				}).then(res=>{
					if(res.code=='200'){
						this.card=res.data
						this.returnflag=true
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
