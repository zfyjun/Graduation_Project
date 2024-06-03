<template>
	<view >
		<view class="box" v-for="(item , index) in bankcards ">
			<view style="padding: 2%;">
				<view style="display: flex;">
					<u--text style="flex: 3;" bold :text="item.typename"></u--text>
					<u--text style="flex: 1;" v-if="item.loststate==0" type="warning" @click="guashi(item,1)" text="挂失"></u--text>
					<u--text style="flex: 1;" v-if="item.loststate==1" type="primary" @click="guashi(item,2)" text="解除挂失"></u--text>
				</view>
				<u--text type="info" :text="'卡号：'+item.cardnumber"></u--text>
				<u-divider ></u-divider>
				<view style="display: flex;">
					<u--text v-if="item.state==1" type="error" text="状态:封禁" @click="chakanban(item)" ></u--text>
					<u--text v-if="item.state==0" type="success" text="状态:正常" ></u--text>
					<u--text v-if="item.abnormal==1" type="warning" text="是否存在异常操作:是" @click="chakanyichang(item)" ></u--text>
					<u--text v-if="item.abnormal==0" type="success" text="是否存在异常操作:否" ></u--text>
				</view>
				<view  style="padding: 2%;">
					<u--text  v-if="item.state==1&&item.restate==0" size="12" type="warning" text="未申请解封"></u--text>
					<u--text  v-if="item.state==1&&item.restate==1" size="12" type="success" text="已申请解封"></u--text>
					<u-button @click="rebancard(item)" type="primary" style="margin-top: 2%;" v-if="item.state==1&&item.restate==0"  text="银行卡解封申请"></u-button>
				</view>
			</view>
		</view>
		<u-modal :show="show" @cancel="cancel" @confirm="confirm" :showCancelButton="true" :closeOnClickOverlay="true" :title="title" >
			<view class="slot-content">
				<u--text :text="text"></u--text>
				<u--input style="margin-top: 2%;" v-model="password" type="password"  placeholder="请输入银行卡密码" ></u--input>
				<u-toast ref="uToast"></u-toast>
			</view>
		</u-modal>
		
		<u-modal :show="show1" @cancel="cancel" @confirm="confirm1" confirmText="消除异常" :showCancelButton="true" :closeOnClickOverlay="true" title="异常操作" >
			<view class="slot-content">
				<view style="padding: 2%;">
					<u--text bold text="异常操作如下:"></u--text>
					<view v-for="( item,index ) in errordata " class="box" >
						<view style="padding: 2%;">
							<u--text style="padding-bottom: 1%;"  :text="'时间：'+item.time"></u--text>
							<u--text style="padding-bottom: 1%;"  :text="'异常操作：'+item.description"></u--text>
						</view>
					</view>
					<u--text v-if="errordata.length==0" text="暂无异常操作"></u--text>
					<u-gap height="8" bgColor="#bbb"></u-gap>
					<view>
						<u--text type="info" text="您可以选择消除银行卡的异常操作,但在消除之前确认这些异常操作都是由本人自愿的操作形成,一旦同意本协议,用户因消除异常操作造成的损失由用户自行承担,本行概不负责"></u--text>
						 <u-checkbox-group
						             v-model="checkboxValue1"
						             placement="column"
						         >
						             <u-checkbox
						                 :customStyle="{marginBottom: '8px'}"
						                 v-for="(item, index) in checkboxList1"
						                 :key="index"
						                 :label="item.name"
						                 :name="item.name"
						             >
						             </u-checkbox>
						         </u-checkbox-group>
					</view>
					<u-gap height="8" bgColor="#bbb"></u-gap>
					<view v-if="errordata.length>0">
						<u--input style="margin-top: 2%;" v-model="password" type="password"  placeholder="请输入银行卡密码" ></u--input>
					</view>
					<u-toast ref="uToast2"></u-toast>
				</view>
			</view>
		</u-modal>
		<u-modal :show="show3" @cancel="cancel" @confirm="confirm2" confirmText="确定申请" :showCancelButton="true" :closeOnClickOverlay="true" title="解封申请" >
			<view class="slot-content">
				<view style="padding: 2%;">
					<u-gap height="8" bgColor="#bbb"></u-gap>
					<view>
						<u--text type="info" text="您的银行卡因为一些原因被管理员封禁,您必须承诺在今后使用银行卡的过程中遵守银行卡的使用规则,不能违法乱纪,不能频繁进行异常操作,否则本银行将有权对用户的银行卡进行封禁操作"></u--text>
						 <u-checkbox-group
						             v-model="checkboxValue2"
						             placement="column"
						         >
						             <u-checkbox
						                 :customStyle="{marginBottom: '8px'}"
						                 v-for="(item, index) in checkboxList2"
						                 :key="index"
						                 :label="item.name"
						                 :name="item.name"
						             >
						             </u-checkbox>
						         </u-checkbox-group>
					</view>
					<u-gap height="8" bgColor="#bbb"></u-gap>
					<u--input style="margin-top: 2%;" v-model="password" type="password"  placeholder="请输入银行卡密码" ></u--input>
					<u-toast ref="uToast3"></u-toast>
				</view>
			</view>
		</u-modal>
		<u-popup :show="show2" @close="close" >
	            <view style="padding: 2%;">
	                <u--text :text="'封禁原因：'"></u--text>
					<u--text type="error" v-if="caozuobank.statedetail!=null&&caozuobank.statedetail!=''" :text="caozuobank.statedetail"></u--text>
					<u--text type="error" v-if="caozuobank.statedetail==null||caozuobank.statedetail==''" :text="'审核暂未给出，详情请联系客服'"></u--text>
	            </view>
			</u-popup>
		<u-toast ref="uToast1"></u-toast>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				current:0,
				bankcards:[],
				user:uni.getStorageSync('user'),
				show:false,
				show1:false,
				show2:false,
				show3:false,
				caozuobank:{},
				password:'',
				title:'挂失',
				text:'',
				type:1,
				errordata:[],
				checkboxList2:[{name: '同意',}],
				checkboxList1:[{name: '同意',}],
				checkboxValue1:[],
				checkboxValue2:[]
			};
		},
		onLoad() {
			this.getcards()
		},
		methods:{
			 getcards(){
				 this.request({
				 	url:"/BankCard/getCardsmore",
				 	method:"POST",
				 	data:{
				 		uid:this.user.id,
				 	}
				 }).then(res=>{
				 	if(res.code==='200'){
				 		this.bankcards=res.data
						for(let i=0;i<this.bankcards.length;i++){
							if(this.bankcards[i].type==1){
								this.bankcards[i].typename="储蓄卡"
							}
							else if(this.bankcards[i].type==2){
								this.bankcards[i].typename="储蓄副卡"
							}
							else if(this.bankcards[i].type==3){
								this.bankcards[i].typename="信用卡"
							}
						}
				 	}
				 })
			 },
			 guashi(e,type){
				 this.type=type
				 this.caozuobank=e
				 if(type==1){//挂失
					 this.title='挂失'
					 this.text='是否确认挂失卡号为:'+this.caozuobank.cardnumber+'的银行卡？挂失后必须该银行卡将处于封禁状态，须补办后解封或者联系客服进行解封'
				 }
				 else{//解封挂失
					 this.title='解除挂失'
					 this.text='是否确认解除卡号为:'+this.caozuobank.cardnumber+'的银行卡的挂失状态，解除后银行卡可以正常使用'
				}
				 this.show=true
			 },
			 cancel(){
				 this.password=''
				 this.show=false
				 this.show1=false
				 this.show3=false
			 },
			 confirm1(){//消除异常
				 if(this.errordata.length>0){
					 if(this.checkboxValue1.length>0){
						 if(this.password==this.user.password){
						 						 this.request({
						 						 	url:"/BankCard/clearerror",
						 						 	method:"POST",
						 						 	data:{
						 						 		cid:this.caozuobank.id,
						 						 	}
						 						 }).then(res=>{
						 						 	if(res.code==='200'){
						 								this.getcards()
						 						 		this.show1=false
														this.password=''
						 						 		this.$refs.uToast1.show({
						 						 							type:'success',
						 						 							duration:'1500',
						 						 							message:"操作成功",
						 						 						})					
						 						 	}
						 						 })
						 }else{
						 						 this.$refs.uToast2.show({
						 						 					type:'error',
						 						 					duration:'1500',
						 						 					message:"账号密码错误",
						 						 				})
						 }
					 }
					 else{
						 this.$refs.uToast2.show({
						 					type:'warning',
						 					duration:'1500',
						 					message:"消除异常操作前必须同意上述协议",
						 				})
					 }
				 }
				 else{
					 this.$refs.uToast2.show({
					 					type:'warning',
					 					duration:'1500',
					 					message:"暂无需要消除的异常",
					 				})
				 }
			 },
			 confirm2(){//申请解除封禁
				 if(this.checkboxValue2.length>0){
					 if(this.password==this.user.password){
						 this.request({
						 	url:"/BankCard/rebancardsatte",
						 	method:"POST",
						 	data:{
						 		cid:this.caozuobank.id,
						 	}
						 }).then(res=>{
						 	if(res.code==='200'){
						 		this.getcards()
						 		this.show3=false
								this.password=''
						 		this.$refs.uToast3.show({
						 							type:'success',
						 							duration:'1500',
						 							message:"操作成功",
						 						})					
						 	}
						 })
					 }
					 else{
						 this.$refs.uToast3.show({
						 					type:'error',
						 					duration:'1500',
						 					message:"账号密码错误",
						 				})
					 }
				 }else{
					 this.$refs.uToast3.show({
					 					type:'warning',
					 					duration:'1500',
					 					message:"申请封禁解除前必须同意上述协议",
					 				})
				 }
			 },
			 confirm(){//挂失或者解除挂失
				 if(this.password==this.user.password){
					 this.request({
					 	url:"/BankCard/guashicard",
					 	method:"POST",
					 	data:{
					 		cid:this.caozuobank.id,
							type:this.type
					 	}
					 }).then(res=>{
					 	if(res.code==='200'){
							this.getcards()
					 		this.show=false
							this.password=''
							this.$refs.uToast1.show({
												type:'success',
												duration:'1500',
												message:"操作成功",
											})
					 	}
					 })
					 
				 }else{
					 this.$refs.uToast.show({
					 					type:'error',
					 					duration:'1500',
					 					message:"账号密码错误",
					 				})
				 }
			 },
			 chakanban(e){//查看封禁理由
				 this.caozuobank=e
				 this.show2=true
			 },
			 close(){
				 this.show2=false
			 },
			 rebancard(e){//解除银行卡封禁打开
				 this.caozuobank=e
				 this.show3=true
			 },
			 chakanyichang(item){//查看异常(打开面板)
			     this.caozuobank=item
				 this.request({
				 	url:"/BankCard/getabnormoldetail",
				 	method:"POST",
				 	data:{
				 		cid:this.caozuobank.id,
				 		type:1
				 	}
				 }).then(res=>{
				 	if(res.code==='200'){
				 	   this.errordata=res.data
					   this.show1=true
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
