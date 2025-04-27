import { defineStore } from "pinia";
import {base} from "@/apis/ApiService.js"
import { formatDateStringToDate } from "@/utils/DateUtil.js";

export const useExpenseLimitStore = defineStore("expenseLimit", {
  state: () => {
    return {
      expenseLimits: [],
      pagination: {
        field: "startDateLimit",
        pageNumber: 1,
        pageSize: 5,
        sort: "desc",
        search: "",
        totalElements: 0,
        totalPages: 0,
      },
      categoriesId: null,
      bucketPaymentIds: null
    };
  },
  getters: {
    getCurrentStartDate: (state) => (expenseLimit) => {
      if (!expenseLimit?.startDate) return null;
      
      const originalStartDate = new Date(expenseLimit.startDate);
      const today = new Date();
      let currentStartDate = new Date(originalStartDate);
      
      // Nếu không lặp lại, trả về ngày bắt đầu gốc
      if (!expenseLimit.repeatTime || expenseLimit.repeatTime === "" || expenseLimit.repeatTime === "Không lặp lại") {
        return expenseLimit.startDate;
      }
      
      // Nếu ngày hôm nay trước ngày bắt đầu, trả về ngày bắt đầu gốc
      if (today < originalStartDate) {
        return expenseLimit.startDate;
      }
      
      // Tính toán chu kỳ hiện tại
      let nextEndDate;
      
      while (true) {
        switch (expenseLimit.repeatTime) {
          case "daily":
          case "Hàng ngày":
            nextEndDate = new Date(currentStartDate);
            nextEndDate.setDate(nextEndDate.getDate() + 1);
            break;
          case "weekly":
          case "Hàng tuần":
            nextEndDate = new Date(currentStartDate);
            nextEndDate.setDate(nextEndDate.getDate() + 7);
            break;
          case "monthly":
          case "Hàng tháng":
            nextEndDate = new Date(currentStartDate);
            nextEndDate.setMonth(nextEndDate.getMonth() + 1);
            break;
          case "quarterly":
          case "Hàng quý":
            nextEndDate = new Date(currentStartDate);
            nextEndDate.setMonth(nextEndDate.getMonth() + 3);
            break;
          case "yearly":
          case "Hàng năm":
            nextEndDate = new Date(currentStartDate);
            nextEndDate.setFullYear(nextEndDate.getFullYear() + 1);
            break;
          default:
            return expenseLimit.startDate;
        }
        
        // Chuẩn hóa giờ để so sánh chính xác ngày
        const todayStart = new Date(today);
        todayStart.setHours(0,0,0,0);
        
        const currentStartDay = new Date(currentStartDate);
        currentStartDay.setHours(0,0,0,0);
        
        const nextEndDay = new Date(nextEndDate);
        nextEndDay.setHours(0,0,0,0);

        // Nếu ngày hôm nay nằm trong chu kỳ hiện tại
        if (todayStart >= currentStartDay && todayStart < nextEndDay) {
          // Sử dụng phương thức định dạng ngày không phụ thuộc múi giờ
          const year = currentStartDate.getFullYear();
          const month = String(currentStartDate.getMonth() + 1).padStart(2, '0');
          const day = String(currentStartDate.getDate()).padStart(2, '0');
          return `${year}-${month}-${day} 00:00:00`;
        }
        
        // Nếu ngày hôm nay trước chu kỳ hiện tại
        if (todayStart < currentStartDay) {
          const year = currentStartDate.getFullYear();
          const month = String(currentStartDate.getMonth() + 1).padStart(2, '0');
          const day = String(currentStartDate.getDate()).padStart(2, '0');
          return `${year}-${month}-${day} 00:00:00`;
        }
        
        // Di chuyển đến chu kỳ tiếp theo
        currentStartDate = new Date(nextEndDate);
      }
    },

    getEndDate: (state) => (startDate, repeatTime, endDate) => {
      if (!startDate) return null;
      if (repeatTime === "" || repeatTime === "Không lặp lại") return endDate;

      const originalStartDate = new Date(startDate);
      const today = new Date();
      let currentStartDate = new Date(originalStartDate);
      let nextEndDate;

      while (true) {
        switch (repeatTime) {
          case "daily":
          case "Hàng ngày":
            nextEndDate = new Date(currentStartDate);
            nextEndDate.setDate(nextEndDate.getDate() + 1);
            break;
          case "weekly":
          case "Hàng tuần":
            nextEndDate = new Date(currentStartDate);
            nextEndDate.setDate(nextEndDate.getDate() + 7);
            break;
          case "monthly":
          case "Hàng tháng":
            nextEndDate = new Date(currentStartDate);
            nextEndDate.setMonth(nextEndDate.getMonth() + 1);
            break;
          case "quarterly":
          case "Hàng quý":
            nextEndDate = new Date(currentStartDate);
            nextEndDate.setMonth(nextEndDate.getMonth() + 3);
            break;
          case "yearly":
          case "Hàng năm":
            nextEndDate = new Date(currentStartDate);
            nextEndDate.setFullYear(nextEndDate.getFullYear() + 1);
            break;
          default:
            return endDate;
        }

        // Chuẩn hóa giờ để so sánh chính xác ngày
        const todayStart = new Date(today);
        todayStart.setHours(0,0,0,0);
        
        const currentStartDay = new Date(currentStartDate);
        currentStartDay.setHours(0,0,0,0);
        
        const nextEndDay = new Date(nextEndDate);
        nextEndDay.setHours(0,0,0,0);

        // Nếu ngày hôm nay nằm trong chu kỳ hiện tại
        if (todayStart >= currentStartDay && todayStart < nextEndDay) {
          // Đảm bảo trả về đúng ngày mà không bị ảnh hưởng bởi múi giờ
          const year = nextEndDate.getFullYear();
          const month = String(nextEndDate.getMonth() + 1).padStart(2, '0');
          const day = String(nextEndDate.getDate()).padStart(2, '0');
          return `${year}-${month}-${day} 23:59:59`;
        }

        // Nếu ngày hôm nay trước chu kỳ hiện tại
        if (todayStart < currentStartDay) {
          const year = nextEndDate.getFullYear();
          const month = String(nextEndDate.getMonth() + 1).padStart(2, '0');
          const day = String(nextEndDate.getDate()).padStart(2, '0');
          return `${year}-${month}-${day} 23:59:59`;
        }

        // Di chuyển đến chu kỳ tiếp theo
        currentStartDate = new Date(nextEndDate);
      }
    },

    remainingDays: (state) => (expenseLimit) => {
      if (!expenseLimit.startDate) return 0;
      const today = new Date();
      const end = new Date(state.getEndDate(
        expenseLimit.startDate,
        expenseLimit.repeatTime,
        expenseLimit.endDate
      ));
      const diffTime = end - today;
      return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
    },

    remainingBudget: (state) => (expenseLimit) => {
      if (!expenseLimit.amount || !expenseLimit.totalExpense) return 0;
      return expenseLimit.amount - (expenseLimit.totalExpense || 0);
    },

    calculateProgress: (state) => (expenseLimit) => {
      if (!expenseLimit.currentStartDate || !expenseLimit.currentEndDate) return 0;
      
      const startDate = new Date(expenseLimit.currentStartDate);
      const endDate = new Date(expenseLimit.currentEndDate);
      const today = new Date();
      
      const totalDays = Math.ceil((endDate - startDate) / (1000 * 60 * 60 * 24));
      
      const daysPassed = Math.ceil((today - startDate) / (1000 * 60 * 60 * 24));
      
      const progress = (daysPassed / totalDays) * 100;
      
      return Math.min(Math.max(progress, 0), 100);
    },

    formatDateRange: (state) => (expenseLimit) => {
      if (!expenseLimit.startDate) return '';
    
      const startDate = new Date(expenseLimit.currentStartDate);
      const endDate = new Date(state.getEndDate(
        expenseLimit.currentStartDate,
        expenseLimit.repeatTime,
        expenseLimit.endDate
      ));
    
      const showYear = startDate.getFullYear() !== endDate.getFullYear();
      return `${formatDateStringToDate(startDate.toISOString(), showYear)} - ${formatDateStringToDate(endDate.toISOString(), showYear)}`;
    },
  },
  actions: {
    async getExpenseLimits(){
      let response = null;
      await base.get(`/expense-limit`).then((res) => {
        response = res.result;
      })
      return response;
    },
    async getExpenseLimitsPagination(){
      let response = null;
      let request = {
        ...this.pagination,
        categoriesId: this.categoriesId,
        bucketPaymentIds: this.bucketPaymentIds,
      }
      await base.post(`/expense-limit/pagination`, request).then((res) => {
        response = res.result;
        this.expenseLimits = response.content;
        this.pagination.totalElements = response.totalElements;
        this.pagination.totalPages = response.totalPages;
      })
      return response;
    },
    async getExpenseLimitByExpenseLimitId(expenseLimitId){
      let response = null;
      await base.get(`/expense-limit/${expenseLimitId}`).then((res) => {
        response = res.result;
      })
      return response;
    },
    async getExpenseLimitDetailByExpenseLimitId(expenseLimitId, startDate, endDate){
      let response = null;
      await base.get(`/expense-limit/${expenseLimitId}/detail?startDate=${startDate}&endDate=${endDate}`).then((res) => {
        response = res.result;
      })
      return response;
    },
    async createExpenseLimit(expenseLimit){
      let response = null;
      let requestBody = {
        ...expenseLimit,
        startDate: expenseLimit.startDate,
        endDate: expenseLimit.endDate,
      };
      await base.post(`/expense-limit`, requestBody).then((res) => {
        response = res.result;
      })
      return response;
    },
    async updateExpenseLimit(expenseLimit){
      let response = null;
      let requestBody = {
        ...expenseLimit,
        startDate: expenseLimit.startDate,
        endDate: expenseLimit.endDate,
      };
      await base.put(`/expense-limit/${expenseLimit.id}`, requestBody).then((res) => {
        response = res.result;
      })
      return response;
    },
    async deleteExpenseLimit(expenseLimitId){
      let response = null;
      await base.delete(`/expense-limit/${expenseLimitId}`).then((res) => {
        response = res.result;
      })
      return response;
    }
  },
});
