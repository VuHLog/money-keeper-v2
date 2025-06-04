<script setup>
import { ref } from 'vue';
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { library } from '@fortawesome/fontawesome-svg-core';
import { faChartLine, faWallet, faTags, faBalanceScale, faHistory, faListAlt } from '@fortawesome/free-solid-svg-icons';
import IncomeExpenseReport from './report/IncomeExpenseReport.vue';
import AccountReport from './report/AccountReport.vue';
import CategoryReport from './report/CategoryReport.vue';
import SpendingLimitControlReport from './report/SpendingLimitControlReport.vue';

// Register Font Awesome icons
library.add(faChartLine, faWallet, faTags, faBalanceScale, faHistory, faListAlt);

const selectedReport = ref('IncomeExpenseReport');

const reports = [
    { title: 'Báo cáo thu chi', component: 'IncomeExpenseReport', icon: ['fas', 'chart-line'] },
    { title: 'Báo cáo theo tài khoản', component: 'AccountReport', icon: ['fas', 'wallet'] },
    { title: 'Báo cáo theo danh mục', component: 'CategoryReport', icon: ['fas', 'tags'] },
    { title: 'Báo cáo kiểm soát hạn mức chi tiêu', component: 'SpendingLimitControlReport', icon: ['fas', 'balance-scale'] },
];

// Map component names to actual component objects
const componentMap = {
    IncomeExpenseReport,
    AccountReport,
    CategoryReport,
    SpendingLimitControlReport,
};
</script>

<template>
    <div class="flex h-full">
        <div class="w-1/4 border-r border-gray-200 bg-white p-4">
            <ul class="list-none p-0 m-0">
                <li v-for="(report, index) in reports" 
                    :key="index" 
                    @click="selectedReport = report.component" 
                    :class="[
                        'py-4 px-4 cursor-pointer flex items-center border-b border-gray-100 transition-all duration-200 text-[#c56cf0]',
                        selectedReport === report.component 
                            ? 'bg-purple-50 font-bold border-l-4 border-l-purple-500 pl-3' 
                            : 'hover:bg-purple-50'
                    ]"
                >
                    <font-awesome-icon :icon="report.icon" class="mr-3 text-[#c56cf0]" /> 
                    {{ report.title }}
                </li>
            </ul>
        </div>
        <div class="flex-grow w-3/4 px-5 bg-gray-100">
            <transition name="fade" mode="out-in">
                <component :is="componentMap[selectedReport]" :key="selectedReport" />
            </transition>
        </div>
    </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>