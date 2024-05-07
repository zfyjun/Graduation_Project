<template>
  <div id="app" style="padding: 2%" v-loading="loading" >
    <el-form ref="form"  label-width="100px">
      <el-form-item label="选择市场：">
        <el-select  @change="changemarket" v-model="valuemarket" filterable  placeholder="请选择">
          <el-option
              v-for="item in marketname"
              :key="item.id"
              :label="item.marketname"
              :value="item.id">
          </el-option>
        </el-select>
      </el-form-item>
    </el-form>
    <div style="display:flex;">
      <el-upload
          action
          accept = ".xlsx, .xls, .csv"
          :auto-upload="false"
          :show-file-list="false"
          :on-change="handle">
        <el-button type="primary">打开表格</el-button>
      </el-upload>
      <el-button style="margin-left: 5%" type="primary" @click="clearData" :disabled="disabled" >手动清洗</el-button>
      <el-button style="margin-left: 5%" type="primary" @click="AutoclearData" :disabled="disabled" >自动清洗</el-button>
      <el-button style="margin-left: 5%" type="primary" @click="addDate" :disabled="disabled" >上传数据</el-button>
    </div>
    <div style="margin-top: 1%">
      <el-table
          border
          :data="tableData"
          style="width: 70%"
          height="250">
        <el-table-column
            fixed
            prop="id"
            label="位置"
            width="50">
        </el-table-column>
        <el-table-column
            fixed
            prop="date"
            label="日期"

            width="150">
        </el-table-column>
        <el-table-column
            prop="open"
            label="开盘价"
            width="120">
        </el-table-column>
        <el-table-column
            prop="high"
            label="最高价"
            width="120">
        </el-table-column>
        <el-table-column
            prop="low"
            label="最低价"
            width="120">
        </el-table-column>
        <el-table-column
            prop="close"
            label="收盘价"
            width="120">
        </el-table-column>
        <el-table-column
            prop="volume"
            label="成交量"
            width="120">
        </el-table-column>
        <el-table-column
            fixed="right"
            prop="adjustedclose"
            label="加权平均价"
            width="200">
        </el-table-column>
      </el-table>
    </div>

    <div style="margin-top: 5%" v-if="handworkflag==true">
      <h4 style="margin-top: 2%;margin-bottom: 2%">清洗详情表</h4>
      <el-table
          border
          :data="clearDateWork"
          style="width: 70%"
          height="400">

        <el-table-column
            fixed
            prop="id"
            label="原表格位置"
            width="50">
        </el-table-column>
        <el-table-column
            prop="date"
            label="日期"
            width="150">
        </el-table-column>
        <el-table-column
            prop="open"
            label="开盘价"
            width="120">
        </el-table-column>
        <el-table-column
            prop="high"
            label="最高价"
            width="120">
        </el-table-column>
        <el-table-column
            prop="low"
            label="最低价"
            width="120">
        </el-table-column>
        <el-table-column
            prop="close"
            label="收盘价"
            width="120">
        </el-table-column>
        <el-table-column
            prop="volume"
            label="成交量"
            width="120">
        </el-table-column>
        <el-table-column
            prop="adjustedclose"
            label="加权平均价"
            width="150">
        </el-table-column>
        <el-table-column
            fixed="right"
            prop="msg"
            label="分析详情"
            width="200">
        </el-table-column>
        <el-table-column
            fixed="right"
            label="操作"
            width="100">
          <template slot-scope="scope">
            <el-button @click="handleClick(scope.row)" type="text" size="small">编辑</el-button>
            <el-popconfirm
                confirm-button-text='确定'
                cancel-button-text='取消'
                icon="el-icon-info"
                icon-color="red"
                title="确定将这段数据包括原表格中的该数据删除吗？"
                @confirm="deleteDate(scope.row)"
            >
              <el-button style="color: orangered;margin-left: 1%" type="text" size="small" slot="reference" >删除</el-button>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </div>
<!--对话框==================================================================================-->
    <div>
      <el-dialog
          title="字段选取"
          :visible.sync="dialogVisible"
          width="30%"
          :before-close="handleClose">
        <div>
          <div v-for="(item,index) in keyarr">
            <div>
              <div style="display: flex;justify-content: space-between">
                <span>{{item.keyname}}</span>
                <el-select clearable style=" " v-model="value[index]" placeholder="请选择"  >
                  <el-option
                      v-for="items in options"
                      :key="items.value"
                      :label="items.label"
                      :value="items.truev"
                  >
                  </el-option>
                </el-select>
              </div>
              <el-divider></el-divider>
            </div>
          </div>
        </div>
        <span slot="footer" class="dialog-footer">
            <el-button @click="dialogVisible = false">取 消</el-button>
            <el-button type="primary" @click="sure()">确 定</el-button>
        </span>
      </el-dialog>
    </div>

    <div>
      <el-dialog
          title="数据编辑"
          :visible.sync="edithand"
          width="30%"
          :before-close="handleClose">
        <div>
          <el-form :model="editrow" ref="numberValidateForm" label-width="100px" class="demo-ruleForm">
            <el-form-item
                label="日期"
                prop="date"
               >
              <el-input v-model.number="editrow.date" autocomplete="off"></el-input>
            </el-form-item>
            <el-form-item
                label="开盘价"
                prop="open"
            >
              <el-input v-model.number="editrow.open" autocomplete="off"></el-input>
              <el-popover
                  placement="top-start"
                  width="200"
                  trigger="hover"
                  content="自动填入将取该元素所有有效数值的平均值。">
                <el-button slot="reference" v-if="editrow.open==''||editrow.open==null" type="text" @click="autoTonumber(1)">自动填入</el-button>
              </el-popover>
            </el-form-item>
            <el-form-item
                label="最高价"
                prop="high"
            >
              <el-input v-model.number="editrow.high" autocomplete="off"></el-input>
              <el-popover
                  placement="top-start"
                  width="200"
                  trigger="hover"
                  content="自动填入将取该元素所有有效数值的平均值。">
                <el-button slot="reference" v-if="editrow.high==''||editrow.high==null" type="text" @click="autoTonumber(2)">自动填入</el-button>
              </el-popover>

            </el-form-item>
            <el-form-item
                label="最低价"
                prop="low"
            >
              <el-input v-model.number="editrow.low" autocomplete="off"></el-input>
              <el-popover
                  placement="top-start"
                  width="200"
                  trigger="hover"
                  content="自动填入将取该元素所有有效数值的平均值。">
                <el-button slot="reference" v-if="editrow.low==''||editrow.low==null" type="text" @click="autoTonumber(3)">自动填入</el-button>
              </el-popover>
            </el-form-item>
            <el-form-item
                label="收盘价"
                prop="close"
            >
              <el-input v-model.number="editrow.close" autocomplete="off"></el-input>
              <el-popover
                  placement="top-start"
                  width="200"
                  trigger="hover"
                  content="自动填入将取该元素所有有效数值的平均值。">
                <el-button slot="reference" v-if="editrow.close==''||editrow.close==null" type="text" @click="autoTonumber(4)">自动填入</el-button>
              </el-popover>

            </el-form-item>
            <el-form-item
                label="成交量"
                prop="volume"
            >
              <el-input v-model.number="editrow.volume" autocomplete="off"></el-input>

              <el-popover
                  placement="top-start"
                  width="200"
                  trigger="hover"
                  content="自动填入将取该元素所有有效数值的平均值。">
                <el-button slot="reference" v-if="editrow.volume==''||editrow.volume==null" type="text" @click="autoTonumber(5)">自动填入</el-button>
              </el-popover>

            </el-form-item>
            <el-form-item
                label="平均加权价"
                prop="adjustedclose"
            >
              <el-input v-model.number="editrow.adjustedclose" autocomplete="off"></el-input>
              <el-popover
                  placement="top-start"
                  width="200"
                  trigger="hover"
                  content="自动填入将取该元素所有有效数值的平均值。">
                <el-button slot="reference" v-if="editrow.adjustedclose==''||editrow.adjustedclose==null" type="text" @click="autoTonumber(6)">自动填入</el-button>
              </el-popover>

            </el-form-item>

          </el-form>
        </div>
        <span slot="footer" class="dialog-footer">
            <el-button @click="edithand = false">取 消</el-button>
            <el-button type="primary" @click="sureedit()">确 定</el-button>
        </span>
      </el-dialog>
    </div>

    <div >
      <el-dialog  close-on-press-escape="true" title="数据上传" :visible.sync="ctableDataflag">
        <span style="margin-bottom: 2%;color: orange" >以下数据与数据库中数据的时间重复，请选择需要覆盖数据库的数据进行数据更新</span>
        <el-table
            v-loading="loading2"
            ref="multipleTable"
            :data="ctableData"
            tooltip-effect="dark"
            style="width: 100%"
            height="250"
            @selection-change="handleSelectionChange"
        >
          <el-table-column
              type="selection"
              width="55">
          </el-table-column>
          <el-table-column
              type="index"
              width="50">
          </el-table-column>
          <el-table-column
              fixed
              prop="date"
              label="日期"
              width="150">
          </el-table-column>
          <el-table-column
              prop="open"
              label="开盘价"
              width="120">
          </el-table-column>
          <el-table-column
              prop="high"
              label="最高价"
              width="120">
          </el-table-column>
          <el-table-column
              prop="low"
              label="最低价"
              width="120">
          </el-table-column>
          <el-table-column
              prop="close"
              label="收盘价"
              width="120">
          </el-table-column>
          <el-table-column
              prop="volume"
              label="成交量"
              width="120">
          </el-table-column>
          <el-table-column
              fixed="right"
              prop="adjustedclose"
              label="加权平均价"
              width="200">
          </el-table-column>
        </el-table>
        <span slot="footer" class="dialog-footer">
            <el-button @click="ctableDataflag = false">取 消</el-button>
            <el-button type="primary" @click="updatemarketdate()">确 定</el-button>
        </span>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import request from "@/utils/request";
import * as XLSX from 'xlsx/xlsx.mjs'
export default {
  name: 'DataClean',
  data(){
    return {
      message:' XLSX 的使用',
      updateDate:[],
      keyarr:[],
      surekeyarr:[],
      disabled:true,
      dialogVisible:false,
      edithand:false,
      handworkflag:false,
      marketname:[],
      mid:1,
      valuemarket:'',
      options: [
          {
        value: '0',
        label: '日期(date)',
        truev:'date',
        disabled: false
      }, {
        value: '1',
        label: '开盘价(open)',
        truev:'open',
        disabled: false
      }, {
        value: '2',
        label: '最高价(high)',
        truev:'high',
        disabled: false
      }, {
        value: '3',
        label: '最低价(low)',
        truev:'low',
        disabled: false
      }, {
        value: '4',
        label: '收盘价(close)',
        truev:'close',
        disabled: false
      }, {
        value: '5',
        label: '成交量(volume)',
        truev:'volume',
        disabled: false
      }, {
        value: '6',
        label: '加权平均价(adjust)',
        truev:'adjustedclose',
        disabled: false
      },
      ],
      value: [],
      tableData:[],
      clearDateWork:[],//用于保存有错误的数据
      editrow:{},
      ctableData:[],
      ctableDataflag:false,
      multipleSelection:[],
      loading:false,
      loading2:false

    }
  },
  created() {
    // 从后台获取数据
    this.getmarketname()
  },
  methods:{
    readFile(file){//文件读取
      return new Promise(resolve => {
        let reader = new FileReader();
        reader.readAsBinaryString(file);//以二进制的方式读取
        reader.onload = ev =>{
          resolve(ev.target.result);
        }
      })
    },
    async handle(ev){
      let file = ev.raw;
      if(!file){
        console.log("文件打开失败")
        return ;
      }else{
        let data = await this.readFile(file);
        let workbook = XLSX.read(data,{ type: "binary"});//解析二进制格式数据
        // console.log('二进制数据的解析:')
        // console.log(workbook)
        let worksheet = workbook.Sheets[workbook.SheetNames[0]];//获取第一个Sheet
        let result = XLSX.utils.sheet_to_json(worksheet);//json数据格式
        // console.log('最终解析的 json 格式数据:')
        // console.log(result)
        this.updateDate=result;
        this.value=[]
        this.keyarr=[]
        this.tableData=[]
        this.clearDateWork=[]
        for(var key in this.updateDate[0]){
          var keyone={keyname:'',tokeyname:''}
          keyone.keyname=key
          this.keyarr.push(keyone)
          this.value.push('')
        }
        this.startin()
      }
    },
    handleClose(done) {//关闭面板
      this.$confirm('确认关闭？')
          .then(_ => {
            done();
          })
          .catch(_ => {});
    },
    sureedit(){//确定编辑的内容
      if(this.editrow.date==''||this.editrow.open==''||this.editrow.low==''||this.editrow.high==''||this.editrow.close==''||this.editrow.volume==''||this.editrow.adjustedclose==''){
        this.$message({
          showClose: true,
          message: '存在数据未填写！',
          type: 'warning'
        });
      }
      else {
        for(let i=0;i<this.tableData.length;i++){
          if(this.tableData[i].id==this.editrow.id){
            console.log(this.editrow)
            this.tableData[i].open=this.editrow.open
            this.tableData[i].date=this.editrow.date
            this.tableData[i].low=this.editrow.low
            this.tableData[i].high=this.editrow.high
            this.tableData[i].close=this.editrow.close
            this.tableData[i].volume=this.editrow.volume
            this.tableData[i].adjustedclose=this.editrow.adjustedclose
            break
          }
        }
        for(let j=0;j<this.clearDateWork.length;j++){
          if(this.clearDateWork[j].id==this.editrow.id){
            console.log(this.editrow)
            this.clearDateWork[j].open=this.editrow.open
            this.clearDateWork[j].date=this.editrow.date
            this.clearDateWork[j].low=this.editrow.low
            this.clearDateWork[j].high=this.editrow.high
            this.clearDateWork[j].close=this.editrow.close
            this.clearDateWork[j].volume=this.editrow.volume
            this.clearDateWork[j].adjustedclose=this.editrow.adjustedclose
          }
        }
        this.edithand=false
        this.editrow={}
        this.clearData()
      }
    },
    autoTonumber(index){//自动填入数据(平均数)
      let sum=0
      let indexs=0
      if(index==1){//open
        for(let i=0;i<this.tableData.length;i++){
          if((typeof this.tableData[i].open ==='number')){
            sum=sum+this.tableData[i].open
            indexs++
          }
        }
        this.editrow.open=Number((sum/indexs).toFixed(4))
      }
      else if(index==2){//high
        for(let i=0;i<this.tableData.length;i++){
          if((typeof this.tableData[i].high ==='number')){
            sum=sum+this.tableData[i].high
            indexs++
          }
        }
        this.editrow.high=Number((sum/indexs).toFixed(4))
      }
      else if(index==3){//low
        for(let i=0;i<this.tableData.length;i++){
          if((typeof this.tableData[i].low ==='number')){
            sum=sum+this.tableData[i].low
            indexs++
          }
        }
        this.editrow.low=Number((sum/indexs).toFixed(4))
      }
      else if(index==4){//close
        for(let i=0;i<this.tableData.length;i++){
          if((typeof this.tableData[i].close ==='number')){
            sum=sum+this.tableData[i].close
            indexs++
          }
        }
        this.editrow.close=Number((sum/indexs).toFixed(4))
      }
      else if(index==5){//volume
        for(let i=0;i<this.tableData.length;i++){
          if((typeof this.tableData[i].volume ==='number')){
            sum=sum+this.tableData[i].volume
            indexs++
          }
        }
        this.editrow.volume=Number((sum/indexs).toFixed(4))
      }
      else if(index==6){//adjustedclose
        for(let i=0;i<this.tableData.length;i++){
          if((typeof this.tableData[i].adjustedclose ==='number')){
            sum=sum+this.tableData[i].adjustedclose
            indexs++
          }
        }
        this.editrow.adjustedclose=Number((sum/indexs).toFixed(4))
      }
    },
    startin() {//初始化填入
      for(let i=0;i<this.keyarr.length;i++){
        for(let j=0;j<this.options.length;j++){
          if(this.options[j].truev.indexOf(this.keyarr[i].keyname)!=-1){
            this.keyarr[i].tokeyname=this.options[j].truev
            this.value[i]=this.options[j].truev
            break
          }
        }
      }
      this.dialogVisible=true
    },
    sure(){//确定导入表格
      let sum=0;
      for(let i=0;i<this.value.length;i++){
        if(this.value[i]!=''&&this.value[i]!=null){
          sum++;
        }
      }
      if(sum==7){
        let flag=0;
        for(let i=0;i<this.value.length;i++){
          for(let j=i+1;j<this.value.length;j++){
            if(this.value[i]==this.value[j]){
              flag=1;
              break
            }
          }
        }
        if(flag==0){//没有重复的
          for(let i=0;i<this.keyarr.length;i++){
            this.keyarr[i].tokeyname=this.value[i]
          }
          this.maketable()
          this.disabled=false
          this.dialogVisible=false
        }
        else {//有重复的
          console.log(this.value)
          this.$message({
            showClose: true,
            message: '错误！选择了重复表项',
            type: 'error'
          });
        }
      }
      else {
        console.log(this.value)
        this.$message({
          showClose: true,
          message: '错误！必须且只能选择7个对应的数据名称，当前选择数量为：'+sum,
          type: 'error'
        });
      }
    },
    handleClick(row){//数据编辑
      this.editrow=row
      this.edithand=true
    },
    opendelete(){//打开删除面板
      this.opendeleteVisible=true
    },
    deleteDate(row){//数据删除
      for(let i=0;i<this.tableData.length;i++){
        if(row.id==this.tableData[i].id){
          this.tableData.splice(i,1)
          break
        }
      }
      for (let j=0;j<this.clearDateWork.length;j++){
        if(row.id==this.clearDateWork[j].id){
          this.clearDateWork.splice(j,1)
          j--
        }
      }
      this.clearData()
    },
    AutoclearData(){//自动清洗数据
      this.autodeleteDate()
      this.autoinputnull()
    },
    autodeleteDate(){//自动数据删除重复
      for(let i=0;i<this.clearDateWork.length;i++){
        if(this.clearDateWork[i].type===1){//完全一致
          let flag=0
          for(let j=i+1;j<this.clearDateWork.length;j++){
            if((this.clearDateWork[i].open===this.clearDateWork[j].open)&&this.clearDateWork[j].type===1){
              flag=1
              for(let n=0;n<this.tableData.length;n++){
                if(this.tableData[n].id===this.clearDateWork[j].id){
                  this.tableData.splice(n,1)
                  break
                }
              }
              this.clearDateWork.splice(j,1);
              j--
            }
          }
          if(flag==1){
            this.clearDateWork.splice(i,1)
            i--
          }
        }
      }
    },
    autoinputnull(){//自动填空
      for(let i=0;i<this.clearDateWork.length;i++){
        if(this.clearDateWork[i].type==2&&(this.clearDateWork[i].date!=null||this.clearDateWork[i].date!='')){
          let index=-1
          for(let j=0;j<this.tableData.length;j++){
            if(this.tableData[j].id===this.clearDateWork[i].id){
              this.editrow=JSON.parse(JSON.stringify(this.tableData[j]))
              break
            }
          }

          for(let key in this.tableData[0]){
            if(index>=1&&(this.clearDateWork[i][key]==null||this.clearDateWork[i][key]=='')){
              console.log(index)
              this.autoTonumber(index)
            }
            index++
          }
          this.sureedit()
          this.editrow={}
          this.clearDateWork.splice(i,1)
          i--
        }
      }
    },
    clearData(){//数据清洗手动点开
      this.clearDateWork=[]
      this.repetition()
      console.log(this.clearDateWork)
    },
    repetition(){//重复数据
      for(let i=0;i<this.tableData.length;i++){

        for(let j=0;j<this.tableData.length;j++){
          if((this.tableData[i].date==this.tableData[j].date)&&(i!=j)&&(this.tableData[i].low==this.tableData[j].low)
              &&(this.tableData[i].high==this.tableData[j].high)&&(this.tableData[i].close==this.tableData[j].close)
              &&(this.tableData[i].open==this.tableData[j].open)&&(this.tableData[i].volume==this.tableData[j].volume)
              &&(this.tableData[i].adjustedclose==this.tableData[j].adjustedclose)
          ){//完全重复
            let val=Object.assign({},this.tableData[i])
            val.id=(i+1)
            val.msg='与原表格中第'+(j+1)+'条数据完全重复'
            val.type=1
            this.clearDateWork.push(val)
          }
          else if(this.tableData[i].date==this.tableData[j].date&&i!=j){//只有时间上的重复
            let val=Object.assign({},this.tableData[i])
            val.id=(i+1)
            val.msg='与原表格中第'+(j+1)+'条数据存在时间上的重复'
            val.type=5
            this.clearDateWork.push(val)
          }
        }
        this.DataGap(this.tableData[i])
        this.DataFormat(this.tableData[i])
        this.Datedate(this.tableData[i])
      }
      if(this.clearDateWork.length>0){
        this.handworkflag=true
      }
      else {
        this.$message({
          showClose: true,
          message: '暂无需要清洗的数据',
          type: 'success'
        })
      }
    },
    DataFormat(tableDatanow){//错误格式
      var msg1='字段：'
      let flag1=0;
      if(!(typeof tableDatanow.date ==='number')&&!(tableDatanow.date==''||tableDatanow.date==null)){
        flag1=2;
        msg1=msg1+'日期（date）;'
      }
      if(!(typeof tableDatanow.open ==='number')&&!(tableDatanow.open==''||tableDatanow.open==null)){
        flag1=1;
        msg1=msg1+'开盘价(open);'
      }
      if(!(typeof tableDatanow.high ==='number')&&!(tableDatanow.high==''||tableDatanow.high==null)){
        flag1=1;
        msg1=msg1+'最高价(high);'
      }
      if(!(typeof tableDatanow.low ==='number')&&!(tableDatanow.low==''||tableDatanow.low==null)){
        flag1=1;
        msg1=msg1+'最低价（low）;'
      }
      if(!(typeof tableDatanow.close ==='number')&&!(tableDatanow.close==''||tableDatanow.close==null)){
        flag1=1;
        msg1=msg1+'收盘价（close）;'
      }
      if(!(typeof tableDatanow.volume ==='number')&&!(tableDatanow.volume==''||tableDatanow.volume==null)){
        flag1=1;
        msg1=msg1+'成交量（volume）;'
      }
      if(!(typeof tableDatanow.adjustedclose ==='number')&&!(tableDatanow.adjustedclose==''||tableDatanow.adjustedclose==null)){
        flag1=1;
        msg1=msg1+'加权平均价(adjust);'
      }
      if(flag1!=0){
        msg1=msg1+'格式错误，应当全部为数字格式';
        let val=Object.assign({},tableDatanow)
        val.msg=msg1
        if(flag1==1){
          val.type=3
        }
        else {
          val.type=6//日期格式错误
        }
        this.clearDateWork.push(val)
      }
    },
    addDate(){//上传数据
      this.loading=true
      this.clearData()
      let tableupdate=JSON.parse(JSON.stringify(this.tableData))
      if(this.clearDateWork.length==0){//数据清洗完毕
        for(let i=0;i<tableupdate.length;i++){
          tableupdate[i].id=null
          tableupdate[i].marketid=this.mid
        }
        request.post("/market/adddate",tableupdate).then(res => {
          if(res.code==='200'){
            if(res.data!=null){//有重复数据
              this.ctableData=res.data
              this.ctableDataflag=true
            }
            else {
              this.$message({
                showClose: true,
                message: '数据上传成功',
                type: 'success'
              })
            }
          }
          else {
            this.$message({
              showClose: true,
              message: res.msg,
              type: 'error'
            })
          }
          this.loading=false
        })
      }
      else {
        this.$message({
          showClose: true,
          message: '存在未清洗的数据！请进行清洗',
          type: 'warning'
        });
      }
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },
    updatemarketdate(){//覆盖数据
      this.loading2=true
      if(this.multipleSelection.length==0){
        this.$message({
          showClose: true,
          message: '未选择任何数据！请选择数据或者退出',
          type: 'warning'
        })
        this.loading2=false
      }
      else {
        request.post("/market/updatedate",this.ctableData).then(res => {
          if(res.code==='200'){
              this.$message({
                showClose: true,
                message: '数据更新成功',
                type: 'success'
              })
            this.ctableDataflag=false
          }
          else{
            this.$message({
              showClose: true,
              message: res.msg,
              type: 'error'
            })
          }
          this.loading2=false
        })
      }
    },
    getmarketname(){//获取现有的市场数据
      request.post("/MarketName/getMarketNames",{
      }).then(res => {
        if(res.code==='200'){
          this.marketname=res.data
          this.valuemarket=res.data[0].marketname
          this.mid=res.data[0].id
        }
      })
    },
    changemarket(val){//变换市场
      this.mid=val
    },
    Datedate(tableDatanow){//日期格式
      let msg4='';
      let flag=0;
      if((typeof tableDatanow.date ==='number')){
        if(parseInt(tableDatanow.date,10)!=tableDatanow.date){//不是整数
          flag=1
          msg4='日期格式不是整数'
        }
        else {
          let num=0
          let l=tableDatanow.date
          while (l>=1){
            l=l/10
            num++
          }
          if(num!=8){
            flag=1
            msg4='日期格式必须是8位数的整数'
          }
          else {//是八位数，开始拆分数据
            let date=tableDatanow.date
            let year=Number((date/10000).toFixed(0))
            let month=Number(((date-year*10000)/100).toFixed(0))
            let day=(date-year*10000-month*100)
            //开始判断年月日是否正确
            msg4=this.panduanyear(year,month,day)
            if(msg4!=''){
              flag=1
            }
          }
        }
      }
      if(flag==1){
        let val4=Object.assign({},tableDatanow)
        val4.msg=msg4
        val4.type=4
        this.clearDateWork.push(val4)
      }
    },
    panduanyear(year,month,day){//判断年月日是否正确
      let msg=''
      if(day<=0){
        msg='日期中的天数必须大于0'
        return msg
      }
      if(month==1||month==3||month==3||month==5||month==7||month==8||month==10||month==12){//大月
        if(day>31){
          msg=month+'月为大月，天数必须小于或等于31,当前天数为：'+day
          return msg
        }
      }
      if (month==4||month==6||month==9||month==11){//小月
        if(day>30){
          msg=month+'月为小月，天数必须小于或等于30,当前天数为：'+day
          return msg
        }
      }
      if (month==2){//小月
        if(year%4==0&&day>29){//闰年
          msg=year+'为闰年，2月天数必须小于或等于29，当前天数为：'+day
          return msg
        }
        else if(year%4!=0&&day>28) {
          msg=year+'不是闰年，2月天数必须小于或等于28，当前天数为：'+day
          return msg
        }
      }
      return msg
    },
    DataGap(tableDatanow){//数据空缺
      let msg='字段：'
      let flag=0;
      if(tableDatanow.date==''||tableDatanow.date==null){
        flag=1;
        msg=msg+'日期（date）;'
      }
      if(tableDatanow.open==''||tableDatanow.open==null){
        flag=1;
        msg=msg+'开盘价(open);'
      }
      if(tableDatanow.high==''||tableDatanow.high==null){
        flag=1;
        msg=msg+'最高价(high);'
      }
      if(tableDatanow.low==''||tableDatanow.low==null){
        flag=1;
        msg=msg+'最低价（low）;'
      }
      if(tableDatanow.close==''||tableDatanow.close==null){
        flag=1;
        msg=msg+'收盘价（close）;'
      }
      if(tableDatanow.volume==''||tableDatanow.volume==null){
        flag=1;
        msg=msg+'成交量（volume）;'
      }
      if(tableDatanow.adjustedclose==''||tableDatanow.adjustedclose==null){
        flag=1;
        msg=msg+'加权平均价(adjust);'
      }
      if(flag==1){
        msg=msg+'为空';
        let val2=Object.assign({},tableDatanow)
        val2.msg=msg
        val2.type=2
        this.clearDateWork.push(val2)
      }
    },
    maketable(){//制作表格
      for(let i=0;i<this.updateDate.length;i++){
        let tabledate={id:i+1,date:'',open:'',high:'',low:'',close:'',volume:'',adjustedclose:''}
        for(var key in this.updateDate[i]){
          for(let j=0;j<this.keyarr.length;j++){
            if(key==this.keyarr[j].keyname&&this.keyarr[j].tokeyname=='open'){
              tabledate.open=this.updateDate[i][key]
            }
            else if(key==this.keyarr[j].keyname&&this.keyarr[j].tokeyname=='date'){
              tabledate.date=this.updateDate[i][key]
            }
            else if(key==this.keyarr[j].keyname&&this.keyarr[j].tokeyname=='high'){
              tabledate.high=this.updateDate[i][key]
            }
            else if(key==this.keyarr[j].keyname&&this.keyarr[j].tokeyname=='low'){
              tabledate.low=this.updateDate[i][key]
            }
            else if(key==this.keyarr[j].keyname&&this.keyarr[j].tokeyname=='close'){
              tabledate.close=this.updateDate[i][key]
            }
            else if(key==this.keyarr[j].keyname&&this.keyarr[j].tokeyname=='volume'){
              tabledate.volume=this.updateDate[i][key]
            }
            else if(key==this.keyarr[j].keyname&&this.keyarr[j].tokeyname=='adjustedclose'){
              tabledate.adjustedclose=this.updateDate[i][key]
            }
          }
        }
        this.tableData.push(tabledate)
      }
    }
  }
}
</script>

<style>

</style>