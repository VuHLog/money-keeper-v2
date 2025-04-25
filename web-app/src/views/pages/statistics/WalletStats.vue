<script setup>
import { ref } from 'vue'
import VueApexCharts from 'vue3-apexcharts'
import FilterOptions from '@components/FilterOptions.vue'

// Sample data - Replace with actual data from your API
const transactionData = ref({
  walletBalance: [
    { name: 'Ví tiền mặt', amount: 5000000, percentage: 25 },
    { name: 'Ví ngân hàng A', amount: 10000000, percentage: 50 },
    { name: 'Ví ngân hàng B', amount: 5000000, percentage: 25 }
  ],
  walletTransactions: {
    'Ví tiền mặt': [5000000, 4500000, 4000000, 3500000, 3000000],
    'Ví ngân hàng A': [10000000, 11000000, 12000000, 13000000, 14000000],
    'Ví ngân hàng B': [5000000, 5500000, 6000000, 6500000, 7000000]
  }
})

// Chart options
const walletBalanceChart = ref({
  series: transactionData.value.walletBalance.map(wallet => wallet.amount),
  chart: {
    type: 'donut',
    height: 350
  },
  labels: transactionData.value.walletBalance.map(wallet => wallet.name),
  colors: ['#3B82F6', '#10B981', '#F59E0B'],
  legend: {
    position: 'bottom'
  }
})

const walletTransactionsChart = ref({
  series: Object.entries(transactionData.value.walletTransactions).map(([name, data]) => ({
    name,
    data
  })),
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
  colors: ['#3B82F6', '#10B981', '#F59E0B']
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

    <!-- Charts -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <!-- Wallet Balance -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Phân bổ ví</h3>
        <apexchart
          type="donut"
          height="350"
          :options="walletBalanceChart"
          :series="walletBalanceChart.series"
        />
      </div>

      <!-- Wallet Transactions -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Biến động số dư</h3>
        <apexchart
          type="line"
          height="350"
          :options="walletTransactionsChart"
          :series="walletTransactionsChart.series"
        />
      </div>
    </div>
  </div>
</template> 