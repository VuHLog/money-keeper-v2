import { defineStore } from "pinia";
import {base} from "@/apis/ApiService.js"
import { formatDate } from "@/utils/DateUtil.js";

export const useTransactionStore = defineStore("transaction", {
  state: () => {
    return {
      totalExpense: 0,
      totalRevenue: 0,
    };
  },
  getters: {
  },
  actions: {
    async getTotalExpense(timeOption, startDate, endDate){
      let response = null;
      const request = {
        bucketPaymentId: null,
        timeOption: timeOption,
        startDate: formatDate(startDate),
        endDate: formatDate(endDate)
      }
      await base.get("/transaction/total-expense", request).then((res) => {
        response = res.result;
      })
      return response;
    },
    async getTotalRevenue(timeOption, startDate, endDate){
      let response = null;
      const request = {
        bucketPaymentId: null,
        timeOption: timeOption,
        startDate: formatDate(startDate),
        endDate: formatDate(endDate)
      }
      await base.get("/transaction/total-revenue", request).then((res) => {
        response = res.result;
      })
      return response;
    },
    async getTransactionHistory(timeOption, startDate, endDate){
      let response = null;
      const request = {
        bucketPaymentId: null,
        timeOption: timeOption,
        startDate: formatDate(startDate),
        endDate: formatDate(endDate)
      }
      await base.get("/transaction/history", request).then((res) => {
        response = res.result;
      })
      return response;
    },
  },
});
