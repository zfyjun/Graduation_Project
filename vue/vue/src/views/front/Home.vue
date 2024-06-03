<template>
  <div>
    <div style="margin: 10px 0">
      <el-row :gutter="10">
        <el-col :span="6" v-for="item in files" :key="item.id" style="margin-bottom: 10px">
          <div style="border: 1px solid #ccc; padding-bottom: 10px">
            <img :src="item.url" alt="" style="width: 100%">
            <div style="color: #666; padding: 10px">{{ item.name }}</div>
<!--            <div style="padding: 10px"><el-button type="primary">购买</el-button></div>-->
          </div>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
export default {
  name: "FrontHome",
  data() {
    return {
    }
  },
  created() {
    this.request.get("/echarts/file/front/all").then(res => {
      console.log(res.data)
      this.files = res.data.filter(v => v.type === 'png' || v.type === 'jpg' || v.type === 'webp')
    })
  },
  methods: {
    createContent(remoteUser, nowUser, text) {
      return new Promise((resolve, reject) => {
        if (text.type === 'audio') {
          request.get(text.data, {
            responseType: 'blob'
          }).then(res => {
            this.audioBlob = new Blob([res], { type: 'audio/webm' })
            this.audio = URL.createObjectURL(this.audioBlob)
            let audioUrl = this.audio

            let audioTag = `<audio controls>
                      <source src="${audioUrl}" type="audio/webm">
                        你的浏览器不支持audio标签
                      </audio>`;

            let html;
            if (nowUser) {
              html = `<div class="el-row" style="padding: 5px 0">
                  <div class="el-col el-col-22" style="text-align: right; padding-right: 10px">
                    ${audioTag}
                  </div>
                  <div class="el-col el-col-2">
                    <span class="el-avatar el-avatar--circle" style="height: 40px; width: 40px; line-height: 40px;">
                      <img src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" style="object-fit: cover;">
                    </span>
                  </div>
                </div>`;
            } else if (remoteUser) {
              html = `<div class="el-row" style="padding: 5px 0">
                  <div class="el-col el-col-2" style="text-align: right">
                    <span class="el-avatar el-avatar--circle" style="height: 40px; width: 40px; line-height: 40px;">
                      <img src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" style="object-fit: cover;">
                    </span>
                  </div>
                  <div class="el-col el-col-22" style="text-align: left; padding-left: 10px">
                    ${audioTag}
                  </div>
                </div>`;
            }

            this.content += html;
            resolve()
          }).catch(reject)
        } else {
          // 如果不是audio类型，处理其它类型数据
          // ...
          resolve()
        }
      })
    }

  }
}
</script>

<style>

</style>
