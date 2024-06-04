<template>
	 <view class="message-item">
	    <view v-if="message.sender === 'me'">
	        <!-- 这是显示用户自己的消息的部分 -->
			<view v-if="message.text.type==='text'">
				<view class="el-row" style="padding: 5px 0">
					<view class="el-col el-col-22" style="text-align: right; padding-right: 10px">
						<view class="tip left">{{ message.text.data }}</view>
					</view>
					<view class="el-col el-col-2">
						<view class="el-avatar el-avatar--circle" style="height: 40px; width: 40px; line-height: 40px;">
							<!-- <image mode="aspectFill" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" /> -->
						</view>
					</view>
				</view>
			</view>
			<view v-if="message.text.type==='audio'">
				
			</view>
		  
	    </view>
	    <view v-else>
	        <!-- 这是显示其他用户的消息的部分 -->
		    <view v-if="message.text.type==='text'">
		    	<view class="row" style="padding: 5px 0">
		    		<view class="col col-2" style="text-align: right">
		    			<view class="avatar avatar--circle" style="height: 40px; width: 40px; line-height: 40px;">
		    				<!-- <image src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" style="object-fit: cover;"></image> -->
		    			</view>
		    		</view>
		    		<view class="col col-22" style="text-align: left; padding-left: 10px">
		    			<view class="tip right">{{message.text.data}}</view>
		    		</view>
		    	</view>
		    </view>
		    <view v-if="message.text.type==='audio'">
		    	
		    </view>
	    </view>
	  </view>
</template>

<script>
	export default {
		props: ['message'],
		data() {
			return {
				
			}
		},
		methods: {
			
		}
	}
</script>

<style>

</style>
