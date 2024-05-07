<template>
  <div >
    <div>
      <el-menu :default-active="activeIndex" class="el-menu-demo" mode="horizontal" @select="handleSelect">
        <el-menu-item index="1">产品管理中心</el-menu-item>
        <el-menu-item index="2">产品分析中心</el-menu-item>
        <el-menu-item index="3"><a href="https://www.ele.me" target="_blank">订单管理</a></el-menu-item>
      </el-menu>
    </div>
    <div style="padding: 2%">
      <div v-if="activeIndex==1">
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
            <span slot="label" style="font-size: 12px;margin: auto 0;margin-left: 2%;margin-right: 1%">目标市场</span>
            <el-select  style="width: 15%" @change="changemarket" v-model="value" filterable  placeholder="请选择">
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
          </div>
          <el-divider></el-divider>
        </div>
        <div v-for="(item , index) in showproducts[(currentPageIndex-1)]" style="display: flex;margin-top: 2%">
          <el-card class="box-card" style="width: 70%">
            <div style="display: flex">
              <h4 style="margin-right: 2%"><i class="el-icon-s-goods  el-icon--left" style="color: deepskyblue"></i>{{item.name}}</h4>
              <span style="font-size: 12px;margin: auto 0;color: orange" v-if="item.type==1">固期类产品</span>
              <span style="font-size: 12px;margin: auto 0;color: orange" v-if="item.type==2">限期类产品</span>
            </div>
            <el-divider></el-divider>
            <div style="display: flex">
              <span style="font-size: 15px">{{'当前产品利率：%'+item.rate}}</span>
              <span v-if="item.type==2" style="font-size: 10px;margin: auto 1%">(七日年化)</span>
              <span v-if="item.type==1" style="font-size: 10px;margin: auto 1%">(期限内利率)</span>
              <el-button type="text" style="font-size: 10px;margin: auto 3%">历史利率<i class="el-icon-zoom-in el-icon--right"></i></el-button>
            </div>
            <el-collapse v-model="activeNames" @change="handleChange">
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
                      @show="makemarketnames(item.marketid)"
                      placement="bottom"
                      title="目标市场"
                      width="200"
                      trigger="click"
                      :content="marketmsg">
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

        <div class="block" style="margin-top: 2%">
          <el-pagination
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              :current-page="currentPageIndex"
              :page-sizes="[5, 10, 15, 20]"
              :page-size="100"
              layout="total, sizes, prev, pager, next, jumper"
              :total="searchProducts.length">
          </el-pagination>
        </div>

      </div>
    </div>
  </div>
</template>

<script>
import request from "@/utils/request";
export default {
  name: "ProductManagement",
  data(){
    return {
      activeIndex:'1',
      products:[],
      searchProducts:[],
      showproducts:[],
      currentPageIndex:1,
      pageSize:5,
      nameinput:'',
      marketname:[],
      value:'',
      mid:0,
      typeid:0,
      total:0,
      marketmsg:'',
      ProductValue:'全部类型',
      productType:[{typename:'全部类型',id:0},{typename:'固期产品',id:1},{typename:'限期产品',id:2}]
    }
  },
  created() {
    // 从后台获取数据
    this.getmarketname()
    this.getproduct()
  },
  methods:{
    getproduct(){
      request.post("/Product/getProduct",{}).then(res => {
        if(res.code==='200'){
          this.products=res.data
          this.searchProducts=res.data
          this.makeSizepage()
        }
        else{
          this.$message({
            showClose: true,
            message: res.msg,
            type: 'error'
          })
        }
      })
    },
    handleSelect(key, keyPath) {
      this.activeIndex=key
    },
    makemarketnames(val){//查看选中商品目标市场名称
      this.marketmsg=''
      let marketis=JSON.parse(val)
      console.log(marketis)
      for(let i=0;i<marketis.length;i++){
        for(let j=1;j<this.marketname.length;j++){
          if(this.marketname[j].id==marketis[i].id){
            this.marketmsg=this.marketmsg+this.marketname[j].name+"  "
          }
        }
      }
    },
    changemarket(val){//变换市场
      this.mid=val
      this.searchChange(1)
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
    },
    handleSizeChange(val) {//转换每页条数
      this.pageSize=val
      this.makeSizepage()
    },
    handleCurrentChange(val) {//转换到第几页
      this.currentPageIndex=val
    },
    productTypechange(val){//改变产品类型
      this.typeid=val
      this.searchChange(2)
    },
    nameChange(val){//名字改变时
      this.searchChange(0)
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