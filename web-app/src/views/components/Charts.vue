<script setup>
import { ref, onMounted, computed } from 'vue'
import { formatCurrency } from '@/utils/formatters'
import { useReportStore } from '@/store/ReportStore'

const reportStore = useReportStore();
const data = ref([]);

// Tạo mảng tên tháng từ tháng 1 đến tháng hiện tại
const getCurrentMonthCategories = () => {
  const months = ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6',
    'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'];
  const currentMonth = new Date().getMonth(); // 0-11
  return months.slice(0, currentMonth + 1);
};

// Lấy categories động dựa trên tháng hiện tại
const monthCategories = computed(() => getCurrentMonthCategories());

// Gộp options và series thành một đối tượng duy nhất
const incomeExpenseChart = ref({
  series: [
    {
      name: 'Thu nhập',
      data: [] // Sẽ được cập nhật từ API
    },
    {
      name: 'Chi tiêu',
      data: [] // Sẽ được cập nhật từ API
    }
  ],
  options: {
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
      categories: [], // Sẽ được cập nhật động
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
          return formatCurrency(value)
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
  }
});

// Cập nhật danh sách tháng khi component được tạo
onMounted(async () => {
  // Cập nhật xaxis categories với tháng từ tháng 1 đến tháng hiện tại
  incomeExpenseChart.value.options.xaxis.categories = monthCategories.value;
  
  // Khởi tạo mảng dữ liệu với giá trị 0 cho tất cả các tháng
  const currentMonth = new Date().getMonth(); // 0-11
  const revenueData = Array(currentMonth + 1).fill(0);
  const expenseData = Array(currentMonth + 1).fill(0);

  data.value = await reportStore.getReportTransactionType({
    timeOption: "Theo tháng",
    account: null,
    customTimeRange: [
      new Date().getFullYear() + "-" + '01',
      new Date().getFullYear() + "-" + String(new Date().getMonth() + 1).padStart(2, '0')
    ]
  });

  // Xử lý dữ liệu trả về để map vào đúng tháng
  if (data.value && data.value.length > 0) {
    data.value.forEach(item => {
      if (item.time) {
        // Giả sử time có định dạng "MM/YYYY"
        const [month, year] = item.time.split('/');
        const monthIndex = parseInt(month, 10) - 1; // Chuyển từ 1-12 sang 0-11
        
        // Chỉ xử lý dữ liệu của năm hiện tại
        const currentYear = new Date().getFullYear().toString();
        if (year === currentYear && monthIndex >= 0 && monthIndex <= currentMonth) {
          revenueData[monthIndex] = item.totalRevenue || 0;
          expenseData[monthIndex] = item.totalExpense || 0;
        }
      }
    });
  }

  // Cập nhật dữ liệu vào series
  incomeExpenseChart.value.series[0].data = revenueData;
  incomeExpenseChart.value.series[1].data = expenseData;
})
</script>

<template>
  <div class="grid grid-cols-1 gap-4">
    <!-- Income vs Expense Chart -->
    <div class="bg-gray-50 rounded-lg p-4 shadow-sm">
      <h3 class="text-lg font-semibold mb-4">Thu nhập vs Chi tiêu</h3>
      <div class="h-[350px]">
        <apexchart type="bar" height="100%" :options="incomeExpenseChart.options" :series="incomeExpenseChart.series" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.bg-gray-50 {
  background-color: rgb(249, 250, 251);
}
</style>