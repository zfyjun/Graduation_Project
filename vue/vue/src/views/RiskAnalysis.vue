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
        <el-button  style="margin-left: 5%" type="primary" @click="serchUser()">用户搜索</el-button>
        <el-button  style="margin-left: 5%" type="primary" @click="dialogVisible=true">风险分析<i class="el-icon-edit"></i></el-button>
      </div>
      <el-table :data="tableData" border style="width: 100%;margin-top: 1%">
        <el-table-column prop="id" label="id"></el-table-column>
        <el-table-column prop="name" label="姓名"></el-table-column>
        <el-table-column prop="risk" label="风险等级" sortable></el-table-column>
        <el-table-column prop="account" label="账号"></el-table-column>
        <el-table-column prop="idcard" label="身份证号"></el-table-column>
        <el-table-column prop="phone" label="联系电话"></el-table-column>
        <el-table-column prop="lasttime" label="上次登录时间" sortable></el-table-column>
        <el-table-column label="操作" width="200" align="center">

          <template slot-scope="scope">
            <el-button type="success" @click="handleEdit(scope.row)">风险分析 <i class="el-icon-edit"></i> </el-button>
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
          title="分析数据占比"
          :visible.sync="dialogVisible"
          width="30%"
      >
        <span>这是一段信息</span>
        <span slot="footer" class="dialog-footer">
          <el-button @click="dialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="dialogVisible = false">确 定</el-button>
      </span>
      </el-dialog>
    </div>
  </div>
</template>

<script>
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
      dialogVisible:false,
    }
  },
  created() {
    //请求分页查询
    this.serchUser()
  },
  methods:{
    load(){

    },
    serchUser(){
      this.loading=true
      request.post("/User/getUserRisk",{
        pageNum:this.pageNum,
        pageSize:this.pageSize,
        name:this.name
      }).then(res=>{
        if(res.code=='200'){
          this.tableData=res.data.records
          this.total=res.data.total
        }
        this.loading=false
      })
    },
    handleSizeChange(pageSize) {
      this.pageSize=pageSize
      this.serchUser()
    },
    handleCurrentChange(pageNum) {
      this.pageNum=pageNum
      this.serchUser()
    },
  }
}
</script>

<style scoped>

</style>