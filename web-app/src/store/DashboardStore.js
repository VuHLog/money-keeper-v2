import { defineStore } from "pinia";
import {base} from "@/apis/ApiService.js"

export const useDashboardStore = defineStore("dashboard", {
  state: () => {
    return {
      totalExpense: 0,
      totalRevenue: 0,
    };
  },
  getters: {},
  actions: {
    async getMyTotalBalance(){
      let response = null;
      await base
        .get("/dashboard/my-total-balance")
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async getTotalExpenseRevenueThisMonth(){
      let response = null;
      await base
        .get("/dashboard/total-transaction-this-month")
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
  },
});
