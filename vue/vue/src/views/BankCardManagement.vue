<template>
  <div style="padding: 2%">
    <el-tabs v-model="activeName" @tab-click="handleClick">
      <el-tab-pane label="银行卡信息" :name="1"></el-tab-pane>
      <el-tab-pane label="封禁管理" :name="2"></el-tab-pane>
    </el-tabs>
    <div v-if="activeName==1" >
      <div style="margin-bottom: 3%">
        <el-input v-model="search" placeholder="请输入用户名字进行查询" style="width:200px" clearable size="mini"></el-input>
        <el-button @click="gettableData" size="mini" style="margin-left: 10px">查询</el-button>
      </div>
      <el-table
          id="zhankaide"
          stripe
          border
          :data="tableData"
          :row-class-name="tableRowClassName"
          style="width: 100%">
        <el-table-column type="expand">
          <template slot-scope="props">
            <el-table
                id="zhankai"
                :data="props.row.bankCards"
                border
                :row-class-name="tableRowClassName2"
                style="width: 100%">
              <el-table-column
                  prop="typename"
                  label="类型"
                  width="180">
              </el-table-column>
              <el-table-column
                  prop="cardnumber"
                  label="银行卡号"
              >
              </el-table-column>
              <el-table-column
                  prop="createtime"
                  label="创建时间">
              </el-table-column>
              <el-table-column
                  prop="statename"
                  label="是否封禁">
              </el-table-column>
              <el-table-column
                  fixed="right"
                  label="操作"
                  width="100">
                <template slot-scope="scope">
                  <el-button v-if="scope.row.state!=0" type="text" style="margin-left: 10px" size="mini" >详情说明</el-button>
                </template>
              </el-table-column>
            </el-table>
          </template>
        </el-table-column>
        <el-table-column
            label="用户ID"
            prop="id">
        </el-table-column>
        <el-table-column
            label="用户姓名"
            prop="name">
        </el-table-column>
        <el-table-column
            label="用户账号"
            prop="account">
        </el-table-column>
        <el-table-column
            label="联系方式"
            prop="phone">
        </el-table-column>
        <el-table-column
            label="银行卡数"
            prop="cardsum">
        </el-table-column>
      </el-table>
      <div style="display: flex;margin-top: 5px">
        <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="currentPage"
            :page-sizes="[5, 10, 15, 20]"
            :page-size="5"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total">
        </el-pagination>
      </div>
    </div>

    <div  id="woTMthanks" v-if="activeName==2">
      <el-tabs v-model="state" type="card" @tab-click="handleClickstate">
        <el-tab-pane label="未封禁银行卡" :name="Number(0)"></el-tab-pane>
        <el-tab-pane label="已封禁银行卡" :name="Number(1)"></el-tab-pane>
      </el-tabs>
      <div style="margin-bottom: 3%">
        <el-input v-model="Bansearch" placeholder="请输入用户名字进行查询" style="width:200px" clearable size="mini"></el-input>
        <el-select @change="change" style="margin-left: 10px" v-model="value" placeholder="请选择">
          <el-option
              v-for="item in options"
              :key="item.value"
              :label="item.label"
              :value="item.value">
          </el-option>
        </el-select>
        <el-button @click="getbankcards(Number(state))" size="mini" style="margin-left: 10px">查询</el-button>
        <span style="color: limegreen">{{tips}}</span>
      </div>
      <div >
        <el-table
            :row-class-name="tableRowClassName3"
            v-loading="loadingflag"
            stripe
            border
            ref="multipleTable"
            :data="bankcards"
            tooltip-effect="dark"
            style="width: 100%"
            @selection-change="handleSelectionChange">
          <el-table-column
              type="selection"
              width="55">
          </el-table-column>
          <el-table-column
              fixed="left"
              v-if="Number(state)==1"
              prop="restatename"
              width="150"
              label="是否提交解禁申请"
          >
          </el-table-column>
          <el-table-column
              prop="typename"
              label="银行卡类型"
              width="100">
          </el-table-column>
          <el-table-column
              prop="name"
              label="持卡人姓名"
              width="120">
          </el-table-column>
          <el-table-column
              prop="cardnumber"
              label="银行卡号"
          >
          </el-table-column>
          <el-table-column
              prop="createtime"
              label="开通时间"
          >
          </el-table-column>
          <el-table-column
              prop="abnormalname"
              label="是否异常"
          >
          </el-table-column>
          <el-table-column
              fixed="right"
              label="操作"
              width="100">
            <template slot-scope="scope">
              <el-button v-if="Number(state)==0" @click="checkoutOne(scope.row,1)" type="text" style="margin-left: 10px" size="mini" >操作</el-button>
              <el-button v-if="Number(state)==1" @click="checkoutOne(scope.row,1)" type="text" style="margin-left: 10px" size="mini" >解禁</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <el-dialog
          title="批量操作"
          :visible.sync="opensure"
          width="30%"
      >
        <span v-if="Number(state==0)" style="color: orange">{{piltext+',是否批量封禁银行卡？'}}</span>
        <span v-if="Number(state==1)" style="color: orange">{{piltext+',是否批量解开封禁中的银行卡？'}}</span>
        <span slot="footer" class="dialog-footer">
           <el-button @click="opensure = false">取 消</el-button>
            <el-button type="primary" @click="piliangban">确 定</el-button>
           </span>
      </el-dialog>
      <div style="margin-top: 2%">
        <div >
            <el-button @click="opnepiliang"  slot="reference" type="text" style="color: orangered" v-if="Number(state)==0">批量封禁银行卡</el-button>
        </div>
        <div >
          <el-button @click="opnepiliang" type="text" style="color: limegreen" v-if="Number(state)==1">批量解禁银行卡</el-button>
        </div>
      </div>
      <div style="display: flex;margin-top: 5px">
        <el-pagination
            @size-change="BanhandleSizeChange"
            @current-change="BanhandleCurrentChange"
            :current-page="BancurrentPage"
            :page-sizes="[5, 10, 15, 20]"
            :page-size="5"
            layout="total, sizes, prev, pager, next, jumper"
            :total="Bantotal">
        </el-pagination>
      </div>


      <el-dialog
          :title="titles"
          :visible.sync="dialogVisible"
          width="30%"
      >
        <div style="padding: 2%" >
          <div>
            <span style="margin-bottom: 1%">{{'持卡人：'+yuanmsg.name}}</span>
          </div>
          <div style="margin-bottom: 1%">
            <span>{{'卡号：'+yuanmsg.cardnumber}}</span>
          </div>
          <div v-if="Number(state)==1" style="margin-bottom: 1%">
            <span style="color: orangered" v-if="yuanmsg.restate==0" >该用户尚未提交解封申请！</span>
            <span style="color: limegreen" v-if="yuanmsg.restate==1" >该用户已提交解封申请！</span>
          </div>
          <el-tabs v-loading="loadingflag2"  v-model="activeName36" type="border-card" @tab-click="handleClick36">
            <el-tab-pane label="当前异常操作" :name="1" >
              <span v-if="chcekoutonmsg.length==0" style="color: gray">暂无数据</span>
              <div style="margin-bottom: 1%" v-for="(item , index) in chcekoutonmsg" >
                <div>{{'异常操作'+(index+1)+': '}}</div>
                <span style="color: orange" >{{item.description}}</span>
              </div>
            </el-tab-pane>
            <el-tab-pane label="历史异常操作" :name="2">
              <span v-if="chcekoutonmsg.length==0" style="color: gray">暂无数据</span>
              <div style="margin-bottom: 1%" v-for="(item , index) in chcekoutonmsg" >
                <div>{{'异常操作'+(index+1)+': '}}</div>
                <span style="color: orange" >{{item.description}}</span>
              </div>
            </el-tab-pane>
          </el-tabs>
          <el-input
              v-if="Number(state)==0"
              style="margin-top: 2%"
              type="textarea"
              placeholder="请输入封禁理由"
              v-model="textarea">
          </el-input>
        </div>

        <el-dialog
            width="30%"
            title="提示"
            :visible.sync="innerVisible"
            append-to-body>
          <div style="padding: 2%">
            <span style="color: orange">{{'注意：'+textarea22}}</span>
          </div>
          <span slot="footer" class="dialog-footer">
            <el-button style="margin-right: 2%" @click="innerVisible = false">取 消</el-button>
            <el-button  slot="reference" type="primary" @click="surecnnn" >确 定</el-button>
           </span>
        </el-dialog>

        <span slot="footer" class="dialog-footer">
          <div v-if="Number(state)==0">
            <el-popconfirm
                title="确定要清除当前银行卡的异常操作吗？这会使系统忽视用户这个时间以前的异常操作，但不会影响系统继续判断用户产生的新的异常操作"
                @confirm="clearerror()"
            >
            <el-button slot="reference" style="margin-right: 2%" >清除异常</el-button>
          </el-popconfirm>
          <el-popconfirm
              title="确定封禁当前银行卡吗？"
              @confirm="banCard(0)"
          >
            <el-button  slot="reference" type="primary" >封 禁</el-button>
          </el-popconfirm>
          </div>
           <div v-if="Number(state)==1">
          <el-popconfirm
              title="确定解封当前银行卡吗？"
              @confirm="rebancard(0)"
          >
            <el-button  slot="reference" type="primary" >解 禁</el-button>
          </el-popconfirm>
          </div>
        </span>
      </el-dialog>

    </div>

  </div>
</template>

<script>
import request from "@/utils/request";
export default {
  name: "BankCardManagement",
  data(){
    return{
      tableData:[],
      search:'',
      Bansearch:'',
      currentPage: 1,//默认显示第一页
      pageSize: 5,
      total: 0,
      bankcards:[],
      BancurrentPage: 1,//默认显示第一页
      BanpageSize: 5,
      Bantotal: 0,
      multipleSelection: [],
      activeName:1,
      state:0,
      value:0,
      options: [{
        value: 0,
        label: '全部银行卡'
      }, {
        value: 1,
        label: '异常银行卡'
      }],
      tips:'（在“全部银行卡”的选项中，表格会将系统判定的异常银行卡标记成黄色）',
      loadingflag:false,
      loadingflag2:false,
      dialogVisible:false,
      opensure:false,
      yuanmsg:{},
      chcekoutonmsg:[],
      activeName36:1,
      titles:'异常操作',
      textarea:'',
      piltext:'暂无异常',
      innerVisible:false,
      textarea22:''
    }
  },
  created() {
    this.gettableData()
  },
  methods:{
    gettableData(){//获取用户数据（分页）
      request.post("/BankCard/getusercards",
          {
            pageSize:this.pageSize,
            currentPage:this.currentPage,
            search:this.search
          }).then(res=>{
        if(res.code=='200'){
          this.tableData=res.data.records
          for(let i=0;i<this.tableData.length;i++){
            this.tableData[i].state=0
            for(let j=0;j<this.tableData[i].bankCards.length;j++){
              if(this.tableData[i].bankCards[j].type==1){
                this.tableData[i].bankCards[j].typename="储蓄卡"
              }
              else if(this.tableData[i].bankCards[j].type==2){
                this.tableData[i].bankCards[j].typename="储蓄副卡"
              }
              else {
                this.tableData[i].bankCards[j].typename="信用卡"
              }

              if(this.tableData[i].bankCards[j].state==0){
                this.tableData[i].bankCards[j].statename="正常"
              }
              else {
                this.tableData[i].bankCards[j].statename="封禁"
                this.tableData[i].state=1
              }
            }
          }
          this.total=res.data.total
        }
      })
    },
    getbankcards(state){//获取银行卡（分页）
      this.loadingflag=true
      request.post("/BankCard/getcardsall",
          {
            pageSize:this.BanpageSize,
            currentPage:this.BancurrentPage,
            search:this.Bansearch,
            state:state,
            model:this.value
          }).then(res=>{
        this.loadingflag=false
        if(res.code=='200'){
          this.bankcards=res.data.records
          this.Bantotal=res.data.total
          for(let i=0; i <= this.bankcards.length;i++){
            if(this.bankcards[i].type==1){
              this.bankcards[i].typename="储蓄卡"
            }
            else if(this.bankcards[i].type==2){
              this.bankcards[i].typename="储蓄副卡"
            }
            else if(this.bankcards[i].type==3){
              this.bankcards[i].typename="信用卡"
            }
            if(this.bankcards[i].abnormal==0){
              this.bankcards[i].abnormalname="正常"
            }
            else {
              this.bankcards[i].abnormalname="异常"
            }
            if(this.bankcards[i].restate==0){
              this.bankcards[i].restatename="未申请"
            }
            else{
              this.bankcards[i].restatename="已申请"
            }
          }
        }
      })
    },
    tableRowClassName({row, rowIndex}) {
      if (row.state === 1) {
        return 'warning-row';
      }
      return '';
    },
    tableRowClassName2({row, rowIndex}) {
      if (row.state === 1) {
        return 'warning-row';
      }
      return '';
    },
    tableRowClassName3({row, rowIndex}) {
      if (row.abnormal === 1) {
        return 'warning-row';
      }
      return '';
    },
    handleSizeChange(pageSize) {//改变当前每页的个数触发
      this.pageSize=pageSize
      this.gettableData()
    },
    handleCurrentChange(pageNum) {//改变当前页面触发
      this.currentPage=pageNum
      this.gettableData()
    },
    handleSelectionChange(val) {//多选
      this.multipleSelection = val;
      console.log(this.multipleSelection)
    },
    opnepiliang(){//打开批量面板
      this.piltext='暂无异常'
      for(let i=0;i<this.multipleSelection.length;i++){
        if(this.state==1){//解禁
          if(this.multipleSelection[i].restate==0){
            this.piltext='所选银行卡存在未提交解封申请的银行卡'
            break
          }
        }
        if(this.state==0){//封禁
          if(this.multipleSelection[i].abnormal==0){
            this.piltext='所选银行卡存在系统判断无异常操作的银行卡'
            break
          }
        }
      }
      this.opensure=true
    },
    piliangban(){//批量封禁或者解禁银行卡
      let ids=[]
      for(let i=0;i<this.multipleSelection.length;i++){
        ids.push(this.multipleSelection[i].id)
      }
      let type=0;
      if(this.state==1){
        type=0
      }
      else{
        type=1
      }
      if(ids.length>0){
        request.post("/BankCard/banorreBanCardbyId",
            {
              ids:ids,
              type:type
            }).then(res=>{
          if(res.code=='200'){
            this.getbankcards(Number(this.state))
            this.opensure=false
          }
        })
      }
      else{
        this.$message({
          type:"error",
          message:'您尚未选择任何银行卡'
        })
      }
    },
    BanhandleSizeChange(pageSize) {//改变当前每页的个数触发
      this.BanpageSize=pageSize
      this.state=Number(this.state)
      this.getbankcards(this.state)
    },
    BanhandleCurrentChange(pageNum) {//改变当前页面触发
      this.BancurrentPage=pageNum
      this.state=Number(this.state)
      this.getbankcards(this.state)
    },
    handleClickstate(){
      this.state=Number(this.state)
      this.getbankcards(this.state)
    },
    handleClick36(){
      if(this.activeName36==1){
        this.checkoutOne(this.yuanmsg,1)
      }
      else if(this.activeName36==2){
        this.checkoutOne(this.yuanmsg,0)
      }
    },
    handleClick(){//切换模式
      if(this.activeName==1){
        this.gettableData()
      }
      else {
        this.state=Number(this.state)
        this.getbankcards(this.state)
      }
    },
    change(e){//模式选择
      if(e==0){
        this.tips='（在“全部银行卡”的选项中，表格会将系统判定的异常银行卡标记成黄色）'
      }
      else {
        this.tips= '（在“异常银行卡”的选项中，表格只会展示系统判定的异常银行卡，这些银行卡建议被封禁）'
      }
    },
    checkoutOne(e,type){//异常银行卡操作（＋解禁操作）
      //获取信息
      this.yuanmsg=e
      this.activeName36=1
      if(this.state==0){//异常操作
        this.titles='异常操作'
      }
      else {
        this.titles='解禁操作'
      }
      this.chcekoutonmsg=[]
      this.loadingflag2=true
      request.post("/BankCard/getabnormoldetail",
          {
            cid:e.id,
            type:type
          }).then(res=>{
        if(res.code=='200'){
          this.chcekoutonmsg=res.data
          this.dialogVisible=true
          this.loadingflag2=false
        }
      })
    },
    surecnnn(){
      if(this.state==0){//封禁
        this.banCard(1)
      }
      else if(this.state==1){//解禁
        this.rebancard(1)
      }
    },
    banCard(type){//封禁银行卡
      this.textarea22=''
      if(type==0){
        if(this.yuanmsg.abnormal==0){
          this.textarea22='当前银行卡系统判断不存在异常操作，是否确定进行封禁？'
          this.innerVisible=true
        }
        else{
          request.post("/BankCard/banCardbyId",
              {
                cid:this.yuanmsg.id,
                msg:this.textarea
              }).then(res=>{
            if(res.code=='200'){
              this.getbankcards(0)
              this.dialogVisible=false
              this.innerVisible=false
            }
          })
        }
      }
      else{
        request.post("/BankCard/banCardbyId",
            {
              cid:this.yuanmsg.id,
              msg:this.textarea
            }).then(res=>{
          if(res.code=='200'){
            this.getbankcards(0)
            this.dialogVisible=false
            this.innerVisible=false
          }
        })
      }

    },
    clearerror(){//清除异常
      request.post("/BankCard/clearerror",
          {
            cid:this.yuanmsg.id,
          }).then(res=>{
        if(res.code=='200'){
          this.getbankcards(0)
          this.dialogVisible=false
        }
      })
    },
    rebancard(type){//解禁银行卡
      this.textarea22=''
      if(type==0){
        if(this.yuanmsg.restate==0){
          this.textarea22='当前银行卡用户并未提交银行卡解禁申请，是否对该卡进行解禁'
          this.innerVisible=true
        }
        else {
          request.post("/BankCard/rebancard",
              {
                cid:this.yuanmsg.id,
              }).then(res=>{
            if(res.code=='200'){
              this.getbankcards(1)
              this.dialogVisible=false
              this.innerVisible=false
            }
          })
        }
      }
      else{
        request.post("/BankCard/rebancard",
            {
              cid:this.yuanmsg.id,
            }).then(res=>{
          if(res.code=='200'){
            this.getbankcards(1)
            this.dialogVisible=false
            this.innerVisible=false
          }
        })
      }
    },
  }
}
</script>

<style >
.el-table .warning-row {
  background: oldlace;
}
</style>