<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useReportStore } from '@stores/ReportStore'
import VueApexCharts from 'vue3-apexcharts'
import FilterOptions from '@components/FilterOptions.vue'
import { formatCurrency, formatReverseStringDate } from '@/utils/formatters'


const reportStore = useReportStore()
const filters = ref();
const data = ref([]);
const totalExpense = ref(0);
const totalIncome = ref(0);

// Sample data - Replace with actual data from your API
const transactionData = ref({
  income: [],
  expense: [],
  transactionCount: [],
  incomeVsExpense: [] // Percentage
})

// Generate categories based on timeOption and customTimeRange
const generateCategories = () => {
  if (!filters.value || !filters.value.customTimeRange || filters.value.customTimeRange.length < 2) {
    return ['Tháng 01', 'Tháng 02', 'Tháng 03', 'Tháng 04', 'Tháng 05', 'Tháng 06',
      'Tháng 07', 'Tháng 08', 'Tháng 09', 'Tháng 10', 'Tháng 11', 'Tháng 12'];
  }

  const startDate = new Date(filters.value.customTimeRange[0]);
  const endDate = new Date(filters.value.customTimeRange[1]);
  const categories = [];

  // Based on timeOption
  if (filters.value.timeOption === "Tùy chọn") {
    // For dates, we generate daily categories
    const currentDate = new Date(startDate);
    while (currentDate <= endDate) {
      const day = String(currentDate.getDate()).padStart(2, '0');
      const month = String(currentDate.getMonth() + 1).padStart(2, '0');
      categories.push(`${day}/${month}/${currentDate.getFullYear()}`);
      currentDate.setDate(currentDate.getDate() + 1);
    }
  } else if (filters.value.timeOption === "Theo tháng") {
    // For months, generate monthly categories
    const currentDate = new Date(startDate);
    currentDate.setDate(1); // Start from the first day of month

    while (currentDate <= endDate ||
      (currentDate.getMonth() <= endDate.getMonth() && currentDate.getFullYear() <= endDate.getFullYear())) {
      const month = String(currentDate.getMonth() + 1).padStart(2, '0');
      categories.push(`${month}/${currentDate.getFullYear()}`);
      currentDate.setMonth(currentDate.getMonth() + 1);
    }
  } else if (filters.value.timeOption === "Theo năm") {
    // For years, generate yearly categories
    const startYear = startDate.getFullYear();
    const endYear = endDate.getFullYear();

    for (let year = startYear; year <= endYear; year++) {
      categories.push(`${year}`);
    }
  } else {
    // Default case for custom ranges
    const currentDate = new Date(startDate);
    currentDate.setDate(1); // Start from the first day of month

    while (currentDate <= endDate ||
      (currentDate.getMonth() <= endDate.getMonth() && currentDate.getFullYear() <= endDate.getFullYear())) {
      const month = String(currentDate.getMonth() + 1).padStart(2, '0');
      categories.push(`Tháng ${month}-${currentDate.getFullYear()}`);
      currentDate.setMonth(currentDate.getMonth() + 1);
    }
  }

  return categories;
};

// Chart options with computed categories
const chartCategories = computed(() => generateCategories());

// Chart options
const incomeChart = ref({
  series: [{
    name: 'Thu nhập',
    data: transactionData.value.income
  }],
  chart: {
    type: 'bar',
    height: 350,
    zoom: {
      enabled: true
    },
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
    categories: chartCategories.value
  },
  yaxis: {
    labels: {
      formatter: function (val) {
        return formatCurrency(val)
      }
    }
  },
  colors: ['#10B981'],
  tooltip: {
    y: {
      formatter: function (val) {
        return formatCurrency(val)
      }
    }
  },
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

const expenseChart = ref({
  series: [{
    name: 'Chi tiêu',
    data: transactionData.value.expense
  }],
  chart: {
    type: 'bar',
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
    categories: chartCategories.value
  },
  yaxis: {
    labels: {
      formatter: function (val) {
        return formatCurrency(val)
      }
    }
  },
  colors: ['#EF4444'],
  tooltip: {
    y: {
      formatter: function (val) {
        return formatCurrency(val)
      }
    }
  },
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

const transactionCountChart = ref({
  series: [{
    name: 'Số giao dịch',
    data: transactionData.value.transactionCount
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
    categories: chartCategories.value
  },
  yaxis: {
    labels: {
      formatter: function (val) {
        return val.toFixed(0) // Số lượng giao dịch không cần định dạng tiền tệ
      }
    },
    min: 0 // Đảm bảo hiển thị giá trị 0
  },
  colors: ['#3B82F6']
})

const incomeVsExpenseChart = ref({
  series: transactionData.value.incomeVsExpense,
  chart: {
    type: 'donut',
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
  labels: ['Thu nhập', 'Chi tiêu'],
  colors: ['#10B981', '#EF4444'],
  legend: {
    position: 'bottom'
  },
  tooltip: {
    y: {
      formatter: function (val) {
        return formatCurrency(val)
      }
    }
  },
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

// Watch for changes in chartCategories and update all charts
watch(chartCategories, (newCategories) => {
  incomeChart.value.xaxis = { categories: newCategories };
  incomeChart.value.chart.zoom = { enabled: true };
  expenseChart.value.xaxis = { categories: newCategories };
  transactionCountChart.value.xaxis = { categories: newCategories };
}, { deep: true });

// Thêm biến kiểm soát hiển thị biểu đồ
const showCharts = ref(false);

onMounted(async () => {
  let currentDate = new Date();
  filters.value = {
        timeOption: "Theo tháng",
        account : [],
        customTimeRange: [currentDate.toISOString().slice(0, 7), currentDate.toISOString().slice(0, 7)],
  }
  
  // Lấy dữ liệu trước
  await getData();
  
  // Cập nhật dữ liệu cho biểu đồ
  updateTransactionData(data.value);
  
  // Hiển thị biểu đồ với animation
  setTimeout(() => {
    showCharts.value = true;
  }, 300);
})

const preserveChartSettings = (originalChart) => {
    const { chart: chartSettings } = originalChart;
    return {
      animations: chartSettings.animations
    };
};

const getData = async () => {
  data.value = await reportStore.getReportTransactionType(filters.value);
  data.value = data.value.map(item => {
    return {
      ...item,
      time: filters.value.timeOption === "Tùy chọn" ?formatReverseStringDate(item.time).replace(/-/g, "/") : item.time.replace(/-/g, "/"),
    }
  });
  totalExpense.value = data.value.reduce((sum, item) => sum + item.totalExpense, 0);
  totalIncome.value = data.value.reduce((sum, item) => sum + item.totalRevenue, 0);
}

const updateTransactionData = (data) => {
  // Prepare data first
  const incomeData = chartCategories.value.map((category) => {
    let dataItem = data.find((item) => category === item.time);
    return dataItem ? dataItem.totalRevenue : 0;
  });
  
  const expenseData = chartCategories.value.map((category) => {
    let dataItem = data.find((item) => category === item.time);
    return dataItem ? dataItem.totalExpense : 0;
  });
  
  const transactionCountData = chartCategories.value.map((category) => {
    let dataItem = data.find((item) => category === item.time);
    return dataItem ? dataItem.totalTransaction : 0;
  });
  
  // Update charts while preserving animation settings
  const incomeChartSettings = preserveChartSettings(incomeChart.value);
  
  // Update income chart
  incomeChart.value.series[0].data = incomeData;
  incomeChart.value = {
    ...incomeChart.value,
    xaxis: {
      categories: chartCategories.value
    },
    chart: {
      ...incomeChart.value.chart,
      ...incomeChartSettings.animations
    }
  };
  
  // Update expense chart
  const expenseChartSettings = preserveChartSettings(expenseChart.value);
  expenseChart.value.series[0].data = expenseData;
  expenseChart.value = {
    ...expenseChart.value,
    xaxis: {
      categories: chartCategories.value
    },
    chart: {
      ...expenseChart.value.chart,
      ...expenseChartSettings.animations
    }
  };
  
  // Update transaction count chart
  const transactionCountChartSettings = preserveChartSettings(transactionCountChart.value);
  transactionCountChart.value.series[0].data = transactionCountData;
  transactionCountChart.value = {
    ...transactionCountChart.value,
    xaxis: {
      categories: chartCategories.value
    },
    chart: {
      ...transactionCountChart.value.chart,
      ...transactionCountChartSettings.animations
    }
  };
  
  // Update donut chart
  const incomeVsExpenseChartSettings = preserveChartSettings(incomeVsExpenseChart.value);
  incomeVsExpenseChart.value.series = [
    totalIncome.value, 
    totalExpense.value
  ];
  incomeVsExpenseChart.value = {
    ...incomeVsExpenseChart.value,
    chart: {
      ...incomeVsExpenseChart.value.chart,
      ...incomeVsExpenseChartSettings.animations
    }
  };
}

const handleFilterChange = (filterOptions) => {
  filters.value = filterOptions;
  console.log('Filters updated:', filters.value);
}

const handleApplyFilter = async () => {
  // Ẩn biểu đồ trước khi cập nhật
  showCharts.value = false;
  
  await getData();
  
  // Cập nhật dữ liệu cho biểu đồ
  updateTransactionData(data.value);
  
  // Hiển thị lại biểu đồ sau một khoảng thời gian ngắn để kích hoạt animation
  setTimeout(() => {
    showCharts.value = true;
  }, 100);
}
</script>

<template>
  <div>
    <!-- Filter Options -->
    <FilterOptions
      :show-time-range="true"
      :show-transaction-type="false"
      :show-account="true"
      :show-expense-category="false"
      :show-revenue-category="false"
      @filter-change="handleFilterChange" 
      @apply-filter="handleApplyFilter"
      :showCategories="false"
      :defaultFilters="filters"
    />

    <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
      <!-- Income Chart -->
      <div class="bg-gray-50 p-4 rounded-lg">
        <h3 class="text-lg font-medium text-gray-800 mb-3">Thu nhập theo thời gian</h3>
        <vue-apex-charts 
          type="bar" 
          height="350" 
          :options="incomeChart" 
          :series="incomeChart.series"
          v-if="showCharts">
        </vue-apex-charts>
      </div>

      <!-- Expense Chart -->
      <div class="bg-gray-50 p-4 rounded-lg">
        <h3 class="text-lg font-medium text-gray-800 mb-3">Chi tiêu theo thời gian</h3>
        <vue-apex-charts 
          type="bar" 
          height="350" 
          :options="expenseChart" 
          :series="expenseChart.series"
          v-if="showCharts">
        </vue-apex-charts>
      </div>

      <!-- Transaction Count Chart -->
      <div class="bg-gray-50 p-4 rounded-lg">
        <h3 class="text-lg font-medium text-gray-800 mb-3">Số lượng giao dịch theo thời gian</h3>
        <vue-apex-charts 
          type="line" 
          height="350" 
          :options="transactionCountChart" 
          :series="transactionCountChart.series"
          v-if="showCharts">
        </vue-apex-charts>
      </div>

      <!-- Income vs Expense Donut Chart -->
      <div class="bg-gray-50 p-4 rounded-lg">
        <h3 class="text-lg font-medium text-gray-800 mb-3">Tỷ lệ thu nhập và chi tiêu</h3>
        <vue-apex-charts 
          type="donut" 
          height="350" 
          :options="incomeVsExpenseChart" 
          :series="incomeVsExpenseChart.series"
          v-if="showCharts">
        </vue-apex-charts>
      </div>
    </div>
  </div>
</template>