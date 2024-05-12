<template>
  <div>
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane label="贷款审核" :name="1"></el-tab-pane>
      <el-tab-pane label="信用卡升级" :name="2"></el-tab-pane>
      <el-tab-pane label="角色管理" :name="3"></el-tab-pane>
    </el-tabs>
    <div style="padding: 2%">
      <div style="width: 100%">
        <el-table
            border
            :data="LoanstableData"
            stripe
            style="width: 80%">
          <el-table-column
              fixed
              prop="username"
              label="贷款人"
              width="100">
          </el-table-column>
          <el-table-column
              prop="typename"
              label="贷款类型"
              width="180">
          </el-table-column>
          <el-table-column
              prop="cost"
              label="贷款金额（万元）"
              width="180">
          </el-table-column>
          <el-table-column
              prop="timelimit"
              label="贷款期限"
              width="180">
          </el-table-column>
          <el-table-column
              prop="returntypename"
              label="还款类型"
              width="180">
          </el-table-column>
          <el-table-column
              prop="salary"
              label="月收入(税后)"
              width="180">
          </el-table-column>
          <el-table-column
              fixed="right"
              label="详情操作"
              width="100">
            <template slot-scope="scope">
              <el-button @click="getOnemsg(scope.row)" type="text" size="small">审核<i class="el-icon-edit"></i></el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pageNum"
        :page-sizes="[3, 5, 10]"
        :page-size="pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total">
    </el-pagination>

    <el-dialog
        title="详情信息"
        :visible.sync="show"
        width="30%"
    >
      <div style="display: flex;justify-content: space-between">
        <span>{{'申请人：'}}</span>
        <span>{{lookone.tablemsg.username}}</span>
      </div >
      <div style="display: flex;justify-content: space-between" >
        <span>{{'申请时间：'}}</span>
        <span>{{lookone.senttime}}</span>
      </div>
      <div style="display: flex;justify-content: space-between" >
        <span>{{'申请人年龄：'}}</span>
        <span>{{lookone.age}}</span>
      </div>
      <div style="display: flex;justify-content: space-between" >
        <span>{{'婚姻状况：'}}</span>
        <span>{{lookone.marital}}</span>
      </div>
      <div style="display: flex;justify-content: space-between" >
        <span>{{'是否存在违约记录：'}}</span>
        <span>{{lookone.defaults+'次'}}</span>
      </div>
      <el-divider></el-divider>
      <div style="display: flex;justify-content: space-between" >
        <span>{{'贷款类型：'}}</span>
        <span>{{lookone.tablemsg.typename}}</span>
      </div>
      <div style="display: flex;justify-content: space-between" >
        <span>{{'贷款金额：'}}</span>
        <span>{{lookone.tablemsg.cost+' 万元'}}</span>
      </div>
      <div style="display: flex;justify-content: space-between" >
        <span>{{'分期期限：'}}</span>
        <span>{{lookone.tablemsg.timelimit+' 月'}}</span>
      </div>
      <div style="display: flex;justify-content: space-between" >
        <span>{{'还款类型：'}}</span>
        <span>{{lookone.tablemsg.returntypename}}</span>
      </div>
      <div style="display: flex;justify-content: space-between" >
        <span>{{'月收入（税后）：'}}</span>
        <span>{{lookone.tablemsg.salary+' 元'}}</span>
      </div>
      <div style="display: flex;justify-content: space-between" >
        <span>{{'首月需归还金额：'}}</span>
        <span>{{lookone.returntmoney+' 元'}}</span>
      </div>
      <el-divider></el-divider>
      <div  >
        <span>{{'证明材料：'}}</span>
        <div class="demo-image__preview">
          <el-image
              v-for="(item,index) in lookone.urls"
              style="width: 100px; height: 100px"
              :src="item"
              :preview-src-list="lookone.urls">
          </el-image>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
         <el-button @click="show = false">不通过</el-button>
         <el-button type="primary" @click="show = false">通过</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import request from "@/utils/request";
export default {
  name: "Audit",
  data(){
    return{
      activeName: 1,
      LoanstableData:[],
      pageSize:5,
      pageNum:1,
      total:0,
      ispass:0,
      lenders:[],
      lookone:{senttime:'',age:'',defaults:'',tablemsg:{},marital:'',returntmoney:'',urls:[]},
      show:false,
    }
  },
  created() {
    // 从后台获取数据
    this.getLoanstype()
  },
  methods:{
    getLoanstype(){
      request.post("/Lender/getLenders",{
      }).then(res=>{
        if(res.code=='200'){
          if(res.code=='200'){
            this.lenders=res.data
            this.selectLoans()
          }
        }
      })
    },
    getOnemsg(e){//获取单人详情信息
      request.post("/Lender/getonemsg",{
        id:e.uid
      }).then(res=>{
        if(res.code=='200'){
          if(res.code=='200'){
            this.lookone=res.data
            this.LoansAudit(e)
          }
        }
      })
    },
    selectLoans(){//贷款申请查询
      request.post("/UserLoans/getLoans",{
          pageNum:this.pageNum,
          pageSize:this.pageSize,
          ispass:this.ispass
      }).then(res=>{
        if(res.code=='200'){
          this.LoanstableData=[]
          for(let i=0;i<res.data.records.length;i++){
            this.LoanstableData.push(res.data.records[i])
            for(let j=0;j<this.lenders.length;j++){
              if(res.data.records[i].lid==this.lenders[j].id){
                this.LoanstableData[i].typename=this.lenders[j].name
                this.LoanstableData[i].rate=this.lenders[j].rate
                break
              }
            }
            if(this.LoanstableData[i].returntype==1){
              this.LoanstableData[i].returntypename='等额本息'
            }
            else {
              this.LoanstableData[i].returntypename='等额本金'
            }
            this.LoanstableData[i].cost=this.LoanstableData[i].cost/10000
          }
          this.total=res.data.total
        }
      })
    },
    LoansAudit(e){//单人贷款审核
      this.lookone.tablemsg=e
      if(this.lookone.tablemsg.returntype==2){//等额本金
        this.lookone.returntmoney=(((Number(this.lookone.tablemsg.cost)*10000)/Number(this.lookone.tablemsg.timelimit))+((Number(this.lookone.tablemsg.cost)*10000)*Number(this.lookone.tablemsg.rate)/1200)).toFixed(2)
      }
      else {//等额本息
        this.lookone.returntmoney=(((this.lookone.tablemsg.cost*10000)+((this.lookone.tablemsg.cost*10000)*(this.lookone.tablemsg.rate/1200)*this.lookone.tablemsg.timelimit))/this.lookone.tablemsg.timelimit).toFixed(2)
      }
      this.show=true
    },
    handleClick(tab, event) {
    },
    handleSizeChange(pageSize) {
      this.pageSize=pageSize
      this.selectLoans()
    },
    handleCurrentChange(pageNum) {
      this.pageNum=pageNum
      this.selectLoans()
    },
  }
}
</script>

<style scoped>

</style>