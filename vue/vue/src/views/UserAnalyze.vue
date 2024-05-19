<template>
  <div style="padding: 2%" >

    <div>
      <el-menu :default-active="activeIndex" class="el-menu-demo" mode="horizontal" @select="handleSelect">
        <el-menu-item index="1">用户数据探索性分析</el-menu-item>
        <el-menu-item index="2">产品上架</el-menu-item>
        <el-menu-item index="3">产品分析中心</el-menu-item>
      </el-menu>
    </div>


    <div>
    <el-form ref="form"  label-width="100px" v-if="activeIndex==1">
      <el-form-item label="用户基本信息展示：">
        <el-select @change="changeValue" v-model="value" filterable  placeholder="请选择">
          <el-option
              v-for="item in userdataname"
              :key="item"
              :label="item"
              :value="item">
          </el-option>
        </el-select>
      </el-form-item>

    </el-form>

    <el-card class="box-card" v-if="activeIndex==1">
      <div style="display: flex;justify-content: space-between">
<!--        <span>用户数据散点图</span>-->
<!--        <el-button  size="medium" @click="opendig()" type="text">详情</el-button>-->
      </div>
      <div id="chart" style="width: 100%;height: 400px" ></div>
      <div style="display: flex;justify-content: space-between">
        <span v-for="(value, key) in this.describe" :key="key" >{{ key }}: {{ value }}</span>

      </div>
    </el-card>
    </div>


<!--==========================================================================================-->
<!--    <el-dialog-->
<!--        :visible.sync="showdetailflag"-->
<!--        width="80%"-->
<!--        :before-close="handleClose"-->
<!--        close-on-press-escape="true"-->
<!--    >-->
<!--      <div style="padding: 2%;width: 100%">-->
<!--        <div id="chart2" style="width: 100%;height: 400px" ></div>-->
<!--      </div>-->
<!--    </el-dialog>-->
  </div>
</template>

<script>

import * as echarts from 'echarts';

export default {
  name: "UserAnalyze",
  data(){
    return{
      data:{},
      value:'',
      userdataname:[],
      describe:{},
      activeIndex:'1',
    }
  },
  created() {
    // 从后台获取数据
    // this.getUserAnalyze();
    this.getUserAge()


  },
  mounted(){//图形绘制
    // this.getUserAnalyze()
    this.getUserJob()
  },
  methods: {
    load(){
      this.drawChartsAge(this.data.filter(item => item.name === this.value))
    },
    changeValue(val) {
      this.value = val;
      this.drawChartsAge(this.data.filter(item => item.name === this.value))

      let describe1 = this.data.filter(item => item.name === this.value)[0].describe
      if (describe1 !== null && typeof describe1 === 'object') {
        this.describe = describe1
      } else {
        this.describe = JSON.parse(describe1)

      }
      console.log(this.describe)
      // this.describe=this.data.filter(item => item.name === this.value)[0].describe
    },

    handleSelect(key) {
      this.activeIndex=key
    },

    getUserAnalyze(){ //获取用户分析结果并绘制图形
      this.pyrequest.post("/user_analyze",{
      }).then(res => {
        this.data=res.data

        this.drawCharts(this.data.clusters);
      })
    },

    getUserAge(){ //获取用户信息根据用户年龄分布画出图形
      this.pyrequest.post("/user_age",{
      }).then(res => {
        this.data=res.data

        console.log('res')
        console.log(res)
        console.log('res.data')
        console.log(res.data)
        console.log('this.data')
        console.log(this.data)

        //从对象数组中找到所有对象的name属性值形成新的数组
        this.userdataname=res.data.map((function(obj) {
          return obj.name;
        }))
        this.value=this.userdataname[0]
        this.describe=this.data.filter(item => item.name === this.value)[0].describe

        // this.drawChartsAge(this.data.filter(item => item.name === this.value));
      })
    },


    getUserJob(){ //获取用户信息根据用户年龄分布画出图形
      this.pyrequest.post("/user_job",{
        dataname1:'marital',
        dataname2:'education',
      }).then(res => {
        this.data=res.data
        // this.userdataname=res.data.name
        // this.value=this.userdataname[0]

        console.log('res')
        console.log(res)
        console.log('res.data')
        console.log(res.data)
        console.log('this.data')
        console.log(this.data)

        this.drawChartsJob(this.data);
      })
    },


    drawChartsAge(response) {
      console.log("response")
      console.log(response)

      let domElement = document.getElementById('chart');

      let instance = echarts.getInstanceByDom(domElement);
      if (instance) {
        // 如果有，则清除
        echarts.dispose(instance);
      }
      //初始化
      let myChart = echarts.init(domElement);
      let data = response[0][this.value];


      let dataName = data.map(d => d[this.value]);
      let counts = data.map(d => d.count);

      let text=this.value +' Distribution'

      let option = {
        title: {
          text: text
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: [{
          type: 'category',
          data: dataName,
          axisTick: {
            alignWithLabel: true
          }
        }],
        yAxis: [{
          type: 'value'
        }],
        series: [{
          data: counts,
          type: 'bar'
        }]
      };

      // let option = {
      //   title: {
      //     text: text
      //   },
      //   grid: [{
      //     left: '10%',
      //     width: '15%',
      //   }, {
      //     right: '10%',
      //     width: '50%',
      //   }],
      //   xAxis: [{
      //     type: 'category',
      //     data: dataName,
      //     gridIndex: 0,
      //   },{
      //     type: 'category',
      //     data: Object.keys(describe),
      //     gridIndex: 1,
      //   }],
      //   yAxis: [{
      //     type: 'value',
      //     gridIndex: 0,
      //   },{
      //     type: 'value',
      //     gridIndex: 1,
      //   }],
      //   series: [{
      //     data: counts,
      //     type: 'bar',
      //     xAxisIndex: 0,
      //     yAxisIndex: 0,
      //   },{
      //     data: Object.values(describe),
      //     type: 'bar',
      //     xAxisIndex: 1,
      //     yAxisIndex: 1,
      //   }]
      // };

      myChart.setOption(option);
    },

    drawChartsJob(response) {
      console.log("response")
      console.log(response)

      let domElement = document.getElementById('chart');

      let instance = echarts.getInstanceByDom(domElement);
      if (instance) {
        // 如果有，则清除
        echarts.dispose(instance);
      }
      //没有则初始化
      let myChart = echarts.init(domElement);
      // let data = response[this.value];
      let data1 = response.education;
      let data2 = response.loan;


      // let dataName = data.map(d => d[this.value]);
      // let counts = data.map(d => d.count);
      //
      // let text=this.value +' Distribution'

      let option = {
        // title: {
        //   text: text
        // },
        tooltip : {
          trigger: 'axis'
        },
        legend: {
          data:['Education', 'Loan']
        },
        xAxis: {
          type: 'category',
          // type: 'value',
          data: Object.keys(data1)
        },
        yAxis: {
          type: 'value'
        },
        // series: [{
        //   data: data,
        //   type: 'scatter'
        // }]

        series: [{
          name: 'Education',
          data: Object.values(data1),
          type: 'bar'
        },{
          name: 'Loan',
          type: 'bar',
          data: Object.values(data2)
        }]
      };

      myChart.setOption(option);
    },


  // , centers
    drawCharts(data) {
      //初始化
      let myChart = echarts.init(document.getElementById('chart'));
      //
      let seriesData = []
      for (let i = 0; i < data.length; i++) {
        seriesData.push({
          name: 'Cluster ' + (i+1),
          data: data[i],
          type: 'scatter'
        });
      }
      // 将簇中心添加到数据中
      // seriesData.push({
      //   name: 'Centroids',
      //   data: centers,
      //   type: 'scatter',
      //   symbolSize: 12,
      //   itemStyle: {
      //     color: 'black'
      //   }
      // });
      let option = {
        tooltip: {},
        legend: {},
        xAxis: {},
        yAxis: {},
        series: seriesData
      };
      myChart.setOption(option);
    }
  },


}
</script>

<style scoped>

</style>