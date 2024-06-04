<template>
  <div style="padding: 2%" >

    <div>
      <el-menu :default-active="activeIndex" class="el-menu-demo" mode="horizontal" @select="handleSelect">
        <el-menu-item index="1">人口统计信息分析</el-menu-item>
        <el-menu-item index="2">贷款行为分析</el-menu-item>
        <el-menu-item index="3">K-means算法</el-menu-item>
        <el-menu-item index="4">模型预测</el-menu-item>
        <el-button @click="predict">测试</el-button>
        <el-button @click="train">训练</el-button>
      </el-menu>
    </div>


<!--    人口统计信息页面-->
    <div v-show="activeIndex==1">

      <div>
        <el-form ref="form"  label-width="100px" >
          <el-form-item label="数据选择:">
            <el-select @change="changeValue" v-model="rowname" filterable  placeholder="请选择">
              <el-option
                  v-for="item in userdataname"
                  :key="item"
                  :label="item"
                  :value="item">
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <el-card class="box-card" >
        <div style="display: flex;justify-content: space-between">
  <!--        <span>用户数据散点图</span>-->
  <!--        <el-button  size="medium" @click="opendig()" type="text">详情</el-button>-->
        </div>
        <div id="chart1" style="width: 100%;height: 400px" ></div>
        <div style="display: flex;justify-content: space-between">
          <span v-for="(value, key) in this.describe" :key="key" >{{ key }}: {{ value }}</span>

        </div>
      </el-card>
    </div>


    <!--    人口统计信息页面-->
    <div v-show="activeIndex==2">
      <div>

      </div>

      <el-card class="box-card" >
        <div style="display: flex;justify-content: space-between">
          <!--        <span>用户数据散点图</span>-->
          <!--        <el-button  size="medium" @click="opendig()" type="text">详情</el-button>-->
        </div>
        <div id="chart2" style="width: 100%;height: 400px" ></div>
<!--        <div style="display: flex;justify-content: space-between">-->
<!--          <span v-for="(value, key) in this.describe" :key="key" >{{ key }}: {{ value }}</span>-->

<!--        </div>-->
      </el-card>
    </div>


    <!--    K-means算法-->
    <div v-show="activeIndex==3">
      <div>

      </div>

      <el-card class="box-card" >
        <div style="display: flex;justify-content: space-between">
          <!--        <span>用户数据散点图</span>-->
          <!--        <el-button  size="medium" @click="opendig()" type="text">详情</el-button>-->
        </div>
        <div id="chart3" style="width: 100%;height: 400px" ></div>
      </el-card>
    </div>

    <div v-show="activeIndex==4">
      <div>
        <el-table :data="tableData" border style="width: 100%">
          <el-table-column prop="ID" label="id"></el-table-column>
          <el-table-column prop="age" label="age"></el-table-column>
          <el-table-column prop="education" label="education"></el-table-column>
          <el-table-column prop="job" label="job"></el-table-column>
          <el-table-column prop="marital" label="marital"></el-table-column>
          <el-table-column sortable prop="y" label="可能性" width="200" align="center"></el-table-column>
        </el-table>
      </div>

<!--      <el-card class="box-card" >-->
<!--        <div style="display: flex;justify-content: space-between">-->
<!--          &lt;!&ndash;        <span>用户数据散点图</span>&ndash;&gt;-->
<!--          &lt;!&ndash;        <el-button  size="medium" @click="opendig()" type="text">详情</el-button>&ndash;&gt;-->
<!--        </div>-->
<!--        <div id="chart4" style="width: 100%;height: 400px" ></div>-->
<!--      </el-card>-->
    </div>



  </div>
</template>

<script>

import * as echarts from 'echarts';

export default {
  name: "UserAnalyze",
  data(){
    return{
      data:{},
      typename:[
        {name:'单变量分析',value:0},
        {name:'多变量分析',value:1}
      ],
      typeValue:0,
      rowname:'',
      userdataname:[],
      describe:{},
      activeIndex:'1',
      chart1:null,
      chart2:null,
      chart3:null,
      chart4:null,
      tableData: {},
    }
  },
  created() {
    // 从后台获取数据
    // this.getUserAnalyze()
    this.getUserAge()

  },
  mounted(){//图形绘制
    // this.getUserAnalyze()
    // this.getUserJob()
  },
  methods: {
    load(){
      if(this.activeIndex=='1'){
        this.getUserAge()
        this.changeValue('age')
      } else if(this.activeIndex=='2'){
        this.getUserJob()
      } else if(this.activeIndex=='3'){
        this.getUserAnalyze()
      } else if(this.activeIndex=='4'){
        this.predict()
      }
    },

    //选择用户信息属性中的某一项值
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
    },

    handleSelect(key) {
      this.activeIndex=key
      this.load()
    },

    train(){
      this.pyrequest.post("/train",{

      }).then(res=>{
        alert("提示")
        this.data=res.data
        console.log(res)
      })
    },
    predict(){
      this.pyrequest.post("/predict",{

      }).then(res=>{
        // alert("提示")
        this.data=res.data.data
        console.log(res.data)
        let json1=JSON.parse(res.data.data)
        let json2=JSON.parse(res.data.proba)
        for (let i=0;i<json1.length;i++){
          json1[i].y=(json2[i][1]*100).toFixed(2)+'%'
        }
        this.tableData=json1
        // console.log(json1)
        console.log(this.tableData)
        this.drawChartModel(this.data)
      })
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
        //从对象数组中找到所有对象的name属性值形成新的数组
        this.userdataname=res.data.map((function(obj) {
          return obj.name;
        }))
        this.value=this.userdataname[0]
        this.describe=this.data.filter(item => item.name === this.value)[0].describe
      })
    },

    getUserJob(){ //贷款行为分析
      this.pyrequest.post("/user_job").then(res => {
        this.data=res.data
        this.drawChartsJob(this.data);
      })
    },

    drawChartsAge(response) {
      let domElement = document.getElementById('chart1');

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
      myChart.setOption(option);
    },

    drawChartsJob(response) {
      let keys = Object.keys(response);
      let corrData = keys.map(key1 => keys.map(key2 => response[key1][key2]));

      var correlation = [
        ['job', 'marital', 'education', 'default', 'balance', 'loan'],
        corrData  // 你从后端获取的相关性数据
      ]
      var data = [];
      for (var i = 0; i < corrData.length; i++) {
        for (var j = 0; j < corrData.length; j++) {
          data.push([(i), (j), corrData[i][j] || '-']);
        }
      }
      let domElement = document.getElementById('chart2');

      let instance = echarts.getInstanceByDom(domElement);
      if (instance) {
        // 如果有，则清除
        echarts.dispose(instance);
      }
      //没有则初始化
      let myChart = echarts.init(domElement);

      let option = {
        tooltip: {
          position: 'top'
        },
        grid: {
          height: '50%',
          y: '10%'
        },
        xAxis: {
          type: 'category',
          data: correlation[0],
          splitArea: {
            show: true
          }
        },
        yAxis: {
          type: 'category',
          data: correlation[0],
          splitArea: {
            show: true
          }
        },
        visualMap: {
          min: -1,
          max: 1,
          calculable: true,
          orient: 'horizontal',
          left: 'center',
          top: 'top'
        },
        series: [{
          name: 'Correlation',
          type: 'heatmap',
          data: data,
          label: {
            show: true
          },
          itemStyle: {
            emphasis: {
              shadowBlur: 10,
              shadowColor: 'rgba(0, 0, 0, 0.5)'
            }
          }
        }]
      };
      myChart.setOption(option);
    },

    drawCharts(data) {
      //初始化
      let myChart = echarts.init(document.getElementById('chart3'));
      //
      let seriesData = []
      for (let i = 0; i < data.length; i++) {
        seriesData.push({
          name: 'Cluster ' + (i+1),
          data: data[i],
          type: 'scatter'
        });
      }
      let option = {
        tooltip: {},
        legend: {},
        xAxis: {},
        yAxis: {},
        series: seriesData
      };
      myChart.setOption(option);
    },

    drawChartModel(data) {
      let domElement = document.getElementById('chart4');

      let instance = echarts.getInstanceByDom(domElement);
      if (instance) {
        // 如果有，则清除
        echarts.dispose(instance);
      }
      //没有则初始化
      let myChart = echarts.init(domElement);

      let option = {
        xAxis: {
          type: 'category',
          data: [], // 根据你的数据将这里填充
        },
        yAxis: {
          type: 'value',
        },
        series: [
          {
            type: 'bar',
            data: [], // 根据你的数据将这里填充
          },
        ],
      };
      myChart.setOption(option);
    }


  },


}
</script>

<style scoped>

</style>