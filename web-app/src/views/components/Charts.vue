<script setup>
import { ref, onMounted } from 'vue'
import VueApexCharts from 'vue3-apexcharts'
import { formatCurrency } from '@/utils/formatters'

// Sample data - Replace with your actual data
const incomeExpenseSeries = ref([
  {
    name: 'Thu nhập',
    data: [3000000, 4000000, 3500000, 5000000, 4900000, 6000000]
  },
  {
    name: 'Chi tiêu',
    data: [2000000, 3000000, 2500000, 4000000, 3900000, 5000000]
  }
])

const incomeExpenseOptions = ref({
  chart: {
    type: 'bar',
    toolbar: {
      show: false
    }
  },
  plotOptions: {
    bar: {
      horizontal: false,
      columnWidth: '55%',
      endingShape: 'rounded'
    }
  },
  dataLabels: {
    enabled: false
  },
  stroke: {
    show: true,
    width: 2,
    colors: ['transparent']
  },
  xaxis: {
    categories: ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6'],
    labels: {
      style: {
        colors: '#64748b'
      }
    }
  },
  yaxis: {
    title: {
      text: 'VND',
      style: {
        color: '#64748b'
      }
    },
    labels: {
      formatter: function (value) {
        return (value / 1000000).toFixed(1) + 'M'
      },
      style: {
        colors: '#64748b'
      }
    }
  },
  fill: {
    opacity: 1,
    colors: ['#22c55e', '#ef4444']
  },
  tooltip: {
    y: {
      formatter: function (value) {
        return formatCurrency(value)
      }
    }
  },
  legend: {
    position: 'top',
    horizontalAlign: 'center',
    labels: {
      colors: '#64748b'
    }
  }
})

const expenseCategoriesSeries = ref([44, 55, 13, 43, 22])
const expenseCategoriesOptions = ref({
  chart: {
    type: 'donut',
    toolbar: {
      show: false
    }
  },
  labels: ['Ăn uống', 'Mua sắm', 'Giải trí', 'Đi lại', 'Khác'],
  colors: ['#22c55e', '#3b82f6', '#f59e0b', '#ef4444', '#8b5cf6'],
  plotOptions: {
    pie: {
      donut: {
        size: '70%',
        labels: {
          show: true,
          name: {
            show: true,
            fontSize: '14px',
            fontWeight: 600,
            color: '#64748b'
          },
          value: {
            show: true,
            fontSize: '16px',
            fontWeight: 600,
            color: '#64748b',
            formatter: function (val) {
              return (val / 1000000).toFixed(1) + 'M'
            }
          },
          total: {
            show: true,
            label: 'Tổng chi tiêu',
            color: '#64748b',
            formatter: function (w) {
              return (w.globals.seriesTotals.reduce((a, b) => a + b, 0) / 1000000).toFixed(1) + 'M'
            }
          }
        }
      }
    }
  },
  legend: {
    position: 'bottom',
    horizontalAlign: 'center',
    labels: {
      colors: '#64748b'
    }
  },
  tooltip: {
    y: {
      formatter: function (value) {
        return formatCurrency(value)
      }
    }
  }
})

// Register the component
const apexchart = VueApexCharts
</script>

<template>
  <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
    <!-- Income vs Expense Chart -->
    <div class="bg-gray-50 rounded-lg p-4 shadow-sm">
      <h3 class="text-lg font-semibold mb-4">Thu nhập vs Chi tiêu</h3>
      <div class="h-[350px]">
        <apexchart
          type="bar"
          height="100%"
          :options="incomeExpenseOptions"
          :series="incomeExpenseSeries"
        />
      </div>
    </div>

    <!-- Expense Categories Chart -->
    <div class="bg-gray-50 rounded-lg p-4 shadow-sm">
      <h3 class="text-lg font-semibold mb-4">Phân loại chi tiêu</h3>
      <div class="h-[350px]">
        <apexchart
          type="donut"
          height="100%"
          :options="expenseCategoriesOptions"
          :series="expenseCategoriesSeries"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.bg-gray-50 {
  background-color: rgb(249, 250, 251);
}
</style> 