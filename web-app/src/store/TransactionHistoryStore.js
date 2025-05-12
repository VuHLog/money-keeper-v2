import { defineStore } from "pinia";
import {base} from "@/apis/ApiService.js"
import { formatDateStringToDate } from "@/utils/DateUtil.js";
import { instance } from "@/apis/ApiService.js";

export const useTransactionHistoryStore = defineStore("transactionHistory", {
  state: () => {
    return {
      transactionHistory: [],
      pagination: {
        field: "date",
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
    
  },
  actions: {
    async getAllTransactionHistory(filters){
      let response = null;
      let bucketPaymentIds = (filters.account === null || filters.account.length === 0 || filters.account[0] === "all") ? null : filters.account.join(",");
      let expenseCategoriesId = (filters.expenseCategory === null || filters.expenseCategory.length === 0 || filters.expenseCategory[0] === "all") ? null : filters.expenseCategory.join(",");
      let revenueCategoriesId = (filters.revenueCategory === null || filters.revenueCategory.length === 0 || filters.revenueCategory[0] === "all") ? null : filters.revenueCategory.join(",");
      let request = {
        timeOption: filters.timeOption,
        transactionType: filters.transactionType,
        bucketPaymentIds,
        expenseCategoriesId,
        revenueCategoriesId,
        customTimeRange: filters.customTimeRange,
      }
      await base.post(`/report/transaction-history?pageNumber=${this.pagination.pageNumber - 1}&pageSize=${this.pagination.pageSize}`, request).then((res) => {
        response = res.result;
        this.transactionHistory = response.content;
        this.pagination.totalElements = response.totalElements;
        this.pagination.totalPages = response.totalPages;
      })
      return response.content;
    },
    resetPagination(){
      this.pagination.pageNumber = 1;
      this.pagination.pageSize = 5;
      this.pagination.sort = "desc";
      this.pagination.search = "";
      this.pagination.totalElements = 0;
      this.pagination.totalPages = 0;
    },
    async exportExcel(filters){
      let response = null;
      let bucketPaymentIds = (filters.account === null || filters.account.length === 0 || filters.account[0] === "all") ? null : filters.account.join(",");
      let expenseCategoriesId = (filters.expenseCategory === null || filters.expenseCategory.length === 0 || filters.expenseCategory[0] === "all") ? null : filters.expenseCategory.join(",");
      let revenueCategoriesId = (filters.revenueCategory === null || filters.revenueCategory.length === 0 || filters.revenueCategory[0] === "all") ? null : filters.revenueCategory.join(",");
      let request = {
        timeOption: filters.timeOption,
        transactionType: filters.transactionType,
        bucketPaymentIds,
        expenseCategoriesId,
        revenueCategoriesId,
        customTimeRange: filters.customTimeRange,
      }
      try {
        response = await instance.post(`/report/transaction-history/export-excel`,request, {
          responseType: 'blob',
        });
        const blob = new Blob([response.data], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        });
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        link.download = 'Lịch sử giao dịch' + (filters.timeOption && filters.timeOption !== 'Tùy chọn' ? ' ' + filters.timeOption.toLowerCase() : '') + '.xlsx';
        document.body.appendChild(link);
        link.click();
        link.remove();
      } catch (error) {
        return Promise.reject(error);
      }
    }
  },
});
