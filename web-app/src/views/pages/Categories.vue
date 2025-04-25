<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
import { library } from '@fortawesome/fontawesome-svg-core'
import { 
  faTags, 
  faPlus, 
  faTrash, 
  faPen, 
  faUtensils, 
  faShoppingBag, 
  faHome, 
  faTaxi, 
  faTshirt, 
  faHeartbeat, 
  faGraduationCap,
  faMoneyBillWave,
  faGift,
  faBriefcase,
  faHandHoldingDollar,
  faCoins,
  faFileInvoiceDollar
} from '@fortawesome/free-solid-svg-icons'
import Swal from 'sweetalert2'

// Thêm icons vào library
library.add(
  faTags, 
  faPlus, 
  faTrash, 
  faPen, 
  faUtensils, 
  faShoppingBag, 
  faHome, 
  faTaxi, 
  faTshirt, 
  faHeartbeat, 
  faGraduationCap,
  faMoneyBillWave,
  faGift,
  faBriefcase,
  faHandHoldingDollar,
  faCoins,
  faFileInvoiceDollar
)

// Tab hiện tại (mặc định là "income")
const activeTab = ref('income')

// Các tab
const tabs = [
  { id: 'income', name: 'Thu nhập' },
  { id: 'expense', name: 'Chi tiêu' }
]

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

// Lọc icons dựa trên tab hiện tại
const filteredIcons = computed(() => {
  if (activeTab.value === 'income') {
    return availableIcons.value.slice(0, 6)
  } else {
    return availableIcons.value.slice(6)
  }
})

// Danh sách danh mục
const categories = ref([
  // Danh mục thu
  { 
    id: 1, 
    name: 'Lương', 
    icon: 'file-invoice-dollar', 
    color: 'text-green-500', 
    type: 'income',
    parentId: null
  },
  { 
    id: 2, 
    name: 'Tiền thưởng', 
    icon: 'coins', 
    color: 'text-yellow-500', 
    type: 'income',
    parentId: 1 
  },
  { 
    id: 3, 
    name: 'Quà tặng', 
    icon: 'gift', 
    color: 'text-pink-500', 
    type: 'income',
    parentId: null 
  },
  { 
    id: 4, 
    name: 'Đầu tư', 
    icon: 'hand-holding-dollar', 
    color: 'text-purple-500', 
    type: 'income',
    parentId: null 
  },
  
  // Danh mục chi
  { 
    id: 5, 
    name: 'Ăn uống', 
    icon: 'utensils', 
    color: 'text-red-500', 
    type: 'expense',
    parentId: null
  },
  { 
    id: 6, 
    name: 'Ăn ngoài', 
    icon: 'utensils', 
    color: 'text-red-500', 
    type: 'expense',
    parentId: 5
  },
  { 
    id: 7, 
    name: 'Mua sắm', 
    icon: 'shopping-bag', 
    color: 'text-purple-500', 
    type: 'expense',
    parentId: null
  },
  { 
    id: 8, 
    name: 'Nhà cửa', 
    icon: 'home', 
    color: 'text-blue-500', 
    type: 'expense',
    parentId: null
  }
])

// Lọc danh mục theo tab hiện tại
const filteredCategories = computed(() => {
  return categories.value.filter(category => category.type === activeTab.value && category.parentId === null)
})

// Lấy danh mục cha cho dropdown
const parentCategories = computed(() => {
  return categories.value.filter(category => 
    category.type === activeTab.value && 
    category.parentId === null
  )
})

// Lấy danh mục con
const getSubcategories = (parentId) => {
  return categories.value.filter(category => category.parentId === parentId)
}

// Quản lý modal thêm mới danh mục
const isAddCategoryModalOpen = ref(false)
const newCategory = ref({
  name: '',
  icon: filteredIcons.value[0]?.value || 'tags',
  color: filteredIcons.value[0]?.color || 'text-blue-500',
  type: activeTab.value,
  parentId: null
})

// Quản lý modal chỉnh sửa danh mục
const isEditCategoryModalOpen = ref(false)
const editingCategory = ref(null)

// Quản lý modal xóa danh mục
const isDeleteCategoryModalOpen = ref(false)
const deletingCategory = ref(null)

// Quản lý dropdown danh mục cha
const isParentDropdownOpen = ref(false)
const isEditParentDropdownOpen = ref(false)
const selectedParentCategory = ref(null)
const selectedEditParentCategory = ref(null)

// Chọn danh mục cha mới
const selectParentCategory = (category) => {
  newCategory.value.parentId = category ? category.id : null
  selectedParentCategory.value = category
  isParentDropdownOpen.value = false
}

// Chọn danh mục cha khi chỉnh sửa
const selectEditParentCategory = (category) => {
  editingCategory.value.parentId = category ? category.id : null
  selectedEditParentCategory.value = category
  isEditParentDropdownOpen.value = false
}

// Mở modal thêm mới danh mục
const openAddModal = () => {
  newCategory.value = {
    name: '',
    icon: filteredIcons.value[0]?.value || 'tags',
    color: filteredIcons.value[0]?.color || 'text-blue-500',
    type: activeTab.value,
    parentId: null
  }
  selectedParentCategory.value = null
  isAddCategoryModalOpen.value = true
}

// Cập nhật selectedEditParentCategory khi mở modal chỉnh sửa
const openEditModal = (category) => {
  editingCategory.value = { ...category }
  selectedEditParentCategory.value = category.parentId === null 
    ? null 
    : categories.value.find(c => c.id === category.parentId)
  isEditCategoryModalOpen.value = true
}

// Icon dropdown management
const isIconDropdownOpen = ref(false)
const isEditIconDropdownOpen = ref(false)

// Lấy icon đã chọn
const getSelectedIcon = (iconValue) => {
  const icon = availableIcons.value.find(icon => icon.value === iconValue)
  return icon || { value: 'tags', label: 'Mặc định', color: 'text-blue-500' }
}

// Cập nhật icon đã chọn
const updateSelectedIcon = (icon, type) => {
  if (type === 'new') {
    newCategory.value.icon = icon.value
    newCategory.value.color = icon.color
    isIconDropdownOpen.value = false
  } else {
    editingCategory.value.icon = icon.value
    editingCategory.value.color = icon.color
    isEditIconDropdownOpen.value = false
  }
}

// Thay đổi tab
const changeTab = (tabId) => {
  activeTab.value = tabId
  // Khởi tạo lại giá trị mặc định cho danh mục mới
  newCategory.value = {
    name: '',
    icon: filteredIcons.value[0]?.value || 'tags',
    color: filteredIcons.value[0]?.color || 'text-blue-500',
    type: activeTab.value,
    parentId: null
  }
  selectedParentCategory.value = null
}

// Kiểm tra xem một danh mục có phải là con (hoặc cháu...) của một danh mục khác hay không
const isDescendantOf = (potentialChild, potentialParentId) => {
  // Nếu không có danh mục cha tiềm năng, thì không phải là con cháu
  if (!potentialParentId) return false
  
  // Nếu danh mục con trực tiếp của danh mục cha
  if (potentialChild.parentId === potentialParentId) return true
  
  // Nếu không có danh mục cha, không phải là con cháu
  if (!potentialChild.parentId) return false
  
  // Tìm danh mục cha của danh mục hiện tại
  const parent = categories.value.find(cat => cat.id === potentialChild.parentId)
  if (!parent) return false
  
  // Kiểm tra đệ quy với danh mục cha
  return isDescendantOf(parent, potentialParentId)
}

// Kiểm tra xem một danh mục có thể được chọn làm danh mục cha cho danh mục đang chỉnh sửa hay không
const canBeParentOf = (potentialParent, category) => {
  // Không thể chọn chính nó làm danh mục cha
  if (potentialParent.id === category.id) return false
  
  // Không thể chọn con cháu làm danh mục cha (sẽ tạo chu trình)
  if (isDescendantOf(potentialParent, category.id)) return false
  
  // Phải cùng loại (thu/chi)
  if (potentialParent.type !== category.type) return false
  
  // Kiểm tra danh mục cha cần phải là danh mục gốc
  if (potentialParent.parentId !== null) return false
  
  return true
}

// Xử lý sự kiện click ngoài dropdown để đóng chúng
const handleClickOutside = (event) => {
  // Đóng dropdown chọn icon khi thêm mới
  const iconDropdownElement = document.querySelector('#icon-dropdown-container')
  if (isIconDropdownOpen.value && iconDropdownElement && !iconDropdownElement.contains(event.target)) {
    isIconDropdownOpen.value = false
  }
  
  // Đóng dropdown chọn danh mục cha khi thêm mới
  const parentDropdownElement = document.querySelector('#parent-dropdown-container')
  if (isParentDropdownOpen.value && parentDropdownElement && !parentDropdownElement.contains(event.target)) {
    isParentDropdownOpen.value = false
  }
  
  // Đóng dropdown chọn icon khi chỉnh sửa
  const editIconDropdownElement = document.querySelector('#edit-icon-dropdown-container')
  if (isEditIconDropdownOpen.value && editIconDropdownElement && !editIconDropdownElement.contains(event.target)) {
    isEditIconDropdownOpen.value = false
  }
  
  // Đóng dropdown chọn danh mục cha khi chỉnh sửa
  const editParentDropdownElement = document.querySelector('#edit-parent-dropdown-container')
  if (isEditParentDropdownOpen.value && editParentDropdownElement && !editParentDropdownElement.contains(event.target)) {
    isEditParentDropdownOpen.value = false
  }
}

// Đăng ký và hủy đăng ký sự kiện click ngoài
onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<template>
  <div class="p-4">
    <div class="flex justify-between items-center mb-6">
      <button 
        @click="openAddModal" 
        class="bg-primary hover:bg-primary/90 text-white px-4 py-2 rounded-lg flex items-center space-x-2 transition-colors"
      >
        <font-awesome-icon :icon="['fas', 'plus']" />
        <span>Thêm mới</span>
      </button>
    </div>

    <!-- Tabs -->
    <div class="bg-surface rounded-xl p-4 shadow-sm">
      <div class="border-b border-gray-200 mb-4">
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

      <!-- Danh sách danh mục -->
      <div>
        <div v-for="category in filteredCategories" :key="category.id" class="mb-6">
          <div class="flex items-center justify-between mb-2 bg-gray-50 p-3 rounded-lg">
            <div class="flex items-center space-x-3">
              <div class="w-10 h-10 rounded-full flex items-center justify-center" :class="`bg-${category.color.split('-')[1]}-100`">
                <font-awesome-icon :icon="['fas', category.icon]" :class="category.color" />
              </div>
              <h3 class="font-medium text-text">{{ category.name }}</h3>
            </div>
            <div class="flex space-x-2">
              <button 
                @click="openEditModal(category)"
                class="p-2 text-text-secondary hover:text-primary rounded-full hover:bg-gray-100"
                title="Chỉnh sửa"
              >
                <font-awesome-icon :icon="['fas', 'pen']" />
              </button>
              <button 
                @click="openDeleteModal(category)"
                class="p-2 text-text-secondary hover:text-danger rounded-full hover:bg-gray-100"
                title="Xóa"
              >
                <font-awesome-icon :icon="['fas', 'trash']" />
              </button>
            </div>
          </div>

          <!-- Danh mục con -->
          <div class="pl-8 space-y-2">
            <div 
              v-for="subCategory in getSubcategories(category.id)" 
              :key="subCategory.id"
              class="flex items-center justify-between p-3 rounded-lg bg-gray-50/70"
            >
              <div class="flex items-center space-x-3">
                <div class="w-8 h-8 rounded-full flex items-center justify-center" :class="`bg-${subCategory.color.split('-')[1]}-100`">
                  <font-awesome-icon :icon="['fas', subCategory.icon]" :class="subCategory.color" />
                </div>
                <h4 class="text-text">{{ subCategory.name }}</h4>
              </div>
              <div class="flex space-x-2">
                <button 
                  @click="openEditModal(subCategory)"
                  class="p-1.5 text-text-secondary hover:text-primary rounded-full hover:bg-gray-100"
                  title="Chỉnh sửa"
                >
                  <font-awesome-icon :icon="['fas', 'pen']" class="text-sm" />
                </button>
                <button 
                  @click="openDeleteModal(subCategory)"
                  class="p-1.5 text-text-secondary hover:text-danger rounded-full hover:bg-gray-100"
                  title="Xóa"
                >
                  <font-awesome-icon :icon="['fas', 'trash']" class="text-sm" />
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Hiển thị khi không có danh mục nào -->
        <div v-if="filteredCategories.length === 0" class="text-center py-8">
          <p class="text-text-secondary">Chưa có danh mục nào. Hãy thêm danh mục mới.</p>
        </div>
      </div>
    </div>

    <!-- Modal Thêm Danh Mục -->
    <Teleport to="body">
      <div v-if="isAddCategoryModalOpen" class="fixed inset-0 z-50 overflow-y-auto">
        <div class="flex items-center justify-center min-h-screen px-4 pt-4 pb-20 text-center sm:block sm:p-0">
          <div class="fixed inset-0 transition-opacity bg-gray-500 bg-opacity-75" @click="isAddCategoryModalOpen = false"></div>

          <span class="hidden sm:inline-block sm:align-middle sm:h-screen">&#8203;</span>

          <div class="inline-block overflow-hidden text-left align-bottom transition-all transform bg-white rounded-lg shadow-xl sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
            <div class="px-6 py-4 bg-white">
              <div class="flex items-center justify-between mb-4">
                <h3 class="text-lg font-medium leading-6 text-text">Thêm danh mục mới</h3>
                <button 
                  @click="isAddCategoryModalOpen = false"
                  class="text-text-secondary hover:text-text"
                >
                  <font-awesome-icon icon="times" />
                </button>
              </div>

              <div class="space-y-4">
                <!-- Tên danh mục -->
                <div>
                  <label class="block text-sm font-medium text-text-secondary mb-1">
                    Tên danh mục <span class="text-danger">*</span>
                  </label>
                  <input 
                    v-model="newCategory.name"
                    type="text"
                    class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 focus:border-primary/50"
                    placeholder="Nhập tên danh mục"
                  />
                </div>

                <!-- Chọn icon -->
                <div>
                  <label class="block text-sm font-medium text-text-secondary mb-1">
                    Icon <span class="text-danger">*</span>
                  </label>
                  <div class="relative select-none" id="icon-dropdown-container">
                    <div 
                      class="flex items-center w-full px-3 py-2 border border-gray-100 rounded-lg cursor-pointer hover:border-gray-200"
                      :class="{'ring-1 ring-primary/20 border-primary/50': isIconDropdownOpen}"
                      @click="isIconDropdownOpen = !isIconDropdownOpen"
                    >
                      <div class="flex items-center flex-1">
                        <div class="w-8 h-8 rounded-full flex items-center justify-center mr-2" :class="`bg-${getSelectedIcon(newCategory.icon).color.split('-')[1]}-100`">
                          <font-awesome-icon 
                            :icon="['fas', newCategory.icon]" 
                            :class="getSelectedIcon(newCategory.icon).color"
                          />
                        </div>
                        <span>{{ getSelectedIcon(newCategory.icon).label }}</span>
                      </div>
                      <font-awesome-icon 
                        :icon="['fas', 'chevron-down']" 
                        class="text-gray-400 ml-2 transition-transform"
                        :class="{'rotate-180': isIconDropdownOpen}"
                      />
                    </div>

                    <div 
                      v-if="isIconDropdownOpen"
                      class="absolute z-10 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg py-1"
                    >
                      <div class="max-h-48 overflow-y-auto">
                        <div 
                          v-for="icon in filteredIcons" 
                          :key="icon.value"
                          class="flex items-center px-3 py-2 cursor-pointer hover:bg-gray-50"
                          :class="{'bg-primary/5': icon.value === newCategory.icon}"
                          @click="updateSelectedIcon(icon, 'new')"
                        >
                          <div class="w-8 h-8 rounded-full flex items-center justify-center mr-2" :class="`bg-${icon.color.split('-')[1]}-100`">
                            <font-awesome-icon 
                              :icon="['fas', icon.value]" 
                              :class="icon.color"
                            />
                          </div>
                          <span>{{ icon.label }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Chọn danh mục cha -->
                <div>
                  <label class="block text-sm font-medium text-text-secondary mb-1">
                    Danh mục cha
                  </label>
                  <div class="relative select-none" id="parent-dropdown-container">
                    <div 
                      class="flex items-center w-full px-3 py-2 border border-gray-100 rounded-lg cursor-pointer hover:border-gray-200"
                      :class="{'ring-1 ring-primary/20 border-primary/50': isParentDropdownOpen}"
                      @click="isParentDropdownOpen = !isParentDropdownOpen"
                    >
                      <div class="flex items-center flex-1">
                        <span v-if="newCategory.parentId === null">Không có</span>
                        <template v-else>
                          <div 
                            v-for="category in parentCategories" 
                            :key="category.id"
                            v-show="category.id === newCategory.parentId"
                            class="flex items-center"
                          >
                            <div class="w-8 h-8 rounded-full flex items-center justify-center mr-2" :class="`bg-${category.color.split('-')[1]}-100`">
                              <font-awesome-icon 
                                :icon="['fas', category.icon]" 
                                :class="category.color"
                              />
                            </div>
                            <span>{{ category.name }}</span>
                          </div>
                        </template>
                      </div>
                      <font-awesome-icon 
                        :icon="['fas', 'chevron-down']" 
                        class="text-gray-400 ml-2 transition-transform"
                        :class="{'rotate-180': isParentDropdownOpen}"
                      />
                    </div>

                    <div 
                      v-if="isParentDropdownOpen"
                      class="absolute z-10 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg py-1"
                    >
                      <!-- Không có danh mục cha (root) -->
                      <div 
                        class="flex items-center px-3 py-2 cursor-pointer hover:bg-gray-50"
                        :class="{'bg-primary/5': newCategory.parentId === null}"
                        @click="selectParentCategory(null)"
                      >
                        <span>Không có</span>
                      </div>

                      <!-- Danh sách danh mục cha -->
                      <div 
                        v-for="category in parentCategories" 
                        :key="category.id"
                        class="flex items-center px-3 py-2 cursor-pointer hover:bg-gray-50"
                        :class="{'bg-primary/5': category.id === newCategory.parentId}"
                        @click="selectParentCategory(category)"
                      >
                        <div class="w-8 h-8 rounded-full flex items-center justify-center mr-2" :class="`bg-${category.color.split('-')[1]}-100`">
                          <font-awesome-icon 
                            :icon="['fas', category.icon]" 
                            :class="category.color"
                          />
                        </div>
                        <span>{{ category.name }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="px-6 py-4 bg-gray-50 flex justify-end space-x-3">
              <button 
                @click="isAddCategoryModalOpen = false" 
                class="px-4 py-2 text-text-secondary bg-white border border-gray-200 rounded-lg hover:bg-gray-50"
              >
                Hủy
              </button>
              <button 
                @click="handleAddCategory" 
                class="px-4 py-2 text-white bg-primary rounded-lg hover:bg-primary/90"
              >
                Thêm mới
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Modal Chỉnh Sửa Danh Mục -->
    <Teleport to="body">
      <div v-if="isEditCategoryModalOpen" class="fixed inset-0 z-50 overflow-y-auto">
        <div class="flex items-center justify-center min-h-screen px-4 pt-4 pb-20 text-center sm:block sm:p-0">
          <div class="fixed inset-0 transition-opacity bg-gray-500 bg-opacity-75" @click="isEditCategoryModalOpen = false"></div>

          <span class="hidden sm:inline-block sm:align-middle sm:h-screen">&#8203;</span>

          <div class="inline-block overflow-hidden text-left align-bottom transition-all transform bg-white rounded-lg shadow-xl sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
            <div class="px-6 py-4 bg-white">
              <div class="flex items-center justify-between mb-4">
                <h3 class="text-lg font-medium leading-6 text-text">Chỉnh sửa danh mục</h3>
                <button 
                  @click="isEditCategoryModalOpen = false"
                  class="text-text-secondary hover:text-text"
                >
                  <font-awesome-icon icon="times" />
                </button>
              </div>

              <div v-if="editingCategory" class="space-y-4">
                <!-- Tên danh mục -->
                <div>
                  <label class="block text-sm font-medium text-text-secondary mb-1">
                    Tên danh mục <span class="text-danger">*</span>
                  </label>
                  <input 
                    v-model="editingCategory.name"
                    type="text"
                    class="w-full px-3 py-2 border border-gray-100 rounded-lg focus:outline-none focus:ring-1 focus:ring-primary/20 focus:border-primary/50"
                    placeholder="Nhập tên danh mục"
                  />
                </div>

                <!-- Chọn icon -->
                <div>
                  <label class="block text-sm font-medium text-text-secondary mb-1">
                    Icon <span class="text-danger">*</span>
                  </label>
                  <div class="relative select-none" id="edit-icon-dropdown-container">
                    <div 
                      class="flex items-center w-full px-3 py-2 border border-gray-100 rounded-lg cursor-pointer hover:border-gray-200"
                      :class="{'ring-1 ring-primary/20 border-primary/50': isEditIconDropdownOpen}"
                      @click="isEditIconDropdownOpen = !isEditIconDropdownOpen"
                    >
                      <div class="flex items-center flex-1">
                        <div class="w-8 h-8 rounded-full flex items-center justify-center mr-2" :class="`bg-${getSelectedIcon(editingCategory.icon).color.split('-')[1]}-100`">
                          <font-awesome-icon 
                            :icon="['fas', editingCategory.icon]" 
                            :class="getSelectedIcon(editingCategory.icon).color"
                          />
                        </div>
                        <span>{{ getSelectedIcon(editingCategory.icon).label }}</span>
                      </div>
                      <font-awesome-icon 
                        :icon="['fas', 'chevron-down']" 
                        class="text-gray-400 ml-2 transition-transform"
                        :class="{'rotate-180': isEditIconDropdownOpen}"
                      />
                    </div>

                    <div 
                      v-if="isEditIconDropdownOpen"
                      class="absolute z-10 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg py-1"
                    >
                      <div class="max-h-48 overflow-y-auto">
                        <div 
                          v-for="icon in filteredIcons" 
                          :key="icon.value"
                          class="flex items-center px-3 py-2 cursor-pointer hover:bg-gray-50"
                          :class="{'bg-primary/5': icon.value === editingCategory.icon}"
                          @click="updateSelectedIcon(icon, 'edit')"
                        >
                          <div class="w-8 h-8 rounded-full flex items-center justify-center mr-2" :class="`bg-${icon.color.split('-')[1]}-100`">
                            <font-awesome-icon 
                              :icon="['fas', icon.value]" 
                              :class="icon.color"
                            />
                          </div>
                          <span>{{ icon.label }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Chọn danh mục cha -->
                <div>
                  <label class="block text-sm font-medium text-text-secondary mb-1">
                    Danh mục cha
                  </label>
                  <div class="relative select-none" id="edit-parent-dropdown-container">
                    <div 
                      class="flex items-center w-full px-3 py-2 border border-gray-100 rounded-lg cursor-pointer hover:border-gray-200"
                      :class="{'ring-1 ring-primary/20 border-primary/50': isEditParentDropdownOpen}"
                      @click="isEditParentDropdownOpen = !isEditParentDropdownOpen"
                    >
                      <div class="flex items-center flex-1">
                        <span v-if="editingCategory.parentId === null">Không có</span>
                        <template v-else>
                          <div 
                            v-for="category in parentCategories" 
                            :key="category.id"
                            v-show="category.id === editingCategory.parentId"
                            class="flex items-center"
                          >
                            <div class="w-8 h-8 rounded-full flex items-center justify-center mr-2" :class="`bg-${category.color.split('-')[1]}-100`">
                              <font-awesome-icon 
                                :icon="['fas', category.icon]" 
                                :class="category.color"
                              />
                            </div>
                            <span>{{ category.name }}</span>
                          </div>
                        </template>
                      </div>
                      <font-awesome-icon 
                        :icon="['fas', 'chevron-down']" 
                        class="text-gray-400 ml-2 transition-transform"
                        :class="{'rotate-180': isEditParentDropdownOpen}"
                      />
                    </div>

                    <div 
                      v-if="isEditParentDropdownOpen"
                      class="absolute z-10 w-full mt-1 bg-white border border-gray-200 rounded-lg shadow-lg py-1"
                    >
                      <!-- Không có danh mục cha (root) -->
                      <div 
                        class="flex items-center px-3 py-2 cursor-pointer hover:bg-gray-50"
                        :class="{'bg-primary/5': editingCategory.parentId === null}"
                        @click="selectEditParentCategory(null)"
                      >
                        <span>Không có</span>
                      </div>

                      <!-- Danh sách danh mục cha (lọc theo điều kiện phù hợp) -->
                      <div 
                        v-for="category in parentCategories" 
                        :key="category.id"
                        v-if="canBeParentOf(category, editingCategory)"
                        class="flex items-center px-3 py-2 cursor-pointer hover:bg-gray-50"
                        :class="{'bg-primary/5': category.id === editingCategory.parentId}"
                        @click="selectEditParentCategory(category)"
                      >
                        <div class="w-8 h-8 rounded-full flex items-center justify-center mr-2" :class="`bg-${category.color.split('-')[1]}-100`">
                          <font-awesome-icon 
                            :icon="['fas', category.icon]" 
                            :class="category.color"
                          />
                        </div>
                        <span>{{ category.name }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="px-6 py-4 bg-gray-50 flex justify-end space-x-3">
              <button 
                @click="isEditCategoryModalOpen = false" 
                class="px-4 py-2 text-text-secondary bg-white border border-gray-200 rounded-lg hover:bg-gray-50"
              >
                Hủy
              </button>
              <button 
                @click="handleUpdateCategory" 
                class="px-4 py-2 text-white bg-primary rounded-lg hover:bg-primary/90"
              >
                Cập nhật
              </button>
            </div>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Modal Xóa Danh Mục -->
    <Teleport to="body">
      <div v-if="isDeleteCategoryModalOpen" class="fixed inset-0 z-50 overflow-y-auto">
        <div class="flex items-center justify-center min-h-screen px-4 pt-4 pb-20 text-center sm:block sm:p-0">
          <div class="fixed inset-0 transition-opacity bg-gray-500 bg-opacity-75" @click="isDeleteCategoryModalOpen = false"></div>

          <span class="hidden sm:inline-block sm:align-middle sm:h-screen">&#8203;</span>

          <div class="inline-block overflow-hidden text-left align-bottom transition-all transform bg-white rounded-lg shadow-xl sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
            <div class="px-6 py-4 bg-white">
              <div class="flex items-center justify-center mb-4">
                <div class="mx-auto flex items-center justify-center h-14 w-14 rounded-full bg-danger/10">
                  <font-awesome-icon icon="trash" class="h-6 w-6 text-danger" />
                </div>
              </div>

              <div class="mt-3 text-center sm:mt-0 sm:text-center">
                <h3 class="text-lg font-medium leading-6 text-text">
                  Xác nhận xóa
                </h3>
                <div class="mt-2">
                  <p class="text-sm text-text-secondary">
                    Bạn có chắc chắn muốn xóa danh mục "{{ deletingCategory?.name }}"? Hành động này không thể hoàn tác.
                  </p>
                </div>
              </div>
            </div>

            <div class="px-6 py-4 bg-gray-50 flex justify-center space-x-3">
              <button 
                @click="isDeleteCategoryModalOpen = false" 
                class="px-4 py-2 text-text-secondary bg-white border border-gray-200 rounded-lg hover:bg-gray-50"
              >
                Hủy
              </button>
              <button 
                @click="handleDeleteCategory" 
                class="px-4 py-2 text-white bg-danger rounded-lg hover:bg-danger/90"
              >
                Xóa
              </button>
            </div>
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