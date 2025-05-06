import { defineStore } from "pinia";
import {base} from "@/apis/ApiService.js"

export const useExpenseRegularStore = defineStore("expenseRegular", {
  state: () => {
    return {
      expenseRegulars: [],
      pagination: {
        field: "modifiedDate",
        pageNumber: 1,
        pageSize: 5,
        sort: "desc",
        search: "",
        totalElements: 0,
        totalPages: 0,
      },
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
    async updateExpenseRegular(id, data) {
      let response = null;
      await base.put("/expense-regular/" + id, data).then((res) => {
        response = res.result;
      });
      return response;
    },
    async deleteExpenseRegular(id) {
      let response = null;
      await base.delete("/expense-regular/" + id).then((res) => {
        response = res.result;
      });
      return response;
    },
    async getExpenseRegularById(id) {
      let response = null;
      await base.get("/expense-regular/" + id).then((res) => {
        response = res.result;
      });
      return response;
    },
    async getAllExpenseRegularPagination() {
      let response = null;
      await base.get("/expense-regular/pagination", {
          field: this.pagination.field,
          pageNumber: this.pagination.pageNumber - 1,
          pageSize: this.pagination.pageSize,
          sort: this.pagination.sort,
          search: this.pagination.search,
        }).then((res) => {
          response = res.result;
          this.expenseRegulars = response.content;
          this.pagination.totalElements = response.totalElements;
          this.pagination.totalPages = response.totalPages;
        });
      return response.content;
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
