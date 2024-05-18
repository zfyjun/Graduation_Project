<template>
	<view>
		<view>
			<u-subsection :list="list" :current="current" @change="sectionChange"></u-subsection>
		</view>
		
		<view v-if="current==0">
			<u-empty v-if="workloans.length==0" style="padding-top: 20%;" mode="search" text="暂无生效贷款" >
			</u-empty>
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
						<u--text  :prefixIcon="item.icontype" iconStyle="font-size: 19px" bold :text="item.type.name"></u--text>
					    <u--text style="padding-left:  50%;" color="rgba(1,167,112)" text="详情" @click="toloans(item)" suffixIcon="search" iconStyle="font-size: 18px" ></u--text>
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
			}
		},
		onLoad(){
			this.getloanstype()
			
		},
		methods: {
			sectionChange(index) {
				this.current = index;
				if(index==2){
					this.getLoansshenqing()
				}
			},
			toloans(e){//跳转至贷款申请界面
			    uni.setStorageSync('myloans',e)
				uni.navigateTo({
					url:"/pages/bankcards/loans/loans"
				})
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
							
							console.log(this.loanstable)
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
