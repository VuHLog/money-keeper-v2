<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import { faTags, faPlus, faTrash, faPen, faUtensils, faShoppingBag, faHome, faTaxi, faTshirt, faHeartbeat, faGraduationCap, faMoneyBillWave, faGift, faBriefcase, faHandHoldingDollar, faCoins, faFileInvoiceDollar, faTimes, faSearch, faImage, faChevronDown, faExclamationTriangle } from '@fortawesome/free-solid-svg-icons'
import Swal from 'sweetalert2'
import Avatar from "@/views/components/Avatar.vue"
import { useDictionaryExpenseStore } from "@/store/DictionaryExpenseStore"
import { useDictionaryRevenueStore } from "@/store/DictionaryRevenueStore"
import { DICTIONARY_EXPENSE_ICON_CUSTOM } from "@constants/DictionaryExpenseIconCustom"
import { DICTIONARY_REVENUE_ICON_CUSTOM } from "@constants/DictionaryRevenueIconCustom"

// Thêm icons vào library
library.add(faTags, faPlus, faTrash, faPen, faUtensils, faShoppingBag, faHome, faTaxi, faTshirt, faHeartbeat, faGraduationCap, faMoneyBillWave, faGift, faBriefcase, faHandHoldingDollar, faCoins, faFileInvoiceDollar, faTimes, faSearch, faImage, faChevronDown, faExclamationTriangle)

// Store instances
const expenseStore = useDictionaryExpenseStore()
const revenueStore = useDictionaryRevenueStore()
const dictionaryExpenseIconCustom = ref([...DICTIONARY_EXPENSE_ICON_CUSTOM])
const dictionaryRevenueIconCustom = ref([...DICTIONARY_REVENUE_ICON_CUSTOM])

// Tab hiện tại (mặc định là "income")
const activeTab = ref('income')

// Thêm biến cho search functionality
const searchQuery = ref('')

// Các tab
const tabs = [
  { id: 'income', name: 'Thu nhập' },
  { id: 'expense', name: 'Chi tiêu' }
]

// Danh sách danh mục
const categories = ref([])

// Danh sách các icons có thể chọn
const availableIcons = ref([
  // Icons cho danh mục thu
  { value: 'money-bill-wave', label: 'Tiền mặt', color: 'text-success' },
  { value: 'gift', label: 'Quà tặng', color: 'text-pink-500' },
  { value: 'briefcase', label: 'Công việc', color: 'text-blue-500' },
  { value: 'hand-holding-dollar', label: 'Thu nhập phụ', color: 'text-purple-500' },
  { value: 'coins', label: 'Tiết kiệm', color: 'text-yellow-500' },
  { value: 'file-invoice-dollar', label: 'Lương', color: 'text-green-500' },

  // Icons cho danh mục chi
  { value: 'utensils', label: 'Ăn uống', color: 'text-red-500' },
  { value: 'shopping-bag', label: 'Mua sắm', color: 'text-purple-500' },
  { value: 'home', label: 'Nhà cửa', color: 'text-blue-500' },
  { value: 'taxi', label: 'Di chuyển', color: 'text-yellow-500' },
  { value: 'tshirt', label: 'Quần áo', color: 'text-pink-500' },
  { value: 'heartbeat', label: 'Sức khỏe', color: 'text-green-500' },
  { value: 'graduation-cap', label: 'Giáo dục', color: 'text-indigo-500' }
])

// Lọc icons dựa trên tab hiện tại và search query
const filteredIcons = computed(() => {
  let icons = []
  if (activeTab.value === 'income') {
    icons = dictionaryRevenueIconCustom.value
  } else {
    icons = dictionaryExpenseIconCustom.value
  }
  return icons
})

// Lọc danh mục theo chuỗi tìm kiếm
const filteredCategories = computed(() => {
  if (!searchQuery.value.trim()) {
    return categories.value;
  }
  
  const query = searchQuery.value.toLowerCase().trim();
  return categories.value.filter(category => 
    category.name.toLowerCase().includes(query)
  );
})

// Load categories based on the active tab
const loadCategories = async () => {
  if (activeTab.value === 'income') {
    const response = await revenueStore.getMyRevenueCategoriesWithoutTransfer()
    if (response) {
      categories.value = response.map(item => ({
        ...item,
        type: 'income'
      }))
    }
  } else {
    const response = await expenseStore.getMyExpenseCategoriesWithoutTransfer()
    if (response) {
      categories.value = response.map(item => ({
        ...item,
        type: 'expense'
      }))
    }
  }
}

// Quản lý modal thêm mới danh mục
const isAddCategoryModalOpen = ref(false)
const isAddIconDropdownOpen = ref(false)
const newCategory = ref({
  name: '',
  iconUrl: activeTab.value === 'income' ? dictionaryRevenueIconCustom.value[0] : dictionaryExpenseIconCustom.value[0] || '',
  type: activeTab.value
})

// Quản lý modal chỉnh sửa danh mục
const isEditCategoryModalOpen = ref(false)
const isEditIconDropdownOpen = ref(false)
const editingCategory = ref(null)

// Quản lý modal xóa danh mục
const isDeleteCategoryModalOpen = ref(false)
const deletingCategory = ref(null)

// Icon dropdown management
const isIconDropdownOpen = ref(false)

// Bỏ hoặc thay thế các biến không cần thiết như availableIcons, filteredIcons (nếu có)

// Thêm hàm để cập nhật icon đã chọn trong modal chỉnh sửa
const updateSelectedIcon = (icon, mode) => {
  if (mode === 'add') {
    newCategory.value.iconUrl = icon
    isAddIconDropdownOpen.value = false
  } else if (mode === 'edit') {
    editingCategory.value.iconUrl = icon
    isEditIconDropdownOpen.value = false
  }
}

// Mở modal thêm mới danh mục
const openAddModal = () => {
  newCategory.value = {
    name: '',
    iconUrl: activeTab.value === 'income' ? dictionaryRevenueIconCustom.value[0] : dictionaryExpenseIconCustom.value[0] || '',
    type: activeTab.value
  }
  isAddCategoryModalOpen.value = true
}

// Cập nhật khi mở modal chỉnh sửa
const openEditModal = (category) => {
  editingCategory.value = { ...category }
  isEditCategoryModalOpen.value = true
}

// Open delete modal
const openDeleteModal = (category) => {
  deletingCategory.value = category
  isDeleteCategoryModalOpen.value = true
}

// Thay đổi tab
const changeTab = (tabId) => {
  activeTab.value = tabId
  // Khởi tạo lại giá trị mặc định cho danh mục mới
  newCategory.value = {
    name: '',
    iconUrl: activeTab.value === 'income' ? dictionaryRevenueIconCustom.value[0] : dictionaryExpenseIconCustom.value[0] || '',
    type: activeTab.value
  }
  loadCategories()
}

// Xử lý sự kiện click ngoài dropdown để đóng chúng
const handleClickOutside = (event) => {
  // Đóng dropdown chọn icon khi thêm mới
  const addIconDropdownElement = document.querySelector('#add-icon-dropdown-container')
  if (isAddIconDropdownOpen.value && addIconDropdownElement && !addIconDropdownElement.contains(event.target)) {
    isAddIconDropdownOpen.value = false
  }

  // Đóng dropdown chọn icon khi chỉnh sửa
  const editIconDropdownElement = document.querySelector('#edit-icon-dropdown-container')
  if (isEditIconDropdownOpen.value && editIconDropdownElement && !editIconDropdownElement.contains(event.target)) {
    isEditIconDropdownOpen.value = false
  }
}

// Đăng ký và hủy đăng ký sự kiện click ngoài
onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  loadCategories()
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})

// Handle adding a new category
const handleAddCategory = async () => {
  if (!newCategory.value.name) {
    Swal.fire({
      icon: 'error',
      title: 'Lỗi',
      text: 'Vui lòng nhập tên danh mục'
    })
    return
  }

  if (newCategory.value.name.toLowerCase() === "phát triển bản thân".toLowerCase()) {
    Swal.fire({
      icon: 'info',
      title: 'Thông tin',
      text: 'Danh mục "Phát triển bản thân" không được phép tạo. Vui lòng chọn tên khác.'
    })
    return
  }

  try {
    let response
    if (activeTab.value === 'income') {
      response = await revenueStore.createRevenueCategory({
        name: newCategory.value.name,
        iconUrl: newCategory.value.iconUrl
      })
    } else {
      response = await expenseStore.createExpenseCategory({
        name: newCategory.value.name,
        iconUrl: newCategory.value.iconUrl
      })
    }

    if (response) {
      Swal.fire({
        icon: 'success',
        title: 'Thành công',
        text: 'Thêm danh mục thành công'
      })
      isAddCategoryModalOpen.value = false
      loadCategories()
    }
  } catch (error) {
    Swal.fire({
      icon: 'error',
      title: 'Lỗi',
      text: error?.response?.data?.message || 'Có lỗi xảy ra khi thêm danh mục'
    })
  }
}

// Handle updating a category
const handleUpdateCategory = async () => {
  if (!editingCategory.value || !editingCategory.value.name) {
    Swal.fire({
      icon: 'error',
      title: 'Lỗi',
      text: 'Vui lòng nhập tên danh mục'
    })
    return
  }

  try {
    let response
    if (editingCategory.value.type === 'income') {
      response = await revenueStore.update(
        editingCategory.value.id,
        {
          name: editingCategory.value.name,
          iconUrl: editingCategory.value.iconUrl
        }
      )
    } else {
      response = await expenseStore.update(
        editingCategory.value.id,
        {
          name: editingCategory.value.name,
          iconUrl: editingCategory.value.iconUrl
        }
      )
    }

    if (response) {
      Swal.fire({
        icon: 'success',
        title: 'Thành công',
        text: 'Cập nhật danh mục thành công'
      })
      isEditCategoryModalOpen.value = false
      loadCategories()
    }
  } catch (error) {
    Swal.fire({
      icon: 'error',
      title: 'Lỗi',
      text: error?.response?.data?.message || 'Có lỗi xảy ra khi cập nhật danh mục'
    })
  }
}

// Handle deleting a category
const handleDeleteCategory = async () => {
  if (!deletingCategory.value) return

  if (deletingCategory.value.name === "Phát triển bản thân") {
    Swal.fire({
      icon: 'info',
      title: 'Thông tin',
      text: 'Danh mục "Phát triển bản thân" không được phép xóa'
    })
    return
  }

  try {
    let response
    if (deletingCategory.value.type === 'income') {
      response = await revenueStore.deleteById(deletingCategory.value.id)
    } else {
      response = await expenseStore.deleteById(deletingCategory.value.id)
    }

    if (response) {
      Swal.fire({
        icon: 'success',
        title: 'Thành công',
        text: 'Xóa danh mục thành công'
      })
      isDeleteCategoryModalOpen.value = false
      loadCategories()
    }
  } catch (error) {
    Swal.fire({
      icon: 'error',
      title: 'Lỗi',
      text: error?.response?.data?.message || 'Có lỗi xảy ra khi xóa danh mục'
    })
  }
}
</script>

<template>
  <div class="p-4">
    <!-- Tabs -->
    <div class="bg-surface rounded-xl p-4 shadow-sm">
      <div class="flex justify-between items-center mb-4">
        <div class="flex space-x-4">
          <button v-for="tab in tabs" :key="tab.id" @click="changeTab(tab.id)"
            class="py-2 px-4 border-b-2 transition-colors" :class="[
              activeTab === tab.id
                ? 'border-primary text-primary font-medium'
                : 'border-transparent text-text-secondary hover:text-text hover:border-gray-300'
            ]">
            {{ tab.name }}
          </button>
        </div>
        <div class="flex justify-between items-center">
          <button @click="openAddModal"
            class="bg-primary hover:bg-primary/90 text-white px-4 py-2 rounded-lg flex items-center space-x-2 transition-colors">
            <font-awesome-icon :icon="['fas', 'plus']" />
            <span>Thêm mới</span>
          </button>
        </div>
      </div>

      <!-- Thanh tìm kiếm danh mục -->
      <div class="mb-4 relative">
        <div class="relative">
          <input
            v-model="searchQuery"
            type="text"
            class="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 focus:border-primary/50"
            placeholder="Tìm kiếm danh mục theo tên..."
          />
          <div class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400">
            <font-awesome-icon :icon="['fas', 'search']" />
          </div>
          <button 
            v-if="searchQuery" 
            @click="searchQuery = ''" 
            class="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
          >
            <font-awesome-icon :icon="['fas', 'times']" />
          </button>
        </div>
      </div>

      <!-- Danh sách danh mục -->
      <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
        <div v-for="category in filteredCategories" :key="category.id" class="bg-gray-50 p-4 rounded-lg">
          <div class="flex items-center justify-between mb-2">
            <div class="flex items-center space-x-3">
              <Avatar :src="category.iconUrl" :alt="category.name" size="md" customClass="bg-gray-100" />
              <h3 class="font-medium text-text">{{ category.name }}</h3>
            </div>
            <div class="flex space-x-2">
              <button @click="openEditModal(category)"
                class="p-2 text-text-secondary hover:text-primary rounded-full hover:bg-gray-100" title="Chỉnh sửa">
                <font-awesome-icon :icon="['fas', 'pen']" />
              </button>
              <button @click="openDeleteModal(category)"
                class="p-2 text-text-secondary hover:text-danger rounded-full hover:bg-gray-100" title="Xóa">
                <font-awesome-icon :icon="['fas', 'trash']" />
              </button>
            </div>
          </div>
        </div>

        <!-- Hiển thị khi không có danh mục nào -->
        <div v-if="categories.length === 0" class="text-center py-8 col-span-full">
          <p class="text-text-secondary">Chưa có danh mục nào. Hãy thêm danh mục mới.</p>
        </div>
      </div>
    </div>

    <!-- Modal Thêm Mới Danh Mục -->
    <Teleport to="body">
      <div v-if="isAddCategoryModalOpen"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
        @click="isAddCategoryModalOpen = false">
        <div class="bg-white rounded-lg shadow-xl w-full max-w-md" @click.stop>
          <!-- Modal Header -->
          <div class="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
            <h3 class="text-lg font-semibold text-text">Thêm danh mục mới</h3>
            <button @click="isAddCategoryModalOpen = false" class="text-gray-400 hover:text-gray-600">
              <font-awesome-icon :icon="['fas', 'times']" />
            </button>
          </div>

          <!-- Modal Content -->
          <div class="px-6 py-4">
            <!-- Tên danh mục -->
            <div class="mb-4">
              <label class="block text-sm font-medium text-text-secondary mb-1">
                Tên danh mục <span class="text-danger">*</span>
              </label>
              <input v-model="newCategory.name" type="text"
                class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 focus:border-primary/50"
                placeholder="Nhập tên danh mục" />
            </div>

            <!-- Chọn icon - With improved selection interface -->
            <div>
              <label class="block text-sm font-medium text-text-secondary mb-1">
                Icon <span class="text-danger">*</span>
              </label>

              <div class="relative select-none" id="add-icon-dropdown-container">
                <div
                  class="flex items-center w-full px-3 py-2 border border-gray-100 rounded-lg cursor-pointer hover:border-gray-200"
                  :class="{ 'ring-1 ring-primary/20 border-primary/50': isAddIconDropdownOpen }"
                  @click="isAddIconDropdownOpen = !isAddIconDropdownOpen">
                  <div class="flex items-center flex-1">
                    <div v-if="newCategory.iconUrl" class="w-8 h-8 rounded-full flex items-center justify-center">
                      <Avatar :src="newCategory.iconUrl" alt="Selected icon" size="m" />
                    </div>
                    <div v-else class="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center">
                      <font-awesome-icon :icon="['fas', 'image']" class="text-gray-400" />
                    </div>
                    <span class="ml-2 text-text-secondary">
                      {{ newCategory.iconUrl ? 'Icon đã chọn' : 'Chọn icon' }}
                    </span>
                  </div>
                  <font-awesome-icon :icon="['fas', 'chevron-down']" class="text-gray-400 ml-2 transition-transform"
                    :class="{ 'rotate-180': isAddIconDropdownOpen }" />
                </div>

                <!-- Icon Grid Dropdown for better selection experience -->
                <div v-if="isAddIconDropdownOpen"
                  class="absolute z-10 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg py-1">
                  <div class="max-h-48 overflow-y-auto p-2">
                    <div class="grid grid-cols-4 gap-2">
                      <div v-for="icon in filteredIcons" :key="icon"
                        class="aspect-square border border-gray-100 rounded-md p-1 cursor-pointer hover:bg-gray-50 flex items-center justify-center"
                        :class="{ 'bg-primary/5 border-primary/30': icon === newCategory.iconUrl }"
                        @click="updateSelectedIcon(icon, 'add')">
                        <Avatar :src="icon" :alt="'icon'" size="m" />
                      </div>
                    </div>
                    <div v-if="filteredIcons.length === 0" class="py-2 text-center text-gray-500">
                      Không tìm thấy icon phù hợp
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Modal Footer -->
          <div class="px-6 py-4 bg-gray-50 rounded-b-lg flex justify-end space-x-3">
            <button @click="isAddCategoryModalOpen = false"
              class="px-4 py-2 text-text-secondary bg-white border border-gray-200 rounded-lg hover:bg-gray-50">
              Hủy
            </button>
            <button @click="handleAddCategory" class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90">
              Thêm mới
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Modal Chỉnh Sửa Danh Mục -->
    <Teleport to="body">
      <div v-if="isEditCategoryModalOpen"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
        @click="isEditCategoryModalOpen = false">
        <div class="bg-white rounded-lg shadow-xl w-full max-w-md" @click.stop>
          <!-- Modal Header -->
          <div class="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
            <h3 class="text-lg font-semibold text-text">Chỉnh sửa danh mục</h3>
            <button @click="isEditCategoryModalOpen = false" class="text-gray-400 hover:text-gray-600">
              <font-awesome-icon :icon="['fas', 'times']" />
            </button>
          </div>

          <!-- Modal Content -->
          <div class="px-6 py-4">
            <!-- Tên danh mục -->
            <div class="mb-4">
              <label class="block text-sm font-medium text-text-secondary mb-1">
                Tên danh mục <span class="text-danger">*</span>
              </label>
              <input v-model="editingCategory.name" type="text"
                class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 focus:border-primary/50"
                placeholder="Nhập tên danh mục" />
            </div>

            <!-- Chọn icon - With improved selection interface -->
            <div>
              <label class="block text-sm font-medium text-text-secondary mb-1">
                Icon <span class="text-danger">*</span>
              </label>

              <div class="relative select-none" id="edit-icon-dropdown-container">
                <div
                  class="flex items-center w-full px-3 py-2 border border-gray-100 rounded-lg cursor-pointer hover:border-gray-200"
                  :class="{ 'ring-1 ring-primary/20 border-primary/50': isEditIconDropdownOpen }"
                  @click="isEditIconDropdownOpen = !isEditIconDropdownOpen">
                  <div class="flex items-center flex-1">
                    <div v-if="editingCategory.iconUrl" class="w-8 h-8 rounded-full flex items-center justify-center">
                      <Avatar :src="editingCategory.iconUrl" alt="Selected icon" size="m" />
                    </div>
                    <div v-else class="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center">
                      <font-awesome-icon :icon="['fas', 'image']" class="text-gray-400" />
                    </div>
                    <span class="ml-2 text-text-secondary">
                      {{ editingCategory.iconUrl ? 'Icon đã chọn' : 'Chọn icon' }}
                    </span>
                  </div>
                  <font-awesome-icon :icon="['fas', 'chevron-down']" class="text-gray-400 ml-2 transition-transform"
                    :class="{ 'rotate-180': isEditIconDropdownOpen }" />
                </div>

                <!-- Icon Grid Dropdown for better selection experience -->
                <div v-if="isEditIconDropdownOpen"
                  class="absolute z-10 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg py-1">
                  <div class="max-h-48 overflow-y-auto p-2">
                    <div class="grid grid-cols-4 gap-2">
                      <div v-for="icon in filteredIcons" :key="icon"
                        class="aspect-square border border-gray-100 rounded-md p-1 cursor-pointer hover:bg-gray-50 flex items-center justify-center"
                        :class="{ 'bg-primary/5 border-primary/30': icon === editingCategory.iconUrl }"
                        @click="updateSelectedIcon(icon, 'edit')">
                        <Avatar :src="icon" :alt="'icon'" size="m" />
                      </div>
                    </div>
                    <div v-if="filteredIcons.length === 0" class="py-2 text-center text-gray-500">
                      Không tìm thấy icon phù hợp
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Modal Footer -->
          <div class="px-6 py-4 bg-gray-50 rounded-b-lg flex justify-end space-x-3">
            <button @click="isEditCategoryModalOpen = false"
              class="px-4 py-2 text-text-secondary bg-white border border-gray-200 rounded-lg hover:bg-gray-50">
              Hủy
            </button>
            <button @click="handleUpdateCategory"
              class="px-4 py-2 bg-primary text-white rounded-lg hover:bg-primary/90">
              Cập nhật
            </button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Modal Xóa Danh Mục -->
    <Teleport to="body">
      <div v-if="isDeleteCategoryModalOpen"
        class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
        @click="isDeleteCategoryModalOpen = false">
        <div class="bg-white rounded-lg shadow-xl w-full max-w-md" @click.stop>
          <!-- Modal Header -->
          <div class="px-6 py-4 border-b border-gray-200">
            <h3 class="text-lg font-semibold text-text">Xác nhận xóa danh mục</h3>
          </div>

          <!-- Modal Content -->
          <div class="px-6 py-4">
            <div class="flex items-center space-x-4 mb-4">
              <div class="w-12 h-12 rounded-full bg-gray-100 flex items-center justify-center">
                <Avatar :src="deletingCategory?.iconUrl" :alt="deletingCategory?.name" size="m" />
              </div>
              <div>
                <h4 class="font-medium text-text">{{ deletingCategory?.name }}</h4>
                <p class="text-sm text-text-secondary">
                  {{ deletingCategory?.type === 'income' ? 'Danh mục thu' : 'Danh mục chi' }}
                </p>
              </div>
            </div>

            <div class="bg-danger/5 border border-danger/10 rounded-lg p-4">
              <div class="flex items-start space-x-3">
                <div class="mt-0.5">
                  <font-awesome-icon :icon="['fas', 'exclamation-triangle']" class="text-danger" />
                </div>
                <div>
                  <p class="text-text">Bạn có chắc chắn muốn xóa danh mục này?</p>
                  <p class="text-sm text-text-secondary mt-1">Hành động này không thể hoàn tác. Tất cả dữ liệu liên quan
                    đến danh mục này sẽ bị xóa vĩnh viễn.</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Modal Footer -->
          <div class="px-6 py-4 bg-gray-50 rounded-b-lg flex justify-end space-x-3">
            <button @click="isDeleteCategoryModalOpen = false" class="px-4 py-2 text-text-secondary hover:text-text">
              Hủy
            </button>
            <button @click="handleDeleteCategory"
              class="px-4 py-2 bg-danger text-white rounded-lg hover:bg-danger/90 flex items-center space-x-2">
              <font-awesome-icon :icon="['fas', 'trash']" />
              <span>Xóa danh mục</span>
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
/* Fix cho dropdown không bị hiển thị sai vị trí */
.category-dropdown-container {
  position: relative;
}
</style>