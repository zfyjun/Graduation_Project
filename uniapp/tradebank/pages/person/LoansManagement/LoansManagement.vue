<template>
	<view>
		<u-toast ref="uToast"></u-toast>
		<view>
			<u-subsection :list="list" :current="current" @change="sectionChange"></u-subsection>
		</view>
		<!-- 生效贷款 -->
		<view v-if="current==0">
			<u-empty v-if="workloans.length==0" style="padding-top: 20%;" mode="search" text="暂无生效贷款" >
			</u-empty>
			<view class="box" v-for="(item,index) in workloans" @click="intoreturn(item)" >
				<view style="padding: 2%;">
					<view style="display: flex">
						<u--text style="flex: 1;"  :prefixIcon="item.icontype" iconStyle="font-size: 19px" bold :text="item.type.name"></u--text>
						<u--text style="flex:1" type="success" size="13" :text="'生效时间：'+item.worktime"></u--text>
					</view>
					<u--text size="12" type='info' :text="'年利率：'+item.type.rate+'%'"></u--text>
					<view style="display: flex;">
						<u--text type="error" :text="'欠款￥：'+item.needreturncost" ></u--text>
						<u--text type="primary" :text="'剩余期限（月）：'+item.lasttime" ></u--text>
					</view>
					<u--text size="13" type='info' :text="'已归还￥：'+(item.cost-item.needreturncost).toFixed(2)"></u--text>
					<u-divider ></u-divider>
					<view style="display: flex;">
						<u--text size="13" :text="'贷款金额：'+(item.cost/10000).toFixed(2)+'万元'"></u--text>
						<u--text size="13" type='info' :text="'贷款期限：'+(item.timelimit)+'月'"></u--text>
					</view>
				</view>
			</view>
		</view>
	    
	    <view v-if="current==1">
			<u-empty v-if="historyloans.length==0" style="padding-top: 20%;" mode="search" text="暂无历史贷款" >
			</u-empty>
		</view>
		<!-- 贷款审核历史 -->
		<view v-if="current==2">
			<u-empty v-if="loanstable.length==0" style="padding-top: 20%;" mode="search" text="暂无贷款申请记录" >
			</u-empty>
			<u-loading-icon :show="showloading" style="padding-top: 30%;"></u-loading-icon>
			<view class="box" v-for="(item,index) in loanstable">
				<view style="padding: 2%;">
					<view style="display: flex">
						<u--text style="flex: 3;"  :prefixIcon="item.icontype" iconStyle="font-size: 19px" bold :text="item.type.name"></u--text>
					    <u--text style="padding-left:  40%;flex: 2;" color="rgba(1,167,112)" text="修改" @click="toloans(item)" suffixIcon="search" iconStyle="font-size: 18px" ></u--text>
						<u--text style="flex: 2;"  color="rgba(48,125,239)" text="历史" @click="history(item)" suffixIcon="search" iconStyle="font-size: 18px" ></u--text>
					</view>
					<u--text :type="item.textType" :text="'审核状态：'+ item.state" ></u--text>
					<u--text size="12" type='info' :text="'年利率：'+item.type.rate+'%'"></u--text>
					<u-divider ></u-divider>
					<view style="display: flex;">
						<u--text size="13" :text="'贷款金额：'+(item.cost/10000).toFixed(2)+'万元'"></u--text>
						<u--text size="13" type='info' :text="'贷款期限：'+(item.timelimit)+'月'"></u--text>
					</view>
				</view>
			</view>
		</view>
		
		<u-popup :show="showhistory" customStyle="width:80%" mode="center" :round="8" @close="close" @open="open">
		    <view style="padding: 2%;">
		    	<view style="width: 95%;margin: 0 auto;" v-for="( item , index ) in historys">
					<u-divider ></u-divider>
					<u--text :text="'申请时间：'+item.senttime"></u--text>
					<u--text v-if="item.state!=0" :text="'回应时间：'+item.respondtime"></u--text>
					<u--text type="primary" v-if="item.state==0" text="申请状态：审核中"></u--text>
					<u--text type="error" v-if="item.state==1" text="申请状态：审核未通过"></u--text>
					<u--text type="success" v-if="item.state==2" text="申请状态：审核通过"></u--text>
					<u--text v-if="item.state!=0" :text="'详情描述：'+item.description"></u--text>
					<u-divider ></u-divider>
				</view>			 
		    </view>     
		</u-popup>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				list: ['生效贷款', '历史贷款','贷款审核'],
				current: 0,
				user:uni.getStorageSync('user'),
				loanstable:[],//用户贷款申请列表
				workloans:[],//生效中贷款
				historyloans:[],//历史贷款
				loanstype:[],
				showloading:false,
				historys:[],//单卡历史记录
				showhistory:false,
			}
		},
		onLoad(){
			this.getworkloans()
			this.getloanstype()
		},
		methods: {
			sectionChange(index) {
				this.current = index;
				if(index==0){
					this.getworkloans()
				}
				if(index==2){
					this.getLoansshenqing()
				}
			},
			toloans(e){//跳转至贷款申请界面
			    if(e.ispass!=2){
					uni.setStorageSync('myloans',e)
					uni.navigateTo({
						url:"/pages/bankcards/loans/loans"
					})
				}
				else{
					this.$refs.uToast.show({
										type:'warning',
										duration:'1000',
										message:"该贷款已通过，无法修改信息",
									})
				}
			},
			getloanstype(){//获取贷款类型
				this.request({
					url:"/Lender/getLenders",
					method:"POST",
					data:{
					}
				}).then(res=>{
					if(res.code=='200'){
						this.loanstype=res.data
					}
				})
			},
			close(){
				this.showhistory=false
			},
			getworkloans(){//获取生效的贷款
				this.request({
					url:"/Lender/getworkloansbyid",
					method:"POST",
					data:{
						id:this.user.id
					}
				}).then(res=>{
					if(res.code=='200'){
						this.workloans=res.data
						for(let i=0;i<this.workloans.length;i++){
							for(let j=0;j<this.loanstype.length;j++){
								if(this.workloans[i].lid==this.loanstype[j].id){
									this.workloans[i].type=this.loanstype[j]
									if(this.loanstype[j].id==1){//住房贷款
										this.workloans[i].icontype='home-fill'
									}
									else if(this.loanstype[j].id==2){//商业贷款
										this.workloans[i].icontype='rmb-circle-fill'
									}
									else if(this.loanstype[j].id==3){//消费贷款
										this.workloans[i].icontype='shopping-cart-fill'
									}
									break
								}
							}
							if(this.workloans[i].returntype==1){
								this.workloans[i].returnname='等额本息'
							}
							else if(this.workloans[i].returntype==2){
								this.workloans[i].returnname='等额本金'
							}
						}
					}
				})
			},
			history(item){//获取当前贷款申请历史
				this.request({
					url:"/Lender/getLenderhistory",
					method:"POST",
					data:{
						id:item.id
					}
				}).then(res=>{
					if(res.code=='200'){
						this.historys=res.data
						this.showhistory=true
					}
				})
			},
			intoreturn(item){//进入还款界面
				uni.setStorageSync('returnloans',item)
				uni.navigateTo({
					url:"/pages/person/LoansManagement/returnloans/returnloans",
				})
			},
			getLoansshenqing(){//获取贷款申请列表
				if(this.loanstable.length==0){
					this.showloading=true
					this.request({
						url:"/UserLoans/getLoansByUserid",
						method:"POST",
						data:{
							uid:this.user.id,
						}
					}).then(res=>{
						if(res.code=='200'){
							this.loanstable=res.data
							for(let i=0;i<this.loanstable.length;i++){
								for(let j=0;j<this.loanstype.length;j++){
									if(this.loanstable[i].lid==this.loanstype[j].id){
										this.loanstable[i].type=this.loanstype[j]
										if(this.loanstype[j].id==1){//住房贷款
											this.loanstable[i].icontype='home-fill'
										}
										else if(this.loanstype[j].id==2){//商业贷款
											this.loanstable[i].icontype='rmb-circle-fill'
										}
										else if(this.loanstype[j].id==3){//消费贷款
											this.loanstable[i].icontype='shopping-cart-fill'
										}
										break
									}
								}
								if(this.loanstable[i].ispass==0){
									this.loanstable[i].state='审核中'
									this.loanstable[i].textType='primary'
								}
								else if(this.loanstable[i].ispass==1){
									this.loanstable[i].state='审核未通过'
									this.loanstable[i].textType='error'
								}
								else if(this.loanstable[i].ispass==2){
									this.loanstable[i].state='审核通过'
									this.loanstable[i].textType='success'
								}
								if(this.loanstable[i].returntype==1){
									this.loanstable[i].returnname='等额本息'
								}
								else if(this.loanstable[i].returntype==2){
									this.loanstable[i].returnname='等额本金'
								}
							}
						}
						this.showloading=false
					})
				}
			}
		}
	}
</script>

<style>
.box{
	margin: 5% auto;
	width: 95%;
	background-color: #fff;
	border: 1px solid #ddd;
	border-radius: 5px;
	box-shadow: 0 2px 5px rgba(0,0,0,0.1);
}
</style>
