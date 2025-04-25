import { defineStore } from "pinia";
import { jwtDecode } from "jwt-decode";
import {base} from "@/apis/ApiService.js"
import TokenService from "@/service/TokenService.js"
import { useRouter } from "vue-router";
import { initializeStompClient } from "@/config/StompClientNotificationConfig.js";

const token = TokenService.getSessionAccessToken();
const tokenDecoded = token? jwtDecode(token): {};


export const useAuthStore = defineStore("auth", {
  state: () => {
    return {
      stompClient: initializeStompClient(),
      isLoading: false,
      isLoggedIn: tokenDecoded?true:false,
      roles: tokenDecoded?.scope || "",
      username: tokenDecoded?.sub || "",
      fullName: "",
      avatarUrl: "",
      avatarUserDefault:
        "https://res.cloudinary.com/cloud1412/image/upload/v1739899158/hffbxsj6wbkbzkxjfetz.png",
    };
  },
  getters: {
  },
  actions: {
    refreshToken(accessToken) {
      this.isLoggedIn = true;
      sessionStorage.setItem("token",accessToken);
    },
    async getMyInfo(){
      let response = null;
      await base.get("/users/myInfo").then((res) => {
        response = res.result;
      })
      .catch((error) => console.log(error)
      )
      return response;
    },
    async getMyUserId(){
      let response = null;
      await this.getMyInfo().then((res) => {
        response = res;
      });
      return response.id;
    },
    async getUserById(userId){
      let response = null;
      await base.get("/users/"+ userId).then((res) => {
        response = res.result;
      })
      .catch((error) => console.log(error)
      )
      return response;
    },
    getCurrentDateTime() {
      const now = new Date();
    
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, '0');  // Tháng bắt đầu từ 0, nên cần +1
      const day = String(now.getDate()).padStart(2, '0');
      
      const hours = String(now.getHours()).padStart(2, '0');
      const minutes = String(now.getMinutes()).padStart(2, '0');
      const seconds = String(now.getSeconds()).padStart(2, '0');
    
      return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
    },
    connectStompClient() {
      this.stompClient.activate();
    },
    disconnectStompClient() {
      this.stompClient.deactivate();
    },
    async logOut() {
      const token = sessionStorage.getItem("token");
      if (token) {
        await base
          .post("/auth/logout", token)
          .then(() => {
            this.isLoggedIn = false;
            sessionStorage.removeItem("token");
            this.stompClient.deactivate();
          })
          .catch();
      }
    }
  },
});
