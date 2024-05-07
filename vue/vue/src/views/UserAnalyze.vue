<template>
  <div style="padding: 2%" >

    <el-form ref="form"  label-width="100px">
      <el-form-item label="用户分析：">
      </el-form-item>

    </el-form>

    <el-card class="box-card">
      <div style="display: flex;justify-content: space-between">
        <span>用户数据散点图</span>
        <el-button  size="medium" @click="opendig()" type="text">详情</el-button>
      </div>
      <div id="chart" style="width: 100%;height: 400px" ></div>
    </el-card>


<!--==========================================================================================-->
    <el-dialog
        :visible.sync="showdetailflag"
        width="80%"
        :before-close="handleClose"
        close-on-press-escape="true"
    >
      <div style="padding: 2%;width: 100%">
        <div id="chart2" style="width: 100%;height: 400px" ></div>
      </div>
    </el-dialog>
  </div>
</template>

<script>

import * as echarts from 'echarts';

export default {
  name: "UserAnalyze",
  data(){
    return{
      data:{},
    }
  },
  created() {
    // 从后台获取数据
    this.getUserAnalyze()

  },
  // mounted(){//图形绘制
  //   this.getUserAnalyze();
  // },
  methods: {
    getUserAnalyze(){ //获取用户分析结果并绘制图形
      this.pyrequest.post("/user_analyze",{
      }).then(res => {
          this.data=res.data
        console.log(res)
        console.log(res.data)
        console.log(this.data)
          this.drawCharts(this.data.clusters,this.data.centers);
      })
    },


    drawCharts(clusters, centers) {
      let myChart = echarts.init(document.getElementById('chart'));
      let seriesData = []
      for (let i = 0; i < clusters.length; i++) {
        seriesData.push({
          name: 'Cluster ' + (i+1),
          data: clusters[i],
          type: 'scatter'
        });
      }
      // 将簇中心添加到数据中
      seriesData.push({
        name: 'Centroids',
        data: centers,
        type: 'scatter',
        symbolSize: 12,
        itemStyle: {
          color: 'black'
        }
      });
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