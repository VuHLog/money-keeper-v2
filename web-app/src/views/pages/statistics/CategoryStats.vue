<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useReportStore } from '@stores/ReportStore'
import VueApexCharts from 'vue3-apexcharts'
import FilterOptions from '@components/FilterOptions.vue'
import { formatCurrency } from '@/utils/formatters'
import Avatar from "@components/Avatar.vue"
import { colorRepos } from '@constants/ColorRepos'


const reportStore = useReportStore()
const expenseCategoryData = ref([])
const revenueCategoryData = ref([])
const filters = ref();
const expenseCategoryColors = ref([]);
const revenueCategoryColors = ref([]);

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

// Chart options for horizontal bar charts
const revenueCategoriesChart = ref({
  series: [{
    name: 'Doanh thu',
    data: []
  }],
  chart: {
    type: 'bar',
    height: 350,
    toolbar: {
      show: false
    }
  },
  plotOptions: {
    bar: {
      horizontal: true,
      barHeight: '70%',
      distributed: true,
      dataLabels: {
        position: 'top'
      }
    }
  },
  dataLabels: {
    enabled: true,
    formatter: function (val) {
      return formatCurrency(val);
    },
    offsetX: 30,
    style: {
      fontSize: '12px',
      colors: ['#333']
    }
  },
  xaxis: {
    categories: [],
    labels: {
      formatter: function (val) {
        return formatCurrency(val);
      }
    }
  },
  yaxis: {
    labels: {
      show: true
    }
  },
  colors: ['#3B82F6', '#10B981', '#F59E0B', '#F97316', '#8B5CF6', '#EC4899'],
  tooltip: {
    y: {
      formatter: function (val) {
        return formatCurrency(val);
      }
    }
  },
  legend: {
    show: false
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

const expenseCategoriesChart = ref({
  series: [{
    name: 'Chi tiêu',
    data: []
  }],
  chart: {
    type: 'bar',
    height: 350,
    toolbar: {
      show: false
    }
  },
  plotOptions: {
    bar: {
      horizontal: true,
      barHeight: '70%',
      distributed: true,
      dataLabels: {
        position: 'top'
      }
    }
  },
  dataLabels: {
    enabled: true,
    formatter: function (val) {
      return formatCurrency(val);
    },
    offsetX: 30,
    style: {
      fontSize: '12px',
      colors: ['#333']
    }
  },
  xaxis: {
    categories: [],
    labels: {
      formatter: function (val) {
        return formatCurrency(val);
      }
    }
  },
  yaxis: {
    labels: {
      show: true,
    }
  },
  colors: ['#EF4444', '#F59E0B', '#10B981', '#3B82F6', '#8B5CF6', '#EC4899'],
  tooltip: {
    y: {
      formatter: function (val) {
        return formatCurrency(val);
      }
    }
  },
  legend: {
    show: false
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

const expenseCategoryTrendsChart = ref({
  series: [],
  chart: {
    type: 'line',
    height: 350,
    stacked: false,
    toolbar: {
      show: true
    }
  },
  stroke: {
    curve: 'smooth',
    width: 3
  },
  xaxis: {
    categories: [],
  },
  yaxis: {
    labels: {
      show: true,
      formatter: function (val) {
        return formatCurrency(val);
      }
    }
  },
  colors: ['#3B82F6', '#10B981', '#F59E0B', '#F97316', '#8B5CF6', '#EC4899'],
  tooltip: {
    y: {
      formatter: function (val) {
        return formatCurrency(val);
      }
    }
  },
  legend: {
    position: 'top'
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

const revenueCategoryTrendsChart = ref({
  series: [],
  chart: {
    type: 'line',
    height: 350,
    stacked: false,
    toolbar: {
      show: true
    }
  },
  stroke: {
    curve: 'smooth',
    width: 3
  },
  xaxis: {
    categories: [],
  },
  yaxis: {
    labels: {
      show: true,
      formatter: function (val) {
        return formatCurrency(val);
      }
    }
  },
  colors: ['#3B82F6', '#10B981', '#F59E0B', '#F97316', '#8B5CF6', '#EC4899'],
  tooltip: {
    y: {
      formatter: function (val) {
        return formatCurrency(val);
      }
    }
  },
  legend: {
    position: 'top'
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

watch(chartCategories, (newCategories) => {
  expenseCategoryTrendsChart.value.xaxis = { categories: newCategories };
  revenueCategoryTrendsChart.value.xaxis = { categories: newCategories };
}, { deep: true });

onMounted(async () => {
  let currentDate = new Date();
  filters.value = {
    timeOption: "Theo tháng",
    account: [],
    expenseCategory: [],
    revenueCategory: [],
    customTimeRange: [currentDate.toISOString().slice(0, 7), currentDate.toISOString().slice(0, 7)],
  }
  await getData();
  updateTransactionData();
})

const getData = async () => {
  expenseCategoryData.value = await reportStore.getReportExpenseCategory(filters.value);
  expenseCategoryData.value = expenseCategoryData.value.map(item => {
    return {
      ...item,
      time: filters.value.timeOption === "Tùy chọn" ?formatReverseStringDate(item.time).replace(/-/g, "/") : item.time.replace(/-/g, "/"),
    }
  });
  revenueCategoryData.value = await reportStore.getReportRevenueCategory(filters.value);
  revenueCategoryData.value = revenueCategoryData.value.map(item => {
    return {
      ...item,
      time: filters.value.timeOption === "Tùy chọn" ?formatReverseStringDate(item.time).replace(/-/g, "/") : item.time.replace(/-/g, "/"),
    }
  });
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
  let expenseCategorySet = new Set(
    expenseCategoryData.value.map(item =>
      JSON.stringify({
        categoryKey: `${item.categoryId}_${item.categoryName}`,
        categoryId: item.categoryId,
        categoryName: item.categoryName,
        iconUrl: item.iconUrl
      })
    )
  );

  let revenueCategorySet = new Set(
    revenueCategoryData.value.map(item =>
      JSON.stringify({
        categoryKey: `${item.categoryId}_${item.categoryName}`,
        categoryId: item.categoryId,
        categoryName: item.categoryName,
        iconUrl: item.iconUrl
      })
    )
  );

  const availableExpenseColors = [...colorRepos];
  expenseCategorySet.forEach(() => {
    const randomIndex = Math.floor(Math.random() * availableExpenseColors.length);
    const selectedColor = availableExpenseColors[randomIndex];
    expenseCategoryColors.value.push(selectedColor);
    availableExpenseColors.splice(randomIndex, 1);
  });

  const availableRevenueColors = [...colorRepos];
  revenueCategorySet.forEach(() => {
    const randomIndex = Math.floor(Math.random() * availableRevenueColors.length);
    const selectedColor = availableRevenueColors[randomIndex];
    revenueCategoryColors.value.push(selectedColor);
    availableRevenueColors.splice(randomIndex, 1);
  });


  
  let revenueCategoryWithoutTime = Array.from(
    revenueCategoryData.value.reduce((acc, item) => {
      const { categoryId, categoryName, iconUrl, totalRevenue } = item;
      if (!acc.has(categoryId)) {
        acc.set(categoryId, { categoryId, categoryName, iconUrl, totalRevenue });
      } else {
        acc.get(categoryId).totalRevenue += totalRevenue;
      }
      return acc;
    }, new Map()).values()
  );
  revenueCategoriesChart.value.series[0].data = revenueCategoryWithoutTime.map((e) => e.totalRevenue);
  revenueCategoriesChart.value = {
    ...revenueCategoriesChart.value,
    xaxis: {
      categories: revenueCategoryWithoutTime.map(e => e.categoryName),
    },
    colors: revenueCategoryColors.value,
  };

  let expenseCategoryWithoutTime = Array.from(
    expenseCategoryData.value.reduce((acc, item) => {
      const { categoryId, categoryName, iconUrl, totalExpense } = item;
      if (!acc.has(categoryId)) {
        acc.set(categoryId, { categoryId, categoryName, iconUrl, totalExpense });
      } else {
        acc.get(categoryId).totalExpense += totalExpense;
      }
      return acc;
    }, new Map()).values()
  );
  expenseCategoriesChart.value.series[0].data = expenseCategoryWithoutTime.map((e) => e.totalExpense);
  expenseCategoriesChart.value = {
    ...expenseCategoriesChart.value,
    xaxis: {
      categories: expenseCategoryWithoutTime.map(e => e.categoryName),
    },
    colors: expenseCategoryColors.value,
  };
  console.log(expenseCategoriesChart.value);

  let expenseCategoryTrends = {};
  chartCategories.value.forEach((time) => {
    expenseCategorySet.forEach((cStr) => {
      const c = JSON.parse(cStr);
      let dataItem = expenseCategoryData.value.find((item) =>
        time === item.time && c.categoryId === item.categoryId
      );
      let value = dataItem ? dataItem.totalExpense : 0;

      if (!expenseCategoryTrends[c.categoryKey]) {
        expenseCategoryTrends[c.categoryKey] = {
          name: c.categoryName,
          data: [],
          iconUrl: c.iconUrl,
        };
      }

      expenseCategoryTrends[c.categoryKey].data.push(value);
    });
  });
  expenseCategoryTrendsChart.value.series = Object.values(expenseCategoryTrends);
  const expenseSeriesMetaData = Object.values(expenseCategoryTrends).map(({ name, iconUrl }) => ({
    name,
    iconUrl
  }))
  expenseCategoryTrendsChart.value = {
    ...expenseCategoryTrendsChart.value,
    xaxis: {
      categories: chartCategories.value
    },
    colors: expenseCategoryColors.value,
  };



  let revenueCategoryTrends = {};
  chartCategories.value.forEach((time) => {
    revenueCategorySet.forEach((cStr) => {
      const c = JSON.parse(cStr);
      let dataItem = revenueCategoryData.value.find((item) =>
        time === item.time && c.categoryId === item.categoryId
      );
      let value = dataItem ? dataItem.totalRevenue : 0;

      if (!revenueCategoryTrends[c.categoryKey]) {
        revenueCategoryTrends[c.categoryKey] = {
          name: c.categoryName,
          data: [],
          iconUrl: c.iconUrl,
        };
      }

      revenueCategoryTrends[c.categoryKey].data.push(value);
    });
  });
  revenueCategoryTrendsChart.value.series = Object.values(revenueCategoryTrends);
  const revenueSeriesMetaData = Object.values(revenueCategoryTrends).map(({ name, iconUrl }) => ({
    name,
    iconUrl
  }))
  revenueCategoryTrendsChart.value = {
    ...revenueCategoryTrendsChart.value,
    xaxis: {
      categories: chartCategories.value
    },
    colors: revenueCategoryColors.value,
  };
}

</script>

<template>
  <div>
    <!-- Filter Options -->
    <FilterOptions :show-transaction-type="false" :show-time-range="true" :show-account="true" :show-category="true"
      @filter-change="handleFilterChange" @apply-filter="handleApplyFilter" />

    <!-- Charts -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <!-- Income Categories -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Phân bổ thu nhập</h3>
        <apexchart type="bar" height="350" :options="revenueCategoriesChart" :series="revenueCategoriesChart.series" />
      </div>

      <!-- Expense Categories -->
      <div class="bg-surface rounded-xl p-4 shadow-sm">
        <h3 class="text-lg font-medium text-text mb-4">Phân bổ chi tiêu</h3>
        <apexchart type="bar" height="350" :options="expenseCategoriesChart" :series="expenseCategoriesChart.series" />
      </div>

      <!-- Category Trends -->
      <div class="bg-surface rounded-xl p-4 shadow-sm md:col-span-2">
        <h3 class="text-lg font-medium text-text mb-4">Xu hướng chi tiêu theo danh mục thu</h3>
        <apexchart type="line" height="350" :options="revenueCategoryTrendsChart"
          :series="revenueCategoryTrendsChart.series" />
      </div>

      <div class="bg-surface rounded-xl p-4 shadow-sm md:col-span-2">
        <h3 class="text-lg font-medium text-text mb-4">Xu hướng chi tiêu theo danh mục chi</h3>
        <apexchart type="line" height="350" :options="expenseCategoryTrendsChart"
          :series="expenseCategoryTrendsChart.series" />
      </div>
    </div>
  </div>
</template>