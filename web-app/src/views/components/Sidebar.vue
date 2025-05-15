<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { library } from '@fortawesome/fontawesome-svg-core'
import { 
  faChartLine, 
  faWallet, 
  faPlusCircle, 
  faMinusCircle, 
  faTags, 
  faChartBar, 
  faHistory, 
  faUser,
  faArrowUp,
  faArrowDown,
  faPiggyBank,
  faUtensils,
  faShoppingBag,
  faMoneyBillWave,
  faFileAlt
} from '@fortawesome/free-solid-svg-icons'
import logoFull from '@/assets/img/logo/logo.png'
import logoIcon from '@/assets/img/logo/logo.png'

// Add icons to library
library.add(
  faChartLine,
  faWallet,
  faPlusCircle,
  faMinusCircle,
  faTags,
  faChartBar,
  faHistory,
  faUser,
  faArrowUp,
  faArrowDown,
  faPiggyBank,
  faUtensils,
  faShoppingBag,
  faMoneyBillWave,
  faFileAlt
)

const props = defineProps({
  isOpen: {
    type: Boolean,
    required: true
  }
})

const route = useRoute()

const menuItems = [
  {
    label: 'Dashboard',
    path: '/dashboard',
    icon: ['fas', 'chart-line']
  },
  {
    label: 'Tài khoản',
    path: '/accounts',
    icon: ['fas', 'wallet']
  },
  {
    label: 'Ghi Thu',
    path: '/income',
    icon: ['fas', 'plus-circle']
  },
  {
    label: 'Ghi Chi',
    path: '/expense',
    icon: ['fas', 'minus-circle']
  },
  {
    label: 'Hạn Mức Chi',
    path: '/spending-limits',
    icon: ['fas', 'money-bill-wave']
  },
  {
    label: 'Danh Mục',
    path: '/categories',
    icon: ['fas', 'tags']
  },
  {
    label: 'Lịch Sử Giao Dịch',
    path: '/transaction-history',
    icon: ['fas', 'history']
  },
  {
    label: 'Thống Kê',
    path: '/statistics',
    icon: ['fas', 'chart-bar']
  },
  {
    label: 'Báo cáo',
    path: '/report',
    icon: ['fas', 'file-alt']
  },
  {
    label: 'Thông Tin Người Dùng',
    path: '/profile',
    icon: ['fas', 'user']
  }
]

const isActive = (path) => {
  return route.path === path
}
</script>

<template>
  <aside 
    class="fixed top-0 bottom-0 left-0 bg-surface border-r border-gray-200 z-30 transition-all duration-300 overflow-x-hidden"
    :class="[
      isOpen ? 'w-64' : 'w-16',
      'shadow-lg lg:shadow-none'
    ]"
  >
    <div class="h-full flex flex-col">
      <!-- Logo -->
      <div class="p-4 border-b border-gray-200">
        <router-link to="/dashboard" class="flex items-center" :class="{ 'justify-center': !isOpen }">
          <img 
            :src="isOpen ? logoFull : logoIcon" 
            :alt="isOpen ? 'Money Keeper Logo' : 'MK'" 
            class="h-8 w-auto"
          />
          <span v-if="isOpen" class="ml-2 text-lg font-semibold gradient-text">Sổ thu chi</span>
        </router-link>
      </div>

      <!-- Navigation Menu -->
      <nav class="flex-1 overflow-y-auto overflow-x-hidden py-4 px-2">
        <div class="space-y-1">
          <router-link 
            v-for="item in menuItems" 
            :key="item.path"
            :to="item.path"
            class="flex items-center relative px-3 py-2.5 rounded-lg text-text-secondary hover:bg-primary/5 hover:text-primary group transition-colors duration-200"
            :class="{ 
              'bg-primary/10 !text-primary font-medium': isActive(item.path),
              'justify-center': !isOpen
            }"
          >
            <font-awesome-icon 
              :icon="item.icon" 
              class="text-xl transition-colors duration-200" 
              :class="{ 
                'mr-3': isOpen,
                'text-primary': isActive(item.path)
              }" 
            />
            <span 
              v-if="isOpen" 
              class="text-base transition-colors duration-200"
              :class="{ 'font-medium': isActive(item.path) }"
            >{{ item.label }}</span>
            
            <!-- Tooltip when collapsed -->
            <div
              v-if="!isOpen"
              class="absolute left-full ml-2 px-2 py-1 bg-gray-800 text-white text-xs rounded opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 whitespace-nowrap z-50"
            >
              {{ item.label }}
            </div>
          </router-link>
        </div>
      </nav>
    </div>
  </aside>
</template>

<style scoped>
aside {
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

/* Hide scrollbar for Chrome, Safari and Opera */
::-webkit-scrollbar {
  display: none;
}

/* Hide scrollbar for IE, Edge and Firefox */
* {
  -ms-overflow-style: none;  /* IE and Edge */
  scrollbar-width: none;  /* Firefox */
}

.gradient-text {
  background: linear-gradient(90deg, #4F46E5 0%, #06B6D4 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  color: transparent;
}
</style>