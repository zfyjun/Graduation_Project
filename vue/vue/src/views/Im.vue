<template>
  <div style="padding: 10px; margin-bottom: 50px">
    <el-row>
<!--      显示用户-->
      <el-col :span="8">
        <el-card style="width: 100%; min-height: 300px; color: #333">
          <div style="padding-bottom: 10px; border-bottom: 1px solid #ccc">在线用户<span style="font-size: 12px">（点击聊天气泡开始聊天）</span></div>
          <div style="padding: 10px 0" v-for="(user,index) in users" :key="user.username">
            <span>{{ user.username }}</span>
            <i class="el-icon-chat-dot-round" style="margin-left: 10px; font-size: 16px; cursor: pointer"
               @click="historyChat(user)"></i>
            <span style="font-size: 12px;color: limegreen; margin-left: 5px" v-if="user.username === chatUser">chatting...</span>
          </div>
        </el-card>
      </el-col>


<!--      聊天框-->
      <el-col :span="16">
        <div style="width: 800px; margin: 0 auto; background-color: white;
                    border-radius: 5px; box-shadow: 0 0 10px #ccc">
          <div style="text-align: center; line-height: 50px;">
            Web聊天室（{{ chatUser }}）
          </div>

<!--          聊天内容-->
          <div class="chat-content" style="height: 350px; overflow:auto; border-top: 1px solid #ccc" v-html="content"></div>
          <div style="height: 200px">

            <!-- 语音-->
            <button id="record" @click="yuyin">点击录音</button>
            <button id="stop">结束录音</button>
            <audio id="player" controls></audio>

            <textarea v-model="text" style="height: 160px; width: 100%; padding: 20px; border: none; border-top: 1px solid #ccc;
             border-bottom: 1px solid #ccc; outline: none">
            </textarea>


            <div style="text-align: right; padding-right: 10px">
              <el-button type="primary" size="mini" @click="send">发送</el-button>
            </div>
          </div>
        </div>





      </el-col>
    </el-row>
  </div>
</template>
<script>
import request from "@/utils/request";
let socket;
export default {
  name: "Im",
  data() {
    return {
      circleUrl: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png',
      user: {},
      isCollapse: false,
      users: [],
      chatUser: '',
      text: "",
      messages: [],
      content: '',
      historyMessage:[],
      chatFlag:false,
      userFlag:[true,true,true],
    }
  },
  created() {
    this.init()
  },
  mounted() {

  },
  methods: {
    yuyin(){
      let recordButton = document.querySelector('.record');
      let stopButton = document.querySelector('.stop');
      let player = document.querySelector('.player');

      let mediaRecorder;
      let chunks = []; // 用于保存音频数据

// 异步获取音频权限
      async function getMedia() {
        let stream = null;

        try {
          stream = await navigator.mediaDevices.getUserMedia({audio: true});
          mediaRecorder = new MediaRecorder(stream);

          mediaRecorder.ondataavailable = function(e) {
            chunks.push(e.data);

            // 录音结束，自动播放
            if (mediaRecorder.state == 'inactive'){
              let blob = new Blob(chunks, { type: 'audio/webm' });
              player.src = URL.createObjectURL(blob);
              chunks = []; // 清空音频数据
            }
          }
        } catch(err) {
          // 处理错误: 比如用户拒绝提供麦克风权限
          console.log(err);
        }
      }

      recordButton.onclick = function() {
        getMedia();
        mediaRecorder.start();
        console.log('开始录音');
      }

      stopButton.onclick = function() {
        mediaRecorder.stop();
        console.log('结束录音');
      }
    },
    historyChat(user) {

      request.post('/chatHistory/findAll',{
        fromUser:this.user.username,
        toUser:user.username
      }).then(res=>{
        this.historyMessage=res.data
        console.log(this.historyMessage)

        this.content=''
        this.chatUser=user.username
        for (let i=0;i<this.historyMessage.length;i++){

          if(this.historyMessage[i].from == this.chatUser){
            this.createContent(this.historyMessage[i].from, null, this.historyMessage[i].text)
          }
          else{
            this.createContent(null, this.historyMessage[i].from, this.historyMessage[i].text)
          }
        }


        //让聊天记录显示在下面一条
        console.log('chat')
        console.log(this.$el.querySelector(".chat-content"));
        this.$nextTick(() => {
          let container = this.$el.querySelector(".chat-content");
          console.log('con')
          console.log(container)
          container.scrollTop = container.scrollHeight;
        })

      })


    },


    send() {
      if (!this.chatUser) {
        this.$message({type: 'warning', message: "请选择聊天对象"})
        return;
      }
      if (!this.text) {
        this.$message({type: 'warning', message: "请输入内容"})
      } else {
        if (typeof (WebSocket) == "undefined") {
          console.log("您的浏览器不支持WebSocket");
        } else {
          console.log("您的浏览器支持WebSocket");
          // 组装待发送的消息 json
          // {"from": "zhang", "to": "admin", "text": "聊天文本"}
          let message = {from: this.user.username, to: this.chatUser, text: this.text}



          socket.send(JSON.stringify(message));  // 将组装好的json发送给服务端，由服务端进行转发
          request.post('/chatHistory/add',{
            message:message
          }).then(res=>{
            console.log("保存成功")
          })

          this.messages.push({user: this.user.username, text: this.text})


          // console.log("消息记录s")
          // console.log(this.messages)
          // 构建消息内容，本人消息
          this.createContent(null, this.user.username, this.text)
          this.text = '';
        }
      }

      //让聊天记录显示在下面一条
      console.log('chat')
      console.log(this.$el.querySelector(".chat-content"));
      this.$nextTick(() => {
        let container = this.$el.querySelector(".chat-content");
        console.log('con')
        console.log(container)
        container.scrollTop = container.scrollHeight;
      })

    },
    createContent(remoteUser, nowUser, text) {  // 这个方法是用来将 json的聊天消息数据转换成 html的。
      let html
      // 当前用户消息
      if (nowUser) { // nowUser 表示是否显示当前用户发送的聊天消息，绿色气泡
        html = "<div class=\"el-row\" style=\"padding: 5px 0\">\n" +
            "  <div class=\"el-col el-col-22\" style=\"text-align: right; padding-right: 10px\">\n" +
            "    <div class=\"tip left\">" + text + "</div>\n" +
            "  </div>\n" +
            "  <div class=\"el-col el-col-2\">\n" +
            "  <span class=\"el-avatar el-avatar--circle\" style=\"height: 40px; width: 40px; line-height: 40px;\">\n" +
            "    <img src=\"https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png\" style=\"object-fit: cover;\">\n" +
            "  </span>\n" +
            "  </div>\n" +
            "</div>";
      } else if (remoteUser) {   // remoteUser表示远程用户聊天消息，蓝色的气泡
        html = "<div class=\"el-row\" style=\"padding: 5px 0\">\n" +
            "  <div class=\"el-col el-col-2\" style=\"text-align: right\">\n" +
            "  <span class=\"el-avatar el-avatar--circle\" style=\"height: 40px; width: 40px; line-height: 40px;\">\n" +
            "    <img src=\"https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png\" style=\"object-fit: cover;\">\n" +
            "  </span>\n" +
            "  </div>\n" +
            "  <div class=\"el-col el-col-22\" style=\"text-align: left; padding-left: 10px\">\n" +
            "    <div class=\"tip right\">" + text + "</div>\n" +
            "  </div>\n" +
            "</div>";
      }
      this.content += html;
    },
    init() {
      this.user = sessionStorage.getItem("user") ? JSON.parse(sessionStorage.getItem("user")) : {}
      let username = this.user.username;
      let _this = this;
      if (typeof (WebSocket) == "undefined") {
        console.log("您的浏览器不支持WebSocket");
      } else {
        console.log("您的浏览器支持WebSocket");
        let socketUrl = "ws://124.70.51.6:9090/imserver/" + username;
        if (socket != null) {
          socket.close();
          socket = null;
        }
        // 开启一个websocket服务
        socket = new WebSocket(socketUrl);
        //打开事件
        socket.onopen = function () {
          console.log("websocket已打开");

          // if(_this.chatFlag){
          //   for (let i=0;i<3;i++){
          //     if(_this.historyMessage[i].from == username){
          //       _this.createContent(null, _this.historyMessage[i].from, _this.historyMessage[i].text)
          //     }
          //     else{
          //       _this.createContent(_this.historyMessage[i].from, null, _this.historyMessage[i].text)
          //     }
          //   }
          // }

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
</style>
