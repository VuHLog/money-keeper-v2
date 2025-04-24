<script setup>
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { formatCurrency } from '@/utils/formatters'

defineProps({
  totalBalance: {
    type: Number,
    required: true
  },
  totalIncome: {
    type: Number,
    required: true
  },
  totalExpense: {
    type: Number,
    required: true
  },
  totalSavings: {
    type: Number,
    required: true
  }
})
</script>

<template>
  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4">
    <!-- Total Balance Card -->
    <div class="bg-gray-50 rounded-xl p-4 shadow-sm">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-text-secondary text-sm">Tổng số dư</p>
          <p class="text-2xl font-semibold text-text mt-1">{{ formatCurrency(totalBalance) }}</p>
        </div>
        <div class="bg-primary/10 p-3 rounded-lg">
          <font-awesome-icon :icon="['fas', 'wallet']" class="text-primary text-xl" />
        </div>
      </div>
    </div>

    <!-- Income Card -->
    <div class="bg-gray-50 rounded-xl p-4 shadow-sm">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-text-secondary text-sm">Thu nhập tháng này</p>
          <p class="text-2xl font-semibold text-success mt-1">{{ formatCurrency(totalIncome) }}</p>
        </div>
        <div class="bg-success/10 p-3 rounded-lg">
          <font-awesome-icon :icon="['fas', 'arrow-up']" class="text-success text-xl" />
        </div>
      </div>
    </div>

    <!-- Expense Card -->
    <div class="bg-gray-50 rounded-xl p-4 shadow-sm">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-text-secondary text-sm">Chi tiêu tháng này</p>
          <p class="text-2xl font-semibold text-danger mt-1">{{ formatCurrency(totalExpense) }}</p>
        </div>
        <div class="bg-danger/10 p-3 rounded-lg">
          <font-awesome-icon :icon="['fas', 'arrow-down']" class="text-danger text-xl" />
        </div>
      </div>
    </div>

    <!-- Savings Card -->
    <div class="bg-gray-50 rounded-xl p-4 shadow-sm">
      <div class="flex items-center justify-between">
        <div>
          <p class="text-text-secondary text-sm">Tiết kiệm tháng này</p>
          <p class="text-2xl font-semibold text-primary mt-1">{{ formatCurrency(totalSavings) }}</p>
        </div>
        <div class="bg-primary/10 p-3 rounded-lg">
          <font-awesome-icon :icon="['fas', 'piggy-bank']" class="text-primary text-xl" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
</style> 