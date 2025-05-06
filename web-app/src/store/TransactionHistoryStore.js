import { defineStore } from "pinia";
import {base} from "@/apis/ApiService.js"
import { formatDateStringToDate } from "@/utils/DateUtil.js";

export const useTransactionHistoryStore = defineStore("transactionHistory", {
  state: () => {
    return {
      transactionHistory: [],
      pagination: {
        field: "date",
        pageNumber: 1,
        pageSize: 10,
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
      let request = {
        timeOption: filters.timeOption,
        transactionType: filters.transactionType,
        bucketPaymentIds,
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
  },
});
