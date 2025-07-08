<template>
  <div class="modal-fix-demo">
    <div class="container mx-auto px-4 py-8">
      <div class="max-w-4xl mx-auto">
        <h1 class="text-3xl font-bold text-center mb-8 text-gray-800">
          Modal Flickering Fix Demonstration
        </h1>
        
        <div class="bg-blue-50 border border-blue-200 rounded-lg p-6 mb-8">
          <h2 class="text-xl font-semibold text-blue-800 mb-4">Problem Solved</h2>
          <p class="text-blue-700 mb-4">
            This demonstration shows how the modal flickering issue has been resolved using a centralized modal state manager.
          </p>
          <div class="bg-white p-4 rounded border-l-4 border-blue-400">
            <h3 class="font-semibold text-gray-800 mb-2">Before (Flickering Issue):</h3>
            <ol class="list-decimal list-inside text-sm text-gray-600 space-y-1">
              <li>User clicks upload button → isConfirmModal opens</li>
              <li>User clicks submit in isConfirmModal → submitRegist() is called</li>
              <li>If duplicate files found → isConfirmModal closes AND isConfirmModalExistFileName opens</li>
              <li>Both modals briefly flicker during transition</li>
            </ol>
          </div>
          <div class="bg-white p-4 rounded border-l-4 border-green-400 mt-4">
            <h3 class="font-semibold text-gray-800 mb-2">After (Fixed):</h3>
            <ol class="list-decimal list-inside text-sm text-gray-600 space-y-1">
              <li>Modal state manager controls all modal visibility</li>
              <li>Smooth transition between modals with controlled timing</li>
              <li>No flickering - only one modal visible at a time</li>
              <li>Proper state isolation and transition queuing</li>
            </ol>
          </div>
        </div>
        
        <!-- Comparison Section -->
        <div class="grid md:grid-cols-2 gap-8 mb-8">
          <div class="bg-white rounded-lg shadow-md p-6">
            <h3 class="text-lg font-semibold text-red-600 mb-4">
              ❌ Old Approach (Problematic)
            </h3>
            <pre class="bg-gray-100 p-3 rounded text-xs overflow-x-auto"><code>// Multiple boolean flags competing
const isConfirmModal = ref(false)
const isConfirmModalExistFileName = ref(false)

const submitRegist = async () => {
  // Check for duplicates
  if (duplicates.length > 0) {
    isConfirmModal.value = false  // ⚠️ Closes first modal
    isConfirmModalExistFileName.value = true  // ⚠️ Opens second modal
    // Result: Brief flicker as both states change
  }
}</code></pre>
          </div>
          
          <div class="bg-white rounded-lg shadow-md p-6">
            <h3 class="text-lg font-semibold text-green-600 mb-4">
              ✅ New Approach (Fixed)
            </h3>
            <pre class="bg-gray-100 p-3 rounded text-xs overflow-x-auto"><code>// Centralized modal state management
const { switchModal, isModalActive } = useModalStateManager()

const submitRegist = async () => {
  // Check for duplicates
  if (duplicates.length > 0) {
    // Controlled transition with timing
    switchModal('uploadConfirm', 'duplicateConfirm', 150)
    // Result: Smooth transition, no flicker
  }
}</code></pre>
          </div>
        </div>
        
        <!-- Interactive Demo -->
        <div class="bg-white rounded-lg shadow-lg p-6">
          <h2 class="text-2xl font-semibold text-gray-800 mb-6 text-center">
            Interactive Demonstration
          </h2>
          <p class="text-gray-600 text-center mb-6">
            Try uploading files with duplicate names to see the smooth modal transitions in action.
          </p>
          
          <!-- File Upload Demo Component -->
          <FileUploadDemo />
        </div>
        
        <!-- Technical Details -->
        <div class="mt-8 bg-gray-50 rounded-lg p-6">
          <h3 class="text-xl font-semibold text-gray-800 mb-4">Technical Implementation</h3>
          <div class="grid md:grid-cols-2 gap-6">
            <div>
              <h4 class="font-semibold text-gray-700 mb-2">Key Features:</h4>
              <ul class="list-disc list-inside text-sm text-gray-600 space-y-1">
                <li>Single source of truth for modal state</li>
                <li>Transition queuing to prevent conflicts</li>
                <li>Controlled timing between modal switches</li>
                <li>State isolation to prevent flicker</li>
                <li>Composable design for reusability</li>
              </ul>
            </div>
            <div>
              <h4 class="font-semibold text-gray-700 mb-2">Files Created/Modified:</h4>
              <ul class="list-disc list-inside text-sm text-gray-600 space-y-1">
                <li><code>/utils/modalStateManager.js</code> - Core utility</li>
                <li><code>/components/FileUploadDemo.vue</code> - Demo component</li>
                <li>Any existing components can be refactored to use this pattern</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import FileUploadDemo from '@/views/components/FileUploadDemo.vue'
</script>

<style scoped>
.container {
  min-height: 100vh;
}

pre code {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  line-height: 1.4;
}
</style>