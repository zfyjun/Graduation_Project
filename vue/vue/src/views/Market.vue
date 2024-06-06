<template>
  <div style="padding: 2%" v-loading="trianloading" >
    <div @click="handerVideo(1)" >
      市场分析：
    </div>
    <el-form ref="form"  label-width="100px">
      <div style="display: flex">
        <el-form-item label="选择市场：">
          <el-select @change="changemarket" v-model="value" filterable  placeholder="请选择">
            <el-option
                v-for="item in marketname"
                :key="item.id"
                :label="item.marketname"
                :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item style="margin-left: 5%" label="市场预测：">
          <div style="display: flex">
            <el-select @change="changemodel" v-model="modelvalue" filterable  placeholder="选择模型">
              <el-option
                  v-for="item in modelname"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id">
              </el-option>
            </el-select>
            <div style="margin-left: 2%">
              <el-button @click="makedata" >确定</el-button>
            </div>
            <div style="margin-left: 5%">
              <el-popover
                  placement="top-start"
                  title="提示"
                  width="200"
                  trigger="hover"
                  content="查看保存的预测数据与数据库中的预测数据对比">
                <el-button slot="reference" @click="predicttable()" type="primary" >预测列表</el-button>
              </el-popover>
            </div>
          </div>
        </el-form-item>

      </div>
      <el-form-item label="时间选择：">
        <div class="block">
          <el-date-picker
              v-model="value1"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="yyyy-MM-dd"
              :picker-options="dateRange"
              @change="timeSelect"
          >
          </el-date-picker>
        </div>
      </el-form-item>

    </el-form>

    <el-card class="box-card">
      <div style="display: flex;justify-content: space-between">
        <span>市场加权平均值数据折线图</span>
        <el-button  size="medium" @click="opendig()" type="text">详情</el-button>
      </div>
      <div id="market" style="width: 100%;height: 400px" ></div>
    </el-card>


<!--==========================================================================================-->
    <el-dialog
        :visible.sync="showdetailflag"
        width="80%"
        :before-close="handleClose"
        close-on-press-escape="true"
    >
      <div style="padding: 2%;width: 100%">
        <div id="market2" style="width: 100%;height: 400px" ></div>
      </div>
    </el-dialog>

    <el-dialog
        :title="'市场预测('+modelnameone+')'"
        :visible.sync="predictflag"
        width="60%"
        close-on-press-escape="true"
    >
      <div style="padding: 2%;width: 100%" v-loading="repredictloading">
        <div style="margin-bottom: 2%">
          <span>{{'模型准确率（预测数据与原数据相差5%以内为准确预测）：'+workquit+'%'}}
             <el-popover
                 placement="top-start"
                 title="提示"
                 width="200"
                 trigger="hover"
                 content="暂时保存这一结果，如果需要变动请重新预测">
                  <el-button  slot="reference" type="text" size="medium" @click="save" >保存<i class="el-icon-edit-outline"></i></el-button>
              </el-popover>
              <el-button style="margin-left: 2%"  slot="reference" type="text" size="medium" @click="reperdict" >重新预测<i class="el-icon-edit-outline"></i></el-button>
          </span>
        </div>
        <div id="market3" style="width: 100%;height: 300px" ></div>
        <div style="padding-top: 2%">
          <div style="display: flex;margin-top: 3%">
            <span>{{'预测平均值: '+this.pingjun}}</span>
            <span style="margin-left: 2%" >{{'预测最小值: '+this.min}}</span>
            <span style="margin-left: 2%" >{{'预测最大值: '+this.max}}</span>
          </div>
          <div>
            <span>{{'与市场最新数据差距: '+caju+'%'}}</span>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog
        title="预测表"
        :visible.sync="dialogVisible"
        width="30%"
    >
      <div style="padding: 2%" v-loading="addloading" >
          <el-select style="width: 40%" @change="changemarketpredict" v-model="predictmarketvalue" filterable  placeholder="请选择">
            <el-option
                v-for="item in looksavemarktpredict"
                :key="item.market.id"
                :label="item.market.marketname"
                :value="item.market.id"
                :disabled="item.mdisable"
            >
            </el-option>
          </el-select>
        <el-select v-if="nowdata.market.length!=0" style="margin-left: 5%;width: 40%"  @change="changemarketpredict2" v-model="predictmoldevalue" filterable  placeholder="请选择">
          <el-option
              v-for="item in nowdata.save"
              :key="item.id"
              :label="item.name"
              :value="item.id"
              :disabled="nowdata.disable[item.id]"
          >
          </el-option>
         </el-select>
        <div v-if="nowdata.market.length!=0" style="margin: 0 auto" >
          <div style="margin-top: 2%">{{'原最新数据：'+nowdata.market.nowdata}}</div>
          <div style="margin-top: 2%" >{{' 数据库内预测数据：'+nowdata.market.predictdata}}</div>
          <div style="margin-top: 2%;display: flex" >
            <span  >{{'当前模型预测数据：'+nowdata.save[predictmoldevalue].pnum}}
            </span>
          </div>
          <el-popconfirm
              title="确定要把最新预测的数据导入数据库吗，原预测数据会被删除！"
              @confirm="sureadd()"
          >
            <el-button slot="reference" type="text" style="margin-left: 2%"  >导入数据库</el-button>
          </el-popconfirm>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
         <el-button @click="dialogVisible = false">取 消</el-button>
         <el-button type="primary" @click="dialogVisible = false">确 定</el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>
import request from "@/utils/request";
import pyrequest from "@/utils/pyrequest";
import * as echarts from 'echarts';
export default {
  name: "Market",
  data(){
    return{
      startdate:20230101,
      endate:20230313,
      value1:['2023-01-01','2023-3-13'],
      modelname:[{id:0,name:"bp神经网络"},{id:1,name:"卷积神经网络"},{id:2,name:"循环神经网络"}],
      marketDate:[],
      marketname:[],
      showdetailflag:false,
      marketAdjustedclose:[],
      legend:{data: []},
      timedate:{
        type: 'category',
        boundaryGap: false,
        data: []
      },
      value:'',
      seriesmarketone:[],
      seriesmarketone2:[],
      mid:1,
      modelvalue:0,
      predictflag:false,
      predictData:[],
      etime:'',
      btime:'',
      dateRange:this.limitDate(),
      testdata:[],
      trianloading:false,
      workquit:0,
      pingjun:0,
      max:0,
      min:0,
      workbie3number:[],
      caju:0,
      modelnameone:'bp神经网络',
      marketsave:[],
      repredictloading:false,
      dialogVisible:false,
      looksavemarktpredict:[],
      predictmarketvalue:'',
      predictmoldevalue:0,
      nowdata:{"market":[],"save":[],"disable":[],"mdisable":''},
      addloading:false
    }
  },
  created() {
    // 从后台获取数据
    this.getmarketname()
    this.getmarketDateTime(0)

  },
  mounted(){//图形绘制
    this.getmarketDateTime(1)
  },
  methods: {
    opendig(){
      this.showdetailflag=true
      this.$nextTick(()=>{
        this.workbie2(this.seriesmarketone2)
      })
    },
    makedata(){//制作训练集与测试集
      this.trianloading=true
      var reg1 = new RegExp("-","g")
      let btime=Number(this.btime.replace(reg1,""))
      let etime=Number(this.etime.replace(reg1,""))
      this.testdata=[]
      request.post("/market/getMarketDatebyId",{
          mid:this.mid,
          startTime:btime,
          endtiem:etime
        }).then(res => {
          if(res.code==='200'){
            let length=res.data.length
            this.testdata=res.data.slice((length*0.9).toFixed(0),length)
            console.log(this.marketsave)
            console.log(this.mid)
            console.log(this.modelvalue)
            if(this.marketsave[this.mid-1][this.modelvalue].pnum==0){//没有预测的数据保存
              this.marketPredict(res.data)
            }
            else {//有预测的数据保存
              this.workbie3number=[]
              this.workbie3number=this.testdata.slice((this.testdata.length-27).toFixed(0),this.testdata.length)
              this.pingjun=this.marketsave[this.mid-1][this.modelvalue].pnum
              this.max=this.marketsave[this.mid-1][this.modelvalue].maxnum
              this.min=this.marketsave[this.mid-1][this.modelvalue].minnum
              this.workbie3number.push({'date':'预测数据（平均）','adjustedclose':this.pingjun})
              this.workbie3number.push({'date':'预测数据（最低）','adjustedclose':this.min})
              this.workbie3number.push({'date':'预测数据（最大）','adjustedclose':this.max})
              let datetime=[],date=[]
              for(let i=0;i<this.workbie3number.length;i++){
                datetime.push(this.workbie3number[i].date)
                date.push(this.workbie3number[i].adjustedclose)
              }
              this.trianloading=false
              this.predictflag=true
              this.$nextTick(()=>{
                this.workbie3(datetime,date)
              })
            }
          }
        })
    },
    marketPredict(data){//市场预测
      pyrequest.post("/train",{
        type:this.modelvalue,
        data:data,
      }).then(res => {
        if(res.data.code=="200"){
          if(this.modelvalue==0||this.modelvalue==1){
            this.predictData=res.data.data
            console.log(this.predictData)
          }
          else {
            this.predictData=[]
            let pp=res.data.data.data
            for(let i=0;i<pp.length;i++){
              this.predictData.push(Number(pp[i][1]))
            }
            console.log(this.predictData)
          }
          this.workquits()
        }
        else {
          this.trianloading=false
        }
      })
    },
    workquits(){//计算准确率,最低、最高、平均值
      let sum=0,sumss=0;
      this.workquit=0
      this.max=this.predictData[0]
      this.min=this.predictData[0]
      for(let i=0;i<this.testdata.length;i++){
        sumss=sumss+this.predictData[i]
        if(this.testdata[i].adjustedclose*0.6<=this.predictData[i]&&this.predictData[i]<=this.testdata[i].adjustedclose*1.4){
          sum++;
        }
        if(this.min>this.predictData[i]){
          this.min=this.predictData[i]
        }
        if(this.max<this.predictData[i]){
          this.max=this.predictData[i]
        }
      }
      this.pingjun=(sumss/this.testdata.length).toFixed(0)
      this.workquit=((sum/this.predictData.length)*100).toFixed(2)
      this.workbie3number=[]
      this.workbie3number=this.testdata.slice((this.testdata.length-27).toFixed(0),this.testdata.length)
      this.caju=(((this.pingjun-this.workbie3number[this.workbie3number.length-1].adjustedclose)/this.workbie3number[this.workbie3number.length-1].adjustedclose)*100).toFixed(2)
      this.workbie3number.push({'date':'预测数据（平均）','adjustedclose':this.pingjun})
      this.workbie3number.push({'date':'预测数据（最低）','adjustedclose':this.min})
      this.workbie3number.push({'date':'预测数据（最大）','adjustedclose':this.max})
      let datetime=[],date=[]
      for(let i=0;i<this.workbie3number.length;i++){
        datetime.push(this.workbie3number[i].date)
        date.push(this.workbie3number[i].adjustedclose)
      }
      this.trianloading=false
      this.repredictloading=false
      this.predictflag=true
      this.$nextTick(()=>{
        this.workbie3(datetime,date)
      })
      console.log(this.marketsave)
    },
    limitDate(){//时间限制器
      let self =this;
      return{
        disabledDate(time){
          return new Date(time).getTime()<new Date(self.btime).getTime()||new Date(time).getTime()>new Date(self.etime).getTime()
        }
      }
    },
    /*字符串转时间的函数*/
    stringToDate(dateStr,separator){
      if(!separator){
        separator="-";
      }
      let dateArr = dateStr.split(separator);
      let year = parseInt(dateArr[0]);
      let month;
      if(dateArr[1].indexOf("0") == 0){
        month = parseInt(dateArr[1].substring(1));
      }else{
        month = parseInt(dateArr[1]);
      }
      let day = parseInt(dateArr[2]);
      let date = new Date(year,month -1,day);
      return date;
    },
    timeSelect(val){//时间选择
      let start=val[0]
      let ended=val[1]
      start=Number(start.replace(/-/g,''))
      ended=Number(ended.replace(/-/g,''))
      this.startdate=start
      this.endate=ended
      this.getmarketDateTime()
    },
    changemarket(val){//变换市场
      this.mid=val
      this.getmarketDateTime()
    },
    changemolde(val){//变换市场
      this.modelvalue=val
    },
    insertStr(source,start,newStr){//字符串指定位置插入
       return source.slice(0,start) + newStr+source.slice(start)
    },
    getmarketDateTime(val){//获取市场数据时间范围
      request.post("/market/getMarketTimebyId",{
        mid:this.mid
      }).then(res => {
        if(res.code==='200'){
          // console.log(res.data)
          this.btime=String(res.data.btime)
          this.etime=String(res.data.etime)
          this.btime=this.insertStr(this.btime,4,'-')
          this.btime=this.insertStr(this.btime,7,'-')
          this.etime=this.insertStr(this.etime,4,'-')
          this.etime=this.insertStr(this.etime,7,'-')
          this.value1[0]=String(this.btime)
          this.value1[1]=String(this.etime)
          console.log(this.btime+"  "+this.etime)
          console.log(this.value1)
          if(val!=0){
            this.getmarketDate()

          }
        }

      })
    },
    getmarketDate(){//获取市场数据并绘制图片
      this.seriesmarketone=[]
      this.seriesmarketone2=[]
      request.post("/market/getMarketDatebyId",{
        mid:this.mid,
        startTime:this.startdate,
        endtiem:this.endate
      }).then(res => {
        if(res.code==='200'){
          this.marketDate=res.data
          // console.log(res.data)
          this.workbie(this.getmarketdataone(1,1),this.getmarketdataone(6,1))
          this.addmarketdata()
        }
      })
    },
    addmarketdata(){//添加市场数据的值
      for(let i=2;i<=5;i++){
        this.getmarketdataone(i,2)
      }
    },
    getmarketdataone(val,flag){//获取市场数据中的一个数值数组
      let keyname=''
      let text=''
      let serie={data:[]}
      this.seriesmarketone=[]
      if(val!=1){
        serie={
          name: '',
          type: 'line',
          data: []
        }
      }
      if(val==1){//时间
        keyname='date'
      }
      else if(val==2){//开盘价
        text='开盘价'
        keyname='open'
      }
      else if(val==3){//最高价
        text='最高价'
        keyname='high'
      }
      else if(val==4){//最低价
        text='最低价'
        keyname='low'
      }
      else if(val==5){//收盘价
        text='收盘价'
        keyname='close'
      }
      else if(val==6){//加权平均值
        text='加权平均值'
        keyname='adjustedclose'
      }
      for(let i=0;i<this.marketDate.length;i++){
        for(var key in this.marketDate[i]){
          if(key==keyname){
            serie.data.push(this.marketDate[i][key])
          }
        }
      }
      if(val==1){//时间数据
        this.timedate.data=serie.data
        return this.timedate
      }
      else if(val!=1){
        if(flag==1){
          this.seriesmarketone.push(serie)
        }
        else if(flag==2){
          if(this.legend.data.length<4){
            this.legend.data.push(text)
          }
          serie.name=text
          this.seriesmarketone2.push(serie)
          return this.seriesmarketone2
        }
        return this.seriesmarketone
      }
      return -1
    },
    getmarketname(){//获取现有的市场数据
      request.post("/MarketName/getMarketNames",{
      }).then(res => {
        if(res.code==='200'){
          this.marketname=res.data
          this.value=res.data[0].marketname
          this.mid=res.data[0].id
          this.marketsave=[]
          for(let i=0;i<this.marketname.length;i++){
            this.marketsave.push([{id:0,name:"bp神经网络",pnum:0,maxnum:0,minnum:0,caju:0},{id:1,name:"卷积神经网络",pnum:0,maxnum:0,minnum:0,caju:0},{id:2,name:"循环神经网络",pnum:0,maxnum:0,minnum:0,caju:0}])
          }
        }
      })
    },
    workbie(timedate,seriesmarketone){//绘制折线图（市场数据的平均加权值）
      let myChart = echarts.getInstanceByDom(document.getElementById("market"))
      // 如果不存在，就进行初始化
      if (myChart == null) {
        myChart = echarts.init(document.getElementById("market"));
      }
      let option2;
      option2 = {
        tooltip: {
          trigger: 'axis'
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        toolbox: {
          feature: {
            saveAsImage: {}
          }
        },
        xAxis: timedate,
        yAxis: {
          type: 'value'
        },
        series: seriesmarketone
      };
      option2 && myChart.setOption(option2);
    },
    workbie2(seriesmarketone){//绘制折线图（市场数据的平均加权值）
      let myChart = echarts.getInstanceByDom(document.getElementById("market2"))
          // 如果不存在，就进行初始化
      if (myChart == null) {
        myChart = echarts.init(document.getElementById("market2"));
      }
      let option;
      option = {
        title: {
          text: '市场数据详情折线图'
        },
        tooltip: {
          trigger: 'axis'
        },
        legend: this.legend,
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        toolbox: {
          feature: {
            saveAsImage: {}
          }
        },
        xAxis: this.timedate,
        yAxis: {
          min:(value)=>{
            return (value.min-value.min*0.02).toFixed(0)
          },
          max:(value)=>{
            return (value.max+value.max*0.02).toFixed(0)
          }
        },
        series: seriesmarketone
      };
      option && myChart.setOption(option);
      // console.log(this.legend)
    },
    workbie3(datatime,data){
      let myChart = echarts.getInstanceByDom(document.getElementById("market3"))
      // 如果不存在，就进行初始化
      if (myChart == null) {
        myChart = echarts.init(document.getElementById("market3"));
      }
      let option;
      option = {
        title: {
          text: '市场走向预测折线图',
          subtext: '市值'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          }
        },
        toolbox: {
          show: true,
          feature: {
            saveAsImage: {}
          }
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          // prettier-ignore
          data: datatime
        },
        yAxis: {
          type: 'value',
          axisLabel: {
            formatter: '{value} '
          },
          axisPointer: {
            snap: true
          }
        },
        visualMap: {
          show: false,
          dimension: 0,
          pieces: [
            {
              gt: 0,
              lte: 26,
              color: 'green'
            },
            {
              gt: 26,
              color: 'red'
            },
          ]
        },
        series: [
          {
            name: '市场带权平均值',
            type: 'line',
            smooth: true,
            data: data,
            markArea: {
              itemStyle: {
                color: 'rgba(255, 173, 177, 0.4)'
              },
              data: [
                [
                  {
                    name: '预测数据',
                    xAxis:'预测数据（平均）'
                  },
                  {
                    xAxis: '预测数据（最大）'
                  }
                ],
              ]
            }
          }
        ]
      };

      option && myChart.setOption(option);
    },
    changemodel(e){//切换模型
      for(let i=0;i<this.modelname.length;i++){
        if(this.modelname[i].id==e){
          this.modelnameone=this.modelname[i].name
          break
        }
      }
    },
    save(){//保存预测结果
      console.log(this.marketsave[Number(this.mid-1)])
      this.marketsave[Number(this.mid-1)][Number(this.modelvalue)].pnum=Number(this.pingjun)
      this.marketsave[Number(this.mid-1)][Number(this.modelvalue)].maxnum=this.max
      this.marketsave[Number(this.mid-1)][Number(this.modelvalue)].minnum=this.min
      this.marketsave[Number(this.mid-1)][Number(this.modelvalue)].caju=this.caju
      this.$message({
        showClose: true,
        message: '保存成功！',
        type: 'success'
      })
    },
    reperdict(){//重新预测
      this.repredictloading=true
      let reg1 = new RegExp("-","g")
      let btime=Number(this.btime.replace(reg1,""))
      let etime=Number(this.etime.replace(reg1,""))
      this.testdata=[]
      request.post("/market/getMarketDatebyId",{
        mid:this.mid,
        startTime:btime,
        endtiem:etime
      }).then(res => {
        if(res.code==='200'){
          let length=res.data.length
          this.testdata=res.data.slice((length*0.9).toFixed(0),length)
          this.marketPredict(res.data)
        }
      })
    },
    changemarketpredict(e){//预测表内切换市场
      this.nowdata=this.looksavemarktpredict[e-1]
    },
    changemarketpredict2(e){//预测表内切换模型
      this.predictmoldevalue=e
    },
    predicttable(){//打开预测表
      this.looksavemarktpredict=[]
      for(let i=0;i<this.marketsave.length;i++){
        let aa={"market":JSON.parse(JSON.stringify(this.marketname[i])),"save":JSON.parse(JSON.stringify(this.marketsave[i])),"disable":[],"mdisable":''}
        let flag=0
        for(let j=0;j<this.marketsave[i].length;j++){
          if(this.marketsave[i][j].pnum!=0){//存在数据
            flag=1
            aa.disable[j]=false
            if(this.predictmarketvalue==''){
              this.predictmarketvalue=i+1
            }
          }
          else {//不存在数据
            aa.disable[j]=true
          }
        }
        if(flag==0){//两个都不存在数据
          console.log('不存在')
          aa.mdisable=true
        }
        else {
          aa.mdisable=false
        }
        this.looksavemarktpredict.push(aa)
      }
      console.log(this.looksavemarktpredict)
      if(this.predictmarketvalue!=''){
        this.nowdata=this.looksavemarktpredict[this.predictmarketvalue-1]
        console.log(this.nowdata)
      }
      this.dialogVisible=true
    },
    sureadd(){//确定上传到数据库
      this.addloading=true
      console.log(this.nowdata.save[this.predictmoldevalue].pnum)
      request.post("/market/updatepredict",{
        mid:this.nowdata.market.id,
        predict:this.nowdata.save[this.predictmoldevalue].pnum
      }).then(res => {
        if(res.code==='200'){
          this.nowdata.market=res.data
          this.looksavemarktpredict[this.mid-1].market=res.data
          this.marketname[this.mid-1]=JSON.parse(JSON.stringify(res.data))
          this.addloading=false
          this.$message({
            showClose: true,
            message: '导入成功！',
            type: 'success'
          })

        }
      })
    }
  },
}
</script>

<style scoped>

</style>