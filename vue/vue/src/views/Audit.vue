<template>
  <div>
    <el-tabs v-model="activeName" @tab-click="handleClick1">
      <el-tab-pane label="贷款审核" :name="1"></el-tab-pane>
      <el-tab-pane label="信用卡升级" :name="2"></el-tab-pane>
    </el-tabs>
    <div style="padding: 2%">
      <div v-if="activeName==1"  style="width: 100%">
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

      <div v-if="activeName==2" style="width: 100%"  >
        <el-tabs v-model="activeNamebankcard" type="card" @tab-click="handleClick2">
          <el-tab-pane label="信用卡升级" :name="1"></el-tab-pane>
          <el-tab-pane label="额度提升" :name="2"></el-tab-pane>
        </el-tabs>
        <div >
          <el-table
              v-loading="loadingflag"
              border
              :data="ranktable"
              stripe
              style="width: 80%">
            <el-table-column
                fixed
                prop="name"
                label="申请人"
                width="100">
            </el-table-column>
            <el-table-column
                prop="senttime"
                label="申请时间"
                width="180">
            </el-table-column>
            <el-table-column
                prop="rankname"
                label="当前等级"
                width="180">
            </el-table-column>
            <el-table-column
                v-if="activeNamebankcard==1"
                prop="torankname"
                label="申请等级"
                width="180">
            </el-table-column>
            <el-table-column
                prop="limits"
                label="当前额度（元）"
                width="180">
            </el-table-column>
            <el-table-column
                prop="tolimits"
                label="申请后额度（元）"
                width="180">
            </el-table-column>
            <el-table-column
                label="信用卡违约次数"
                width="80">
              <template slot-scope="scope">
                <el-button @click="looktime(scope.row)" type="text" size="big">{{scope.row.defaults+'次'}}<i style="margin-left: 1%" class="el-icon-view"></i></el-button>
              </template>
            </el-table-column>
            <el-table-column
                fixed="right"
                label="详情操作"
                width="100">
              <template slot-scope="scope">
                <el-button @click="getOnecardrank(scope.row)" type="text" size="small">审核<i class="el-icon-edit"></i></el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
        <div v-if="activeNamebankcard==2">

        </div>
      </div>
    </div>


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
      <div style="display: flex;justify-content: space-between" >
        <span style="font-size: 15px">{{'上次用户风险等级得分：'}}</span>
        <el-popover
            placement="top-start"
            title="风险得分"
            width="200"
            trigger="hover"
        >
          <div style="font-size: 12px">{{'用户得分65及以上为低风险用户'}}</div>
          <div style="font-size: 12px;margin-top: 1%">{{'用户得分58~64（包含）为一般风险用户'}}</div>
          <div style="font-size: 12px;margin-top: 1%">{{'用户得分57及以下为高风险用户'}}</div>
          <h4  slot="reference" style="font-size: 18px;color: limegreen" v-if="lookone.risk>=65" >{{lookone.risk}}</h4>
          <h4 slot="reference" style="font-size: 18px;color: orange" v-if="lookone.risk>=58&&lookone.risk<65" >{{lookone.risk}}</h4>
          <h4 slot="reference" style="font-size: 18px;color: red" v-if="lookone.risk<58" >{{lookone.risk}}</h4>
        </el-popover>
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
              style="width: 80px; height: 80px;margin-right: 1%;margin-top: 1%"
              :src="item"
              :preview-src-list="lookone.urls">
          </el-image>
        </div>
      </div>
      <div>
        <el-divider></el-divider>
        <span>{{'理由描述：'}}</span>
        <el-input
            type="textarea"
            autosize
            placeholder="请输入内容"
            v-model="textarea1"
            style="margin-top: 1%"
        >
        </el-input>
      </div>
      <el-dialog
          width="30%"
          title="警告"
          :visible.sync="warning1"
          append-to-body>
        <span>{{text+'是否确定通过？'}}</span>
        <span slot="footer" class="dialog-footer">
         <el-button @click="warning1=false">返回</el-button>
         <el-button type="primary" @click="setmsg(2)">确定通过</el-button>
        </span>
      </el-dialog>
      <span slot="footer" class="dialog-footer">
         <el-button @click="notpass()">不通过</el-button>
         <el-button type="primary" @click="pass()">通过</el-button>
      </span>
    </el-dialog>

    <el-dialog
        title="逾期时间"
        :visible.sync="seetime"
        width="30%"
    >
      <div style="padding: 3%">
        <span v-for="(item,index) in times">{{'第'+(index+1)+'次：'+item}}</span>
      </div>
         <span slot="footer" class="dialog-footer">
          <el-button type="primary" @click="seetime = false">确 定</el-button>
         </span>
    </el-dialog>

    <el-dialog
        title="申请审核"
        :visible.sync="seecardmsg"
        width="30%"
    >
      <div style="padding: 1%">
        <div style="display: flex;justify-content: space-between">
          <span>{{'申请人：'}}</span>
          <span>{{cardmsg.name}}</span>
        </div >
        <div style="display: flex;justify-content: space-between" >
          <span>{{'申请时间：'}}</span>
          <span>{{cardmsg.senttime}}</span>
        </div>
        <div style="display: flex;justify-content: space-between" >
          <span>{{'信用卡违约记录：'}}</span>
          <span style="color: limegreen" v-if="cardmsg.defaults==0" >{{cardmsg.defaults+'次'}}</span>
          <span style="color: red" v-if="cardmsg.defaults>0" >{{cardmsg.defaults+'次'}}</span>
        </div>
        <div style="display: flex;justify-content: space-between" >
          <span style="font-size: 15px">{{'上次用户风险等级得分：'}}</span>
          <el-popover
              placement="top-start"
              title="风险得分"
              width="200"
              trigger="hover"
          >
            <div style="font-size: 12px">{{'用户得分65及以上为低风险用户'}}</div>
            <div style="font-size: 12px;margin-top: 1%">{{'用户得分58~64（包含）为一般风险用户'}}</div>
            <div style="font-size: 12px;margin-top: 1%">{{'用户得分57及以下为高风险用户'}}</div>
            <h4  slot="reference" style="font-size: 18px;color: limegreen" v-if="cardmsg.risk>=65" >{{cardmsg.risk}}</h4>
            <h4 slot="reference" style="font-size: 18px;color: orange" v-if="cardmsg.risk>=58&&cardmsg.risk<65" >{{cardmsg.risk}}</h4>
            <h4 slot="reference" style="font-size: 18px;color: red" v-if="cardmsg.risk<58" >{{cardmsg.risk}}</h4>
          </el-popover>
        </div>
        <el-divider></el-divider>
        <div style="display: flex;justify-content: space-between" >
          <span>{{'当前等级：'}}</span>
          <span>{{cardmsg.rankname}}</span>
        </div>
        <div style="display: flex;justify-content: space-between" >
          <span>{{'申请等级：'}}</span>
          <span>{{cardmsg.torankname}}</span>
        </div>
        <div style="display: flex;justify-content: space-between" >
          <span>{{'当前额度：'}}</span>
          <span>{{cardmsg.limits+'元'}}</span>
        </div>
        <div style="display: flex;justify-content: space-between" >
          <span>{{'提升后额度：'}}</span>
          <span>{{cardmsg.tolimits+'元'}}</span>
        </div>
      </div>
      <span slot="footer" class="dialog-footer">
          <el-button @click="seecardmsg=false">返回</el-button>
        <el-button @click="cardpass(1)">不通过</el-button>
          <el-button type="primary" @click="cardpass(2)">通过</el-button>
         </span>
    </el-dialog>
  </div>
</template>

<script>
import request from "@/utils/request";
import Decimal from 'decimal.js'
export default {
  name: "Audit",
  data(){
    return{
      activeName: 1,
      activeNamebankcard:1,
      LoanstableData:[],
      pageSize:5,
      pageNum:1,
      total:0,
      ispass:0,
      lenders:[],
      lookone:{risk:'',senttime:'',age:'',defaults:'',tablemsg:{},marital:'',returntmoney:'',urls:[]},
      ranknames:[{rank:1,name:"普通卡"},{rank:2,name:"金卡"},{rank:3,name:"白金卡"},{rank:4,name:"钻石卡"},{rank:5,name:"黑金卡"}],
      show:false,
      warning1:false,
      seetime:false,
      textarea1:'',
      text:'',
      ranktable:[],//等级
      costtable:[],//额度
      times:[],
      cardmsg:{},
      seecardmsg:false,
      loadingflag:false
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
    ncifang(A,n){//计算A的n次方
      for(let i=1;i<n;i++){
        A=new Decimal(A).mul(new Decimal(A));
      }
      return A
    },
    LoansAudit(e){//单人贷款审核
      this.lookone.tablemsg=e
      let A=(Number(this.lookone.tablemsg.cost)*10000);//本金
      let r=Number(this.lookone.tablemsg.rate)/1200;//月利率
      let m=Number(this.lookone.tablemsg.timelimit)//借款月数
      console.log(m)
      console.log(this.ncifang(1+r,m));
      if(this.lookone.tablemsg.returntype==2){//等额本金
        this.lookone.returntmoney=(A/m+A*r).toFixed(2);
      }
      else if(this.lookone.tablemsg.returntype==1) {//等额本息
        this.lookone.returntmoney=((A*r*Math.pow((1+r),m))/(Math.pow((1+r),m)-1)).toFixed(2)
      }
      this.show=true
    },
    handleClick1(tab, event) {
      if(tab.index==1){
        this.getcardrable(2)
      }
    },
    handleClick2(tab, event) {//信用卡升级选择
      if(this.activeNamebankcard==1){
        this.getcardrable(2)//等级提升
      }
      else if(this.activeNamebankcard==2){
        this.getcardrable(1)//额度提升
      }
    },
    handleSizeChange(pageSize) {
      this.pageSize=pageSize
      this.selectLoans()
    },
    handleCurrentChange(pageNum) {
      this.pageNum=pageNum
      this.selectLoans()
    },
    notpass(){//不通过
      if(this.textarea1==''){
        this.$message({
          showClose: true,
          message: '未通过的申请需注明理由！',
          type: 'warning'
        });
      }
      else{
        this.setmsg(1)
      }
    },
    pass(){//通过
      let flag=0
      this.text=''
      if(this.lookone.risk<58){
        flag=2
        this.$message({
          showClose: true,
          message: '该用户风险评估分数过低，无法通过贷款申请！',
          type: 'error'
        });
      }
      if(this.lookone.risk>=58&&this.lookone.risk<65&&flag!=2){
        flag=1
        this.text=this.text+'该用户风险分析得分为：'+this.lookone.risk+' 分，为一般风险;'
      }
      if(this.lookone.returntmoney/this.lookone.tablemsg.salary>=0.5&&flag!=2){
        flag=1
        this.text=this.text+' 该用户首月需还贷款占月收入的：'+((this.lookone.returntmoney/this.lookone.tablemsg.salary)*100).toFixed(2)+'%;'
      }
      if(flag==1){
        this.warning1=true
      }
    },
    setmsg(type){//设置用户贷款申请情况
      request.post("/UserLoans/setloans",{
        id:this.lookone.id,
        type:type,
        msg:this.textarea1
      }).then(res=>{
        if(res.code=='200'){
          this.$message({
            showClose: true,
            message: '操作成功！',
            type: 'success'
          });
        }
        this.warning1=false
        this.show=false
        this.selectLoans()
      })
    },
    getcardrable(type){
      this.ranktable=[]
      this.loadingflag=true
      request.post("/UserUpdate/getUpdatebyType",{
        type:type,
      }).then(res=>{
        if(res.code=='200'){
          this.ranktable=res.data
          for(let i=0;i<this.ranktable.length;i++){
            for(let j=0;j<this.ranknames.length;j++){
              if(this.ranktable[i].rank==this.ranknames[j].rank){
                this.ranktable[i].rankname=this.ranknames[j].name
              }
              if(this.ranktable[i].update.rank==this.ranknames[j].rank){
                this.ranktable[i].torankname=this.ranknames[j].name
                break
              }
            }
            this.ranktable[i].update.passmsg=JSON.parse(this.ranktable[i].update.passmsg)
            this.ranktable[i].tolimits=this.ranktable[i].update.cost
          }
        }
        this.loadingflag=false
      })
    },
    getrisk(id){//获取风险等级
      request.post("/Risk/getriskOne",{
        uid:id,
      }).then(res=>{
        if(res.code=='200'){
          this.cardmsg.risk=res.data
          this.seecardmsg=true
        }
      })
    },
    looktime(e){
      this.times=e.times
      this.seetime=true
    },
    getOnecardrank(e){
      this.cardmsg=e
      this.getrisk(e.update.uid)

    },
    cardpass(type){//银行卡申请审核

    }
  }
}
</script>

<style scoped>

</style>