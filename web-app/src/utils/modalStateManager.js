/**
 * Modal State Manager Utility
 * 
 * This utility helps prevent modal flickering by managing modal state transitions
 * in a controlled manner. It ensures that only one modal is visible at a time
 * and provides smooth transitions between modals.
 */

import { ref, computed } from 'vue'

export function useModalStateManager() {
  // Current active modal state
  const currentModal = ref(null)
  
  // Flag to track if we're in the middle of a transition
  const isTransitioning = ref(false)
  
  // Queue for pending modal transitions
  const transitionQueue = ref([])
  
  /**
   * Show a modal with optional transition delay
   * @param {string} modalName - Name of the modal to show
   * @param {number} delay - Optional delay in milliseconds before showing modal
   */
  const showModal = async (modalName, delay = 0) => {
    if (isTransitioning.value) {
      // If we're transitioning, queue this modal change
      transitionQueue.value.push({ action: 'show', modalName, delay })
      return
    }
    
    if (delay > 0) {
      isTransitioning.value = true
      await new Promise(resolve => setTimeout(resolve, delay))
      isTransitioning.value = false
    }
    
    currentModal.value = modalName
    processQueue()
  }
  
  /**
   * Hide the current modal with optional transition delay
   * @param {number} delay - Optional delay in milliseconds before hiding modal
   */
  const hideModal = async (delay = 0) => {
    if (isTransitioning.value) {
      // If we're transitioning, queue this modal change
      transitionQueue.value.push({ action: 'hide', delay })
      return
    }
    
    if (delay > 0) {
      isTransitioning.value = true
      await new Promise(resolve => setTimeout(resolve, delay))
      isTransitioning.value = false
    }
    
    currentModal.value = null
    processQueue()
  }
  
  /**
   * Switch from one modal directly to another with controlled timing
   * @param {string} fromModal - Current modal to close
   * @param {string} toModal - Target modal to open
   * @param {number} transitionDelay - Delay between closing and opening (default: 100ms)
   */
  const switchModal = async (fromModal, toModal, transitionDelay = 100) => {
    if (currentModal.value !== fromModal) {
      console.warn(`Expected current modal to be ${fromModal}, but it was ${currentModal.value}`)
    }
    
    isTransitioning.value = true
    
    // First hide the current modal
    currentModal.value = null
    
    // Wait for transition delay
    if (transitionDelay > 0) {
      await new Promise(resolve => setTimeout(resolve, transitionDelay))
    }
    
    // Then show the new modal
    currentModal.value = toModal
    isTransitioning.value = false
    
    processQueue()
  }
  
  /**
   * Process any queued modal transitions
   */
  const processQueue = async () => {
    if (transitionQueue.value.length === 0 || isTransitioning.value) {
      return
    }
    
    const nextTransition = transitionQueue.value.shift()
    
    if (nextTransition.action === 'show') {
      await showModal(nextTransition.modalName, nextTransition.delay)
    } else if (nextTransition.action === 'hide') {
      await hideModal(nextTransition.delay)
    }
  }
  
  /**
   * Check if a specific modal is currently active
   * @param {string} modalName - Name of the modal to check
   * @returns {boolean}
   */
  const isModalActive = (modalName) => {
    return computed(() => currentModal.value === modalName)
  }
  
  /**
   * Clear all modal states and queue
   */
  const reset = () => {
    currentModal.value = null
    isTransitioning.value = false
    transitionQueue.value = []
  }
  
  /**
   * Get current modal state for debugging
   */
  const getState = () => {
    return {
      currentModal: currentModal.value,
      isTransitioning: isTransitioning.value,
      queueLength: transitionQueue.value.length
    }
  }
  
  return {
    currentModal: computed(() => currentModal.value),
    isTransitioning: computed(() => isTransitioning.value),
    showModal,
    hideModal,
    switchModal,
    isModalActive,
    reset,
    getState
  }
}

/**
 * Enhanced modal composable that handles common file upload with duplicate detection scenario
 */
export function useFileUploadModals() {
  const modalManager = useModalStateManager()
  
  // State for file upload flow
  const uploadedFiles = ref([])
  const duplicateFiles = ref([])
  
  /**
   * Handle initial upload confirmation
   */
  const showUploadConfirmation = () => {
    modalManager.showModal('uploadConfirm')
  }
  
  /**
   * Handle submission with duplicate checking
   * @param {Array} files - Files to check for duplicates
   * @param {Function} duplicateChecker - Function that returns array of duplicate files
   */
  const handleSubmitRegist = async (files, duplicateChecker) => {
    try {
      // Check for duplicates
      const duplicates = await duplicateChecker(files)
      
      if (duplicates && duplicates.length > 0) {
        // Store duplicate info
        duplicateFiles.value = duplicates
        
        // Switch from upload confirmation to duplicate confirmation modal
        modalManager.switchModal('uploadConfirm', 'duplicateConfirm', 150)
      } else {
        // No duplicates, proceed with upload
        modalManager.hideModal(100)
        return true
      }
    } catch (error) {
      console.error('Error checking for duplicates:', error)
      modalManager.hideModal()
      throw error
    }
    
    return false
  }
  
  /**
   * Handle duplicate file confirmation
   * @param {boolean} shouldOverwrite - Whether to overwrite duplicate files
   */
  const handleDuplicateConfirmation = async (shouldOverwrite) => {
    modalManager.hideModal(100)
    
    // Process the files based on user choice
    if (shouldOverwrite) {
      // Proceed with overwrite
      return true
    } else {
      // Cancel upload
      duplicateFiles.value = []
      return false
    }
  }
  
  /**
   * Reset all file upload state
   */
  const resetFileUploadState = () => {
    uploadedFiles.value = []
    duplicateFiles.value = []
    modalManager.reset()
  }
  
  return {
    // Modal states
    isUploadConfirmModalOpen: modalManager.isModalActive('uploadConfirm'),
    isDuplicateConfirmModalOpen: modalManager.isModalActive('duplicateConfirm'),
    
    // File state
    uploadedFiles,
    duplicateFiles,
    
    // Actions
    showUploadConfirmation,
    handleSubmitRegist,
    handleDuplicateConfirmation,
    resetFileUploadState,
    
    // Modal manager access for advanced usage
    modalManager
  }
}