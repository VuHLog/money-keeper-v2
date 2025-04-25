import { defineStore } from "pinia";
import { jwtDecode } from "jwt-decode";
import {base} from "@/apis/ApiService.js"
import { formatDate } from "@/utils/DateUtil.js";
import { AccountType } from "@/constants/AccountType.js";
import { inject } from "vue";

export const useNotificationStore = defineStore("notification", {
  state: () => {
    return {
        pageNumber: 1,
        pageSize: 4,
        totalElements: 0,
        totalPages: 0,
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
        .get("/notification?pageNumber=" + (this.pageNumber - 1) + "&pageSize=" + this.pageSize)
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
    showToastNotify(responseBody, swal) {
      const Toast = swal.mixin({
        toast: true,
        position: "bottom-end",
        showConfirmButton: false,
        width: 360,
        timer: 2000,
        timerProgressBar: false,
        didOpen: (toast) => {
          toast.onmouseenter = swal.stopTimer;
          toast.onmouseleave = swal.resumeTimer;
        },
      });
      if (
          responseBody.type === "expense limit"
      ) {
        Toast.fire({
          html:   `<a href="http://localhost:5173/${responseBody.href}" class="d-flex align-center text-decoration-none">
                    <v-avatar class="mr-2">
                      <img class="icon-size" src="https://res.cloudinary.com/cloud1412/image/upload/v1745068565/logo_mpkmjj.png" />
                    </v-avatar>
                    <div>
                      <h5 class="text-primary text-16 mb-2">${responseBody.title}</h5>
                      <p class="text-14 text-grey-darken-4">${responseBody.content}</p>
                    </div>
                  </a>`,
        });
      }
    }
  },
});
