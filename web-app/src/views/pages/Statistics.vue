<script setup>
import { ref } from 'vue'
import TransactionTypeStats from './statistics/TransactionTypeStats.vue'
import CategoryStats from './statistics/CategoryStats.vue'
import TrendStats from './statistics/TrendingStats.vue'
import AccountStats from './statistics/AccountStats.vue'

const activeTab = ref('transaction')

const tabs = [
  { id: 'transaction', name: 'Thời gian' },
  { id: 'category', name: 'Danh mục' },
  { id: 'trend', name: 'Xu hướng' },
  { id: 'account', name: 'Tài khoản' },
]

const changeTab = (tabId) => {
  activeTab.value = tabId
}
</script>

<template>
  <div class="p-4">
    
    <!-- Tabs -->
    <div class="bg-surface rounded-xl p-4 shadow-sm mb-6">
      <div>
        <div class="flex space-x-4">
          <button 
            v-for="tab in tabs" 
            :key="tab.id"
            @click="changeTab(tab.id)"
            class="py-2 px-4 border-b-2 transition-colors"
            :class="[
              activeTab === tab.id 
                ? 'border-primary text-primary font-medium' 
                : 'border-transparent text-text-secondary hover:text-text hover:border-gray-300'
            ]"
          >
            {{ tab.name }}
          </button>
        </div>
      </div>
    </div>

    <!-- Tab Content -->
    <div class="bg-surface rounded-xl p-4 shadow-sm">
      <div v-if="activeTab === 'transaction'">
        <TransactionTypeStats />
      </div>
      
      <div v-if="activeTab === 'category'">
        <CategoryStats />
      </div>
      
      <div v-if="activeTab === 'trend'">
        <TrendStats />
      </div>
      
      <div v-if="activeTab === 'account'">
        <AccountStats />
      </div>
    </div>
  </div>
</template> 