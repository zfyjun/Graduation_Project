<template>
  <view class="padding-bottom" style="margin-bottom: 50px">
          <view>
              <!-- 显示用户 -->
              <view class="show_user" v-for="(user,index) in users" :key="user.username">
                  <view style="display: flex;justify-content: center;align-items: center;height: 100%;" v-if="user.role==1">
					  <!-- <view> -->
                        <text>{{user.username}}</text>
						<view v-for="unRead in unReadMsgs">
						    <view v-if="user.username===unRead.from_user">
						        <view class="dot" v-if="unRead.un_read>0">
						            <text class="count">{{ unRead.un_read }}</text>
						        </view>
						    </view>
						</view>
					  <!-- </view> -->
					
					    <view>
					        <view style="margin-left: 12px;" v-if="user.isOnline==0">(离线)</view>
					        <view style="margin-left: 12px;" v-if="user.isOnline==1">(在线)</view>
						</view>
					  
					  <icon type="success" @click="historyChat(user)"></icon>
					  <!-- <image src="/static/image/icon/card.png" @click="historyChat(user)"></image> -->
					  <text v-if="user.username === chatUser">chatting...</text>
                  </view>
  
				  
              </view>   
  
              <!-- 聊天框 -->
              <view class="chat_box">
                  <view style="display: flex;justify-content: center;align-items: center;height: 100%;">
                      Web聊天室（{{chatUser}}）
                  </view>
  
  
					<!-- 用组件 -->
					<!-- <view>
						<message-item v-for="(message,index) in contentMsg" :key="index" :message="message"></message-item>
					</view> -->
  
                  <!-- 聊天内容（直接写） -->
				  <view>
					    <view v-for="(message, index) in contentMsg" :key="index">
						  <view class="message-item">
						    <view v-if="message.from === user.account">
						        <!-- 这是显示用户自己的消息的部分 -->
								<view v-if="message.text.type==='text'">
									<view class="el-row" style="padding: 5px 0">
										<view class="el-col el-col-22" style="text-align: right; padding-right: 10px">
											<view class="tip left">{{ message.text.data }}</view>
										</view>
										<!-- <view class="el-col el-col-2"> -->
											<!-- <view class="el-avatar el-avatar--circle" style="height: 40px; width: 40px; line-height: 40px;"> -->
												<!-- <image mode="aspectFill" src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" /> -->
											<!-- </view> -->
										<!-- </view> -->
									</view>
								</view>
								<view v-if="message.text.type==='audio'">
									<view class="row" style="padding: 5px 0">
									        <view class="col col-22" style="text-align: right; padding-right: 10px">
									          <audio :src="message.text.data" controls></audio>
									        </view>
									        <!-- <view class="col col-2"> -->
									            <!-- <view class="avatar avatar--circle" style="height: 40px; width: 40px; line-height: 40px;"> -->
									                <!-- <image src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" style="object-fit: cover;"></image> -->
									            <!-- </view> -->
									        <!-- </view> -->
									</view>
								</view>
							</view>
						    <view v-else>
						    <!-- 这是显示其他用户的消息的部分 -->
								<view v-if="message.text.type==='text'">
									<view class="row" style="padding: 5px 0">
										<!-- <view class="col col-2" style="text-align: right"> -->
											<!-- <view class="avatar avatar--circle" style="height: 40px; width: 40px; line-height: 40px;"> -->
												<!-- <image src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" style="object-fit: cover;"></image> -->
											<!-- </view> -->
										<!-- </view> -->
										<view class="col col-22" style="text-align: left; padding-left: 10px">
											<view class="tip right">{{message.text.data}}</view>
										</view>
									</view>
								</view>
								<view v-if="message.text.type==='audio'">
									<view class="row" style="padding: 5px 0">
									    <!-- <view class="col col-2" style="text-align: right"> -->
									        <!-- <view class="avatar avatar--circle" style="height: 40px; width: 40px; line-height: 40px;"> -->
									            <!-- <image src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" style="object-fit: cover;"></image> -->
									        <!-- </view> -->
									    <!-- </view> -->
									<audio :src="message.text.data"  controls></audio>
									</view>
								</view>
							</view>
						    </view>
					  </view>
				  </view>
				  
                  <view class="chat-content">{{content}}</view>
                  
                  <view>
                      <button @click="startRecording">点击录音</button>
                      <button @click="stopRecording">结束录音</button>
                      <button @click="test">测试</button>
  
                      <textarea v-model="text"></textarea>
  
                      <view style="text-align: right;">
                          <button type="primary" @click="send">发送</button>
                      </view>
                  </view>
              </view>
          </view>
      </view>
</template>
<script>
import request from "@/utils/request";
import upload from "@/utils/request.js"

let socket;
export default {
  name: "Im",
  data() {
    return {
	  testUrl:'',
      circleUrl: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      user: {},//当前用户
      isCollapse: false,
      users: [],
      chatUser: '',//聊天对象
      text: "",
      messages: [],
	  contentMsg:[],//渲染聊天信息数据
      content: '',
      historyMessage:[],
      chatFlag:false,
      userFlag:[true,true,true],

      // chunks:[], // 用于保存音频数据
      //语音数据
      recording: false,
      audio: null,
      mediaRecorder: null,
      audioBlob:null,
      audioBlobs:[],
      audioUrls:[],
      unReadMsgs:[],
      unreadCount:2,
    }
  },
  created() {
    this.init()
    this.unRead()
  },
  mounted() {

  },
  methods: {
    test(){
	  this.request({
		  url:"/chatHistory/unRead",
		  method:"POST",
		  data:{
			  name:this.user.account
		  }
	  }).then(res=>{
		  console.log(res.data)
        this.unRead=res.data[0].un_read
        // alert(this.unRead)
        // console.log('this.users')
        // console.log(this.users)
      })
    },
	unRead(){
	      this.request({
			  url:"/chatHistory/unRead",
			  method:'POST',
			  data:{
				name:this.user.account,
			}
	      }).then(res=>{
	        console.log('res')
	        this.unReadMsgs=res.data
	        console.log(this.unReadMsgs)
	      })
	
	    },
    download(url) {
      this.request({
		  url:url,
		  method:'GET',
		  // data:{},
		  config:{
			responseType:'blob',
		}
      }).then(res=>{
        this.audioBlob=new Blob([res], { type: 'audio/webm' })
        this.audio=URL.createObjectURL(this.audioBlob)
      })
    },

    async startRecording() {
      console.log('start')
      let stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      this.mediaRecorder = new MediaRecorder(stream);

      let chunks = [];
      this.mediaRecorder.ondataavailable = e => chunks.push(e.data);

      this.mediaRecorder.onstop = () => {
        this.audioBlob=new Blob(chunks, { type: 'audio/webm' }) //存储 Blob 对象
        this.audio = URL.createObjectURL(this.audioBlob);  //将Blob对象转换为URL
        chunks = [];  // 清空音频数据
      };

      this.mediaRecorder.start();
      this.recording = true;
    },
    stopRecording() {
      console.log('stop')
      if (this.mediaRecorder) {
        this.mediaRecorder.stop();
        this.recording = false;
      }
    },


    historyChat(user) {
	  this.contentMsg=[],
	  // this.tempUser=user
		// this.audioBlobs=[],

      this.request({
		  url:'/chatHistory/isRead',
		  method:'POST',
		  data:{
			  fromUser:user.username,
			toUser:this.user.account,
		},
      }).then(res=>{
        this.unRead()
      })

      this.request({
		  url:'/chatHistory/findAll',
		  method:'POST',
		  data:{
			fromUser:this.user.account,
			toUser:user.username,
		}
      }).then(res=> {
        this.historyMessage = res.data
		console.log('111')
		console.log(res.data)

        this.content = ''
        this.chatUser = user.username


  //       let URLs=[]
  //       for (let i = 0; i < this.historyMessage.length; i++){
  //         let msgData = JSON.parse(this.historyMessage[i].text)
  //         if(msgData.type=='audio'){
  //           URLs.push(msgData.data)
  //         }
  //       }
		// console.log('URLs')
		// console.log(URLs)

   //      // 使用map把每个音频链接都转换为一个Promise
   //      let audioPromises = URLs.map((url,index) => {
   //        return new Promise((resolve, reject) => {
			//   console.log(url.length)
			//   url=url.substring(21,url.length)
			//   console.log(url)
   //          this.request({
			// 	url:url, 
			// 	method:'GET',
			// 	config:{ responseType: 'blob' },
			// }).then(res => {
   //            let audioBlob = new Blob([res], { type: 'audio/webm' });
   //            resolve({audioBlob,url});
   //          }).catch(err => {
   //            reject(err);
   //          });
   //        });
   //      });

   //      Promise.all(audioPromises).then(blobs=>{
   //        blobs.forEach(({audioBlob,url})=>{
			// url="http://localhost:9090"+url
   //          this.audioBlobs.push({audioBlob,url})
   //        })
   //        console.log('this.audioUrls')
   //        console.log(this.audioBlobs)


          for (let i = 0; i < this.historyMessage.length; i++) {
            let msgData = JSON.parse(this.historyMessage[i].text)
			if(msgData.type==='text'){
				let tmpMsg={
					"from":this.historyMessage[i].from,
					text:{
						"type":msgData.type,
						"data":msgData.data
					},
					"to":this.historyMessage[i].to
				}
				this.contentMsg.push(tmpMsg)
			}else if(msgData.type==='audio'){
				// let audioUrl;
				// let falg;
				// for (let i = 0; i < this.audioBlobs.length; i++) {
		  //           if(msgData.data==this.audioBlobs[i].url){
				// 		audioUrl=URL.createObjectURL(this.audioBlobs[i].audioBlob)
				// 		this.audio=URL.createObjectURL(this.audioBlobs[i].audioBlob),
				// 		falg=i;
				//         break;
				// 	}
				// }
				// console.log("au")
				// console.log(audioUrl)
				let tmpMsg={
					"from":this.historyMessage[i].from,
					text:{
						"type":msgData.type,
						"data":msgData.data
					},
					"to":this.historyMessage[i].to
				}
				this.contentMsg.push(tmpMsg)
			}
			
			console.log('tmpMsg')
			console.log(this.contentMsg)
			console.log(this.audioBlobs)
            // this.$nextTick(() => {
            //   let container = this.$el.querySelector(".chat-content");
            //   // console.log('con')
            //   // console.log(container)
            //   container.scrollTop = container.scrollHeight;
            // })
          }
        })
      
    },

    send() {
      if (!this.chatUser) {
        this.$message({type: 'warning', message: "请选择聊天对象"})
        return;
      }
      // if (!this.text) {
      //   this.$message({type: 'warning', message: "请输入内容"})
      // }
      else {
        if (typeof (WebSocket) == "undefined") {
          console.log("您的浏览器不支持WebSocket");
        } else {
          console.log("您的浏览器支持WebSocket");
          // 组装待发送的消息 json
          // {"from": "zhang", "to": "admin", "text": "聊天文本"}

          let message;
          let msgData;

          if (this.text) {
            msgData=JSON.stringify({
              "type":"text",
              "data":this.text
            })
			
			
			let tmpMsg={
				"from":this.user.account,
				text:{
					"type":"text",
					"data":this.text
				},
				"to":this.chatUser
			}
            //发送文本信息
            message = {from: this.user.account, to: this.chatUser, text: msgData}
            //文本
            this.messages.push({user: this.user.account, text: msgData})
            // // 构建消息内容，本人消息
            // this.createContent(null, this.user.username, JSON.parse(msgData))
            this.text = '';

            socket.send(JSON.stringify(message));  // 将组装好的json发送给服务端，由服务端进行转发
            this.request({
				url:'/chatHistory/add',
				method:'POST',
				data:{
				  message:message,
				}
            }).then(res=>{
              console.log("保存成功")
              // 构建消息内容，本人消息
			 
			  this.contentMsg.push(tmpMsg)

              // // this.createContent(null, this.user.account, JSON.parse(msgData))
              // //让聊天记录显示在下面一条
              // this.$nextTick(() => {
              //   let container = this.$el.querySelector(".chat-content");
              //   container.scrollTop = container.scrollHeight;
              // })

            })

          }

          if (this.audio){
            // 创建一个 form 数据对象并添加录音数据
            let formData = new FormData()
            formData.append("audio", this.audioBlob)

            // 使用 axios 发送 POST 请求并包含 form 数据
		

            fetch("http://localhost:9090/file/uploadAudio",{ 
				method:'POST',
				body:formData, 
            }).then(res1=>res1.json())
			  .then(res => {
              console.log("音频文件已被发送到服务器")
              this.audio=res.data
              console.log(res)

              msgData=JSON.stringify({
                "type":"audio",
                "data":this.audio
              })
			  
			  let tmpMsg={
			  	"from":this.user.account,
			  	text:{
			  		"type":"audio",
			  		"data":this.audio
			  	},
			  	"to":this.chatUser
			  }

              //发送语音信息
              message = {from: this.user.account, to: this.chatUser, text: msgData}
              //语音
              this.messages.push({user: this.user.account, text: msgData})
              // // 构建消息内容，本人消息
              // this.createContent(null, this.user.username, JSON.parse(msgData))
              console.log("audio标签")
              this.audio = null;

              socket.send(JSON.stringify(message));  // 将组装好的json发送给服务端，由服务端进行转发
              this.request({
				  url:'/chatHistory/add',
				  method:'POST',
				  data:{
					message:message,
				}
              }).then(res=>{
                console.log("保存成功")
                // 构建消息内容，本人消息
				
				this.contentMsg.push(tmpMsg)
                // this.createContent(null, this.user.account, JSON.parse(msgData))
                //让聊天记录显示在下面一条
                this.$nextTick(() => {
                  let container = this.$el.querySelector(".chat-content");
                  container.scrollTop = container.scrollHeight;
                })
              })
            })
		
		
          }
        }
      }


    },
    init() {
      this.user = sessionStorage.getItem("user") ? JSON.parse(sessionStorage.getItem("user")) : {}
      let username = this.user.account;
      let _this = this;
      if (typeof (WebSocket) == "undefined") {
        console.log("您的浏览器不支持WebSocket");
      } else {
        console.log("您的浏览器支持WebSocket");
        let socketUrl = "ws://localhost:9090/imserver/" + username;
        if (socket != null) {
          socket.close();
          socket = null;
        }
        // 开启一个websocket服务
        socket = new WebSocket(socketUrl);
        //打开事件
        socket.onopen = function () {
          console.log("websocket已打开");
        };
        //  浏览器端收消息，获得从服务端发送过来的文本消息
        socket.onmessage = function (msg) {
          console.log("收到数据====" + msg.data)
          let data = JSON.parse(msg.data)  // 对收到的json数据进行解析， 类似这样的： {"users": [{"account": "zhang"},{ "account": "admin"}]}
          if (data.users) {  // 获取在线人员信息
            _this.users = data.users.filter(user => user.username !== username)  // 获取当前连接的所有用户信息，并且排除自身，自己不会出现在自己的聊天列表里
          } else {
            // 如果服务器端发送过来的json数据 不包含 users 这个key，那么发送过来的就是聊天文本json数据
            //  // {"from": "zhang", "text": "hello"}
            if (data.from === _this.chatUser) {
              _this.messages.push(data)
              // 构建消息内容

              console.log("_this.messages")
              console.log(_this.messages)

              console.log(data.text)
              _this.createContent(data.from, null, data.text)
            }
          }
        };
        //关闭事件
        socket.onclose = function () {
          console.log("websocket已关闭");
        };
        //发生了错误事件
        socket.onerror = function () {
          console.log("websocket发生了错误");
        }
      }
    }
  }
}
</script>
<style>
.tip {
  color: white;
  text-align: center;
  border-radius: 10px;
  font-family: sans-serif;
  padding: 10px;
  width:auto;
  display:inline-block !important;
  display:inline;
}
.right {
  background-color: deepskyblue;
}
.left {
  background-color: forestgreen;
}

/*用户未读信息*/
.userBox {
  position: relative;
}
.usernameBox {
  display: inline-block;
  position: relative;
}
.dot {
  position: absolute;
  right: 245px;
  top: 23px;
  width: 13px;
  height: 13px;
  background-color: red;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
}
.count {
  color: white;
  font-size: 10px;
}
</style>
