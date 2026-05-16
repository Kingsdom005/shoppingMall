<template>
  <div class="seckill">
    <div class="seckill-header">
      <h2>🔥 限时秒杀</h2>
      <div class="countdown" v-if="currentSeckill">
        <span class="countdown-label">距离结束:</span>
        <span class="countdown-time">{{ countdown }}</span>
      </div>
    </div>

    <div class="seckill-products">
      <div 
        v-for="product in seckillProducts" 
        :key="product.id" 
        class="seckill-card"
      >
        <div class="seckill-badge">秒杀</div>
        <div class="seckill-image">
          <span class="seckill-icon">🎁</span>
        </div>
        <div class="seckill-info">
          <h3>{{ product.name }}</h3>
          <p class="seckill-desc">{{ product.description }}</p>
          <div class="seckill-price-row">
            <span class="original">¥{{ product.price }}</span>
            <span class="seckill">¥{{ product.seckillPrice }}</span>
            <span class="discount">省¥{{ (product.price - product.seckillPrice).toFixed(2) }}</span>
          </div>
          <div class="seckill-stock">
            <span>剩余库存: <strong>{{ product.seckillStock }}</strong></span>
            <span class="progress-bar">
              <span 
                class="progress-fill" 
                :style="{width: (product.seckillStock / product.stock * 100) + '%'}"
              ></span>
            </span>
          </div>
          <div class="seckill-actions">
            <el-input 
              v-model="quantityMap[product.id]" 
              type="number" 
              min="1" 
              :max="Math.min(5, product.seckillStock)"
              size="small"
              style="width: 80px;"
            />
            <el-button 
              type="danger" 
              size="large" 
              @click="buyProduct(product)"
              :disabled="product.seckillStock <= 0"
            >
              {{ product.seckillStock > 0 ? '立即抢购' : '已抢光' }}
            </el-button>
          </div>
        </div>
      </div>

      <div v-if="seckillProducts.length === 0" class="empty-state">
        <span class="empty-icon">😢</span>
        <p>暂无秒杀商品</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, inject, reactive, computed } from 'vue'
import { productApi, seckillApi } from '../api'

const currentUserId = inject('currentUserId')
const seckillProducts = ref([])
const quantityMap = reactive({})
let countdownTimer = null

const currentSeckill = computed(() => {
  return seckillProducts.value.find(p => p.seckillStatus === 1)
})

const countdown = ref('')

const updateCountdown = () => {
  if (!currentSeckill.value || !currentSeckill.value.seckillEndTime) {
    countdown.value = '已结束'
    return
  }
  
  const endTime = new Date(currentSeckill.value.seckillEndTime.replace('T', ' '))
  const now = new Date()
  const diff = endTime - now
  
  if (diff <= 0) {
    countdown.value = '已结束'
    return
  }
  
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  const seconds = Math.floor((diff % (1000 * 60)) / 1000)
  
  countdown.value = `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
}

const loadSeckillProducts = async () => {
  try {
    const products = await productApi.list()
    seckillProducts.value = products.filter(p => p.seckillStatus === 1)
    seckillProducts.value.forEach(p => {
      if (!quantityMap[p.id]) {
        quantityMap[p.id] = 1
      }
    })
    updateCountdown()
  } catch (error) {
    console.error('加载秒杀商品失败:', error)
  }
}

const buyProduct = async (product) => {
  const quantity = quantityMap[product.id] || 1
  
  if (quantity > product.seckillStock) {
    alert('购买数量超过剩余库存')
    return
  }

  try {
    const result = await seckillApi.buy({
      productId: product.id,
      userId: currentUserId.value,
      quantity: quantity
    })
    alert(result)
    loadSeckillProducts()
  } catch (error) {
    console.error('秒杀失败:', error)
    alert('秒杀失败，请重试')
  }
}

onMounted(() => {
  loadSeckillProducts()
  countdownTimer = setInterval(updateCountdown, 1000)
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<style scoped>
.seckill {
  padding: 20px;
}

.seckill-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.seckill-header h2 {
  font-size: 24px;
  color: #ff6b6b;
}

.countdown {
  display: flex;
  align-items: center;
  gap: 10px;
}

.countdown-label {
  color: #666;
}

.countdown-time {
  font-size: 20px;
  font-weight: bold;
  color: #ff6b6b;
  font-family: monospace;
}

.seckill-products {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.seckill-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
}

.seckill-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  z-index: 1;
}

.seckill-image {
  background: linear-gradient(135deg, #fff5f5 0%, #ffe0e0 100%);
  height: 180px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.seckill-icon {
  font-size: 60px;
}

.seckill-info {
  padding: 20px;
}

.seckill-info h3 {
  font-size: 18px;
  margin-bottom: 8px;
  color: #333;
}

.seckill-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.seckill-price-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 12px;
}

.original {
  font-size: 16px;
  color: #999;
  text-decoration: line-through;
}

.seckill {
  font-size: 28px;
  color: #ff6b6b;
  font-weight: bold;
}

.discount {
  font-size: 12px;
  color: #27ae60;
  background: #e8f5e9;
  padding: 2px 8px;
  border-radius: 10px;
}

.seckill-stock {
  margin-bottom: 16px;
}

.seckill-stock span:first-child {
  font-size: 14px;
  color: #666;
}

.seckill-stock strong {
  color: #ff6b6b;
}

.progress-bar {
  display: block;
  height: 6px;
  background: #f0f0f0;
  border-radius: 3px;
  margin-top: 8px;
  overflow: hidden;
}

.progress-fill {
  display: block;
  height: 100%;
  background: linear-gradient(90deg, #ff6b6b 0%, #ee5a24 100%);
  border-radius: 3px;
  transition: width 0.3s;
}

.seckill-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.empty-state {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px;
  background: white;
  border-radius: 12px;
}

.empty-icon {
  font-size: 60px;
  display: block;
  margin-bottom: 16px;
}

.empty-state p {
  color: #999;
  font-size: 16px;
}
</style>