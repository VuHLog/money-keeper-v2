import { defineStore } from "pinia";
import {base} from "@/apis/ApiService.js"

export const useExpenseRegularStore = defineStore("expenseRegular", {
  state: () => {
    return {
      expenseRegular: null,
    };
  },
  getters: {
  },
  actions: {
    async createTransfer(data) {
      let response = null;
      await base.post("/expense-regular/transfer", data).then((res) => {
        response = res.result;
      });
      return response;
    },
    async createExpenseRegular(data) {
      let response = null;
      await base.post("/expense-regular", data).then((res) => {
        response = res.result;
      });
      return response;
    },
    async getTotalExpenseByExpenseLimit(expenseLimitId, startDate, endDate){
      let response = null;
      await base.get(`/expense-regular/total-by-expense-limit?expenseLimitId=${expenseLimitId}&startDate=${startDate}&endDate=${endDate}`).then((res) => {
        response = res.result;
      })
      return response;
    },
  },
});
