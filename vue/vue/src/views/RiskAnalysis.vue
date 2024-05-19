<template>
  <div v-loading="loading">
    <div style="padding: 2%">
      <div class="demo-input-suffix" style="display: flex" >
        <el-input
            clearable
            style="width: 15%"
            placeholder="请输入姓名"
            prefix-icon="el-icon-search"
            v-model=name>
        </el-input>
        <el-button  style="margin-left: 5%" plain type="primary" @click="serchUser()">用户搜索</el-button>
        <el-button  style="margin-left: 5%" plain type="primary" @click="dialogVisible=true">风险分析<i class="el-icon-edit"></i></el-button>
        <el-button  style="margin-left: 5%" plain type="primary" @click="getpictrue()">数据视图</el-button>
      </div>
      <el-table stripe :data="tableData" border style="width: 100%;margin-top: 1%">
        <el-table-column prop="id" label="id" width="50"></el-table-column>
        <el-table-column prop="risk" label="风险等级"  sortable>
          <template slot-scope="scope">
            <h4  v-if="scope.row.risk<65&&scope.row.risk>=58" style="color: orange" >
              {{scope.row.risk}}
            </h4>
            <h4  v-if="scope.row.risk>=65" style="color: limegreen" >
              {{scope.row.risk}}
            </h4>
            <h4  v-if="scope.row.risk<58" style="color: red" >
              {{scope.row.risk}}
            </h4>
          </template>
        </el-table-column>
        <el-table-column prop="usertype" label="是否为系统用户"></el-table-column>
        <el-table-column prop="name" label="姓名"></el-table-column>
        <el-table-column prop="account" label="账号"></el-table-column>
        <el-table-column prop="idcard" label="身份证号" width="150"></el-table-column>
        <el-table-column prop="phone" label="联系电话"></el-table-column>
        <el-table-column prop="lasttime" label="上次登录时间" sortable></el-table-column>
        <el-table-column label="操作" width="200" align="center">

          <template slot-scope="scope">
            <el-button type="text" @click="handleEdit(scope.row)">风险分析 <i class="el-icon-edit"></i> </el-button>
          </template>
        </el-table-column>
      </el-table>
     </div>
    <div style="padding: 10px 0">
      <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pageNum"
          :page-sizes="[3, 5, 10]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total">
      </el-pagination>
    </div>
    <div>
      <el-dialog
          v-loading="loading2"
          title="分析数据占比"
          :visible.sync="dialogVisible"
          width="30%"
          @close="closeDialog"
      >
        <div style="display: flex">
          <div >分析范围：</div>
          <el-select v-model="analysis.type" placeholder="分析范围">
            <el-option v-if="analysis.id==0" label="全部用户分析" :value='1'></el-option>
            <el-option v-if="analysis.id==0" label="系统用户分析" :value='2'></el-option>
            <el-option v-if="analysis.id!=0" label="单用户分析" :value='3'></el-option>
          </el-select>
        </div>
        <div v-if="analysis.id!=0" style="display: flex;margin-top: 2%;margin-bottom: 2%">
          <div >选定用户：</div>
          <h4>{{usermsg.name}}</h4>
        </div>
        <div >
          <div style="padding: 3%;">
            <h4>风险得分计算公式：</h4>
            <span>r1*用户资产得分 + r2*用户信用得分 + r3*用户贷款历史得分 + r4*用户信息得分</span>
          </div>
          <div>
        </div>
        </div>
        <div style="margin-top: 2%">
          <h4>调整系数r</h4>
          <div style="display: flex;margin-top: 2%">
            <span style="margin-right: 2%">r1: </span>
            <el-input-number v-model="r1" :precision="2" :step="0.01" step-strictly="true" :min="0" :max="1"></el-input-number>
          </div>
          <div style="display: flex;margin-top: 2%">
            <span style="margin-right: 2%">r2: </span>
            <el-input-number v-model="r2" :precision="2" :step="0.01" step-strictly="true" :min="0" :max="1"></el-input-number>
          </div>
          <div style="display: flex;margin-top: 2%">
            <span style="margin-right: 2%">r3: </span>
            <el-input-number v-model="r3" :precision="2" :step="0.01" step-strictly="true" :min="0" :max="1"></el-input-number>
          </div>
          <div style="display: flex;margin-top: 2%">
            <span style="margin-right: 2%">r4: </span>
            <el-input-number v-model="r4" :precision="2" :step="0.01" step-strictly="true" :min="0" :max="1"></el-input-number>
            <el-button style="margin-left: 3%" @click="suit()" > 调整 </el-button>
            <el-button @click="rebuilt()" > 重置 </el-button>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="gotoanalysis">确 定</el-button>
      </span>
      </el-dialog>

      <el-dialog
          v-loading="loading3"
          :visible.sync="pictrue"
          width="65%"
      >
        <span slot="title"><h4> {{"用户风险视图"}}</h4><i class="el-icon-view" @click="rangesworks" ></i>

        </span>
        <div style="padding: 2%">
          <div v-show="showpic==0" >
            <div  id="analysis" style="width: 100%;height: 580px" ></div>
          </div>
          <div v-show="showpic==1" >
            <div  id="analysis2" style="width: 100%;height: 580px" ></div>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
           <el-button v-if="showpic==0" @click="changeType">切换直方图</el-button>
          <el-button v-if="showpic==1" @click="changeType">切换饼状图</el-button>
           <el-button type="primary" pain @click="pictrue = false">退 出</el-button>
        </span>
        <el-dialog title="具体比例数值" append-to-body :visible.sync="showwords" width="30%" >
          <div style="padding: 2%">
            <div style="text-align: center">
              <el-table
                  stripe
                  border
                  :data="ranges"
                  style="width: 100%">
                <el-table-column
                    sortable
                    prop="name"
                    label="风险等级">
                </el-table-column>
                <el-table-column
                    sortable
                    prop="rate"
                    label="占比(%)">
                </el-table-column>
              </el-table>
            </div>
          </div>
        </el-dialog>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import request from "@/utils/request";
export default {
  name: "RiskAnalysis",
  data(){
    return{
      tableData:[],
      total:0,
      pageNum:1,
      pageSize:5,
      name:'',
      loading:false,
      loading2:false,
      loading3:false,
      dialogVisible:false,
      showpic:0,
      analysis:{type:2,id:0},
      r1:0.3,
      r2:0.3,
      r3:0.2,
      r4:0.2,
      pictrue:false,
      usermsg:{},
      data:[],
      linename:[],
      linev:[],
      linChart:null,
      barChart:null,
      showwords:false,
      ranges:[]
    }
  },
  created() {
    //请求分页查询
    this.serchUser()
  },
  methods:{
    getpictrue(){
      this.loading=true
      if(this.data.length==0){
        request.post("/Risk/getriskM",{
        }).then(res=>{
          if(res.code=='200'){
            for(var item in res.data){
              let num={ value: 0, name: '' }
              num.name=item
              num.value=res.data[item]
              this.linename.push(num.name)
              this.linev.push(num.value)
              this.data.push(num)
              //画图
            }
            this.loading=false
            this.pictrue=true
            this.$nextTick(()=>{
              this.showpicture1(this.data)
            })
          }

        })
      }
      else {
        this.loading=false
        this.pictrue=true
        this.$nextTick(()=>{
          this.showpicture1(this.data)
        })
      }
    },
    rangesworks(){//计算具体比例数值
      if(this.ranges.length==0){
        let sum=0;
        for(let i=0;i<this.data.length;i++){
          sum=sum+this.data[i].value
        }
        for (let i=0;i<this.data.length;i++){
          let aa={name:'',rate:0}
          aa.name=this.data[i].name
          aa.rate=((this.data[i].value/sum)*100).toFixed(4)
          aa.rate=Number(aa.rate)
          this.ranges.push(aa)
        }
        console.log(this.ranges)
      }
      if(this.ranges.length>0){
        this.showwords=true
      }
    },
    suit(){//调整
      let sum=this.r1+this.r2+this.r3+this.r4
      this.r1=Number((this.r1/sum).toFixed(2))
      this.r2=Number((this.r2/sum).toFixed(2))
      this.r3=Number((this.r3/sum).toFixed(2))
      this.r4=Number((this.r4/sum).toFixed(2))
      return 1
    },
    rebuilt(){//重置
      this.r1=0.3
      this.r2=0.3
      this.r3=0.2
      this.r4=0.2
    },
    serchUser(){
      request.post("/User/getUserRisk",{
        pageNum:this.pageNum,
        pageSize:this.pageSize,
        name:this.name
      }).then(res=>{
        if(res.code=='200'){
          this.tableData=res.data.records
          for(let i=0;i<this.tableData.length;i++){
            if(this.tableData[i].bankcards!='[]'){
              this.tableData[i].usertype='是'
            }
            else {
              this.tableData[i].usertype='否'
            }
          }
          this.total=res.data.total
        }
        this.loading=false
      })
    },
    gotoanalysis(){
      this.loading2=true
      if(this.suit()==1){
        request.post("/Risk/riskanalysis",{
          type:this.analysis.type,
          id:this.analysis.id,
          r1:this.r1,
          r2:this.r2,
          r3:this.r3,
          r4:this.r4,
        }).then(res=>{
          if(res.code=='200'){
            this.$message({
              showClose: true,
              message: '分析成功！',
              type: 'success'
            });
            this.serchUser()
            this.dialogVisible=false
          }
          else {
            this.$message({
              showClose: true,
              message: res.msg,
              type: 'error'
            });
          }
          this.loading2=false
        })
      }
    },
    handleSizeChange(pageSize) {
      this.pageSize=pageSize
      this.serchUser()
    },
    handleCurrentChange(pageNum) {
      this.pageNum=pageNum
      this.serchUser()
    },
    handleEdit(val){
      this.analysis.id=val.id
      this.analysis.type=3
      this.usermsg=val
      this.dialogVisible=true
    },
    closeDialog(){//关闭面板
      this.analysis.id=0
      this.analysis.type=2
      this.usermsg={}
      console.log(this.analysis)
    },
    changeType(){//切换图形
      if(this.showpic==0){
        this.showpicture2(this.data)
        this.$nextTick(()=>{
          if(this.linChart){
            this.linChart.resize()
          }
        })
      }
      else if(this.showpic==1){
        this.showpicture1(this.data);
        this.$nextTick(()=>{
          if(this.barChart){
            this.barChart.resize();
          }
        })
      }
    },
    showpicture1(data){//饼图

      this.showpic=0
      this.barChart = echarts.getInstanceByDom(document.getElementById("analysis"))
      // 如果不存在，就进行初始化
      if (this.barChart == null) {
        this.barChart = echarts.init(document.getElementById("analysis"));
      }
      let option;
      option = {
        legend: {
          top: 'bottom'
        },
        title: {
          text: '用户风险等级分布饼状图',
          subtext: 'Pie chart of user risk level distribution'
        },
        toolbox: {
          show: true,
          feature: {
            mark: { show: true },
            dataView: { show: true, readOnly: false },
            restore: { show: true },
            saveAsImage: { show: true }
          }
        },
        tooltip:{
          trigger:'item',
          showDelay:20,
          hideDelay:20,
          borderColor: 'rgba(255,0,0,0.7)',
          testStyle:{
            fontSize :'16px',
            color:'#000'
          }
        },
        series: [
          {
            name: 'Nightingale Chart',
            type: 'pie',
            radius: [50, 250],
            center: ['50%', '50%'],
            roseType: 'area',
            itemStyle: {
              borderRadius: 8
            },
            data: data
          }
        ]
      };
      option && this.barChart.setOption(option);
    },
    showpicture2(){//直方图
      this.showpic=1

      this.linChart = echarts.getInstanceByDom(document.getElementById("analysis2"))
      // 如果不存在，就进行初始化
      if (this.linChart  == null) {

        this.linChart  = echarts.init(document.getElementById("analysis2"));
      }
// prettier-ignore
      let dataAxis = this.linename
// prettier-ignore
      let data = this.linev
      let yMax = 500;
      let dataShadow = [];
      let option;
      for (let i = 0; i < data.length; i++) {
        dataShadow.push(yMax);
      }
      option = {
        title: {
          text: '用户风险等级分布直方图',
          subtext: 'Histogram of user risk level distribution'
        },
        xAxis: {
          data: dataAxis,
          axisLabel: {
            inside: true,
            color: '#fff'
          },
          axisTick: {
            show: false
          },
          axisLine: {
            show: false
          },
          z: 10
        },
        tooltip: {},
        yAxis: {
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          },
          axisLabel: {
            color: '#999'
          }
        },
        dataZoom: [
          {
            type: 'inside'
          }
        ],
        toolbox: {
          show: true,
          feature: {
            mark: { show: true },
            dataView: { show: true, readOnly: false },
            restore: { show: true },
            saveAsImage: { show: true }
          }
        },
        series: [
          {
            type: 'bar',
            showBackground: true,
            itemStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#83bff6' },
                { offset: 0.5, color: '#188df0' },
                { offset: 1, color: '#188df0' }
              ])
            },
            emphasis: {
              itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                  { offset: 0, color: '#2378f7' },
                  { offset: 0.7, color: '#2378f7' },
                  { offset: 1, color: '#83bff6' }
                ])
              }
            },
            data: data
          }
        ]
      };
// Enable data zoom when user click bar.
      const zoomSize = 4;
      this.linChart.on('click', function (params) {
        console.log(dataAxis[Math.max(params.dataIndex - zoomSize / 2, 0)]);
        this.linChart.dispatchAction({
          type: 'dataZoom',
          startValue: dataAxis[Math.max(params.dataIndex - zoomSize / 2, 0)],
          endValue:
              dataAxis[Math.min(params.dataIndex + zoomSize / 2, data.length - 1)]
        });
      });

      option && this.linChart.setOption(option);
    }
  }
}
</script>

<style scoped>

</style>