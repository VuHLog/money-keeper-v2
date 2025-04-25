import { defineStore } from "pinia";
import {base} from "@/apis/ApiService.js"

export const useBankStore = defineStore("bank", {
  state: () => {
    return {
    };
  },
  getters: {},
  actions: {
    async getBanks() {
      let response = null;
      await base
        .get("/banks")
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
