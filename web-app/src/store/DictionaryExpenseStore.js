import { defineStore } from "pinia";
import {base} from "@/apis/ApiService.js"
import { formatDate } from "@/utils/DateUtil.js";

export const useDictionaryExpenseStore = defineStore("dictionaryExpense", {
  state: () => {
    return {
      totalExpense: 0,
      totalRevenue: 0,
    };
  },
  getters: {},
  actions: {
    async createExpenseCategory(data) {
      let response = null;
      await base
        .post("/dictionary-expense", data)
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async update(id, data) {
      let response = null;
      await base
        .patch("/dictionary-expense/" + id, data)
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async deleteById(id) {
      let response = null;
      await base
        .delete("/dictionary-expense/" + id)
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async getMyExpenseCategories() {
      let response = null;
      await base
        .get("/dictionary-expense")
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async getMyExpenseCategoriesWithoutTransfer() {
      let response = null;
      await base
        .get("/dictionary-expense/without-transfer")
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async getExpenseCategoryById(id) {
      let response = null;
      await base
        .get("/dictionary-expense/" + id)
        .then((res) => {
          response = res.result;
        })
        .catch((error) => console.log(error));
      return response;
    },
  },
});
