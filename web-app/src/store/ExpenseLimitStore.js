import { defineStore } from "pinia";
import {base} from "@/apis/ApiService.js"
import { formatDateStringToDate } from "@/utils/DateUtil.js";

export const useExpenseLimitStore = defineStore("expenseLimit", {
  state: () => {
    return {
      expenseLimit: null,
    };
  },
  getters: {
    getCurrentStartDate: (state) => (expenseLimit) => {
      if (!expenseLimit?.startDate) return null;

      const originalStartDate = new Date(expenseLimit.startDate);
      const today = new Date();
      let currentStartDate = new Date(originalStartDate);
      let nextEndDate;

      while (true) {
        switch (expenseLimit.repeatTime) {
          case "Hàng ngày":
            nextEndDate = new Date(currentStartDate);
            nextEndDate.setDate(nextEndDate.getDate() + 1);
            break;
          case "Hàng tuần":
            nextEndDate = new Date(currentStartDate);
            nextEndDate.setDate(nextEndDate.getDate() + 7);
            break;
          case "Hàng tháng":
            nextEndDate = new Date(currentStartDate);
            nextEndDate.setMonth(nextEndDate.getMonth() + 1);
            break;
          case "Hàng quý":
            nextEndDate = new Date(currentStartDate);
            nextEndDate.setMonth(nextEndDate.getMonth() + 3);
            break;
          case "Hàng năm":
            nextEndDate = new Date(currentStartDate);
            nextEndDate.setFullYear(nextEndDate.getFullYear() + 1);
            break;
          default:
            return expenseLimit.startDate;
        }

        if (today >= currentStartDate && today < nextEndDate) {
          return currentStartDate.toISOString().split('T')[0];
        }

        if (today < currentStartDate) {
          return currentStartDate.toISOString().split('T')[0];
        }

        currentStartDate = new Date(nextEndDate);
      }
    },

    getEndDate: (state) => (startDate, repeatTime, endDate) => {
      if (!startDate) return null;
      if (repeatTime === "Không lặp lại") return endDate;

      const originalStartDate = new Date(startDate);
      const today = new Date();
      let currentStartDate = new Date(originalStartDate);
      let nextEndDate;

      while (true) {
        switch (repeatTime) {
          case "Hàng ngày":
            nextEndDate = new Date(currentStartDate);
            nextEndDate.setDate(nextEndDate.getDate() + 1);
            break;
          case "Hàng tuần":
            nextEndDate = new Date(currentStartDate);
            nextEndDate.setDate(nextEndDate.getDate() + 7);
            break;
          case "Hàng tháng":
            nextEndDate = new Date(currentStartDate);
            nextEndDate.setMonth(nextEndDate.getMonth() + 1);
            break;
          case "Hàng quý":
            nextEndDate = new Date(currentStartDate);
            nextEndDate.setMonth(nextEndDate.getMonth() + 3);
            break;
          case "Hàng năm":
            nextEndDate = new Date(currentStartDate);
            nextEndDate.setFullYear(nextEndDate.getFullYear() + 1);
            break;
        }

        if (today >= currentStartDate && today < nextEndDate) {
          return nextEndDate.toISOString().split('T')[0];
        }

        if (today < currentStartDate) {
          return nextEndDate.toISOString().split('T')[0];
        }

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
  },
});
