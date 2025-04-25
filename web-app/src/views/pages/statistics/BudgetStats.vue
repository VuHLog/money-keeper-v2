<script setup>
import { ref } from 'vue'
import VueApexCharts from 'vue3-apexcharts'
import FilterOptions from '@components/FilterOptions.vue'

// Sample data - Replace with actual data from your API
const transactionData = ref({
  budgetStatus: [
    { name: 'Ăn uống', budget: 2000000, spent: 1800000, percentage: 90 },
    { name: 'Di chuyển', budget: 1500000, spent: 1200000, percentage: 80 },
    { name: 'Giải trí', budget: 1000000, spent: 800000, percentage: 80 },
    { name: 'Mua sắm', budget: 1800000, spent: 2000000, percentage: 111 },
    { name: 'Khác', budget: 1700000, spent: 1500000, percentage: 88 }
  ],
  monthlyBudgetTrends: {
    budget: [8000000, 8000000, 8000000, 8000000, 8000000],
    spent: [6000000, 6500000, 7000000, 7500000, 8000000]
  }
})

// Chart options
const budgetStatusChart = ref({
  series: transactionData.value.budgetStatus.map(item => item.spent),
  chart: {
    type: 'bar',
    height: 350
  },
  plotOptions: {
    bar: {
      horizontal: true,
      dataLabels: {
        position: 'top'
      }
    }
  },
  dataLabels: {
    enabled: true,
    formatter: function(val, opts) {
      const item = transactionData.value.budgetStatus[opts.dataPointIndex]
      return `${item.percentage}% (${item.spent.toLocaleString()}đ/${item.budget.toLocaleString()}đ)`
    }
  },
  xaxis: {
    categories: transactionData.value.budgetStatus.map(item => item.name)
  },
  colors: ['#3B82F6']
})

const monthlyBudgetTrendsChart = ref({
  series: [{
    name: 'Ngân sách',
    data: transactionData.value.monthlyBudgetTrends.budget
  }, {
    name: 'Đã chi',
    data: transactionData.value.monthlyBudgetTrends.spent
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
      <!-- Budget Status -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Tình trạng ngân sách</h3>
        <apexchart
          type="bar"
          height="350"
          :options="budgetStatusChart"
          :series="budgetStatusChart.series"
        />
      </div>

      <!-- Monthly Budget Trends -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Xu hướng ngân sách theo tháng</h3>
        <apexchart
          type="line"
          height="350"
          :options="monthlyBudgetTrendsChart"
          :series="monthlyBudgetTrendsChart.series"
        />
      </div>
    </div>
  </div>
</template> 