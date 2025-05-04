import { defineStore } from "pinia";
import {base} from "@/apis/ApiService.js"
import { formatDate } from "@/utils/DateUtil.js";

export const useReportStore = defineStore("report", {
  state: () => {
    return {
      totalExpense: 0,
      totalRevenue: 0,
    };
  },
  getters: {
  },
  actions: {
    async getReportTransactionType(filters) {
      let response = null;
      let bucketPaymentIds = (filters.account === null || filters.account.length === 0 || filters.account[0] === "all") ? null : filters.account.join(",");
      let request = {
        timeOption: filters.timeOption,
        bucketPaymentIds,
        customTimeRange: filters.customTimeRange,
      }
      await base
        .post("/report/transaction-type", request)
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async getReportExpenseCategory(filters) {
      let response = null;
      let bucketPaymentIds = (filters.account === null || filters.account.length === 0 || filters.account[0] === "all") ? null : filters.account.join(",");
      let categoriesId = (filters.expenseCategory === null || filters.expenseCategory.length === 0 || filters.expenseCategory[0] === "all") ? null : filters.expenseCategory.join(",");
      let request = {
        timeOption: filters.timeOption,
        bucketPaymentIds,
        categoriesId,
        customTimeRange: filters.customTimeRange,
      }
      await base
        .post("/report/expense-category", request)
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async getReportRevenueCategory(filters) {
      let response = null;
      let bucketPaymentIds = (filters.account === null || filters.account.length === 0 || filters.account[0] === "all") ? null : filters.account.join(",");
      let categoriesId = (filters.revenueCategory === null || filters.revenueCategory.length === 0 || filters.revenueCategory[0] === "all") ? null : filters.revenueCategory.join(",");
      let request = {
        timeOption: filters.timeOption,
        bucketPaymentIds,
        categoriesId,
        customTimeRange: filters.customTimeRange,
      }
      await base
        .post("/report/revenue-category", request)
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    }
  },
});
