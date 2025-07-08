<template>
  <div class="file-upload-demo p-6">
    <div class="max-w-2xl mx-auto">
      <h2 class="text-2xl font-bold mb-6 text-gray-800">File Upload with Duplicate Detection - Fixed</h2>
      
      <!-- Upload Area -->
      <div class="bg-white rounded-lg shadow-md p-6 mb-6">
        <div class="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center">
          <div class="mb-4">
            <font-awesome-icon :icon="['fas', 'cloud-upload-alt']" class="text-4xl text-gray-400" />
          </div>
          <div class="mb-4">
            <input
              type="file"
              multiple
              accept=".csv,.xlsx,.xls"
              @change="handleFileSelect"
              class="hidden"
              ref="fileInput"
              id="fileUpload"
            />
            <label
              for="fileUpload"
              class="cursor-pointer bg-blue-500 hover:bg-blue-600 text-white px-6 py-2 rounded-md transition-colors"
            >
              Choose Files
            </label>
          </div>
          <p class="text-gray-500 text-sm">
            Select CSV or Excel files to upload. Supports multiple file selection.
          </p>
        </div>
        
        <!-- Selected Files Display -->
        <div v-if="selectedFiles.length > 0" class="mt-4">
          <h3 class="font-semibold text-gray-700 mb-2">Selected Files:</h3>
          <div class="space-y-2">
            <div
              v-for="(file, index) in selectedFiles"
              :key="index"
              class="flex items-center justify-between bg-gray-50 p-2 rounded"
            >
              <span class="text-sm">{{ file.name }}</span>
              <button
                @click="removeFile(index)"
                class="text-red-500 hover:text-red-700"
              >
                <font-awesome-icon :icon="['fas', 'times']" />
              </button>
            </div>
          </div>
        </div>
        
        <!-- Upload Button -->
        <div v-if="selectedFiles.length > 0" class="mt-4 text-center">
          <button
            @click="startUpload"
            :disabled="isUploading"
            class="bg-green-500 hover:bg-green-600 disabled:bg-gray-400 text-white px-8 py-2 rounded-md transition-colors"
          >
            <span v-if="isUploading">Uploading...</span>
            <span v-else>Upload Files</span>
          </button>
        </div>
      </div>
      
      <!-- Status Display -->
      <div class="bg-white rounded-lg shadow-md p-6">
        <h3 class="font-semibold text-gray-700 mb-4">Modal State Debug Info:</h3>
        <div class="bg-gray-100 p-3 rounded text-sm font-mono">
          <div>Current Modal: {{ modalState.currentModal || 'None' }}</div>
          <div>Is Transitioning: {{ modalState.isTransitioning }}</div>
          <div>Queue Length: {{ modalState.queueLength }}</div>
        </div>
      </div>
    </div>
    
    <!-- Upload Confirmation Modal -->
    <div
      v-if="isUploadConfirmModalOpen"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
      @click.self="cancelUpload"
    >
      <div class="bg-white rounded-lg shadow-xl w-full max-w-md mx-4">
        <div class="px-6 py-4 border-b border-gray-200">
          <h3 class="text-lg font-semibold text-gray-800">Confirm Upload</h3>
        </div>
        <div class="px-6 py-4">
          <p class="text-gray-600 mb-4">
            Are you sure you want to upload {{ selectedFiles.length }} file(s)?
          </p>
          <div class="space-y-2">
            <div
              v-for="file in selectedFiles"
              :key="file.name"
              class="text-sm text-gray-500"
            >
              • {{ file.name }}
            </div>
          </div>
        </div>
        <div class="px-6 py-4 border-t border-gray-200 flex justify-end space-x-3">
          <button
            @click="cancelUpload"
            class="px-4 py-2 text-gray-600 border border-gray-300 rounded hover:bg-gray-50"
          >
            Cancel
          </button>
          <button
            @click="submitRegist"
            :disabled="isUploading"
            class="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 disabled:bg-gray-400"
          >
            {{ isUploading ? 'Processing...' : 'Confirm Upload' }}
          </button>
        </div>
      </div>
    </div>
    
    <!-- Duplicate Files Confirmation Modal -->
    <div
      v-if="isDuplicateConfirmModalOpen"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
      @click.self="handleDuplicateResponse(false)"
    >
      <div class="bg-white rounded-lg shadow-xl w-full max-w-md mx-4">
        <div class="px-6 py-4 border-b border-gray-200">
          <h3 class="text-lg font-semibold text-orange-600">
            <font-awesome-icon :icon="['fas', 'exclamation-triangle']" class="mr-2" />
            Duplicate Files Detected
          </h3>
        </div>
        <div class="px-6 py-4">
          <p class="text-gray-600 mb-4">
            The following files already exist. Do you want to overwrite them?
          </p>
          <div class="space-y-2 max-h-32 overflow-y-auto">
            <div
              v-for="file in duplicateFiles"
              :key="file.name"
              class="text-sm text-orange-600 bg-orange-50 p-2 rounded"
            >
              • {{ file.name }}
            </div>
          </div>
        </div>
        <div class="px-6 py-4 border-t border-gray-200 flex justify-end space-x-3">
          <button
            @click="handleDuplicateResponse(false)"
            class="px-4 py-2 text-gray-600 border border-gray-300 rounded hover:bg-gray-50"
          >
            Cancel
          </button>
          <button
            @click="handleDuplicateResponse(true)"
            class="px-4 py-2 bg-orange-500 text-white rounded hover:bg-orange-600"
          >
            Overwrite Files
          </button>
        </div>
      </div>
    </div>
    
    <!-- Success Modal -->
    <div
      v-if="showSuccessModal"
      class="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
      @click.self="closeSuccessModal"
    >
      <div class="bg-white rounded-lg shadow-xl w-full max-w-md mx-4">
        <div class="px-6 py-4 border-b border-gray-200">
          <h3 class="text-lg font-semibold text-green-600">
            <font-awesome-icon :icon="['fas', 'check-circle']" class="mr-2" />
            Upload Successful
          </h3>
        </div>
        <div class="px-6 py-4">
          <p class="text-gray-600">
            All files have been uploaded successfully!
          </p>
        </div>
        <div class="px-6 py-4 border-t border-gray-200 flex justify-end">
          <button
            @click="closeSuccessModal"
            class="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600"
          >
            OK
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import { faCloudUploadAlt, faTimes, faExclamationTriangle, faCheckCircle } from '@fortawesome/free-solid-svg-icons'
import { useFileUploadModals } from '@/utils/modalStateManager'

// Add icons to library
library.add(faCloudUploadAlt, faTimes, faExclamationTriangle, faCheckCircle)

// File upload state
const selectedFiles = ref([])
const fileInput = ref(null)
const isUploading = ref(false)
const showSuccessModal = ref(false)

// Use the modal state manager
const {
  isUploadConfirmModalOpen,
  isDuplicateConfirmModalOpen,
  duplicateFiles,
  showUploadConfirmation,
  handleSubmitRegist,
  handleDuplicateConfirmation,
  resetFileUploadState,
  modalManager
} = useFileUploadModals()

// Get modal state for debugging
const modalState = computed(() => modalManager.getState())

// Simulated existing files for demonstration
const existingFiles = ref([
  { name: 'expenses_2024.csv', uploadDate: '2024-01-15' },
  { name: 'income_january.xlsx', uploadDate: '2024-01-10' },
  { name: 'transactions.csv', uploadDate: '2024-01-05' }
])

/**
 * Handle file selection
 */
const handleFileSelect = (event) => {
  const files = Array.from(event.target.files)
  selectedFiles.value = files
}

/**
 * Remove a file from selection
 */
const removeFile = (index) => {
  selectedFiles.value.splice(index, 1)
}

/**
 * Start the upload process
 */
const startUpload = () => {
  if (selectedFiles.value.length === 0) return
  showUploadConfirmation()
}

/**
 * Cancel upload process
 */
const cancelUpload = () => {
  resetFileUploadState()
}

/**
 * Simulate duplicate checking function
 */
const checkForDuplicates = async (files) => {
  // Simulate API call delay
  await new Promise(resolve => setTimeout(resolve, 500))
  
  // Check which files already exist
  const duplicates = files.filter(file => 
    existingFiles.value.some(existing => existing.name === file.name)
  )
  
  return duplicates
}

/**
 * Handle the submit registration (main upload logic)
 */
const submitRegist = async () => {
  isUploading.value = true
  
  try {
    // Use the modal manager to handle duplicate detection
    const canProceed = await handleSubmitRegist(selectedFiles.value, checkForDuplicates)
    
    if (canProceed) {
      // Simulate upload process
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      // Show success
      showSuccessModal.value = true
      selectedFiles.value = []
      
      // Reset file input
      if (fileInput.value) {
        fileInput.value.value = ''
      }
    }
  } catch (error) {
    console.error('Upload failed:', error)
    // Handle error (show error modal, etc.)
  } finally {
    isUploading.value = false
  }
}

/**
 * Handle duplicate file response
 */
const handleDuplicateResponse = async (shouldOverwrite) => {
  const result = await handleDuplicateConfirmation(shouldOverwrite)
  
  if (result) {
    // Proceed with upload
    try {
      // Simulate upload with overwrite
      await new Promise(resolve => setTimeout(resolve, 1000))
      
      // Show success
      showSuccessModal.value = true
      selectedFiles.value = []
      
      // Reset file input
      if (fileInput.value) {
        fileInput.value.value = ''
      }
    } catch (error) {
      console.error('Upload failed:', error)
    }
  } else {
    // Reset everything
    selectedFiles.value = []
    if (fileInput.value) {
      fileInput.value.value = ''
    }
  }
  
  isUploading.value = false
}

/**
 * Close success modal
 */
const closeSuccessModal = () => {
  showSuccessModal.value = false
}
</script>

<style scoped>
/* Smooth transitions for modals */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}

/* Custom scrollbar for duplicate files list */
.max-h-32::-webkit-scrollbar {
  width: 4px;
}

.max-h-32::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 2px;
}

.max-h-32::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 2px;
}

.max-h-32::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}
</style>