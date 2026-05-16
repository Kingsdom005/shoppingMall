<template>
  <div class="product-detail" v-if="product">
    <el-button @click="goBack" class="back-btn">← 返回列表</el-button>
    
    <div class="detail-content">
      <div class="product-image">
        <span class="image-icon">📦</span>
      </div>
      
      <div class="product-info">
        <h1>{{ product.name }}</h1>
        <p class="product-description">{{ product.description }}</p>
        
        <div class="price-section">
          <span class="current-price">¥{{ product.price }}</span>
          <span v-if="product.seckillPrice" class="seckill-badge">
            秒杀价 ¥{{ product.seckillPrice }}
          </span>
        </div>
        
        <div class="stock-section">
          <span>库存: <strong>{{ product.stock }}</strong></span>
          <span v-if="product.seckillStock">
            秒杀库存: <strong>{{ product.seckillStock }}</strong>
          </span>
        </div>
        
        <div v-if="product.seckillStatus === 1" class="seckill-section">
          <div class="seckill-time">
            <span>秒杀时间:</span>
            <span>{{ formatDateTime(product.seckillStartTime) }} - {{ formatDateTime(product.seckillEndTime) }}</span>
          </div>
          <div class="seckill-action">
            <el-input 
              v-model="quantity" 
              type="number" 
              min="1" 
              :max="Math.min(5, product.seckillStock)"
              style="width: 100px;"
            />
            <el-button 
              type="danger" 
              size="large"
              @click="buySeckill"
              :disabled="product.seckillStock <= 0"
            >
              立即抢购
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>

  <div v-else class="loading">
    <el-loading text="加载中..." />
  </div>
</template>

<script setup>
import { ref, onMounted, inject } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productApi, seckillApi } from '../api'

const route = useRoute()
const router = useRouter()
const currentUserId = inject('currentUserId')
const product = ref(null)
const quantity = ref(1)

const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return dateTime.replace('T', ' ')
}

const loadProduct = async () => {
  try {
    product.value = await productApi.get(route.params.id)
  } catch (error) {
    console.error('加载商品详情失败:', error)
  }
}

const buySeckill = async () => {
  if (!product.value) return
  
  try {
    const result = await seckillApi.buy({
      productId: product.value.id,
      userId: currentUserId.value,
      quantity: quantity.value
    })
    alert(result)
    loadProduct()
  } catch (error) {
    console.error('秒杀失败:', error)
    alert('秒杀失败，请重试')
  }
}

const goBack = () => {
  router.push('/products')
}

onMounted(loadProduct)
</script>

<style scoped>
.product-detail {
  padding: 20px;
}

.back-btn {
  margin-bottom: 20px;
}

.detail-content {
  display: flex;
  gap: 40px;
  background: white;
  border-radius: 12px;
  padding: 30px;
}

.product-image {
  flex-shrink: 0;
  width: 300px;
  height: 300px;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-icon {
  font-size: 100px;
}

.product-info {
  flex: 1;
}

.product-info h1 {
  font-size: 28px;
  margin-bottom: 16px;
  color: #333;
}

.product-description {
  font-size: 16px;
  color: #666;
  margin-bottom: 24px;
  line-height: 1.6;
}

.price-section {
  display: flex;
  align-items: baseline;
  gap: 16px;
  margin-bottom: 20px;
}

.current-price {
  font-size: 36px;
  font-weight: bold;
  color: #e74c3c;
}

.seckill-badge {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  color: white;
  padding: 8px 16px;
  border-radius: 8px;
  font-weight: bold;
}

.stock-section {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
  font-size: 16px;
  color: #666;
}

.stock-section strong {
  color: #333;
}

.seckill-section {
  background: linear-gradient(135deg, #fff5f5 0%, #ffe0e0 100%);
  padding: 20px;
  border-radius: 8px;
}

.seckill-time {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #666;
}

.seckill-action {
  display: flex;
  align-items: center;
  gap: 16px;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
}
</style>