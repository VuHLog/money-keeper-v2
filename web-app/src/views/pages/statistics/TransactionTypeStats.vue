<script setup>
import { ref } from 'vue'
import VueApexCharts from 'vue3-apexcharts'
import FilterOptions from '@components/FilterOptions.vue'

// Sample data - Replace with actual data from your API
const transactionData = ref({
  monthlyIncome: [3000, 3500, 4000, 3800, 4200, 4500, 5000, 4800, 5200, 5500, 6000, 5800],
  monthlyExpense: [2500, 2800, 3200, 3000, 3500, 3800, 4000, 4200, 4500, 4800, 5000, 5200],
  transactionCount: [15, 18, 20, 22, 25, 28, 30, 32, 35, 38, 40, 42],
  incomeVsExpense: [45, 55] // Percentage
})

// Chart options
const monthlyIncomeChart = ref({
  series: [{
    name: 'Thu nhập',
    data: transactionData.value.monthlyIncome
  }],
  chart: {
    type: 'bar',
    height: 350
  },
  plotOptions: {
    bar: {
      borderRadius: 4,
      horizontal: false,
    }
  },
  dataLabels: {
    enabled: false
  },
  xaxis: {
    categories: ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6', 
                'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12']
  },
  colors: ['#10B981']
})

const monthlyExpenseChart = ref({
  series: [{
    name: 'Chi tiêu',
    data: transactionData.value.monthlyExpense
  }],
  chart: {
    type: 'bar',
    height: 350
  },
  plotOptions: {
    bar: {
      borderRadius: 4,
      horizontal: false,
    }
  },
  dataLabels: {
    enabled: false
  },
  xaxis: {
    categories: ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6', 
                'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12']
  },
  colors: ['#EF4444']
})

const transactionCountChart = ref({
  series: [{
    name: 'Số giao dịch',
    data: transactionData.value.transactionCount
  }],
  chart: {
    type: 'line',
    height: 350
  },
  stroke: {
    curve: 'smooth'
  },
  xaxis: {
    categories: ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6', 
                'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12']
  },
  colors: ['#3B82F6']
})

const incomeVsExpenseChart = ref({
  series: transactionData.value.incomeVsExpense,
  chart: {
    type: 'donut',
    height: 350
  },
  labels: ['Thu nhập', 'Chi tiêu'],
  colors: ['#10B981', '#EF4444'],
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
      :show-transaction-type="true"
      :show-account="true"
      @filter-change="handleFilterChange"
    />

    <!-- Charts -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <!-- Tổng thu nhập theo tháng -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Tổng thu nhập theo tháng</h3>
        <apexchart
          type="bar"
          height="350"
          :options="monthlyIncomeChart"
          :series="monthlyIncomeChart.series"
        />
      </div>

      <!-- Tổng chi tiêu theo tháng -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Tổng chi tiêu theo tháng</h3>
        <apexchart
          type="bar"
          height="350"
          :options="monthlyExpenseChart"
          :series="monthlyExpenseChart.series"
        />
      </div>

      <!-- Tổng số giao dịch -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Tổng số giao dịch</h3>
        <apexchart
          type="line"
          height="350"
          :options="transactionCountChart"
          :series="transactionCountChart.series"
        />
      </div>

      <!-- Tỉ lệ thu/chi -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Tỉ lệ thu/chi</h3>
        <apexchart
          type="donut"
          height="350"
          :options="incomeVsExpenseChart"
          :series="incomeVsExpenseChart.series"
        />
      </div>
    </div>
  </div>
</template> 