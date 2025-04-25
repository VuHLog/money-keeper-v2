<script setup>
import { ref } from 'vue'
import VueApexCharts from 'vue3-apexcharts'
import FilterOptions from '@components/FilterOptions.vue'

// Sample data - Replace with actual data from your API
const transactionData = ref({
  accountBalances: [
    { name: 'Ví tiền mặt', balance: 5000000, percentage: 25 },
    { name: 'Ngân hàng A', balance: 10000000, percentage: 50 },
    { name: 'Ngân hàng B', balance: 5000000, percentage: 25 }
  ],
  accountTransactions: {
    'Ví tiền mặt': [1000000, 2000000, 3000000, 4000000, 5000000],
    'Ngân hàng A': [5000000, 6000000, 7000000, 8000000, 10000000],
    'Ngân hàng B': [2000000, 2500000, 3000000, 4000000, 5000000]
  },
  accountTypes: [
    { name: 'Tiền mặt', amount: 5000000, percentage: 25 },
    { name: 'Ngân hàng', amount: 15000000, percentage: 75 }
  ]
})

// Chart options
const accountBalancesChart = ref({
  series: transactionData.value.accountBalances.map(account => account.balance),
  chart: {
    type: 'donut',
    height: 350
  },
  labels: transactionData.value.accountBalances.map(account => account.name),
  colors: ['#3B82F6', '#10B981', '#F59E0B'],
  legend: {
    position: 'bottom'
  }
})

const accountTransactionsChart = ref({
  series: Object.entries(transactionData.value.accountTransactions).map(([name, data]) => ({
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

const accountTypesChart = ref({
  series: transactionData.value.accountTypes.map(type => type.amount),
  chart: {
    type: 'donut',
    height: 350
  },
  labels: transactionData.value.accountTypes.map(type => type.name),
  colors: ['#3B82F6', '#10B981'],
  legend: {
    position: 'bottom'
  }
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
      <!-- Account Balances -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Số dư tài khoản</h3>
        <apexchart
          type="donut"
          height="350"
          :options="accountBalancesChart"
          :series="accountBalancesChart.series"
        />
      </div>

      <!-- Account Types -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Phân bổ theo loại tài khoản</h3>
        <apexchart
          type="donut"
          height="350"
          :options="accountTypesChart"
          :series="accountTypesChart.series"
        />
      </div>

      <!-- Account Transactions -->
      <div class="bg-surface rounded-xl p-4 shadow-sm md:col-span-2">
        <h3 class="text-lg font-medium text-text mb-4">Biến động số dư tài khoản</h3>
        <apexchart
          type="line"
          height="350"
          :options="accountTransactionsChart"
          :series="accountTransactionsChart.series"
        />
      </div>
    </div>
  </div>
</template> 