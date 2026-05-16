<template>
  <div class="home">
    <div class="hero">
      <h2>欢迎来到购物商城</h2>
      <p>限时秒杀，超值优惠等你来抢！</p>
      <router-link to="/seckill" class="hero-btn">立即抢购</router-link>
    </div>
    
    <div class="stats">
      <div class="stat-item">
        <div class="stat-icon">🎁</div>
        <div class="stat-info">
          <div class="stat-value">{{ productCount }}</div>
          <div class="stat-label">商品数量</div>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon">🔥</div>
        <div class="stat-info">
          <div class="stat-value">{{ seckillCount }}</div>
          <div class="stat-label">秒杀商品</div>
        </div>
      </div>
      <div class="stat-item">
        <div class="stat-icon">💳</div>
        <div class="stat-info">
          <div class="stat-value">{{ orderCount }}</div>
          <div class="stat-label">订单总数</div>
        </div>
      </div>
    </div>

    <div class="section">
      <h3>热门商品</h3>
      <div class="product-grid">
        <div 
          v-for="product in hotProducts" 
          :key="product.id" 
          class="product-card"
          @click="$router.push(`/product/${product.id}`)"
        >
          <div class="product-image">
            <span class="product-icon">📦</span>
          </div>
          <div class="product-info">
            <h4>{{ product.name }}</h4>
            <p class="product-desc">{{ product.description }}</p>
            <div class="product-price">
              <span class="original-price">¥{{ product.price }}</span>
              <span v-if="product.seckillPrice" class="seckill-price">
                秒杀价 ¥{{ product.seckillPrice }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, inject } from 'vue'
import { productApi, orderApi } from '../api'

const currentUserId = inject('currentUserId')
const productCount = ref(0)
const seckillCount = ref(0)
const orderCount = ref(0)
const hotProducts = ref([])

onMounted(async () => {
  try {
    const products = await productApi.list()
    productCount.value = products.length
    seckillCount.value = products.filter(p => p.seckillStatus === 1).length
    hotProducts.value = products.slice(0, 4)
  } catch (error) {
    console.error('加载商品失败:', error)
  }
  
  try {
    const orders = await orderApi.listByUser(currentUserId.value)
    orderCount.value = orders.length
  } catch (error) {
    console.error('加载订单失败:', error)
  }
})
</script>

<style scoped>
.home {
  padding: 20px 0;
}

.hero {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 60px 40px;
  border-radius: 16px;
  text-align: center;
  margin-bottom: 30px;
}

.hero h2 {
  font-size: 32px;
  margin-bottom: 16px;
}

.hero p {
  font-size: 18px;
  margin-bottom: 24px;
  opacity: 0.9;
}

.hero-btn {
  display: inline-block;
  background-color: #ff6b6b;
  color: white;
  padding: 12px 32px;
  border-radius: 30px;
  text-decoration: none;
  font-weight: bold;
  transition: transform 0.3s, box-shadow 0.3s;
}

.hero-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 20px rgba(255, 107, 107, 0.4);
}

.stats {
  display: flex;
  gap: 20px;
  margin-bottom: 30px;
}

.stat-item {
  flex: 1;
  background: white;
  padding: 24px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.stat-icon {
  font-size: 40px;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-label {
  font-size: 14px;
  color: #999;
}

.section {
  margin-top: 30px;
}

.section h3 {
  font-size: 20px;
  margin-bottom: 20px;
  color: #333;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.product-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.product-image {
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
  height: 150px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-icon {
  font-size: 50px;
}

.product-info {
  padding: 16px;
}

.product-info h4 {
  font-size: 16px;
  margin-bottom: 8px;
  color: #333;
}

.product-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 12px;
}

.original-price {
  font-size: 18px;
  color: #999;
  text-decoration: line-through;
}

.seckill-price {
  font-size: 20px;
  color: #ff6b6b;
  font-weight: bold;
}
</style>