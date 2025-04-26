import { defineStore } from "pinia";
import {base} from "@/apis/ApiService.js"
import { formatDate } from "@/utils/DateUtil.js";

export const useDictionaryRevenueStore = defineStore("dictionaryRevenue", {
  state: () => {
    return {
    };
  },
  getters: {},
  actions: {
    async createRevenueCategory(data) {
      let response = null;
      await base
        .post("/dictionary-revenue", data)
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
        .put("/dictionary-revenue/" + id, data)
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
        .delete("/dictionary-revenue/" + id)
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async getMyRevenueCategories() {
      let response = null;
      await base
        .get("/dictionary-revenue")
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async getMyRevenueCategoriesWithoutTransfer() {
      let response = null;
      await base
        .get("/dictionary-revenue/without-transfer")
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
        .get("/dictionary-revenue/" + id)
        .then((res) => {
          response = res.result;
        })
        .catch((error) => console.log(error));
      return response;
    },
  },
});
