<template>
  <div >
    <div>
      <el-menu :default-active="activeIndex" class="el-menu-demo" mode="horizontal" @select="handleSelect">
        <el-menu-item index="1">产品管理中心</el-menu-item>
        <el-menu-item index="2">产品上架</el-menu-item>
        <el-menu-item index="3">产品分析中心</el-menu-item>
      </el-menu>
    </div>
    <div style="padding: 2%">
      <div>
        <div class="demo-input-suffix" style="display: flex" >
          <el-input
              clearable
              @change="nameChange"
              style="width: 15%"
              placeholder="请输入名称"
              prefix-icon="el-icon-search"
              v-model=nameinput>
          </el-input>
          <span v-if="activeIndex==1||activeIndex==2" slot="label" style="font-size: 12px;margin: auto 0;margin-left: 2%;margin-right: 1%">目标市场</span>
          <el-select v-if="activeIndex==1||activeIndex==2"  style="width: 15%" @change="changemarket" v-model="value" filterable  placeholder="请选择">
            <el-option
                v-for="item in marketname"
                :key="item.id"
                :label="item.marketname"
                :value="item.id">
            </el-option>
          </el-select>
          <span slot="label" style="font-size: 12px;margin: auto 0;margin-left: 2%;margin-right: 1%">产品类型</span>
          <el-select  style="width: 10%" @change="productTypechange" v-model="ProductValue" filterable  placeholder="请选择">
            <el-option
                v-for="item in productType"
                :key="item.id"
                :label="item.typename"
                :value="item.id">
            </el-option>
          </el-select>
          <el-button v-if="activeIndex==1" style="margin-left: 9.5%" type="primary" @click="addproduct()">新增产品</el-button>
        </div>
        <el-divider></el-divider>
      </div>
      <div v-if="activeIndex==1">
        <div style="margin: 0 auto">
          <div v-for="(item , index) in showproducts[(currentPageIndex-1)]" style="display: flex;margin-top: 2%">
            <el-card class="box-card" style="width: 70%">
              <div style="display: flex;justify-content: space-between">
                <div style="display: flex;width: 100%">
                  <h4 style="margin-right: 2%"><i class="el-icon-s-goods  el-icon--left" style="color: deepskyblue"></i>{{item.name}}</h4>
                  <span style="font-size: 12px;margin: auto 0;color: orange" v-if="item.type==1">固期类产品</span>
                  <span style="font-size: 12px;margin: auto 0;color: orange" v-if="item.type==2">限期类产品</span>
                </div>
                <div style="display: flex">
                  <el-button type="text" style="font-size: 13px" @click="editproduct(item)">编辑<i class="el-icon-edit"></i></el-button>
                  <el-popconfirm
                      title="确定要下架该产品吗？"
                      @confirm="remove(item)"
                  >
                    <el-button slot="reference" type="text" style="color: red;margin-left: 5%;font-size: 13px" >下架<i class="el-icon-delete"></i></el-button>
                  </el-popconfirm>
                </div>
              </div>
              <el-divider></el-divider>
              <div style="display: flex">
                <span style="font-size: 15px">{{'当前产品利率：%'+item.rate}}</span>
                <span v-if="item.type==2" style="font-size: 10px;margin: auto 1%">(七日年化)</span>
                <span v-if="item.type==1" style="font-size: 10px;margin: auto 1%">(期限内利率)</span>
                <el-button  @click="showrate(item.historicalrate)" type="text" style="font-size: 10px;margin: auto 3%">历史利率<i class="el-icon-zoom-in el-icon--right"></i></el-button>
              </div>
              <el-collapse >
                <el-collapse-item  name="1">
                  <template slot="title">
                    <i class="header-icon el-icon-info" style="margin-right: 2px"></i>
                    详情描述
                  </template>
                  <div>{{item.description}}</div>
                </el-collapse-item>
              </el-collapse>
              <div>
                <div class="box" style="margin-top: 1%;margin-bottom: -2%">
                  <div style="padding: 1%">
                    <el-popover
                        @show="makemarketnames(item.targetmarket)"
                        placement="bottom"
                        title="目标市场"
                        width="200"
                        trigger="click"
                    >
                      <div>
                        <span>{{marketmsg}}</span>
                      </div>
                      <el-button  slot="reference" type="text" style="font-size: 12px"  >目标市场<i class="el-icon-question  el-icon--right" ></i></el-button>
                    </el-popover>
                    <div style="display: flex">
                      <span style="font-size: 12px">{{'起购价：￥'+item.price+'元'}}</span>
                      <span v-if="item.type==1" style="font-size: 12px;margin-left: 3%">{{'固定封闭：'+item.minday+'天'}}</span>
                      <span v-if="item.type==2" style="font-size: 12px;margin-left: 3%">{{'最低持有：'+item.minday+'天'}}</span>
                      <span v-if="item.risk==1||item.risk==2" style="font-size: 14px;margin-left: 3%;color: limegreen">{{'风险等级R'+item.risk}}</span>
                      <span v-if="item.risk==3" style="font-size: 14px;margin-left: 3%;color: deepskyblue">{{'风险等级R'+item.risk}}</span>
                      <span v-if="item.risk==4" style="font-size: 14px;margin-left: 3%;color: orange">{{'风险等级R'+item.risk}}</span>
                      <span v-if="item.risk==5" style="font-size: 14px;margin-left: 3%;color: red">{{'风险等级R'+item.risk}}</span>
                    </div>
                  </div>
                </div>.
              </div>

            </el-card>
          </div>
        </div>
      </div>

      <div v-if="activeIndex==2">
        <template>
          <el-table
              border
              :data="searchProducts"
              stripe
              style="width: 70%">
            <el-table-column
                fixed
                prop="name"
                label="产品名称"
                width="200">
            </el-table-column>
            <el-table-column
                prop="type"
                label="类型（1为固期，2为限期）"
                width="180">
            </el-table-column>
            <el-table-column
                prop="rate"
                label="利率（%）"
                width="180"
            >
            </el-table-column>
            <el-table-column
                prop="minday"
                label="最低期限"
                width="180"
            >
            </el-table-column>
            <el-table-column
                prop="price"
                label="起购价"
                width="180"
            >
            </el-table-column>
            <el-table-column
                prop="risk"
                label="风险等级"
                width="180"
            >
            </el-table-column>
            <el-table-column
                fixed="right"
                label="操作"
                width="100">
              <template slot-scope="scope">
                <el-popconfirm
                    title="确定要上架该产品吗？"
                    @confirm="upproduct(scope.row)"
                >
                  <el-button slot="reference" type="text" size="small">上架</el-button>
                </el-popconfirm>

                <el-button @click="editproduct(scope.row)" type="text" size="small">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </template>
      </div>

      <div v-if="activeIndex==3">
        <el-table
            :data="choosenproducts"
            stripe
            border
            style="width: 100%">
          <el-table-column
              fixed
              prop="name"
              label="产品名称"
              width="180">
          </el-table-column>
          <el-table-column
              fixed
              prop="createtime"
              label="创建时间"
              width="180">
          </el-table-column>
          <el-table-column
              prop="typename"
              label="产品类型"
              width="80">
          </el-table-column>
          <el-table-column
              prop="risk"
              label="产品风险"
              width="80">
          </el-table-column>
          <el-table-column
              prop="rate"
              label="当前利率（%）"
              width="120">
          </el-table-column>
          <el-table-column
              prop="minday"
              label="持有期限（天）"
              width="100">
          </el-table-column>
          <el-table-column
              prop="amount"
              label="总投资（￥）"
              width="100">
          </el-table-column>
          <el-table-column
              prop="price"
              label="起购价(￥)"
              width="100">
          </el-table-column>
          <el-table-column
              prop="description"
              label="产品描述"
              width="400">
          </el-table-column>
          <el-table-column
              fixed="right"
              label="操作"
              width="100">
            <template slot-scope="scope">
              <el-button style="margin-left: 10%" @click="analysisone(scope.row)" type="text" size="small">分析</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
    <div>
      <el-dialog
          title="历史利率"
          :visible.sync="dialogVisible"
          width="70%"
          close-on-press-escape="true"
      >
        <div id="rates" style="width: 100%;height: 300px"></div>
      </el-dialog>

      <el-dialog
          title="表单填写"
          :visible.sync="dialogVisible2"
          width="30%"
      >
        <el-form ref="form" :model="producteditOne" label-width="80px">
          <el-form-item label="商品名称">
            <el-input style="width: 70%" v-model="producteditOne.name"></el-input>
          </el-form-item>
          <el-form-item v-if="dialogVisible3===true||activeIndex==2" label="目标市场">
            <el-select @change="selectmarket" v-model="targetmarket" multiple placeholder="选择目标市场">
              <el-option
                  v-for="item in marketname"
                  :key="item.id"
                  :label="item.marketname"
                  :value="item.id">
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item  v-if="dialogVisible3===true" label="产品类型">
            <el-select v-model="producteditOne.type" placeholder="请选产品类型">
              <el-option label="固期产品" :value='1'></el-option>
              <el-option label="限期产品" :value='2'></el-option>
            </el-select>
          </el-form-item>
          <el-form-item v-if="producteditOne.type==2" label="最低期限">
            <el-input-number v-model="producteditOne.minday" step-strictly="true" :step="1" :min="1"></el-input-number>
          </el-form-item>
          <el-form-item v-if="producteditOne.type==1" label="封闭天数">
            <el-input-number v-model="producteditOne.minday" step-strictly="true" :step="1" :min="1"></el-input-number>
          </el-form-item>
          <el-form-item v-if="dialogVisible3===true||activeIndex==2" label="利率%">
            <el-input-number controls-position="right" v-model="producteditOne.rate" step-strictly="true" :step="0.01" :min="-100" :max="100"></el-input-number>
          </el-form-item>
          <el-form-item v-if="dialogVisible3===true||activeIndex==2" label="风险等级">
            <el-input-number v-model="producteditOne.risk" step-strictly="true" :step="1" :min="1" :max="5"  ></el-input-number>
          </el-form-item>
          <el-form-item label="总额度￥">
            <el-input-number style="width: 50%" controls-position="right" v-model="producteditOne.amount" step-strictly="true" :step="1" :min="1" ></el-input-number>
          </el-form-item>
          <el-form-item label="起购价￥">
            <el-input-number style="width: 50%" controls-position="right" v-model="producteditOne.price" step-strictly="true" :step="1" :min="1" :max="producteditOne.amount" ></el-input-number>
          </el-form-item>
          <el-form-item label="详情描述">
            <el-input type="textarea" v-model="producteditOne.description"></el-input>
          </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
          <el-button @click="dialogVisible2 = false">取 消</el-button>
          <el-button type="primary" @click="editSure">确 定</el-button>
        </span>
      </el-dialog>

      <el-dialog
          :title="analysisproductone.name"
          :visible.sync="analysoneflag"
          width="60%"
          :before-close="handleClose"
          >
           <div style="padding: 2%">
             <el-tabs v-model="activeName" @tab-click="handleClickactive" type="border-card"  >
               <el-tab-pane label="市场数据" name="first" >
                 <div v-if="activeName=='first'">
                   <div style="display: flex">
                     <span>投资占比：</span>
                     <div v-for="(item ,index) in marketsmsg" style="margin-left: 2%">
                       <div>
                         <span>{{item.marketname+': '}}</span>
                       </div>
                       <el-input-number v-model="mmarketrates[index]" :precision="2" :step="0.01" step-strictly="true" :min="0.00" :max="1"></el-input-number>
                     </div>
                   </div>
                   <div style="margin-top: 2%">
                     <span >得出利率(%)：</span>
                     <el-input-number v-model="truerate" :precision="2" :step="0.01" step-strictly="true" ></el-input-number>
                     <el-button style="margin-left: 2%" @click="rework()" >重新计算</el-button>
                   </div>
                   <el-tabs v-model="editableTabsValue" >
                     <el-tab-pane
                         v-for="(item, index) in marketsmsg"
                         :key="item.id"
                         :label="item.marketname"
                         :name="item.id"
                     >
                       <div>
                         <div >
                           <span>该市场前景为：</span>
                           <span v-if="item.evaluation==1||item.evaluation==2" style="color: limegreen">上升</span>
                           <span v-if="item.evaluation==3" style="color: deepskyblue">平稳</span>
                           <span v-if="item.evaluation==4||item.evaluation==5" style="color: orange">下降</span>
                         </div>
                       </div>
                     </el-tab-pane>
                   </el-tabs>
                 </div>
               </el-tab-pane>
               <el-tab-pane label="用户数据" name="second" ></el-tab-pane>
             </el-tabs>
             <div style="padding-top: 1%">

               <div v-if="activeName=='second'">

               </div>
             </div>
           </div>
           <span slot="footer" class="dialog-footer">
              <el-button type="primary" @click="analysoneflag = false">确 定</el-button>
          </span>
      </el-dialog>
    </div>
    <div v-if="activeIndex==1||activeIndex==2" class="block" style="margin-top: 2%">
      <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPageIndex"
          :page-sizes="[2, 4, 6, 8]"
          :page-size="100"
          layout="total, sizes, prev, pager, next, jumper"
          :total="searchProducts.length">
      </el-pagination>
    </div>
    <div v-if="activeIndex==3" class="block" style="margin-top: 2%">
      <el-pagination
          @size-change="handleSizeChange2"
          @current-change="handleCurrentChange2"
          :current-page="pageNum"
          :page-sizes="[5, 10, 15, 20]"
          :page-size="pageSizes"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total2">
      </el-pagination>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts';
import request from "@/utils/request";
export default {
  name: "ProductManagement",
  data(){
    return {
      activeIndex:'1',
      activeName:'first',
      products:[],
      searchProducts:[],
      showproducts:[],
      currentPageIndex:1,
      pageSize:2,
      pageNum:1,
      pageSizes:5,
      nameinput:'',
      marketname:[],
      value:'',
      mid:0,
      typeid:0,
      total:0,
      total2:0,
      marketmsg:'',
      ProductValue:'全部类型',
      productType:[{typename:'全部类型',id:0},{typename:'固期产品',id:1},{typename:'限期产品',id:2}],
      dialogVisible:false,
      dialogVisible2:false,
      dialogVisible3:false,
      producteditOne:{id:null,name:'',type:'',rate:'',minday:'',createtime:'',risk:'',description:'',amount:'',price:'',sum:'',targetmarket:[],historicalrate:[]},
      rates:[],
      targetmarket:[],
      choosenproducts:[],
      analysisproductone:{},
      analysoneflag:false,
      marketids:[],
      marketsmsg:[],
      editableTabsValue:'',
      mmarketrates:[],
      truerate:0,
    }
  },
  created() {
    // 从后台获取数据
    this.getmarketname()
    this.getproduct()
  },
  methods:{
    getproduct(){//获取商品信息
        request.post("/Product/getProduct",{}).then(res => {
        if(res.code==='200'){
          this.products=res.data
          this.searchProducts=res.data
          this.makeSizepage()
        }
        else{
          this.products=[]
          this.showproducts=[]
          this.searchProducts=[]
        }
      })
    },
    getproductremove(){//获取下架产品
      request.post("/Product/getProductremove",{}).then(res => {
        if(res.code==='200'){
          this.products=res.data
          this.searchProducts=res.data
          this.makeSizepage()
        }
        else{
          this.products=[]
          this.showproducts=[]
          this.searchProducts=[]
        }
      })
    },
    addproduct(){//新增产品
      this.targetmarket=[]
      this.producteditOne={id:null,name:'',type:'',rate:0,minday:1,createtime:'',risk:1,description:'',amount:1,price:1,sum:0,targetmarket:[],historicalrate:"[]"};
      this.dialogVisible2=true
      this.dialogVisible3=true
    },
    editproduct(val){//产品编辑
      this.producteditOne=JSON.parse(JSON.stringify(val))
      let markets=JSON.parse(this.producteditOne.targetmarket)
      this.targetmarket=[]
      for(let i=0;i<markets.length;i++){
        this.targetmarket.push(markets[i].id)
      }
      this.dialogVisible3=false
      this.dialogVisible2=true
    },
    deleteproduct(val){
      console.log(val)
    },
    editSure(){//确定产品编辑或新增
      if(this.dialogVisible3==false){//编辑
        if(this.activeIndex==2){//下架的编辑
          this.producteditOne.targetmarket=[]
          for(let i=0;i<this.targetmarket.length;i++){
            this.producteditOne.targetmarket.push({"id":this.targetmarket[i]})
          }
          this.producteditOne.targetmarket=JSON.stringify(this.producteditOne.targetmarket)
        }
        request.post("/Product/editProduct",this.producteditOne).then(res => {
          if(res.code==='200'){
            if(this.activeIndex==1){
              this.getproduct()
            }
            else {
              this.getproductremove()
            }
            this.dialogVisible2=false
            this.$message({
              showClose: true,
              message: '商品更新成功！',
              type: 'success'
            })
          }
          else{
            this.$message({
              showClose: true,
              message: res.msg,
              type: 'error'
            })
          }
        })
      }
      else {//新增
        for(let i=0;i<this.targetmarket.length;i++){
          let target={"id":''}
          target.id=this.targetmarket[i]
          this.producteditOne.targetmarket.push(target)
        }
        let msg=this.test()
        if(msg!=''){
          this.$message({
            showClose: true,
            message: msg,
            type: 'error'
          })
        }
        else {//验证成功
          this.producteditOne.targetmarket=JSON.stringify(this.producteditOne.targetmarket)
          request.post("/Product/addProduct",this.producteditOne).then(res => {
            if(res.code==='200'){
              this.getproduct()
              this.dialogVisible2=false
              this.dialogVisible3=false
              this.$message({
                showClose: true,
                message: '商品添加成功！',
                type: 'success'
              })
            }
            else{
              this.$message({
                showClose: true,
                message: res.msg,
                type: 'error'
              })
            }
          })
        }
      }
    },
    test(){//输入验证
      let msg=''
      if(this.producteditOne.name==''||this.producteditOne.name==null){
        msg=msg+'产品名称； '
      }
      if(this.producteditOne.targetmarket.length==0){
        msg=msg+'目标市场； '
      }
      if(this.producteditOne.type==''||this.producteditOne.type==null){
        msg=msg+'产品类型； '
      }
      if (msg!=''){
        msg=msg+'字段不能为空'
      }
      return msg
    },
    selectmarket(val){//新增产品时选择的目标市场
      console.log(val)
    },
    makemarketnames(val){//查看选中商品目标市场名称
      this.marketmsg=''
      let marketis=JSON.parse(val)
      for(let i=0;i<marketis.length;i++){
        for(let j=1;j<this.marketname.length;j++){
          if(this.marketname[j].id===marketis[i].id){
            console.log(this.marketname[j])
            this.marketmsg=this.marketmsg+this.marketname[j].marketname+"  "
          }
        }
      }
      return this.marketmsg
    },
    changemarket(val){//变换市场
      this.mid=val
      this.searchChange(1)
    },
    showrate(val){//展示历史利率
      this.rates=JSON.parse(val)
      this.dialogVisible=true
      this.$nextTick(()=>{
        this.makeratecharts(this.rates)
      })
    },
    remove(val){//产品下架
      request.post("/Product/removeProduct",val).then(res => {
        if(res.code==='200'){
          this.$message({
            showClose: true,
            message: '产品下架成功',
            type: 'success'
          })
          this.getproduct()
        }
        else {
          this.$message({
            showClose: true,
            message: res.msg,
            type: 'error'
          })
        }
      })
    },
    upproduct(val){//产品上架
      console.log(val)
      request.post("/Product/upProduct",val).then(res => {
        if(res.code==='200'){
          this.$message({
            showClose: true,
            message: '产品上架成功',
            type: 'success'
          })
          this.getproductremove()
        }
        else {
          this.$message({
            showClose: true,
            message: res.msg,
            type: 'error'
          })
        }
      })
    },
    makeratecharts(rates){//展示市场历史利率折线图
      let myChart = echarts.getInstanceByDom(document.getElementById("rates"))
      // 如果不存在，就进行初始化
      if (myChart == null) {
        myChart = echarts.init(document.getElementById("rates"));
      }
      let option;
      let data=[]
      for(let i=0;i<rates.length;i++){
        let num=[Date.parse(rates[i].time),rates[i].rate]
        data.push(num)
      }
      option = {
        tooltip: {
          trigger: 'axis',
          position: function (pt) {
            return [pt[0], '10%'];
          }
        },
        title: {
          left: 'center',
          text: '产品历史利率折线图'
        },
        toolbox: {
          feature: {
            dataZoom: {
              yAxisIndex: 'none'
            },
            restore: {},
            saveAsImage: {}
          }
        },
        xAxis: {
          type: 'time',
          boundaryGap: false
        },
        yAxis: {
          type: 'value',
          boundaryGap: [0, '100%']
        },
        dataZoom: [
          {
            type: 'inside',
            start: 0,
            end: 100
          },
          {
            start: 0,
            end: 100
          }
        ],
        series: [
          {
            name: 'RATE利率(%)',
            type: 'line',
            smooth: true,
            symbol: 'none',
            areaStyle: {},
            data: data
          }
        ]
      };
      option && myChart.setOption(option);
    },
    getmarketname(){//获取现有的市场数据
      request.post("/MarketName/getMarketNames",{
      }).then(res => {
        if(res.code==='200'){
          this.marketname=res.data
          this.marketname.unshift({
            "id": 0,
            "marketname": "全部市场",
            "evaluation": null,
            "nowdata": 0.0,
            "predictdata": 0.0,
            "predicttime": null
          })
          this.value=res.data[0].marketname
          this.mid=res.data[0].id
        }
      })
    },
    searchChange(val){//搜索改变
      this.searchProducts=JSON.parse(JSON.stringify(this.products))
      console.log(this.mid+"  "+this.typeid+"  "+this.nameinput)
      //名字
      if(val==0&&(this.nameinput!=''||this.nameinput!=null)){
        for(let i=0;i<this.searchProducts.length;i++){
          if((this.searchProducts[i].name.indexOf(this.nameinput)==-1)){//不包含
            this.searchProducts.splice(i,1)
            i--;
          }
        }
      }
      else if(val==1&&this.mid!=0){//市场变换
        for(let i=0;i<this.searchProducts.length;i++){
          let marketis=JSON.parse(this.searchProducts[i].targetmarket)
          let flag=0
          console.log(marketis)
          for(let j=0;j<marketis.length;j++){
            if(marketis[j].id==this.mid){
              flag=1
              break
            }
          }
          if(flag==0){//不包含
            this.searchProducts.splice(i,1)
            i--
          }
        }
      }
      else if(val==2&&this.typeid!=0){//类型变换
        for(let i=0;i<this.searchProducts.length;i++){
          if(this.searchProducts[i].type!=this.typeid){//不包含
            this.searchProducts.splice(i,1)
            i--;
          }
        }
      }
      this.makeSizepage()
    },
    makeSizepage(){//每页装多少条数据的函数实现
      let sum=0
      let size=0
      this.showproducts=[]
      while (sum<this.searchProducts.length){
        let page=[]
        for(let i=0;i<this.pageSize;i++){
          if(sum<this.searchProducts.length){
            page.push(this.searchProducts[sum])
            sum++;
          }
          else {
            break
          }
        }
        this.showproducts.push(page)
        size++
      }
      for(let j=0;j<this.showproducts.length;j++){
        for(let i=0;i<this.showproducts[j].length;i++){
          if(this.showproducts[j][i].type==1){
            this.showproducts[j][i].typename='固期产品'
          }
          else {
            this.showproducts[j][i].typename='限期产品'
          }
        }
      }
      console.log(this.showproducts)
    },
    handleSizeChange(val) {//转换每页条数
      this.pageSize=val
      this.makeSizepage()
    },
    handleSizeChange2(val){//切换
      this.pageSizes=val
      this.selcetpages()
    },
    handleCurrentChange(val) {//转换到第几页
      this.currentPageIndex=val
    },
    handleCurrentChange2(val){
      this.pageNum=val
      this.selcetpages()
    },
    productTypechange(val){//改变产品类型
      this.typeid=val
      if(this.activeIndex==1||this.activeIndex==2){
        this.searchChange(2)
      }
      else {
        this.selcetpages()
      }
    },
    nameChange(val){//名字改变时
      if(this.activeIndex==1||this.activeIndex==2){
        this.searchChange(0)
      }
      else {
        this.selcetpages()
      }
    },
    handleSelect(key, keyPath) {//界面变换
      this.activeIndex=key
      if(key==1){
        this.targetmarket=[]
        this.getproduct()
      }
      else if(key==2){
        this.getproductremove()
      }
      else if(key==3){
        this.selcetpages()
      }
    },
    selcetpages(){//分页操作（产品分析）
      request.post("/Product/selectpages",{
        name:this.nameinput,
        type:this.typeid,
        pageNum:this.pageNum,
        pageSize:this.pageSizes
      }).then(res => {
        if(res.code==='200'){
          this.choosenproducts=res.data.records
          this.total2=res.data.total
          for(let i=0;i<this.choosenproducts.length;i++){
            if(this.choosenproducts[i].type==1){
              this.choosenproducts[i].typename='固期产品'
            }
            else{
              this.choosenproducts[i].typename='限期产品'
            }
          }
        }
        else{

        }
      })
    },
    analysisone(val){//单产品分析
      this.mmarketrates=[]
      this.marketsmsg=[]
      this.analysisproductone=JSON.parse(JSON.stringify(val))
      this.truerate=this.analysisproductone.rate
      this.mmarketrates=JSON.parse(this.analysisproductone.marketrate)
      this.marketids=[]
      this.analysisproductone.targetmarket=JSON.parse(this.analysisproductone.targetmarket)
      for(let i=0;i<this.analysisproductone.targetmarket.length;i++){
        this.marketids.push(this.analysisproductone.targetmarket[i].id)
      }
      request.post("/Predict/getProductMarketRate",
        this.marketids
      ).then(res => {
        if(res.code==='200'){
          this.marketsmsg=res.data
          this.editableTabsValue=this.marketsmsg[0].id
          console.log(this.marketsmsg)
          this.analysoneflag=true
        }
      })
    },
    rework(){//重新计算
      for(let i=0;i<this.marketsmsg.length;i++){
        let chaju=this.mmarketrates[i]*((this.marketsmsg[i].predictdata-this.marketsmsg[i].nowdata)/this.marketsmsg[i].nowdata)*5
        this.truerate=this.truerate+chaju
      }
    },
    handleClickactive(tab,event){

    },
    handleClose(){//关闭
      this.analysoneflag=false
      this.analysisproductone={}
    }
  }
}
</script>

<style scoped>
.box{
  margin: 0 auto;
  width: 100%;
  border: 1px solid #ddd;
  border-radius: 2px;
  box-shadow: 0 2px 2px rgba(0.1,0.1,0.1,0.1);
}
</style>