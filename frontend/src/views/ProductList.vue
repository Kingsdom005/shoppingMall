<template>
  <div class="product-list">
    <div class="list-header">
      <h2>商品列表</h2>
      <el-button type="primary" @click="showCreateModal = true">添加商品</el-button>
    </div>

    <el-table :data="products" border style="width: 100%;">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="商品名称" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="scope">¥{{ scope.row.price }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="seckillPrice" label="秒杀价" width="100">
        <template #default="scope">
          <span v-if="scope.row.seckillPrice">¥{{ scope.row.seckillPrice }}</span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="seckillStatus" label="秒杀状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.seckillStatus === 1 ? 'success' : 'info'">
            {{ scope.row.seckillStatus === 1 ? '进行中' : '未开启' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="small" @click="viewProduct(scope.row.id)">查看</el-button>
          <el-button size="small" type="primary" @click="editProduct(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteProduct(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="editForm.id ? '编辑商品' : '添加商品'" v-model="showCreateModal">
      <el-form :model="editForm" label-width="120px">
        <el-form-item label="商品名称">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="商品描述">
          <el-input v-model="editForm.description" type="textarea" />
        </el-form-item>
        <el-form-item label="商品价格">
          <el-input v-model="editForm.price" type="number" step="0.01" />
        </el-form-item>
        <el-form-item label="库存数量">
          <el-input v-model="editForm.stock" type="number" />
        </el-form-item>
        <el-form-item label="秒杀价格">
          <el-input v-model="editForm.seckillPrice" type="number" step="0.01" />
        </el-form-item>
        <el-form-item label="秒杀库存">
          <el-input v-model="editForm.seckillStock" type="number" />
        </el-form-item>
        <el-form-item label="秒杀开始时间">
          <el-date-picker v-model="editForm.seckillStartTime" type="datetime" />
        </el-form-item>
        <el-form-item label="秒杀结束时间">
          <el-date-picker v-model="editForm.seckillEndTime" type="datetime" />
        </el-form-item>
        <el-form-item label="秒杀状态">
          <el-switch v-model="editForm.seckillStatus" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateModal = false">取消</el-button>
        <el-button type="primary" @click="saveProduct">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>import { ref, onMounted, reactive } from 'vue';
import { productApi } from '../api';
const products = ref([]);
const showCreateModal = ref(false);
const editForm = reactive({
 id: null,
 name: '',
 description: '',
 price: '',
 stock: 0,
 seckillPrice: '',
 seckillStock: 0,
 seckillStartTime: null,
 seckillEndTime: null,
 seckillStatus: 0
});
const loadProducts = async () => {
 try {
 products.value = await productApi.list();
 }
 catch (error) {
 console.error('加载商品失败:', error);
 }
};
onMounted(loadProducts);
const viewProduct = (id) => {
 window.location.href = `#/product/${id}`;
};
const editProduct = (product) => {
 Object.assign(editForm, product);
 showCreateModal.value = true;
};
const saveProduct = async () => {
 try {
 if (editForm.id) {
 await productApi.update(editForm.id, editForm);
 }
 else {
 await productApi.create(editForm);
 }
 showCreateModal.value = false;
 loadProducts();
 }
 catch (error) {
 console.error('保存商品失败:', error);
 }
};
const deleteProduct = async (id) => {
 if (confirm('确定删除该商品吗？')) {
 try {
 await productApi.delete(id);
 loadProducts();
 }
 catch (error) {
 console.error('删除商品失败:', error);
 }
 }
};
</script>

<style scoped>
.product-list {
  padding: 20px;
  background: white;
  border-radius: 12px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.list-header h2 {
  font-size: 20px;
}
</style>