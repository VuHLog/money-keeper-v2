import { defineStore } from "pinia";
import {base} from "@/apis/ApiService.js"

export const useNotificationStore = defineStore("notification", {
  state: () => {
    return {
        pageNumber: 1,
        pageSize: 4,
        totalElements: 0,
        totalPages: 0,
        readStatus: -1,
        notifications: [],
        countNewNotifications: 0,
    };
  },
  getters: {},
  actions: {
    async changPageNumber(pageNumber) {
        this.pageNumber = pageNumber;
        await this.getNotifications();
    },
    async changePageSize(pageSize) {
        this.pageSize = pageSize;
        await this.getNotifications();
    },
    async getNotifications() {
      let response = null;
      await base
        .get("/notification?pageNumber=" + (this.pageNumber - 1) + "&pageSize=" + this.pageSize + "&readStatus=" + this.readStatus)
        .then((res) => {
            response = res.result;
            this.totalPages = response.totalPages;
            this.totalElements = response.totalElements;
            this.notifications = response.content;
            this.pageSize = response.pageable.pageSize;
            this.pageNumber = response.pageable.pageNumber + 1;
        })
        .catch((err) => {
          console.log(err);
        });
    },
    async countNotificationByReadStatus(readStatus=0) {
      let response = null;
      await base
        .get("/notification/count-read-status?readStatus=" + readStatus)
        .then((res) => {
          response = res.result;
          this.countNewNotifications = response;
        });
      return response;
    },
    async updateReadStatus(id, readStatus) {
      let response = null;
      await base
        .patch("/notification/" + id + "/read-status" , readStatus)
        .then((res) => {
          response = res.result;
          this.notifications.find(n => n.id === id).readStatus = readStatus;
        });
      return response;
    },
    async updateAllReadStatus(readStatus) {
      let response = null;
      await base
        .patch("/notification/update-all-read-status" , readStatus)
        .then((res) => {
          response = res.result;
          this.notifications.forEach(notification => {
            notification.readStatus = readStatus;
          });
        });
      return response;
    },
    async deleteNotification(id) {
      let response = null;
      await base
        .delete("/notification/" + id)
        .then((res) => {
          response = res.result;
          this.notifications = this.notifications.filter(n => n.id !== id);
        });
      return response;
    },
    resetPagination() {
      this.pageNumber = 1;
      this.pageSize = 4;
    },
  },
});
