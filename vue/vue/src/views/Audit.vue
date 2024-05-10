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
              <el-button @click="LoansAudit(scope.row)" type="text" size="small">审核<i class="el-icon-edit"></i></el-button>
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
      <div>
        <span>{{'申请人：'+lookone.name}}</span>
      </div>
      <div>
        <span>{{'申请人年龄：'+lookone.age}}</span>
      </div>
      <div>
        <span>{{'婚姻状况：'+lookone.marital}}</span>
      </div>
      <div>
        <span>{{'是否存在违约记录：'+lookone.default}}</span>
      </div>
      <div>
        <span>{{'贷款类型：'+lookone.tablemsg.typename}}</span>
      </div>
      <div>
        <span>{{'贷款金额：'+lookone.tablemsg.cost+' 万元'}}</span>
      </div>
      <div>
        <span>{{'分期期限：'+lookone.tablemsg.timelimit+' 月'}}</span>
      </div>
      <div>
        <span>{{'还款类型：'+lookone.tablemsg.returntypename}}</span>
      </div>
      <div>
        <span>{{'月收入（税后）：'+lookone.tablemsg.salary+' 元'}}</span>
      </div>
      <div>
        <span>{{'首月需归还金额：'+lookone.returntmoney+' 元'}}</span>
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
      lookone:{name:'',sentTime:'',age:'',default:'',tablemsg:{},marital:'',returntmoney:''},
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
    LoansAudit(e){//贷款审核
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