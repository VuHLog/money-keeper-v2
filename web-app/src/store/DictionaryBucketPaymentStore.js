import { defineStore } from "pinia";
import { jwtDecode } from "jwt-decode";
import {base} from "@/apis/ApiService.js"
import { formatDate } from "@/utils/DateUtil.js";

export const useDictionaryBucketPaymentStore = defineStore("dictionaryBucketPayment", {
  state: () => {
    return {
      totalExpense: 0,
      totalRevenue: 0,
      accounts: [],
      pagination: {
        field: "accountName",
        pageNumber: 1,
        pageSize: 5,
        sort: "ASC",
        search: "",
        totalElements: 0,
        totalPages: 0,
      }
    };
  },
  getters: {},
  actions: {
    async createBucketPayment(data) {
      let response = null;
      await base
        .post("/dictionary-bucket-payment", data)
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
        .put("/dictionary-bucket-payment/" + id, data)
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
        .delete("/dictionary-bucket-payment/" + id)
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async getMyBucketPayments() {
      let response = null;
      await base
        .get("/dictionary-bucket-payment")
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async getMyBucketPaymentsPagination(field = null, pageNumber = null, pageSize = null, sort = null, search = null) {
      // Sử dụng giá trị từ store nếu không có tham số truyền vào
      const queryParams = {
        field: field || this.pagination.field,
        pageNumber: (pageNumber !== null ? pageNumber : this.pagination.pageNumber) - 1, // API sử dụng 0-indexed
        pageSize: pageSize || this.pagination.pageSize,
        sort: sort || this.pagination.sort,
        search: search !== null ? search : this.pagination.search
      };

      // Đảm bảo search không phải null
      if (queryParams.search === null) {
        queryParams.search = '';
      }

      let response = null;
      await base
        .get("/dictionary-bucket-payment/pagination?"
          + "field=" + queryParams.field
          + "&pageNumber=" + queryParams.pageNumber
          + "&pageSize=" + queryParams.pageSize
          + "&sort=" + queryParams.sort
          + "&search=" + queryParams.search
        )
        .then((res) => {
          response = res.result;
          // Cập nhật state pagination
          this.accounts = response.content;
          this.pagination.totalElements = response.totalElements;
          this.pagination.totalPages = response.totalPages;
          this.pagination.pageNumber = response.number + 1; // Chuyển về 1-indexed
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async getTotalBalance(search = null){
      let response = null;
      if(search === null){
        search = "";
      }
      await base
        .get("/dictionary-bucket-payment/total-balance?search=" + search)
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    resetPagination() {
      this.pagination.pageNumber = 1;
      this.pagination.pageSize = 5;
      this.pagination.sort = "ASC";
      this.pagination.search = "";
      this.pagination.totalElements = 0;
      this.pagination.totalPages = 0;
    },
    async getBucketPaymentById(id) {
      let response = null;
      await base
        .get("/dictionary-bucket-payment/" + id)
        .then((res) => {
          response = res.result;
        })
        .catch((error) => console.log(error));
      return response;
    },
    async getTotalExpenseByBucketPaymentId(id, timeOption, startDate, endDate) {
      let response = null;
      const request = {
        bucketPaymentId: id,
        timeOption: timeOption,
        startDate: formatDate(startDate),
        endDate: formatDate(endDate),
      };
      await base
        .get("/dictionary-bucket-payment/" + id + "/total-expense", request)
        .then((res) => {
          response = res.result;
        });
      return response;
    },
    async getTotalRevenueByBucketPaymentId(id, timeOption, startDate, endDate) {
      let response = null;
      const request = {
        bucketPaymentId: id,
        timeOption: timeOption,
        startDate: formatDate(startDate),
        endDate: formatDate(endDate),
      };
      await base
        .get("/dictionary-bucket-payment/" + id + "/total-revenue", request)
        .then((res) => {
          response = res.result;
        });
      return response;
    },
    async getBalanceByBucketPaymentId(id, timeOption) {
      let response = null;
      await base
        .get(
          "/dictionary-bucket-payment/" +
            id +
            "/balance?timeOption=" +
            timeOption
        )
        .then((res) => {
          response = res.result;
        });
      return response;
    },
    async getTransactionHistoryByBucketPaymentId(
      id,
      timeOption,
      startDate,
      endDate
    ) {
      let response = null;
      const request = {
        bucketPaymentId: id,
        timeOption: timeOption,
        startDate: formatDate(startDate),
        endDate: formatDate(endDate),
      };
      await base
        .get(
          "/dictionary-bucket-payment/" + id + "/transaction-history",
          request
        )
        .then((res) => {
          response = res.result;
        });
      return response;
    },
  },
});
