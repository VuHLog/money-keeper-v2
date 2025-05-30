import { defineStore } from "pinia";
import {base} from "@/apis/ApiService.js"
import { formatDate } from "@/utils/DateUtil.js";

export const useFinancialGoalStore = defineStore("FinancialGoal", {
  state: () => {
    return {
    };
  },
  getters: {},
  actions: {
    async getFinancialGoalsPagination(field = "accountName", pageNumber = 1, pageSize = 5, sort = 'ASC', search='', status = 0) {
      if(search === null){
        search = '';
      }
      let response = null;
      await base
        .get("/financial-goal/pagination?"
          + "field="+ field
          + "&pageNumber="+ (pageNumber - 1)
          + "&pageSize=" + pageSize
          + "&sort=" + sort
          + "&search=" + search
          + "&status=" + status
        )
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async getFinancialGoalById(id) {
      let response = null;
      await base
        .get("/financial-goal/" + id)
        .then((res) => {
          response = res.result;
        })
        .catch((error) => console.log(error));
      return response;
    },
    async getDipositHistoryByFinancialGoalId(id, pageNumber = 1, pageSize = 2, sort = 'DESC', search='', status = 0) {
      if(search === null){
        search = '';
      }
      let response = null;
      await base
        .get("/financial-goal/" + id + "/diposit-history?"
          + "pageNumber="+ (pageNumber - 1)
          + "&pageSize=" + pageSize
          + "&sort=" + sort
          + "&search=" + search
          + "&status=" + status
        )
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async createFinancialGoal(data) {
      let response = null;
      await base
        .post("/financial-goal", data)
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async createDeposit(id, data) {
      let response = null;
      await base
        .post("/financial-goal/" + id + "/deposit", data)
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
        .put("/financial-goal/" + id, data)
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async updateDeadline(id, data) {
      let response = null;
      await base
        .put("/financial-goal/" + id + "/deadline", data)
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
        .delete("/financial-goal/" + id)
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
