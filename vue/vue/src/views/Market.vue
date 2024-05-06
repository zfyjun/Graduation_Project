<template>
  <div style="padding: 2%" >

    <el-form ref="form"  label-width="100px">
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
  </div>
</template>

<script>
import request from "@/utils/request";
import * as echarts from 'echarts';
export default {
  name: "Market",
  data(){
    return{
      dateRange:this.limitDate(),
      startdate:20230101,
      endate:20230313,
      value1:['2023-01-01','2023-03-13'],
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
    }
  },
  created() {
    // 从后台获取数据
    this.getmarketname()

  },
  mounted(){//图形绘制
    this.getmarketDate()
  },
  methods: {
    opendig(){
      this.showdetailflag=true
      this.$nextTick(()=>{
        this.workbie2(this.seriesmarketone2)
      })
    },
    limitDate(){//时间限制器
      return{
        disabledDate(time){
          return new Date(time).getTime()<new Date('2018-1-1')||new Date(time).getTime()>new Date('2023-3-13').getTime()
        }
      }
    },
    timeSelect(val){//时间选择
      let start=val[0]
      let ended=val[1]
      start=Number(start.replace(/-/g,''))
      ended=Number(ended.replace(/-/g,''))
      if(ended-start>=30){
        this.startdate=start
        this.endate=ended
        this.getmarketDate()
      }

    },
    changemarket(val){//变换市场
      this.mid=val
      this.getmarketDate()
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
      console.log(this.legend)
    }
  },
}
</script>

<style scoped>

</style>