<script setup>
import { ref } from 'vue'
import VueApexCharts from 'vue3-apexcharts'
import FilterOptions from '@components/FilterOptions.vue'

// Sample data - Replace with actual data from your API
const transactionData = ref({
  savingsGoals: [
    { name: 'Mua nhà', target: 1000000000, current: 500000000, percentage: 50 },
    { name: 'Mua xe', target: 500000000, current: 200000000, percentage: 40 },
    { name: 'Du lịch', target: 100000000, current: 50000000, percentage: 50 }
  ],
  savingsTrends: {
    'Mua nhà': [100000000, 200000000, 300000000, 400000000, 500000000],
    'Mua xe': [50000000, 100000000, 150000000, 200000000, 200000000],
    'Du lịch': [10000000, 20000000, 30000000, 40000000, 50000000]
  }
})

// Chart options
const savingsGoalsChart = ref({
  series: transactionData.value.savingsGoals.map(goal => goal.current),
  chart: {
    type: 'donut',
    height: 350
  },
  labels: transactionData.value.savingsGoals.map(goal => goal.name),
  colors: ['#3B82F6', '#10B981', '#F59E0B'],
  legend: {
    position: 'bottom'
  }
})

const savingsTrendsChart = ref({
  series: Object.entries(transactionData.value.savingsTrends).map(([name, data]) => ({
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
      <!-- Savings Goals -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Tiến độ mục tiêu tiết kiệm</h3>
        <apexchart
          type="donut"
          height="350"
          :options="savingsGoalsChart"
          :series="savingsGoalsChart.series"
        />
      </div>

      <!-- Savings Trends -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Xu hướng tiết kiệm</h3>
        <apexchart
          type="line"
          height="350"
          :options="savingsTrendsChart"
          :series="savingsTrendsChart.series"
        />
      </div>
    </div>
  </div>
</template> 