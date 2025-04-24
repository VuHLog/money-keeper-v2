<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import Header from '@/views/components/Header.vue'
import Sidebar from '@/views/components/Sidebar.vue'

// State management for sidebar and screen size
const route = useRoute()
const isSidebarOpen = ref(true)
const isLargeScreen = ref(window.innerWidth >= 1024)

// Check if route requires authentication
const isAuthenticated = computed(() => {
  return route.meta.requiresAuth
})

// Toggle sidebar visibility
const toggleSidebar = () => {
  isSidebarOpen.value = !isSidebarOpen.value
}

// Close sidebar on mobile when clicking outside
const closeSidebar = () => {
  if (!isLargeScreen.value) {
    isSidebarOpen.value = false
  }
}

// Handle screen resize and update sidebar state
const handleResize = () => {
  isLargeScreen.value = window.innerWidth >= 1024
  if (isLargeScreen.value) {
    isSidebarOpen.value = true
  } else {
    isSidebarOpen.value = false
  }
}

// Lifecycle hooks for resize event listener
onMounted(() => {
  handleResize()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<template>
  <!-- Root container -->
  <div class="min-h-screen bg-background flex">
    <!-- Main layout container when not authenticated -->
    <div v-if="!isAuthenticated" class="flex-1 flex flex-col w-full">
      <!-- Sidebar component with responsive behavior -->
      <Sidebar 
        :is-open="isSidebarOpen" 
        class="fixed inset-y-0 left-0 z-50 transform transition-transform duration-300 ease-in-out"
        :class="{
          '-translate-x-full': !isSidebarOpen && !isLargeScreen,
          'translate-x-0': isSidebarOpen || isLargeScreen
        }"
      />

      <!-- Main content area -->
      <div 
        class="flex-1 flex flex-col w-full transition-all duration-300 ease-in-out"
        :class="{
          'lg:pl-64': isSidebarOpen && isLargeScreen,
          'lg:pl-16': !isSidebarOpen && isLargeScreen
        }"
      >
        <!-- Fixed header with responsive padding -->
        <Header 
          @toggle-sidebar="toggleSidebar"
          class="fixed top-0 right-0 left-0 z-40 transition-all duration-300 ease-in-out"
          :class="{
            'lg:pl-64': isSidebarOpen && isLargeScreen,
            'lg:pl-16': !isSidebarOpen && isLargeScreen
          }"
        />
        
        <!-- Main content with router view -->
        <main class="flex-1 pt-16 w-full bg-background">
          <div class="w-full px-0 sm:px-1">
            <div class="w-full py-4">
              <router-view />
            </div>
          </div>
        </main>
      </div>

      <!-- Mobile overlay for sidebar -->
      <div 
        v-if="isSidebarOpen && !isLargeScreen" 
        @click="closeSidebar"
        class="fixed inset-0 bg-black bg-opacity-50 z-40 transition-opacity duration-300 ease-in-out"
      ></div>
    </div>
    
    <!-- Router view for authenticated routes -->
    <router-view v-else />
  </div>
</template>

<style>
/* Import Tailwind CSS utilities */
@import 'tailwindcss/base';
@import 'tailwindcss/components';
@import 'tailwindcss/utilities';

/* Global styles for root elements */
html, body {
  @apply min-h-screen w-full m-0 p-0 bg-background;
}

/* Base text styles */
body {
  @apply text-text;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

/* App container styles */
#app {
  @apply min-h-screen w-full bg-background;
}

/* Main content area styles */
main {
  @apply w-full bg-background;
}
</style>
