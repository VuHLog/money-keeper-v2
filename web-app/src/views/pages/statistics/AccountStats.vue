<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useReportStore } from '@stores/ReportStore'
import VueApexCharts from 'vue3-apexcharts'
import FilterOptions from '@components/FilterOptions.vue'
import { formatCurrency, formatReverseStringDate } from '@/utils/formatters'
import { colorRepos } from '@constants/ColorRepos'
import Avatar from '@components/Avatar.vue'

const reportStore = useReportStore()
const accountBalanceData = ref([])
const accountBalanceColors = ref([])
const accountTypeBalanceData = ref([])
const accountTypeBalanceColors = ref([])
const accountBalanceFluctuationData = ref([])
const filters = ref();
// Add showCharts variable to control chart visibility
const showCharts = ref(false);

// Chart categories based on time filters
const chartCategories = computed(() => generateCategories());

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

// Chart options
const accountBalancesChart = ref({
  series: accountBalanceData.value.map((e) => e.balance),
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
  labels: accountBalanceData.value.map((e) => e.accountName),
  colors: [],
  legend: {
    position: 'bottom'
  },
  tooltip: {
    y: {
      formatter: function (val) {
        return formatCurrency(val);
      }
    }
  },
  stroke: {
    show: false
  },
})

const accountTransactionsChart = ref({
  series: [],
  chart: {
    type: 'line',
    height: 350,
    stacked: false,
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
    categories: []
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
  colors: [],
})

const accountTypesChart = ref({
  series: accountTypeBalanceData.value.map(type => type.totalBalance),
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
  labels: accountTypeBalanceData.value.map(type => type.accountType),
  colors: [],
  legend: {
    position: 'bottom'
  },
  tooltip: {
    y: {
      formatter: function (val) {
        return formatCurrency(val);
      }
    }
  },
  stroke: {
    show: false
  },
})

watch(chartCategories, (newCategories) => {
  accountTransactionsChart.value.xaxis = { categories: newCategories };
}, { deep: true });

onMounted(async () => {
  let currentDate = new Date();
  // Tạo đúng ngày 1 tháng 1 năm hiện tại
  let startDate = new Date(currentDate.getFullYear() + '-01-01');
  
  filters.value = {
    timeOption: "Theo tháng",
    account: [],
    customTimeRange: [
      startDate.toISOString().slice(0, 7), 
      currentDate.toISOString().slice(0, 7)
    ],
  }

  await getData();
  updateTransactionData();
  
  // Show charts with animation after a delay
  setTimeout(() => {
    showCharts.value = true;
  }, 500);
})

const getData = async () => {
  accountBalanceData.value = await reportStore.getReportBucketPaymentBalance();
  accountTypeBalanceData.value = await reportStore.getReportBucketPaymentTypeBalance();
  accountBalanceFluctuationData.value = await reportStore.getAccountBalanceFluctuation(filters.value);
  
  // Định dạng dữ liệu thời gian nếu cần
  accountBalanceFluctuationData.value = accountBalanceFluctuationData.value.map(item => {
    return {
      ...item,
      time: filters.value.timeOption === "Tùy chọn" ? formatReverseStringDate(item.time).replace(/-/g, "/") : item.time.replace(/-/g, "/"),
    }
  });
}

const handleFilterChange = (filterOptions) => {
  filters.value = filterOptions;
  console.log('Filters updated:', filters.value);
}

const handleApplyFilter = async () => {
  // Hide charts before updating data
  showCharts.value = false;
  
  await getData();
  updateTransactionData();
  
  // Show charts again after a delay to trigger animations
  setTimeout(() => {
    showCharts.value = true;
  }, 300);
}

// Add preserveChartSettings function
const preserveChartSettings = (originalChart) => {
  const { chart: chartSettings } = originalChart;
  return {
    animations: chartSettings.animations
  };
};

const updateTransactionData = () => {
  // Reset color arrays
  accountBalanceColors.value = [];
  accountTypeBalanceColors.value = [];
  
  // Assign colors for account balances
  const availableAccountBalanceColors = [...colorRepos];
  accountBalanceData.value.forEach(() => {
    const randomIndex = Math.floor(Math.random() * availableAccountBalanceColors.length);
    const selectedColor = availableAccountBalanceColors[randomIndex];
    accountBalanceColors.value.push(selectedColor);
    availableAccountBalanceColors.splice(randomIndex, 1);
  });
  
  // Preserve account balances chart animations
  const accountBalancesChartSettings = preserveChartSettings(accountBalancesChart.value);
  accountBalancesChart.value.series = accountBalanceData.value.map((e) => e.balance);
  accountBalancesChart.value.labels = accountBalanceData.value.map((e) => e.accountName);
  accountBalancesChart.value = {
    ...accountBalancesChart.value,
    colors: accountBalanceColors.value,
    chart: {
      ...accountBalancesChart.value.chart,
      ...accountBalancesChartSettings.animations
    },
    tooltip: {
      custom: function ({ series, seriesIndex, dataPointIndex, w }) {
        return `<div class="flex items-center py-1 px-2" style="background: ${accountBalanceColors.value[seriesIndex]}">
                  <div class="rounded-full overflow-hidden flex items-center justify-center bg-gray-100 h-6 w-6 mr-2">
                    <img
                      src="${accountBalanceData.value[seriesIndex].iconUrl}" 
                      alt="${accountBalanceData.value[seriesIndex].accountName}" 
                      class="w-full h-full object-cover"
                      @error="$event.target.style.display = 'none'"
                    />
                  </div>
                  <span class="">${accountBalanceData.value[seriesIndex].accountName}: <strong class="text-sm">${formatCurrency(series[seriesIndex])}</strong></span>
                </div>`
      }
    }
  };

  // Assign colors for account types
  const availableAccountTypeBalanceColors = [...colorRepos];
  accountTypeBalanceData.value.forEach(() => {
    const randomIndex = Math.floor(Math.random() * availableAccountTypeBalanceColors.length);
    const selectedColor = availableAccountTypeBalanceColors[randomIndex];
    accountTypeBalanceColors.value.push(selectedColor);
    availableAccountTypeBalanceColors.splice(randomIndex, 1);
  });
  
  // Preserve account types chart animations
  const accountTypesChartSettings = preserveChartSettings(accountTypesChart.value);
  accountTypesChart.value.series = accountTypeBalanceData.value.map((e) => e.totalBalance);
  accountTypesChart.value.labels = accountTypeBalanceData.value.map((e) => e.accountType);
  accountTypesChart.value = {
    ...accountTypesChart.value,
    colors: accountTypeBalanceColors.value,
    chart: {
      ...accountTypesChart.value.chart,
      ...accountTypesChartSettings.animations
    },
    tooltip: {
      custom: function ({ series, seriesIndex, dataPointIndex, w }) {
        return `<div class="flex items-center py-1 px-2" style="background: ${accountTypeBalanceColors.value[seriesIndex]}">
                  <div class="rounded-full overflow-hidden flex items-center justify-center bg-gray-100 h-6 w-6 mr-2">
                    <img
                      src="${accountTypeBalanceData.value[seriesIndex].iconUrl}" 
                      alt="${accountTypeBalanceData.value[seriesIndex].accountType}" 
                      class="w-full h-full object-cover"
                      @error="$event.target.style.display = 'none'"
                    />
                  </div>
                  <span class="">${accountTypeBalanceData.value[seriesIndex].accountType}: <strong class="text-sm">${formatCurrency(series[seriesIndex])}</strong></span>
                </div>`
      }
    }
  };
  
  // Xử lý dữ liệu biến động số dư tài khoản
  // Lấy danh sách các tài khoản
  let accountSet = new Set(
    accountBalanceFluctuationData.value.map(item =>
      JSON.stringify({
        accountKey: `${item.bucketPaymentId}_${item.accountName}`,
        bucketPaymentId: item.bucketPaymentId,
        accountName: item.accountName
      })
    )
  );
  
  let accountTransactions = {};

  // Khởi tạo giá trị đầu tiên cho mỗi tài khoản
  accountSet.forEach((aStr) => {
    const a = JSON.parse(aStr);
    accountTransactions[a.accountKey] = {
      name: a.accountName,
      data: [],
      lastValue: 0
    };
  });
  
  chartCategories.value.forEach((time, timeIndex) => {
    accountSet.forEach((aStr) => {
      const a = JSON.parse(aStr);
      let dataItem = accountBalanceFluctuationData.value.find((item) =>
        time === item.time && a.bucketPaymentId === item.bucketPaymentId
      );
      
      if (dataItem) {
        accountTransactions[a.accountKey].lastValue = dataItem.balanceAfterTransaction;
      }
      
      accountTransactions[a.accountKey].data.push(accountTransactions[a.accountKey].lastValue);
    });
  });
  
  // Loại bỏ thuộc tính lastValue trước khi gán vào biểu đồ
  Object.keys(accountTransactions).forEach(key => {
    delete accountTransactions[key].lastValue;
  });
  
  // Preserve account transactions chart animations
  const accountTransactionsChartSettings = preserveChartSettings(accountTransactionsChart.value);
  
  let accountTransactionsColors = [];
  const availableTransactionColors = [...colorRepos];
  
  Object.keys(accountTransactions).forEach(() => {
    const randomIndex = Math.floor(Math.random() * availableTransactionColors.length);
    const selectedColor = availableTransactionColors[randomIndex];
    accountTransactionsColors.push(selectedColor);
    availableTransactionColors.splice(randomIndex, 1);
  });
  
  // Cập nhật biểu đồ
  accountTransactionsChart.value = {
    ...accountTransactionsChart.value,
    series: Object.values(accountTransactions),
    xaxis: {
      categories: chartCategories.value
    },
    colors: accountTransactionsColors,
    chart: {
      ...accountTransactionsChart.value.chart,
      ...accountTransactionsChartSettings.animations
    }
  };
}
</script>

<template>
  <div>
    <!-- Filter Options -->
    <FilterOptions :show-time-range="true" :show-account="false" :show-category="false" :show-transaction-type="false"
      :show-expense-category="false" :show-revenue-category="false" @filter-change="handleFilterChange"
      @apply-filter="handleApplyFilter" />

    <!-- Charts -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <!-- Account Balances -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Số dư tài khoản</h3>
        <apexchart v-if="showCharts" type="donut" height="350" :options="accountBalancesChart" :series="accountBalancesChart.series" />
      </div>

      <!-- Account Types -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Phân bổ theo loại tài khoản</h3>
        <apexchart v-if="showCharts" type="donut" height="350" :options="accountTypesChart" :series="accountTypesChart.series" />
      </div>

      <!-- Account Transactions -->
      <div class="bg-surface rounded-xl p-4 shadow-sm md:col-span-2">
        <h3 class="text-lg font-medium text-text mb-4">Biến động số dư tài khoản</h3>
        <apexchart v-if="showCharts" type="line" height="350" :options="accountTransactionsChart"
          :series="accountTransactionsChart.series" />
      </div>
    </div>
  </div>
</template>