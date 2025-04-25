<script setup>
import { ref } from 'vue'
import OverviewStats from './OverviewStats.vue'
import CategoryStats from './CategoryStats.vue'
import TimeStats from './TimeStats.vue'
import AccountStats from './AccountStats.vue'
import DebtStats from './DebtStats.vue'
import FilterOptions from '@components/FilterOptions.vue'

const activeTab = ref('overview')
const tabs = [
  { id: 'overview', label: 'Tổng quan' },
  { id: 'category', label: 'Danh mục' },
  { id: 'time', label: 'Thời gian' },
  { id: 'account', label: 'Tài khoản' },
  { id: 'debt', label: 'Khoản nợ' }
]

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

    <!-- Tabs -->
    <div class="mb-6">
      <div class="border-b border-border">
        <nav class="-mb-px flex space-x-8" aria-label="Tabs">
          <button
            v-for="tab in tabs"
            :key="tab.id"
            @click="activeTab = tab.id"
            :class="[
              activeTab === tab.id
                ? 'border-primary text-primary'
                : 'border-transparent text-text-secondary hover:text-text hover:border-border',
              'whitespace-nowrap py-4 px-1 border-b-2 font-medium text-sm'
            ]"
          >
            {{ tab.label }}
          </button>
        </nav>
      </div>
    </div>

    <!-- Tab Content -->
    <div>
      <OverviewStats v-if="activeTab === 'overview'" />
      <CategoryStats v-if="activeTab === 'category'" />
      <TimeStats v-if="activeTab === 'time'" />
      <AccountStats v-if="activeTab === 'account'" />
      <DebtStats v-if="activeTab === 'debt'" />
    </div>
  </div>
</template> 