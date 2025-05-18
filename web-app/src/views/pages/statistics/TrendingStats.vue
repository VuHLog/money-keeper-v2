<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useReportStore } from '@stores/ReportStore'
import VueApexCharts from 'vue3-apexcharts'
import FilterOptions from '@components/FilterOptions.vue'
import { formatCurrency } from '@/utils/formatters'


const reportStore = useReportStore()
const dailyTrendData = ref([])
const weeklyTrendData = ref([])
const monthlyTrendData = ref([])
const yearlyTrendData = ref([])
const filters = ref();
const excelFilters = ref();
const transactionType = ref('expense')
const dailyTrendsCategories = ref(['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23', '24', '25', '26', '27', '28', '29', '30', '31'])
const weeklyTrendsCategories = ref(['Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7', 'Chủ nhật'])
const monthlyTrendsCategories = ref(['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6', 'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'])
const yearlyTrendsCategories = ref(Array.from({ length: 5 }, (_, i) => (new Date().getFullYear() - 4 + i).toString()))

// Chart options
const dailyTrendsChart = ref({
  series: [{
    name: 'Chi tiêu',
    data: dailyTrendData.value
  }],
  chart: {
    type: 'line',
    height: 350,
    animations: {
      enabled: true,
      easing: 'easeinout',
      speed: 800,
      animateGradually: {
        enabled: true,
        delay: 150
      },
      dynamicAnimation: {
        enabled: true,
        speed: 350
      }
    }
  },
  stroke: {
    curve: 'smooth'
  },
  markers: {
    size: 4,
    hover: {
      size: 6
    }
  },
  xaxis: {
    categories: ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12', '13', '14', '15', '16', '17', '18', '19', '20', '21', '22', '23', '24', '25', '26', '27', '28', '29', '30', '31']
  },
  yaxis: {
    labels: {
      show: true,
      formatter: function (val) {
        return formatCurrency(val);
      }
    },
    min: 0
  },
  tooltip: {
    x: {
      formatter: function (val) {
        return 'Ngày ' + val.toString().padStart(2, '0');
      }
    },
    y: {
      formatter: function (val) {
        return formatCurrency(val);
      }
    }
  },
  colors: ['#3B82F6'],
  noData: {
    text: "Không có dữ liệu",
    align: 'center',
    verticalAlign: 'middle',
    offsetX: 0,
    offsetY: 0,
    style: {
      color: undefined,
      fontSize: '16px',
      fontFamily: undefined
    }
  }
})

const weeklyTrendsChart = ref({
  series: [{
    name: 'Chi tiêu',
    data: weeklyTrendData.value
  }],
  chart: {
    type: 'line',
    height: 350,
    animations: {
      enabled: true,
      easing: 'easeinout',
      speed: 800,
      animateGradually: {
        enabled: true,
        delay: 150
      },
      dynamicAnimation: {
        enabled: true,
        speed: 350
      }
    }
  },
  stroke: {
    curve: 'smooth'
  },
  markers: {
    size: 4,
    hover: {
      size: 6
    }
  },
  xaxis: {
    categories: ['Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7', 'Chủ nhật']
  },
  yaxis: {
    labels: {
      show: true,
      formatter: function (val) {
        return formatCurrency(val);
      }
    },
    min: 0
  },
  tooltip: {
    y: {
      formatter: function (val) {
        return formatCurrency(val);
      }
    }
  },
  colors: ['#10B981'],
  noData: {
    text: "Không có dữ liệu",
    align: 'center',
    verticalAlign: 'middle',
    offsetX: 0,
    offsetY: 0,
    style: {
      color: undefined,
      fontSize: '16px',
      fontFamily: undefined
    }
  }
})

const monthlyTrendsChart = ref({
  series: [{
    name: 'Chi tiêu',
    data: monthlyTrendData.value
  }],
  chart: {
    type: 'line',
    height: 350,
    animations: {
      enabled: true,
      easing: 'easeinout',
      speed: 800,
      animateGradually: {
        enabled: true,
        delay: 150
      },
      dynamicAnimation: {
        enabled: true,
        speed: 350
      }
    }
  },
  stroke: {
    curve: 'smooth'
  },
  markers: {
    size: 4,
    hover: {
      size: 6
    }
  },
  xaxis: {
    categories: ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6', 'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12']
  },
  yaxis: {
    labels: {
      show: true,
      formatter: function (val) {
        return formatCurrency(val);
      }
    },
    min: 0
  },
  tooltip: {
    y: {
      formatter: function (val) {
        return formatCurrency(val);
      }
    }
  },
  colors: ['#F59E0B'],
  noData: {
    text: "Không có dữ liệu",
    align: 'center',
    verticalAlign: 'middle',
    offsetX: 0,
    offsetY: 0,
    style: {
      color: undefined,
      fontSize: '16px',
      fontFamily: undefined
    }
  }
})

const yearlyTrendsChart = ref({
  series: [{
    name: 'Chi tiêu',
    data: yearlyTrendData.value
  }],
  chart: {
    type: 'line',
    height: 350,
    animations: {
      enabled: true,
      easing: 'easeinout',
      speed: 800,
      animateGradually: {
        enabled: true,
        delay: 150
      },
      dynamicAnimation: {
        enabled: true,
        speed: 350
      }
    }
  },
  stroke: {
    curve: 'smooth'
  },
  markers: {
    size: 4,
    hover: {
      size: 6
    }
  },
  xaxis: {
    categories: Array.from({ length: 5 }, (_, i) => (new Date().getFullYear() - 4 + i).toString())
  },
  yaxis: {
    labels: {
      show: true,
      formatter: function (val) {
        return formatCurrency(val);
      }
    },
    min: 0
  },
  tooltip: {
    y: {
      formatter: function (val) {
        return formatCurrency(val);
      }
    }
  },
  colors: ['#EF4444'],
  noData: {
    text: "Không có dữ liệu",
    align: 'center',
    verticalAlign: 'middle',
    offsetX: 0,
    offsetY: 0,
    style: {
      color: undefined,
      fontSize: '16px',
      fontFamily: undefined
    }
  }
})

onMounted(async () => {
  filters.value = {
    account: [],
    transactionType: 'expense',
  }
  excelFilters.value = filters.value;
  await getData();
  updateTransactionData();
})

const getData = async () => {
  dailyTrendData.value = await reportStore.getReportDailyTrend(filters.value);
  weeklyTrendData.value = await reportStore.getReportWeeklyTrend(filters.value);
  monthlyTrendData.value = await reportStore.getReportMonthlyTrend(filters.value);
  yearlyTrendData.value = await reportStore.getReportYearlyTrend(filters.value);
}

const handleFilterChange = (filterOptions) => {
  filters.value = filterOptions;
  console.log('Filters updated:', filters.value);
}

const handleApplyFilter = async () => {
  transactionType.value = filters.value.transactionType;
  excelFilters.value = filters.value;
  await getData();
  updateTransactionData();
}

const updateTransactionData = () => {
  let name = transactionType.value === 'expense' ? 'Chi tiêu' : 'Doanh thu';
  
  // Cập nhật dữ liệu cho daily chart 
  dailyTrendsChart.value.series[0].name = name;
  dailyTrendsChart.value.series[0].data = dailyTrendsCategories.value.map((category) => {
    let dataItem = dailyTrendData.value.find((item) => category === item.dayOfMonth);
    return dataItem ? dataItem.total : 0;
  });
  
  // Cập nhật xaxis riêng, đảm bảo categories được gán đúng
  dailyTrendsChart.value.xaxis.categories = [...dailyTrendsCategories.value];

  // Cập nhật dữ liệu cho weekly chart
  weeklyTrendsChart.value.series[0].name = name;
  weeklyTrendsChart.value.series[0].data = weeklyTrendsCategories.value.map((category) => {
    let dataItem = weeklyTrendData.value.find((item) => category === item.dayOfWeek);
    return dataItem ? dataItem.total : 0;
  });
  
  // Cập nhật xaxis riêng, đảm bảo categories được gán đúng
  weeklyTrendsChart.value.xaxis.categories = [...weeklyTrendsCategories.value];

  // Cập nhật dữ liệu cho monthly chart
  monthlyTrendsChart.value.series[0].name = name;
  monthlyTrendsChart.value.series[0].data = monthlyTrendsCategories.value.map((category) => {
    let dataItem = monthlyTrendData.value.find((item) => category === item.month);
    return dataItem ? dataItem.total : 0;
  });
  
  // Cập nhật xaxis riêng, đảm bảo categories được gán đúng
  monthlyTrendsChart.value.xaxis.categories = [...monthlyTrendsCategories.value];

  // Cập nhật dữ liệu cho yearly chart
  yearlyTrendsChart.value.series[0].name = name;
  yearlyTrendsChart.value.series[0].data = yearlyTrendsCategories.value.map((category) => {
    let dataItem = yearlyTrendData.value.find((item) => category === item.year);
    return dataItem ? dataItem.total : 0;
  });
  
  // Cập nhật xaxis riêng, đảm bảo categories được gán đúng
  yearlyTrendsChart.value.xaxis.categories = [...yearlyTrendsCategories.value];
}

const exportExcelForDailyTrend = async () => {
  await reportStore.exportExcelForDailyTrend(excelFilters.value);
}

const exportExcelForWeeklyTrend = async () => {
  await reportStore.exportExcelForWeeklyTrend(excelFilters.value);
}

const exportExcelForMonthlyTrend = async () => {
  await reportStore.exportExcelForMonthlyTrend(excelFilters.value);
}

const exportExcelForYearlyTrend = async () => {
  await reportStore.exportExcelForYearlyTrend(excelFilters.value);
}
</script>

<template>
  <div>
    <!-- Filter Options -->
    <FilterOptions
      :show-time-range="false"
      :show-account="true"
      :show-expense-category="false"
      :show-revenue-category="false"
      :show-transaction-type="true"
      @filter-change="handleFilterChange"
      @apply-filter="handleApplyFilter"
    />

    <!-- Charts -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <!-- Daily Trends -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
      <div class="flex justify-between items-center mb-4">
        <h3 class="text-lg font-medium text-text">Xu hướng {{ transactionType === 'expense' ? 'chi tiêu' : 'thu nhập' }} theo ngày</h3>
        <!-- <div class="flex justify-end items-center"> -->
          <!-- Export Excel button -->
          <!-- <button
            class="flex items-center space-x-2 px-4 py-2 bg-success text-white rounded-lg hover:bg-success/90 transition-colors duration-200"
            @click="exportExcelForDailyTrend"
          >
            <font-awesome-icon :icon="['fas', 'file-excel']" />
            <span>Xuất Excel</span>
          </button> -->
        <!-- </div> -->
      </div>
        <apexchart
          type="line"
          height="350"
          :options="dailyTrendsChart"
          :series="dailyTrendsChart.series"
        />
      </div>

      <!-- Weekly Trends -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-lg font-medium text-text">Xu hướng {{ transactionType === 'expense' ? 'chi tiêu' : 'thu nhập' }} theo tuần</h3>
          <!-- <div class="flex justify-end items-center"> -->
            <!-- Export Excel button -->
            <!-- <button
              class="flex items-center space-x-2 px-4 py-2 bg-success text-white rounded-lg hover:bg-success/90 transition-colors duration-200"
              @click="exportExcelForWeeklyTrend"
            >
              <font-awesome-icon :icon="['fas', 'file-excel']" />
              <span>Xuất Excel</span>
            </button> -->
          <!-- </div> -->
        </div>
        <apexchart
          type="line"
          height="350"
          :options="weeklyTrendsChart"
          :series="weeklyTrendsChart.series"
        />
      </div>

      <!-- Monthly Trends -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-lg font-medium text-text">Xu hướng {{ transactionType === 'expense' ? 'chi tiêu' : 'thu nhập' }} theo tháng</h3>
          <!-- <div class="flex justify-end items-center"> -->
            <!-- Export Excel button -->
            <!-- <button
              class="flex items-center space-x-2 px-4 py-2 bg-success text-white rounded-lg hover:bg-success/90 transition-colors duration-200"
              @click="exportExcelForMonthlyTrend"
            >
              <font-awesome-icon :icon="['fas', 'file-excel']" />
              <span>Xuất Excel</span>
            </button> -->
          <!-- </div> -->
        </div>
        <apexchart
          type="line"
          height="350"
          :options="monthlyTrendsChart"
          :series="monthlyTrendsChart.series"
        />
      </div>

      <!-- Yearly Trends -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <div class="flex justify-between items-center mb-4">
          <h3 class="text-lg font-medium text-text">Xu hướng {{ transactionType === 'expense' ? 'chi tiêu' : 'thu nhập' }} 5 năm gần nhất</h3>
          <!-- <div class="flex justify-end items-center"> -->
            <!-- Export Excel button -->
            <!-- <button
              class="flex items-center space-x-2 px-4 py-2 bg-success text-white rounded-lg hover:bg-success/90 transition-colors duration-200"
              @click="exportExcelForYearlyTrend"
            >
              <font-awesome-icon :icon="['fas', 'file-excel']" />
              <span>Xuất Excel</span>
            </button> -->
          <!-- </div> -->
        </div>
        <apexchart
          type="line"
          height="350"
          :options="yearlyTrendsChart"
          :series="yearlyTrendsChart.series"
        />
      </div>
    </div>
  </div>
</template> 