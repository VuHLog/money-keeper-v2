<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import Header from '@/views/components/Header.vue'
import Sidebar from '@/views/components/Sidebar.vue'

const route = useRoute()
const isSidebarOpen = ref(true)
const isLargeScreen = ref(window.innerWidth >= 1024)

const isAuthenticated = computed(() => {
  return route.meta.requiresAuth
})

const toggleSidebar = () => {
  isSidebarOpen.value = !isSidebarOpen.value
}

const closeSidebar = () => {
  if (!isLargeScreen.value) {
    isSidebarOpen.value = false
  }
}

const handleResize = () => {
  isLargeScreen.value = window.innerWidth >= 1024
  if (isLargeScreen.value) {
    isSidebarOpen.value = true
  } else {
    isSidebarOpen.value = false
  }
}

onMounted(() => {
  handleResize()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<template>
  <div class="h-full flex min-h-screen bg-background">
    <div v-if="!isAuthenticated" class="flex-1 flex flex-col h-full">
      <!-- Sidebar -->
      <Sidebar 
        :is-open="isSidebarOpen" 
        class="fixed inset-y-0 left-0 z-50 transform transition-transform duration-300 ease-in-out"
        :class="{
          '-translate-x-full': !isSidebarOpen && !isLargeScreen,
          'translate-x-0': isSidebarOpen || isLargeScreen
        }"
      />

      <!-- Main Content -->
      <div 
        class="flex-1 flex flex-col h-full w-full transition-all duration-300 ease-in-out"
        :class="{
          'lg:pl-64': isSidebarOpen && isLargeScreen,
          'lg:pl-16': !isSidebarOpen && isLargeScreen
        }"
      >
        <Header 
          @toggle-sidebar="toggleSidebar"
          class="fixed top-0 right-0 left-0 z-40 transition-all duration-300 ease-in-out"
          :class="{
            'lg:pl-64': isSidebarOpen && isLargeScreen,
            'lg:pl-16': !isSidebarOpen && isLargeScreen
          }"
        />
        
        <main class="flex-1 pt-16 min-h-full bg-background w-full">
          <div class="h-full w-full px-4 sm:px-6 lg:px-8">
            <div class="w-full max-w-full py-4">
              <router-view />
            </div>
          </div>
        </main>
      </div>

      <!-- Overlay for mobile -->
      <div 
        v-if="isSidebarOpen && !isLargeScreen" 
        @click="closeSidebar"
        class="fixed inset-0 bg-black bg-opacity-50 z-40 transition-opacity duration-300 ease-in-out"
      ></div>
    </div>
    
    <router-view v-else />
  </div>
</template>

<style>
@import 'tailwindcss/base';
@import 'tailwindcss/components';
@import 'tailwindcss/utilities';

html, body {
  @apply h-full m-0 p-0;
}

body {
  @apply bg-background text-text;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

#app {
  @apply h-full;
}

main {
  min-height: calc(100vh - 4rem);
}
</style>
