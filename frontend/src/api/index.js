import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

api.interceptors.response.use(
  response => response.data,
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

export const productApi = {
  list: () => api.get('/products'),
  get: (id) => api.get(`/products/${id}`),
  create: (data) => api.post('/products', data),
  update: (id, data) => api.put(`/products/${id}`, data),
  delete: (id) => api.delete(`/products/${id}`)
}

export const seckillApi = {
  buy: (data) => api.post('/seckill/buy', data),
  warmup: (productId) => api.post(`/seckill/warmup/${productId}`)
}

export const orderApi = {
  listByUser: (userId) => api.get(`/orders/user/${userId}`),
  get: (id) => api.get(`/orders/${id}`),
  pay: (orderId, userId) => api.post(`/orders/${orderId}/pay`, null, { params: { userId } }),
  confirm: (orderId, userId) => api.post(`/orders/${orderId}/confirm`, null, { params: { userId } }),
  cancel: (orderId, userId) => api.post(`/orders/${orderId}/cancel`, null, { params: { userId } })
}
