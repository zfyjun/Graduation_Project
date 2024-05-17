<template>
	<view>
		<u-sticky>
			<view class="box">
				<view style="padding: 2%;">
					<view style="display: flex;">
						<u--text prefixIcon="star-fill" type="success" :text="'申请时间:   '+CreditCardupdate.passmsg[0].senttime"></u--text>
					</view>
					<view style="display: flex;">
						<view style="flex: 5;" >
							<u--text v-if="CreditCardupdate.updatemsg.type==2" text="申请类型:  信用卡升级"></u--text>
							<u--text v-if="CreditCardupdate.updatemsg.type==1" text="申请类型:  额度提升"></u--text>
						</view>
						<view style="display: flex;flex:3 ;">
							<u--text style="flex:4;" text="状态:"></u--text>
							<u--text style="flex:8;" :type="type" :text="CreditCardupdate.state"></u--text>
						</view>
					</view>
					<u-divider ></u-divider>
					<view>
						<u--text size="12" type="info" :text="'信用卡号：( '+CreditCardupdate.cardnumber+' )'"></u--text>
					</view>
					<view style="display: flex;">
						<view style="flex: 1;" v-if="CreditCardupdate.updatemsg.type==2" >
							<u--text :text="'申请等级：  '+CreditCardupdate.rankname"></u--text>
						</view>
						<view style="flex: 1;" >
							<u--text :text="'申请后额度￥：  '+CreditCardupdate.updatemsg.cost"></u--text>
						</view>
					</view>
				</view>
			</view>
		</u-sticky>
		<view style="margin-top: 2%;width: 90%;margin: 0 auto;padding-bottom: 20%;">
			<view class="box" v-for="(item,index) in CreditCardupdate.passmsg" >
				<view style="padding: 3%;">
					<view style="display: flex;" >
						<u--text style="margin-bottom: 3%;flex:2" :text="'历史状态：'" ></u--text>
						<u--text style="margin-bottom: 3%;flex:5" type="primary" text="审核中" v-if="item.state==0" ></u--text>
						<u--text style="margin-bottom: 3%;flex:5" type="error" text="未通过" v-if="item.state==1" ></u--text>
						<u--text style="margin-bottom: 3%;flex:5" type="success" text="已通过" v-if="item.state==2" ></u--text>
					</view>
					<u--text style="margin-bottom: 2%;"  type="warning" prefixIcon="clock-fill" :text="'提交时间:'+item.senttime"></u--text>
					<u--text style="margin-bottom: 2%;" type="success"  v-if="item.respondtime!=null" prefixIcon="clock-fill" :text="'反馈时间:'+item.respondtime"></u--text>
					<u-collapse style="width: 90%;margin: 0 auto;">
					    <u-collapse-item
					      title="信息反馈"
					    >
					      <u--text type="info" :text="item.description"></u--text>
					    </u-collapse-item>
					</u-collapse>
				</view>
			</view>
		</view>
		<view style="bottom: 0;position: fixed;width: 100%;">
			<u-button type="warning" text="申请修改" @click="toedit" ></u-button>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				CreditCardupdate:uni.getStorageSync('CreditCardupdate'),
				type:'primary',
			};
		},
		onLoad() {
			this.setType()
		},
		methods:{
			setType(){//设置状态颜色
				if(this.CreditCardupdate.updatemsg.state==0){
					this.type='primary'
				}
				else if(this.CreditCardupdate.updatemsg.state==1){
					this.type='error'
				}
				else if(this.CreditCardupdate.updatemsg.state==2){
					this.type='success'
				}
			},
			toedit(){//进入申请修改页面
				uni.navigateTo({
					url: "/pages/bankcards/updateCreditCard/updateCreditCard"
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
