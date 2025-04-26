import { defineStore } from "pinia";
import {base} from "@/apis/ApiService.js"

export const useRevenueRegularStore = defineStore("revenueRegular", {
  state: () => {
    return {
      revenueRegulars: [],
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
    async createRevenueRegular(data) {
      let response = null;
      await base.post("/revenue-regular", data).then((res) => {
        response = res.result;
      });
      return response;
    },
    async updateRevenueRegular(id, data) {
      let response = null;
      await base.put("/revenue-regular/" + id, data).then((res) => {
        response = res.result;
      });
      return response;
    },
    async deleteRevenueRegular(id) {
      let response = null;
      await base.delete("/revenue-regular/" + id).then((res) => {
        response = res.result;
      });
      return response;
    },
    async getAllRevenueRegularPagination() {
      let response = null;
      await base.get("/revenue-regular/pagination", {
          field: this.pagination.field,
          pageNumber: this.pagination.pageNumber - 1,
          pageSize: this.pagination.pageSize,
          sort: this.pagination.sort,
          search: this.pagination.search,
        }).then((res) => {
          response = res.result;
          this.revenueRegulars = response.content;
          this.pagination.totalElements = response.totalElements;
          this.pagination.totalPages = response.totalPages;
        });
      return response.content;
    },
  },
});
