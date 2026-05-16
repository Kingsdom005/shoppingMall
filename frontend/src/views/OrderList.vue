<template>
  <div class="order-list">
    <div class="list-header">
      <h2>我的订单</h2>
      <span class="user-id">当前用户ID: {{ currentUserId }}</span>
    </div>

    <div class="order-tabs">
      <el-button 
        v-for="tab in tabs" 
        :key="tab.value"
        :type="activeTab === tab.value ? 'primary' : 'default'"
        @click="activeTab = tab.value"
      >
        {{ tab.label }}
      </el-button>
    </div>

    <div class="order-cards">
      <div 
        v-for="order in filteredOrders" 
        :key="order.id" 
        class="order-card"
      >
        <div class="order-header">
          <span class="order-no">订单号: {{ order.orderNo }}</span>
          <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
        </div>
        <div class="order-body">
          <div class="order-product">
            <span class="product-icon">📦</span>
            <div class="product-info">
              <p class="product-name">商品ID: {{ order.productId }}</p>
              <p class="product-quantity">数量: {{ order.quantity }}</p>
            </div>
          </div>
          <div class="order-price">
            <div class="price-label">
              <span>原价: ¥{{ order.price }}</span>
              <span v-if="order.seckillPrice" class="seckill-tag">秒杀订单</span>
            </div>
            <div class="price-value">
              实付: <strong>¥{{ order.seckillPrice || order.price }}</strong>
            </div>
          </div>
        </div>
        <div class="order-footer">
          <span class="order-time">{{ order.createdAt }}</span>
          <div class="order-actions">
            <el-button 
              v-if="order.status === 0" 
              type="primary" 
              @click="handlePay(order.id)"
            >
              去支付
            </el-button>
            <el-button 
              v-if="order.status === 0" 
              type="danger" 
              @click="handleCancel(order.id)"
            >
              取消订单
            </el-button>
            <el-button 
              v-if="order.status === 2" 
              type="success" 
              @click="handleConfirm(order.id)"
            >
              确认收货
            </el-button>
            <span v-if="order.status === 1" class="status-text">等待发货</span>
            <span v-if="order.status === 3" class="status-text">交易完成</span>
            <span v-if="order.status === 4" class="status-text">订单已取消</span>
          </div>
        </div>
      </div>

      <div v-if="filteredOrders.length === 0" class="empty-state">
        <span class="empty-icon">📭</span>
        <p>暂无订单</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, inject, onMounted, watch } from 'vue'
import { orderApi } from '../api'

const currentUserId = inject('currentUserId')
const orders = ref([])
const activeTab = ref('all')

const tabs = [
  { label: '全部', value: 'all' },
  { label: '待支付', value: 'pending' },
  { label: '已支付', value: 'paid' },
  { label: '已发货', value: 'shipped' },
  { label: '已完成', value: 'completed' },
  { label: '已取消', value: 'cancelled' }
]

const filteredOrders = computed(() => {
  if (activeTab.value === 'all') {
    return orders.value
  }
  return orders.value.filter(o => {
    switch (activeTab.value) {
      case 'pending': return o.status === 0
      case 'paid': return o.status === 1
      case 'shipped': return o.status === 2
      case 'completed': return o.status === 3
      case 'cancelled': return o.status === 4
      default: return true
    }
  })
})

const getStatusType = (status) => {
  switch (status) {
    case 0: return 'warning'
    case 1: return 'primary'
    case 2: return 'info'
    case 3: return 'success'
    case 4: return 'danger'
    default: return 'info'
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 0: return '待支付'
    case 1: return '已支付'
    case 2: return '已发货'
    case 3: return '已完成'
    case 4: return '已取消'
    default: return '未知'
  }
}

const loadOrders = async () => {
  try {
    orders.value = await orderApi.listByUser(currentUserId.value)
  } catch (error) {
    console.error('加载订单失败:', error)
  }
}

const handlePay = async (orderId) => {
  try {
    const response = await orderApi.pay(orderId, currentUserId.value)
    if (response.success) {
      alert('支付成功！')
      loadOrders()
    } else {
      alert('支付失败：' + response.message)
    }
  } catch (error) {
    console.error('支付失败:', error)
    alert('支付失败，请稍后重试')
  }
}

const handleConfirm = async (orderId) => {
  try {
    const response = await orderApi.confirm(orderId, currentUserId.value)
    if (response.success) {
      alert('确认收货成功！')
      loadOrders()
    } else {
      alert('确认收货失败：' + response.message)
    }
  } catch (error) {
    console.error('确认收货失败:', error)
    alert('确认收货失败，请稍后重试')
  }
}

const handleCancel = async (orderId) => {
  if (!confirm('确定要取消该订单吗？')) {
    return
  }
  try {
    const response = await orderApi.cancel(orderId, currentUserId.value)
    if (response.success) {
      alert('取消订单成功！')
      loadOrders()
    } else {
      alert('取消订单失败：' + response.message)
    }
  } catch (error) {
    console.error('取消订单失败:', error)
    alert('取消订单失败，请稍后重试')
  }
}

onMounted(loadOrders)

watch(currentUserId, loadOrders)
</script>

<style scoped>
.order-list {
  padding: 20px;
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

.user-id {
  font-size: 14px;
  color: #666;
}

.order-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.order-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 20px;
}

.order-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.order-no {
  font-size: 14px;
  color: #666;
}

.order-body {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}

.order-product {
  display: flex;
  gap: 12px;
}

.product-icon {
  font-size: 40px;
}

.product-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-name {
  font-size: 14px;
  color: #333;
}

.product-quantity {
  font-size: 12px;
  color: #999;
}

.order-price {
  text-align: right;
}

.price-label {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.seckill-tag {
  background: #ffebee;
  color: #c62828;
  padding: 20px;
  border-radius: 4px;
  font-size: 10px;
}

.price-value {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.order-footer {
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-time {
  font-size: 12px;
  color: #999;
}

.order-actions {
  display: flex;
  gap: 10px;
}

.status-text {
  font-size: 14px;
  color: #666;
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
