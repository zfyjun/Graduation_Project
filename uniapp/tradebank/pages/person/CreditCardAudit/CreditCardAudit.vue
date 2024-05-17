<template>
	<view>
		<u-subsection :list="list" :current="curNow" @change="sectionChange" activeColor="rgba(250,193,106)"></u-subsection>
		<view>
			<view class="box" v-for="(item,index) in tabledate" @click="toDetail(item)" >
				<view style="padding: 2%;">
					<view style="display: flex;">
						<u--text prefixIcon="clock-fill" type="success" :text="'申请时间:   '+item.passmsg[0].senttime"></u--text>
					</view>
					<view style="display: flex;">
						<view style="flex: 5;" >
							<u--text v-if="item.updatemsg.type==2" text="申请类型:  信用卡升级"></u--text>
							<u--text v-if="item.updatemsg.type==1" text="申请类型:  额度提升"></u--text>
						</view>
						<view style="display: flex;flex:3 ;">
							<u--text style="flex:4;" text="状态:"></u--text>
							<u--text style="flex:8;" :type="type" :text="item.state"></u--text>
						</view>
					</view>
					<u-divider ></u-divider>
					<view>
						<u--text size="12" type="info" :text="'信用卡号：( '+item.cardnumber+' )'"></u--text>
					</view>
					<view style="display: flex;">
						<view style="flex: 1;" v-if="item.updatemsg.type==2" >
							<u--text :text="'申请等级：  '+item.rankname"></u--text>
						</view>
						<view style="flex: 1;" >
							<u--text :text="'申请后额度￥：  '+item.updatemsg.cost"></u--text>
						</view>
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
				list: ['审核中', '未通过', '已通过'],
				curNow: 0,
				tabledate:[],
				type:'primary',
				ranknames:[{rank:1,name:'普通卡'},{rank:2,name:'金卡'},{rank:3,name:'白金卡'},{rank:4,name:'钻石卡'},{rank:5,name:'黑金卡'}],
			};
		},
		onLoad() {
			
		},
		onShow(){
			this.getaduitbyuid()
		},
		methods:{
			sectionChange(index) {
				this.curNow = index;
				if(index==0){
					this.type='primary'
				}
				else if(index==1){
					this.type='error'
				}
				else if(index==2){
					this.type='success'
				}
				this.getaduitbyuid()
			},
			getaduitbyuid(){
				this.tabledate=[]
				this.request({
					url:"/UserUpdate/getUpdatebyUID",
					method:"POST",
					data:{
						state:this.curNow,
						uid:uni.getStorageSync('user').id
					}
				}).then(res=>{
					if(res.code=='200'){
						this.tabledate=res.data
						for(let i=0;i<this.tabledate.length;i++){
							this.tabledate[i].passmsg=JSON.parse(this.tabledate[i].passmsg)
							for(let j=0;j<this.ranknames.length;j++){
								if(this.ranknames[j].rank==this.tabledate[i].updatemsg.rank){
									this.tabledate[i].rankname=this.ranknames[j].name
									break
								}
							}
						}
					}
				})
			},
			toDetail(item){//跳转到详情界面
				uni.setStorageSync('CreditCardupdate',item)				
				uni.navigateTo({
					url: "/pages/person/CreditCardAudit/CreditCardAuditDetail/CreditCardAuditDetail"
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
