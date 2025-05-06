import { defineStore } from 'pinia'

export const useLayoutStore = defineStore({
  id: 'layout',
  state: () => ({
    apiLoadingCount: 0,
    apiLoadingFileCount: 0,
  }),
  actions: {
    incrementApiLoadingCount() {
      this.apiLoadingCount++
    },
    decrementApiLoadingCount() {
      setTimeout(() => {
        this.apiLoadingCount--
      }, 50)
    },
  },
})
