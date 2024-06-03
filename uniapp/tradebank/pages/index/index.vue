<template>
	<view >
		<view>
			<u-button text="消费" @click="opneconsume()"></u-button>
			 <u-sticky bgColor="#fff">
			 <u-swiper  
			            :list="list1"
			    ></u-swiper>
			
			    <u-subsection :list="list2" :current="current"   @change="sectionChange" ></u-subsection>
			  </u-sticky>
			  <view v-if="current==0">
				  <u-tabs :list="list3" @click="click"></u-tabs>
				  <view class="u-page">
				  		<u-list
						 scrollable
						 @scrolltolower="scrolltolower"
				  		>
				  			<u-list-item
				  				v-for="(item, indexs) in news[index]"
				  				:key="indexs"
								link
				  			>
				  				<u-cell
				  					:title="item.title"
						            @click="intonews(item)"
				  				>
				  					<u-avatar
									    v-if="item.img!=null&&item.img!=''"
				  						slot="icon"
				  						shape="square"
				  						size="35"
				  						:src="item.img"
				  						customStyle="margin: -3px 5px -3px 0"
				  					></u-avatar>
				  				</u-cell>
				  			</u-list-item>
				  		</u-list>
						<u--text style="margin: 1% ;" align="center" v-if="total<=news[index].length" type="info"  text="已经到底了" ></u--text>
				  	</view>
			  </view>
			  <view v-if="current==1">
			  	<view class="box" v-for="(item ,index) in products" @click="toproductone(item)" >
					<view style="padding: 2%;">
						<view style="display: flex;">
							<u--text prefixIcon="baidu" size="14" :text="item.name" bold ></u--text>
							<u--text type="info" size="12" :text="item.typename"></u--text>
						</view>
						<view style="display: flex;margin-top: 2%;">
						    <u--text v-if="item.type==1" style="flex:3" size="15" bold :text="'封闭'+item.minday+'天'" ></u--text>
							<u--text v-if="item.type==2" style="flex:3" size="15" bold :text="'最低持有'+item.minday+'天'" ></u--text>
							<u--text type="success" v-if="item.risk<=2" :text="'R'+item.risk+'等级'" ></u--text>
							<u--text type="primary" v-if="item.risk==3" :text="'R'+item.risk+'等级'" ></u--text>
							<u--text type="warning" v-if="item.risk==4" :text="'R'+item.risk+'等级'" ></u--text>
							<u--text type="error" v-if="item.risk==5" :text="'R'+item.risk+'等级'" ></u--text>
						</view>
						<view style="display: inline-flex; margin-top: 1%;width: 100%;">
						    <u--text style="flex: 4;" size="12" type="info" :text="item.price+'元起购'" ></u--text>
						</view>
					</view>
				</view>	
				<u-divider
				        @click="tomore"
				        text="更多商品"
				        textColor="#2979ff"
				        lineColor="#ffff"
				></u-divider>
			  </view>
		</view>
		<u-popup customStyle="width: 80%;" :round="7" style="width: 80%;" mode="center" closeOnClickOverlay="true" :show="show" @close="close" @open="open">
		       <view style="padding: 5%;">
				   <u--form
				   				labelPosition="left"
				   				:model="model1"
				   				ref="uForm"
				   		>
				   			<u-form-item
							        labelWidth="30%"
				   					label="消费额度"
				   					borderBottom
				   					ref="item1"
				   			>
				   				<u--input
								        type="number"
				   						v-model="model1.cost"
				   						border="none"
				   				></u--input>
				   			</u-form-item>
				   			<u-form-item
				   					label="账户"
				   					borderBottom
				   					@click="showSex = true; hideKeyboard()"
				   					ref="item1"
				   			>
				   				<u--input
				   						v-model="model1.card.cardnumber"
				   						disabled
				   						disabledColor="#ffffff"
				   						placeholder="请选择账户"
				   						border="none"
				   				></u--input>
				   				<u-icon
				   						slot="right"
				   						name="arrow-right"
										@click="showSex=true"
				   				></u-icon>
				   			</u-form-item>
							<u-form-item
									borderBottom
									ref="item1"
							>
								<u--input
								        placeholder="请输入用途(选填)"
								        style="width: 80%;"
										v-model="model1.describe"
										border="none"
								></u--input>
							</u-form-item>
							<u-form-item
							        labelWidth="30%"
									label="支付密码"
									borderBottom
									ref="item1"
							>
								<u--input
								        type="password"
										v-model="password"
										border="none"
								></u--input>
							</u-form-item>
				   		</u--form>
						<u-action-sheet 
						                :closeOnClickOverlay="true" 
										:closeOnClickAction="true"
										:show="showSex"
										:actions="list"
										title="消费账户"
										description="推荐选择信用卡"
										@close="showSex = false"
										@select="selectClick"
								>
					</u-action-sheet>
					<u-button style="margin-top: 5%;" text="确定" @click="consume()"></u-button>
			   </view>
			   <u-toast ref="uToast2"></u-toast>
		</u-popup>
		<view>
			<u-popup mode="center" :show="showflag" @close="showflag=false" closeable="true">
		        <view style="padding: 5%;">
					<u-toast ref="uToast33"></u-toast>
		            <u--text type="warning" :text="message"></u--text>
		        </view>
				
			</u-popup>
		</view>
		<u-toast ref="uToast"></u-toast>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				user:uni.getStorageSync('user'),
				mycards:[],
				show:false,
				model1:{cost:'',card:{cid:'',cardnumber:''},describe:''},
				showSex:false,
				list:[],
				list1:[
					'https://cdn.uviewui.com/uview/swiper/swiper1.png',
				    'https://cdn.uviewui.com/uview/swiper/swiper2.png',
					'https://cdn.uviewui.com/uview/swiper/swiper3.png',
				],
				current:0,
				list2:[
					'新闻',
					'理财'
				],
				list3: [{
				                    name: '全部',
									vuale:''
				                }, {
				                    name: '财经',
									vuale:'财经'
				                }, {
				                    name: '娱乐',
									vuale:'娱乐'
				                }, {
				                    name: '科技',
									vuale:'科技'
				                },{
				                    name: '时政',
									vuale:'时政'
				                }, {
				                    name: '公告',
									vuale:'公告'
				                }],
				pageNum:[],
				news:[[],[],[],[],[],[]],
				total:0,
				products:[],
				type:'',
				index:0,
				message:'',
				showflag:false,
				password:'',
			}
		},
		onLoad() {
			for(let i=0;i<this.list3.length;i++){
				this.pageNum.push(1)
			}
			this.selectnews(this.index)
		},
		onShow(){
			this.getCards()
		},
		onReachBottom() {
			this.scrolltolower(this.index)
		},
		methods: {
			opneconsume(){//弹出消费框
				this.show=true
			},
			click(item) {//选择新闻类型
			  this.type=item.vuale
			  this.index=(item.index)
			  console.log(this.index)
			  this.selectnews(this.index)
			},
			consume(){//消费
				if(this.model1.cost<=0){
					this.$refs.uToast2.show({
										type:'error',
										duration:'1500',
										message:"消费金额必须大于0",
									})
				}
				else{
					if(this.password==this.user.password){
						this.request({
							url:"/Pay/consume",
							method:"POST",
							data:{
								cid:this.model1.card.cid,
								cost:this.model1.cost,
								describe:this.model1.describe
							}
						}).then(res=>{
							if(res.code==='200'){
								this.show=false
								this.testsave()
							}
							else{
								this.$refs.uToast2.show({
													type:'error',
													duration:'3000',
													message:res.msg,
												})
							}
						})
					}
					else{
						this.$refs.uToast2.show({
											type:'error',
											duration:'1500',
											message:"账号密码错误",
										})
					}
				}
			},
			getCards(){//获取卡号以及银行卡类型等元素的操作
			    this.list=[]
				this.request({
					url:"/BankCard/getCards",
					method:"POST",
					data:{
						uid:uni.getStorageSync('user').id,
						cards:uni.getStorageSync('user').bankcards
					}
				}).then(res=>{
					if(res.code==='200'){
						this.mycards=res.data
						for(let i=0;i<this.mycards.length;i++){
							let card={name:'',subname:'',cid:'',color:"black"}
							card.cid=this.mycards[i].id
							if(this.mycards[i].type!=3){
								card.name="储蓄卡("+this.mycards[i].cardnumber.slice(-4)+")"
								card.subname="余额："+this.mycards[i].balance
								this.list.push(card)
							}
							else{
								card.name="信用卡("+this.mycards[i].cardnumber.slice(-4)+")"
								card.color="rgba(97,175,239)"
								card.subname="剩余额度："+this.mycards[i].balance
								this.list.unshift(card)
							}
						}
					}
				})
			},
			testsave(){//日流水检擦
				this.request({
					url:"/BankCardSave/costsave",
					method:"POST",
					data:{
						cid:this.model1.card.cid,
					}
				}).then(res=>{
					if(res.code==='200'){
						this.message="日流水达到"+(res.data).toFixed(2)+"元，已经超过安全阈值2万元，操作记录将会被计入异常操作中"
						this.showflag=true
						this.$refs.uToast33.show({
											type:'success',
											duration:'1500',
											message:"消费成功",
										})
					}
					else{
						this.$refs.uToast.show({
											type:'success',
											duration:'1500',
											message:"消费成功",
										})
					}
					
				})
			},
		    close(){
				this.show=false
			},
			selectClick(index){
				this.model1.card.cardnumber=index.name
				this.model1.card.cid=index.cid
			},
			sectionChange(index){//切换新闻和产品推荐
				this.current=index
				if(index==1&&this.products.length==0){
					this.selectproducts()
				}
			},
			gettotal(){//获取新闻总数
				this.request({
					url:"/news/gettotal",
					method:"POST",
					data:{
						type:this.type
					}
				}).then(res=>{
					if(res.code==='200'){
						this.total=res.data
					}
				})
			},
			selectnews(index){//新闻搜索
				this.request({
					url:"/news/select",
					method:"POST",
					data:{
						pageNum:this.pageNum[index],
						type:this.type
					}
				}).then(res=>{
					if(res.code==='200'){
						for(let i=0;i<res.data.records.length;i++){
							this.news[index].push(res.data.records[i])
						}
						this.total=res.data.total
						if(res.data.records.length>0){
							this.pageNum[index]++
						}
					}
				})
			},
			selectproducts(){//获取专属推荐产品
				this.request({
					url:"/productanalysis/persontuijian",
					method:"POST",
					data:{
						id:uni.getStorageSync('user').id,
					}
				}).then(res=>{
					if(res.code==='200'){
						this.products=res.data
						for(let i=0;i<this.products.length;i++){
							if(this.products[i].type==1){
								this.products[i].typename="固期产品"
							}
							else if(this.products[i].type==2){
								this.products[i].typename="限期产品"
							}
						}
					}
				})
			},
			intonews(e){
				uni.setStorageSync('newid',e.id)
				uni.navigateTo({
						url:"/pages/index/news/news"
				})
			},
			scrolltolower(index){
				this.gettotal()
				if(this.news[index].length<this.total){
					this.selectnews(this.index)
				}
			},
			toproductone(item){//跳转至某一商品
				uni.setStorageSync('product',item)
				uni.navigateTo({
					url:"/pages/product/detail/detail"
				})
			},
			tomore(){//更多商品
				uni.switchTab({
					url:"/pages/product/product"
				})
			}
		},
		
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
