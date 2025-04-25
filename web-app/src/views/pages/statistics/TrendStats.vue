<script setup>
import { ref } from 'vue'
import VueApexCharts from 'vue3-apexcharts'
import FilterOptions from '@components/FilterOptions.vue'

// Sample data - Replace with actual data from your API
const transactionData = ref({
  dailyTrends: {
    income: [1000, 1500, 2000, 1800, 2500, 3000, 2800],
    expense: [800, 1200, 1500, 1300, 2000, 2500, 2200]
  },
  weeklyTrends: {
    income: [15000, 18000, 20000, 22000],
    expense: [10000, 12000, 15000, 18000]
  },
  monthlyTrends: {
    income: [50000, 60000, 70000, 80000, 90000],
    expense: [40000, 45000, 50000, 55000, 60000]
  }
})

// Chart options
const dailyTrendsChart = ref({
  series: [{
    name: 'Thu nhập',
    data: transactionData.value.dailyTrends.income
  }, {
    name: 'Chi tiêu',
    data: transactionData.value.dailyTrends.expense
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
    categories: ['Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7', 'Chủ nhật']
  },
  colors: ['#10B981', '#EF4444']
})

const weeklyTrendsChart = ref({
  series: [{
    name: 'Thu nhập',
    data: transactionData.value.weeklyTrends.income
  }, {
    name: 'Chi tiêu',
    data: transactionData.value.weeklyTrends.expense
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
    categories: ['Tuần 1', 'Tuần 2', 'Tuần 3', 'Tuần 4']
  },
  colors: ['#10B981', '#EF4444']
})

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

    <!-- Charts -->
    <div class="grid grid-cols-1 gap-6">
      <!-- Daily Trends -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Xu hướng theo ngày</h3>
        <apexchart
          type="line"
          height="350"
          :options="dailyTrendsChart"
          :series="dailyTrendsChart.series"
        />
      </div>

      <!-- Weekly Trends -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Xu hướng theo tuần</h3>
        <apexchart
          type="line"
          height="350"
          :options="weeklyTrendsChart"
          :series="weeklyTrendsChart.series"
        />
      </div>

      <!-- Monthly Trends -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Xu hướng theo tháng</h3>
        <apexchart
          type="line"
          height="350"
          :options="monthlyTrendsChart"
          :series="monthlyTrendsChart.series"
        />
      </div>
    </div>
  </div>
</template> 