<script setup>
import { ref } from 'vue'
import VueApexCharts from 'vue3-apexcharts'
import FilterOptions from '@components/FilterOptions.vue'

// Sample data - Replace with actual data from your API
const transactionData = ref({
  summary: {
    totalIncome: 10000000,
    totalExpense: 8000000,
    netIncome: 2000000,
    savingsRate: 20
  },
  monthlyTrends: {
    income: [15000, 18000, 20000, 22000, 25000],
    expense: [10000, 12000, 15000, 18000, 20000]
  }
})

// Chart options
const monthlyTrendsChart = ref({
  series: [{
    name: 'Thu nhập',
    data: transactionData.value.monthlyTrends.income
  }, {
    name: 'Chi tiêu',
    data: transactionData.value.monthlyTrends.expense
  }],
  chart: {
    type: 'line',
    height: 350,
    stacked: false
  },
  stroke: {
    curve: 'smooth'
  },
  xaxis: {
    categories: ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5']
  },
  colors: ['#10B981', '#EF4444']
})

const handleFilterChange = (filters) => {
  // TODO: Call API with new filters and update charts
  console.log('Filters changed:', filters)
}
</script>

<template>
  <div>
    <!-- Filter Options -->
    <FilterOptions
      :show-time-range="true"
      :show-account="true"
      :show-category="true"
      @filter-change="handleFilterChange"
    />

    <!-- Summary Cards -->
    <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-6">
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-sm font-medium text-text-secondary mb-1">Tổng thu nhập</h3>
        <p class="text-2xl font-semibold text-text">{{ transactionData.summary.totalIncome.toLocaleString() }}đ</p>
      </div>
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-sm font-medium text-text-secondary mb-1">Tổng chi tiêu</h3>
        <p class="text-2xl font-semibold text-text">{{ transactionData.summary.totalExpense.toLocaleString() }}đ</p>
      </div>
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-sm font-medium text-text-secondary mb-1">Thu nhập ròng</h3>
        <p class="text-2xl font-semibold text-text">{{ transactionData.summary.netIncome.toLocaleString() }}đ</p>
      </div>
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-sm font-medium text-text-secondary mb-1">Tỷ lệ tiết kiệm</h3>
        <p class="text-2xl font-semibold text-text">{{ transactionData.summary.savingsRate }}%</p>
      </div>
    </div>

    <!-- Monthly Trends Chart -->
    <div class="bg-surface rounded-xl p-4 shadow-sm">
      <h3 class="text-lg font-medium text-text mb-4">Xu hướng thu chi theo tháng</h3>
      <apexchart
        type="line"
        height="350"
        :options="monthlyTrendsChart"
        :series="monthlyTrendsChart.series"
      />
    </div>
  </div>
</template> 