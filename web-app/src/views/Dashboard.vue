<script setup>
import { ref } from 'vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import Charts from '@/views/components/Charts.vue'
import { formatCurrency } from '@/utils/formatters'

const totalBalance = ref(12500000)
const totalIncome = ref(8500000)
const totalExpense = ref(4200000)
const totalSavings = ref(4300000)

const recentTransactions = [
  {
    id: 1,
    type: 'income',
    amount: 8500000,
    category: 'Lương',
    date: '2024-03-15',
    icon: ['fas', 'arrow-up']
  },
  {
    id: 2,
    type: 'expense',
    amount: 50000,
    category: 'Ăn uống',
    date: '2024-03-14',
    icon: ['fas', 'utensils']
  },
  {
    id: 3,
    type: 'expense',
    amount: 350000,
    category: 'Mua sắm',
    date: '2024-03-13',
    icon: ['fas', 'shopping-bag']
  }
]
</script>

<template>
  <div class="h-full">
    <div class="space-y-6">
      <!-- Stats Cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <!-- Total Balance Card -->
        <div class="bg-surface rounded-xl p-4 shadow-sm">
          <div class="flex items-center justify-between">
            <div class="flex-1">
              <p class="text-text-secondary text-sm">Tổng số dư</p>
              <p class="text-2xl font-semibold text-text mt-1">{{ formatCurrency(totalBalance) }}</p>
            </div>
            <div class="bg-primary/10 p-3 rounded-lg flex-shrink-0">
              <font-awesome-icon :icon="['fas', 'wallet']" class="text-primary text-xl" />
            </div>
          </div>
        </div>

        <!-- Income Card -->
        <div class="bg-surface rounded-xl p-4 shadow-sm">
          <div class="flex items-center justify-between">
            <div class="flex-1">
              <p class="text-text-secondary text-sm">Thu nhập tháng này</p>
              <p class="text-2xl font-semibold text-success mt-1">{{ formatCurrency(totalIncome) }}</p>
            </div>
            <div class="bg-success/10 p-3 rounded-lg flex-shrink-0">
              <font-awesome-icon :icon="['fas', 'arrow-up']" class="text-success text-xl" />
            </div>
          </div>
        </div>

        <!-- Expense Card -->
        <div class="bg-surface rounded-xl p-4 shadow-sm">
          <div class="flex items-center justify-between">
            <div class="flex-1">
              <p class="text-text-secondary text-sm">Chi tiêu tháng này</p>
              <p class="text-2xl font-semibold text-danger mt-1">{{ formatCurrency(totalExpense) }}</p>
            </div>
            <div class="bg-danger/10 p-3 rounded-lg flex-shrink-0">
              <font-awesome-icon :icon="['fas', 'arrow-down']" class="text-danger text-xl" />
            </div>
          </div>
        </div>

        <!-- Savings Card -->
        <div class="bg-surface rounded-xl p-4 shadow-sm">
          <div class="flex items-center justify-between">
            <div class="flex-1">
              <p class="text-text-secondary text-sm">Tiết kiệm tháng này</p>
              <p class="text-2xl font-semibold text-primary mt-1">{{ formatCurrency(totalSavings) }}</p>
            </div>
            <div class="bg-primary/10 p-3 rounded-lg flex-shrink-0">
              <font-awesome-icon :icon="['fas', 'piggy-bank']" class="text-primary text-xl" />
            </div>
          </div>
        </div>
      </div>

      <!-- Charts Section -->
      <Charts />

      <!-- Recent Transactions -->
      <div>
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-lg font-semibold text-text">Giao dịch gần đây</h3>
          <router-link to="/history" class="text-primary hover:text-primary/80 text-sm">
            Xem tất cả
          </router-link>
        </div>
        <div class="bg-surface rounded-xl shadow-sm">
          <div class="divide-y divide-gray-100">
            <!-- Transaction Item -->
            <div class="p-4 flex items-center justify-between">
              <div class="flex items-center space-x-3">
                <div class="bg-success/10 p-3 rounded-lg flex-shrink-0">
                  <font-awesome-icon :icon="['fas', 'arrow-up']" class="text-success" />
                </div>
                <div class="flex-1">
                  <p class="font-medium text-text">Lương tháng 4</p>
                  <p class="text-sm text-text-secondary">Hôm nay, 10:30</p>
                </div>
              </div>
              <p class="text-success font-medium flex-shrink-0">+{{ formatCurrency(8500000) }}</p>
            </div>

            <!-- Transaction Item -->
            <div class="p-4 flex items-center justify-between">
              <div class="flex items-center space-x-3">
                <div class="bg-danger/10 p-3 rounded-lg flex-shrink-0">
                  <font-awesome-icon :icon="['fas', 'utensils']" class="text-danger" />
                </div>
                <div class="flex-1">
                  <p class="font-medium text-text">Ăn trưa</p>
                  <p class="text-sm text-text-secondary">Hôm nay, 12:15</p>
                </div>
              </div>
              <p class="text-danger font-medium flex-shrink-0">-{{ formatCurrency(50000) }}</p>
            </div>

            <!-- Transaction Item -->
            <div class="p-4 flex items-center justify-between">
              <div class="flex items-center space-x-3">
                <div class="bg-danger/10 p-3 rounded-lg flex-shrink-0">
                  <font-awesome-icon :icon="['fas', 'shopping-bag']" class="text-danger" />
                </div>
                <div class="flex-1">
                  <p class="font-medium text-text">Siêu thị</p>
                  <p class="text-sm text-text-secondary">Hôm qua, 18:30</p>
                </div>
              </div>
              <p class="text-danger font-medium flex-shrink-0">-{{ formatCurrency(350000) }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.bg-white {
  background-color: #ffffff;
}
</style>