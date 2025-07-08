/**
 * Example of how to refactor existing modal components to use the new modal state manager
 * This demonstrates converting EditAccountModal.vue to use the centralized modal system
 */

// Before (existing pattern in EditAccountModal.vue):
/*
const isConfirmingClose = ref(false)

const handleClose = () => {
  if (hasChanges.value && !isConfirmingClose.value) {
    isConfirmingClose.value = true
    return
  }
  resetAndClose()
}

const continueEditing = () => {
  isConfirmingClose.value = false
}
*/

// After (using modal state manager):
import { useModalStateManager } from '@/utils/modalStateManager'

export function useEditAccountModal() {
  const modalManager = useModalStateManager()
  
  /**
   * Enhanced modal handling with state manager
   */
  const handleClose = () => {
    if (hasChanges.value && !modalManager.isModalActive('confirmClose').value) {
      modalManager.showModal('confirmClose')
      return
    }
    resetAndClose()
  }
  
  /**
   * Continue editing (dismiss confirmation)
   */
  const continueEditing = () => {
    modalManager.hideModal()
  }
  
  /**
   * If we need to add file upload functionality to account editing
   */
  const handleAccountUpdate = async (accountData, attachedFiles = []) => {
    if (attachedFiles.length > 0) {
      // Show file upload confirmation
      modalManager.showModal('uploadConfirm')
    } else {
      // Proceed with regular update
      await updateAccount(accountData)
      resetAndClose()
    }
  }
  
  /**
   * Handle file upload submission with duplicate checking
   */
  const submitAccountWithFiles = async (accountData, files, duplicateChecker) => {
    try {
      const duplicates = await duplicateChecker(files)
      
      if (duplicates && duplicates.length > 0) {
        // Switch from upload confirmation to duplicate confirmation
        modalManager.switchModal('uploadConfirm', 'duplicateConfirm', 150)
        return false
      } else {
        // No duplicates, proceed with update
        await updateAccount(accountData)
        modalManager.hideModal(100)
        resetAndClose()
        return true
      }
    } catch (error) {
      console.error('Error processing account update:', error)
      modalManager.hideModal()
      throw error
    }
  }
  
  return {
    // Modal states
    isConfirmCloseModalOpen: modalManager.isModalActive('confirmClose'),
    isUploadConfirmModalOpen: modalManager.isModalActive('uploadConfirm'),
    isDuplicateConfirmModalOpen: modalManager.isModalActive('duplicateConfirm'),
    
    // Actions
    handleClose,
    continueEditing,
    handleAccountUpdate,
    submitAccountWithFiles,
    
    // Modal manager for advanced usage
    modalManager
  }
}

/**
 * Template usage in the Vue component:
 * 
 * <template>
 *   <!-- Main edit modal -->
 *   <div v-if="isOpen" class="modal-backdrop">
 *     <!-- Edit form content -->
 *   </div>
 *   
 *   <!-- Confirmation modal for unsaved changes -->
 *   <div v-if="isConfirmCloseModalOpen" class="modal-backdrop">
 *     <div class="modal-content">
 *       <h3>Unsaved Changes</h3>
 *       <p>You have unsaved changes. Are you sure you want to close?</p>
 *       <button @click="continueEditing">Continue Editing</button>
 *       <button @click="handleClose">Discard Changes</button>
 *     </div>
 *   </div>
 *   
 *   <!-- Upload confirmation modal -->
 *   <div v-if="isUploadConfirmModalOpen" class="modal-backdrop">
 *     <div class="modal-content">
 *       <h3>Confirm File Upload</h3>
 *       <p>Upload account documents?</p>
 *       <button @click="submitAccountWithFiles">Confirm</button>
 *       <button @click="modalManager.hideModal">Cancel</button>
 *     </div>
 *   </div>
 *   
 *   <!-- Duplicate files confirmation modal -->
 *   <div v-if="isDuplicateConfirmModalOpen" class="modal-backdrop">
 *     <div class="modal-content">
 *       <h3>Duplicate Files Detected</h3>
 *       <p>Some files already exist. Overwrite?</p>
 *       <button @click="handleDuplicateConfirmation(true)">Overwrite</button>
 *       <button @click="handleDuplicateConfirmation(false)">Cancel</button>
 *     </div>
 *   </div>
 * </template>
 */