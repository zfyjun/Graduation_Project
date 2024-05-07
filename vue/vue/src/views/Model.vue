<template>
  <div style="padding: 2%" >

    <el-form ref="form"  label-width="100px">
      <el-form-item label="选择模型：">
        <el-select @change="changeModel" v-model="model" filterable  placeholder="请选择">
          <el-option
              v-for="item in Tmodel"
              :key="item.value"
              :label="item.value"
              :value="item.value">
          </el-option>
        </el-select>
      </el-form-item>

    </el-form>

    <el-card class="box-card">
      <div style="display: flex;justify-content: space-between">
        <span>市场加权平均值数据折线图</span>
        <el-button  size="medium" @click="opendig()" type="text">详情</el-button>
      </div>
      <div id="market" style="width: 100%;height: 400px" ></div>
    </el-card>


  </div>
</template>

<script>


export default {
  name: "Model",
  data(){
    return{
      Tmodel:[{value:'随机森林'},{value: '决策树'}],
      model:'',
    }
  },
  created() {
    // 从后台获取数据
    this.getmarketname()

  },
  mounted(){//图形绘制
    this.getmarketDate()
  },
  methods: {
    changeModel(){
      // this.
    }
  },


}
</script>

<style scoped>

</style>