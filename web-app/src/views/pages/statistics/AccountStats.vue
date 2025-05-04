<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useReportStore } from '@stores/ReportStore'
import VueApexCharts from 'vue3-apexcharts'
import FilterOptions from '@components/FilterOptions.vue'
import { formatCurrency } from '@/utils/formatters'
import { colorRepos } from '@constants/ColorRepos'
import Avatar from '@components/Avatar.vue'

const reportStore = useReportStore()
const accountBalanceData = ref([])
const accountBalanceColors = ref([])
const accountTypeBalanceData = ref([])
const accountTypeBalanceColors = ref([])
const filters = ref();


// Sample data - Replace with actual data from your API
const transactionData = ref({
  accountTransactions: {
    'Ví tiền mặt': [1000000, 2000000, 3000000, 4000000, 5000000],
    'Ngân hàng A': [5000000, 6000000, 7000000, 8000000, 10000000],
    'Ngân hàng B': [2000000, 2500000, 3000000, 4000000, 5000000]
  },
})

// Chart options
const accountBalancesChart = ref({
  series: accountBalanceData.value.map((e) => e.balance),
  chart: {
    type: 'donut',
    height: 350
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
  yaxis: {
    labels: {
      show: true,
      formatter: function (val) {
        return formatCurrency(val);
      }
    }
  },
  tooltip: {
    y: {
      formatter: function (val) {
        return formatCurrency(val);
      }
    }
  },
  colors: ['#3B82F6', '#10B981', '#F59E0B'],
})

const accountTypesChart = ref({
  series: accountTypeBalanceData.value.map(type => type.totalBalance),
  chart: {
    type: 'donut',
    height: 350
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

onMounted(async () => {
  // filters.value = {
  //   account: [],
  //   transactionType: 'expense',
  // }
  await getData();
  updateTransactionData();
})

const getData = async () => {
  accountBalanceData.value = await reportStore.getReportBucketPaymentBalance();
  accountTypeBalanceData.value = await reportStore.getReportBucketPaymentTypeBalance();
}

const handleFilterChange = (filterOptions) => {
  filters.value = filterOptions;
  console.log('Filters updated:', filters.value);
}

const handleApplyFilter = async () => {
  await getData();
  updateTransactionData();
}

const updateTransactionData = () => {
  const availableAccountBalanceColors = [...colorRepos];
  accountBalanceData.value.forEach(() => {
    const randomIndex = Math.floor(Math.random() * availableAccountBalanceColors.length);
    const selectedColor = availableAccountBalanceColors[randomIndex];
    accountBalanceColors.value.push(selectedColor);
    availableAccountBalanceColors.splice(randomIndex, 1);
  });
  accountBalancesChart.value.series = accountBalanceData.value.map((e) => e.balance);
  accountBalancesChart.value.labels = accountBalanceData.value.map((e) => e.accountName);
  accountBalancesChart.value = {
    ...accountBalancesChart.value,
    colors: accountBalanceColors.value,
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

  const availableAccountTypeBalanceColors = [...colorRepos];
  accountTypeBalanceData.value.forEach(() => {
    const randomIndex = Math.floor(Math.random() * availableAccountTypeBalanceColors.length);
    const selectedColor = availableAccountTypeBalanceColors[randomIndex];
    accountTypeBalanceColors.value.push(selectedColor);
    availableAccountTypeBalanceColors.splice(randomIndex, 1);
  });
  accountTypesChart.value.series = accountTypeBalanceData.value.map((e) => e.totalBalance);
  accountTypesChart.value.labels = accountTypeBalanceData.value.map((e) => e.accountType);
  accountTypesChart.value = {
    ...accountTypesChart.value,
    colors: accountTypeBalanceColors.value,
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
        <apexchart type="donut" height="350" :options="accountBalancesChart" :series="accountBalancesChart.series" />
      </div>

      <!-- Account Types -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Phân bổ theo loại tài khoản</h3>
        <apexchart type="donut" height="350" :options="accountTypesChart" :series="accountTypesChart.series" />
      </div>

      <!-- Account Transactions -->
      <div class="bg-surface rounded-xl p-4 shadow-sm md:col-span-2">
        <h3 class="text-lg font-medium text-text mb-4">Biến động số dư tài khoản</h3>
        <apexchart type="line" height="350" :options="accountTransactionsChart"
          :series="accountTransactionsChart.series" />
      </div>
    </div>
  </div>
</template>