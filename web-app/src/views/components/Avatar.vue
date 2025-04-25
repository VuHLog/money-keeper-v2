<script setup>
import { computed } from 'vue';

const props = defineProps({
  src: {
    type: String,
    default: ''
  },
  alt: {
    type: String,
    default: 'Avatar'
  },
  size: {
    type: String,
    default: 'md'
  },
  customClass: {
    type: String,
    default: ''
  }
});

// Tính toán kích thước dựa trên prop size
const avatarSize = computed(() => {
  switch (props.size) {
    case 'm': return 'w-6 h-6';
    case 'sm': return 'w-8 h-8';
    case 'lg': return 'w-12 h-12';
    case 'xl': return 'w-16 h-16';
    default: return 'w-10 h-10'; // Medium (default)
  }
});
</script>

<template>
  <div :class="['rounded-full overflow-hidden flex items-center justify-center bg-gray-100', avatarSize, customClass]">
    <img 
      v-if="src" 
      :src="src" 
      :alt="alt" 
      class="w-full h-full object-cover"
      @error="$event.target.style.display = 'none'"
    />
    <span 
      v-else
      class="text-gray-500 font-medium text-center"
    >
      {{ alt.substring(0, 2).toUpperCase() }}
    </span>
  </div>
</template>