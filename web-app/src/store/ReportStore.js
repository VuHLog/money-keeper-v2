import { defineStore } from "pinia";
import {base} from "@/apis/ApiService.js"
import { instance } from "@/apis/ApiService.js";

export const useReportStore = defineStore("report", {
  state: () => {
    return {
      totalExpense: 0,
      totalRevenue: 0,
      expenseCategoriesId: [],
      revenueCategoriesId: [],
    };
  },
  getters: {
  },
  actions: {
    async getTotalExpense(filters){
      let response = null;
      let bucketPaymentIds = (filters.account === null || filters.account.length === 0 || filters.account[0] === "all") ? null : filters.account.join(",");
      let expenseCategoriesId = (filters.expenseCategory === null || filters.expenseCategory.length === 0 || filters.expenseCategory[0] === "all") ? null : filters.expenseCategory.join(",");
      let revenueCategoriesId = (filters.revenueCategory === null || filters.revenueCategory.length === 0 || filters.revenueCategory[0] === "all") ? null : filters.revenueCategory.join(",");
      let request = {
        timeOption: filters.timeOption,
        transactionType: filters.transactionType,
        bucketPaymentIds,
        expenseCategoriesId,
        revenueCategoriesId,
        customTimeRange: filters.customTimeRange,
      }
      await base.post(`/report/total-expense`, request).then((res) => {
        response = res.result;
      }).catch((err) => {
        console.log(err);
      });
      return response;
    },
    async getTotalRevenue(filters){
      let response = null;
      let bucketPaymentIds = (filters.account === null || filters.account.length === 0 || filters.account[0] === "all") ? null : filters.account.join(",");
      let expenseCategoriesId = (filters.expenseCategory === null || filters.expenseCategory.length === 0 || filters.expenseCategory[0] === "all") ? null : filters.expenseCategory.join(",");
      let revenueCategoriesId = (filters.revenueCategory === null || filters.revenueCategory.length === 0 || filters.revenueCategory[0] === "all") ? null : filters.revenueCategory.join(",");
      let request = {
        timeOption: filters.timeOption,
        transactionType: filters.transactionType,
        bucketPaymentIds,
        expenseCategoriesId,
        revenueCategoriesId,
        customTimeRange: filters.customTimeRange,
      }
      await base.post(`/report/total-revenue`, request).then((res) => {
        response = res.result;
      }).catch((err) => {
        console.log(err);
      });
      return response;
    },
    async getAllTransactionHistory(filters){
      let response = null;
      let bucketPaymentIds = (filters.account === null || filters.account.length === 0 || filters.account[0] === "all") ? null : filters.account.join(",");
      let expenseCategoriesId = (filters.expenseCategory === null || filters.expenseCategory.length === 0 || filters.expenseCategory[0] === "all") ? null : filters.expenseCategory.join(",");
      let revenueCategoriesId = (filters.revenueCategory === null || filters.revenueCategory.length === 0 || filters.revenueCategory[0] === "all") ? null : filters.revenueCategory.join(",");
      let request = {
        timeOption: filters.timeOption,
        transactionType: filters.transactionType,
        bucketPaymentIds,
        expenseCategoriesId,
        revenueCategoriesId,
        customTimeRange: filters.customTimeRange,
      }
      await base.post(`/report/transaction-history?pageNumber=${this.pagination.pageNumber - 1}&pageSize=${this.pagination.pageSize}`, request).then((res) => {
        response = res.result;
        this.transactionHistory = response.content;
        this.pagination.totalElements = response.totalElements;
        this.pagination.totalPages = response.totalPages;
      })
      return response.content;
    },
    async getReportTransactionType(filters) {
      let response = null;
      let bucketPaymentIds = (filters.account === null || filters.account.length === 0 || filters.account[0] === "all") ? null : filters.account.join(",");
      let request = {
        timeOption: filters.timeOption,
        bucketPaymentIds,
        customTimeRange: filters.customTimeRange,
      }
      await base
        .post("/report/transaction-type", request)
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async exportExcelForTransactionType(filters){
      let response = null;
      let bucketPaymentIds = (filters.account === null || filters.account.length === 0 || filters.account[0] === "all") ? null : filters.account.join(",");
      let request = {
        timeOption: filters.timeOption,
        bucketPaymentIds,
        customTimeRange: filters.customTimeRange,
      }
      try {
        response = await instance.post(`/report/transaction-type/export-excel`,request, {
          responseType: 'blob',
        });
        const blob = new Blob([response.data], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        });
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        link.download = 'Báo cáo giao dịch' + (filters.timeOption && filters.timeOption !== 'Tùy chọn' ?' ' + filters.timeOption.toLowerCase() : '') + '.xlsx';
        document.body.appendChild(link);
        link.click();
        link.remove();
      } catch (error) {
        return Promise.reject(error);
      }
    },
    async getReportExpenseCategory(filters) {
      let response = null;
      let bucketPaymentIds = (filters.account === null || filters.account.length === 0 || filters.account[0] === "all") ? null : filters.account.join(",");
      let categoriesId = (filters.expenseCategory === null || filters.expenseCategory.length === 0 || filters.expenseCategory[0] === "all") ? null : filters.expenseCategory.join(",");
      let request = {
        timeOption: filters.timeOption,
        bucketPaymentIds,
        categoriesId,
        customTimeRange: filters.customTimeRange,
      }
      await base
        .post("/report/expense-category", request)
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async exportExcelForExpenseCategory(filters){
      let response = null;
      let bucketPaymentIds = (filters.account === null || filters.account.length === 0 || filters.account[0] === "all") ? null : filters.account.join(",");
      let categoriesId = (filters.expenseCategory === null || filters.expenseCategory.length === 0 || filters.expenseCategory[0] === "all") ? null : filters.expenseCategory.join(",");
      let request = {
        timeOption: filters.timeOption,
        bucketPaymentIds,
        categoriesId,
        customTimeRange: filters.customTimeRange,
      }
      try {
        response = await instance.post(`/report/expense-category/export-excel`,request, {
          responseType: 'blob',
        });
        const blob = new Blob([response.data], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        });
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        link.download = 'Báo cáo phân bổ chi tiêu' + (filters.timeOption && filters.timeOption !== 'Tùy chọn' ?' ' + filters.timeOption.toLowerCase() : '') + '.xlsx';
        document.body.appendChild(link);
        link.click();
        link.remove();
      } catch (error) {
        return Promise.reject(error);
      }
    },
    async exportExcelForExpenseCategoryTrending(filters){
      let response = null;
      let bucketPaymentIds = (filters.account === null || filters.account.length === 0 || filters.account[0] === "all") ? null : filters.account.join(",");
      let categoriesId = (filters.expenseCategory === null || filters.expenseCategory.length === 0 || filters.expenseCategory[0] === "all") ? null : filters.expenseCategory.join(",");
      let request = {
        timeOption: filters.timeOption,
        bucketPaymentIds,
        categoriesId,
        customTimeRange: filters.customTimeRange,
      }
      try {
        response = await instance.post(`/report/expense-category-trending/export-excel`,request, {
          responseType: 'blob',
        });
        const blob = new Blob([response.data], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        });
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        link.download = 'Báo cáo xu hướng chi tiêu theo danh mục chi tiêu' + (filters.timeOption && filters.timeOption !== 'Tùy chọn' ?' ' + filters.timeOption.toLowerCase() : '') + '.xlsx';
        document.body.appendChild(link);
        link.click();
        link.remove();
      } catch (error) {
        return Promise.reject(error);
      }
    },
    async getReportRevenueCategory(filters) {
      let response = null;
      let bucketPaymentIds = (filters.account === null || filters.account.length === 0 || filters.account[0] === "all") ? null : filters.account.join(",");
      let categoriesId = (filters.revenueCategory === null || filters.revenueCategory.length === 0 || filters.revenueCategory[0] === "all") ? null : filters.revenueCategory.join(",");
      let request = {
        timeOption: filters.timeOption,
        bucketPaymentIds,
        categoriesId,
        customTimeRange: filters.customTimeRange,
      }
      await base
        .post("/report/revenue-category", request)
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async exportExcelForRevenueCategory(filters){
      let response = null;
      let bucketPaymentIds = (filters.account === null || filters.account.length === 0 || filters.account[0] === "all") ? null : filters.account.join(",");
      let categoriesId = (filters.revenueCategory === null || filters.revenueCategory.length === 0 || filters.revenueCategory[0] === "all") ? null : filters.revenueCategory.join(",");
      let request = {
        timeOption: filters.timeOption,
        bucketPaymentIds,
        categoriesId,
        customTimeRange: filters.customTimeRange,
      }
      try {
        response = await instance.post(`/report/revenue-category/export-excel`,request, {
          responseType: 'blob',
        });
        const blob = new Blob([response.data], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        });
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        link.download = 'Báo cáo phân bổ thu nhập' + (filters.timeOption && filters.timeOption !== 'Tùy chọn' ?' ' + filters.timeOption.toLowerCase() : '') + '.xlsx';
        document.body.appendChild(link);
        link.click();
        link.remove();
      } catch (error) {
        return Promise.reject(error);
      }
    },
    async exportExcelForRevenueCategoryTrending(filters){
      let response = null;
      let bucketPaymentIds = (filters.account === null || filters.account.length === 0 || filters.account[0] === "all") ? null : filters.account.join(",");
      let categoriesId = (filters.revenueCategory === null || filters.revenueCategory.length === 0 || filters.revenueCategory[0] === "all") ? null : filters.revenueCategory.join(",");
      let request = {
        timeOption: filters.timeOption,
        bucketPaymentIds,
        categoriesId,
        customTimeRange: filters.customTimeRange,
      }
      try {
        response = await instance.post(`/report/revenue-category-trending/export-excel`,request, {
          responseType: 'blob',
        });
        const blob = new Blob([response.data], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        });
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        link.download = 'Báo cáo xu hướng thu nhập theo danh mục thu nhập' + (filters.timeOption && filters.timeOption !== 'Tùy chọn' ?' ' + filters.timeOption.toLowerCase() : '') + '.xlsx';
        document.body.appendChild(link);
        link.click();
        link.remove();
      } catch (error) {
        return Promise.reject(error);
      }
    },
    async getReportDailyTrend(filters) {
      let response = null;
      let bucketPaymentIds = (filters.account === null || filters.account.length === 0 || filters.account[0] === "all") ? null : filters.account.join(",");
      let request = {
        transactionType: filters.transactionType,
        bucketPaymentIds,
      }
      await base
        .post("/report/daily-trend", request)
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async exportExcelForDailyTrend(filters){
      let response = null;
      let bucketPaymentIds = (filters.account === null || filters.account.length === 0 || filters.account[0] === "all") ? null : filters.account.join(",");
      let request = {
        transactionType: filters.transactionType,
        bucketPaymentIds,
      }
      try {
        response = await instance.post(`/report/daily-trend/export-excel`,request, {
          responseType: 'blob',
        });
        const blob = new Blob([response.data], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        });
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        let transactionType = filters.transactionType && filters.transactionType !== 'Tất cả' ? filters.transactionType === 'expense' ? 'chi tiêu' : 'thu nhập' : '';
        link.download = 'Báo cáo xu hướng ' + (transactionType ? ' ' + transactionType.toLowerCase() : '') + ' theo ngày.xlsx';
        document.body.appendChild(link);
        link.click();
        link.remove();
      } catch (error) {
        return Promise.reject(error);
      }
    },
    async getReportWeeklyTrend(filters) {
      let response = null;
      let bucketPaymentIds = (filters.account === null || filters.account.length === 0 || filters.account[0] === "all") ? null : filters.account.join(",");
      let request = {
        transactionType: filters.transactionType,
        bucketPaymentIds,
      }
      await base
        .post("/report/weekly-trend", request)
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async exportExcelForWeeklyTrend(filters){
      let response = null;
      let bucketPaymentIds = (filters.account === null || filters.account.length === 0 || filters.account[0] === "all") ? null : filters.account.join(",");
      let request = {
        transactionType: filters.transactionType,
        bucketPaymentIds,
      }
      try {
        response = await instance.post(`/report/weekly-trend/export-excel`,request, {
          responseType: 'blob',
        });
        const blob = new Blob([response.data], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        });
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        let transactionType = filters.transactionType && filters.transactionType !== 'Tất cả' ? filters.transactionType === 'expense' ? 'chi tiêu' : 'thu nhập' : '';
        link.download = 'Báo cáo xu hướng ' + (transactionType ? ' ' + transactionType.toLowerCase() : '') + ' theo tuần.xlsx';
        document.body.appendChild(link);
        link.click();
        link.remove();
      } catch (error) {
        return Promise.reject(error);
      }
    },
    async getReportMonthlyTrend(filters) {
      let response = null;
      let bucketPaymentIds = (filters.account === null || filters.account.length === 0 || filters.account[0] === "all") ? null : filters.account.join(",");
      let request = {
        transactionType: filters.transactionType,
        bucketPaymentIds,
      }
      await base
        .post("/report/monthly-trend", request)
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async exportExcelForMonthlyTrend(filters){
      let response = null;
      let bucketPaymentIds = (filters.account === null || filters.account.length === 0 || filters.account[0] === "all") ? null : filters.account.join(",");
      let request = {
        transactionType: filters.transactionType,
        bucketPaymentIds,
      }
      try {
        response = await instance.post(`/report/monthly-trend/export-excel`,request, {
          responseType: 'blob',
        });
        const blob = new Blob([response.data], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        });
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        let transactionType = filters.transactionType && filters.transactionType !== 'Tất cả' ? filters.transactionType === 'expense' ? 'chi tiêu' : 'thu nhập' : '';
        link.download = 'Báo cáo xu hướng ' + (transactionType ? ' ' + transactionType.toLowerCase() : '') + ' theo tháng.xlsx';
        document.body.appendChild(link);
        link.click();
        link.remove();
      } catch (error) {
        return Promise.reject(error);
      }
    },
    async getReportYearlyTrend(filters) {
      let response = null;
      let bucketPaymentIds = (filters.account === null || filters.account.length === 0 || filters.account[0] === "all") ? null : filters.account.join(",");
      let request = {
        transactionType: filters.transactionType,
        bucketPaymentIds,
      }
      await base
        .post("/report/yearly-trend", request)
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async exportExcelForYearlyTrend(filters){
      let response = null;
      let bucketPaymentIds = (filters.account === null || filters.account.length === 0 || filters.account[0] === "all") ? null : filters.account.join(",");
      let request = {
        transactionType: filters.transactionType,
        bucketPaymentIds,
      }
      try {
        response = await instance.post(`/report/yearly-trend/export-excel`,request, {
          responseType: 'blob',
        });
        const blob = new Blob([response.data], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        });
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        let transactionType = filters.transactionType && filters.transactionType !== 'Tất cả' ? filters.transactionType === 'expense' ? 'chi tiêu' : 'thu nhập' : '';
        link.download = 'Báo cáo xu hướng ' + (transactionType ? ' ' + transactionType.toLowerCase() : '') + ' theo năm.xlsx';
        document.body.appendChild(link);
        link.click();
        link.remove();
      } catch (error) {
        return Promise.reject(error);
      }
    },
    async getReportBucketPaymentBalance() {
      let response = null;
      await base
        .post("/report/bucket-payment-balance")
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async exportExcelForBucketPaymentBalance(){
      let response = null;
      try {
        response = await instance.post(`/report/bucket-payment-balance/export-excel`,{} ,{
          responseType: 'blob',
        });
        const blob = new Blob([response.data], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        });
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        link.download = 'Báo cáo số dư tài khoản.xlsx';
        document.body.appendChild(link);
        link.click();
        link.remove();
      } catch (error) {
        return Promise.reject(error);
      }
    },
    async getReportBucketPaymentTypeBalance() {
      let response = null;
      await base
        .post("/report/bucket-payment-type-balance")
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async exportExcelForBucketPaymentTypeBalance(){
      let response = null;
      try {
        response = await instance.post(`/report/bucket-payment-type-balance/export-excel`,{} ,{
          responseType: 'blob',
        });
        const blob = new Blob([response.data], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        });
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        link.download = 'Báo cáo loại tài khoản.xlsx';
        document.body.appendChild(link);
        link.click();
        link.remove();
      } catch (error) {
        return Promise.reject(error);
      }
    },
    async getAccountBalanceFluctuation(filters) {
      let response = null;
      let request = {
        timeOption: filters.timeOption,
        customTimeRange: filters.customTimeRange,
      }
      await base
        .post("/report/account-balance-fluctuation", request)
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async exportExcelForAccountBalanceFluctuation(filters){
      let response = null;
      let request = {
        timeOption: filters.timeOption,
        customTimeRange: filters.customTimeRange,
      }
      try {
        response = await instance.post(`/report/account-balance-fluctuation/export-excel`,request, {
          responseType: 'blob',
        });
        const blob = new Blob([response.data], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        });
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        link.download = 'Báo cáo biến động số dư tài khoản ' + (filters.timeOption && filters.timeOption !== 'Tùy chọn' ?' ' + filters.timeOption.toLowerCase() : '') + '.xlsx';
        document.body.appendChild(link);
        link.click();
        link.remove();
      } catch (error) {
        return Promise.reject(error);
      }
    },
    async getReportBucketPayment(filters, pagination){
      let response = null;
      let bucketPaymentIds = (filters.account === null || filters.account.length === 0 || filters.account[0] === "all") ? null : filters.account.join(",");
      let request = {
        timeOption: filters.timeOption,
        bucketPaymentIds,
        customTimeRange: filters.customTimeRange,
      }
      await base
        .post(`/report/bucket-payment?pageNumber=${pagination.pageNumber - 1}&pageSize=${pagination.pageSize}`, request)
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async getReportTotalBucketPayment(filters){
      let response = null;
      let bucketPaymentIds = (filters.account === null || filters.account.length === 0 || filters.account[0] === "all") ? null : filters.account.join(",");
      let request = {
        timeOption: filters.timeOption,
        bucketPaymentIds,
        customTimeRange: filters.customTimeRange,
      }
      await base
        .post(`/report/total-bucket-payment`, request)
        .then((res) => {
          response = res.result;
        })
        .catch((err) => {
          console.log(err);
        });
      return response;
    },
    async exportExcelForReportBucketPayment(filters){
      let response = null;
      let bucketPaymentIds = (filters.account === null || filters.account.length === 0 || filters.account[0] === "all") ? null : filters.account.join(",");
      let request = {
        timeOption: filters.timeOption,
        customTimeRange: filters.customTimeRange,
        bucketPaymentIds,
      }
      try {
        response = await instance.post(`/report/bucket-payment/export-excel`,request, {
          responseType: 'blob',
        });
        const blob = new Blob([response.data], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        });
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        link.download = 'Báo cáo theo tài khoản ' + (filters.timeOption && filters.timeOption !== 'Tùy chọn' ?' ' + filters.timeOption.toLowerCase() : '') + '.xlsx';
        document.body.appendChild(link);
        link.click();
        link.remove();
      } catch (error) {
        return Promise.reject(error);
      }
    },
    async getReportCategory(filters, pagination){
      let response = null;
      let expenseCategoriesId = (filters.expenseCategory === null || filters.expenseCategory.length === 0) ? null : filters.expenseCategory[0] === "all"? this.expenseCategoriesId.join(",") : filters.expenseCategory.join(",");
      let revenueCategoriesId = (filters.revenueCategory === null || filters.revenueCategory.length === 0) ? null : filters.revenueCategory[0] === "all"? this.revenueCategoriesId.join(",") : filters.revenueCategory.join(",");
      let request = {
        timeOption: filters.timeOption,
        expenseCategoriesId,
        revenueCategoriesId,
        customTimeRange: filters.customTimeRange,
      }
      await base.post(`/report/category?pageNumber=${pagination.pageNumber - 1}&pageSize=${pagination.pageSize}`, request).then((res) => {
        response = res.result;
      })
      return response;
    },
    async getReportCategoryNoPaging(filters){
      let response = null;
      let expenseCategoriesId = (filters.expenseCategory === null || filters.expenseCategory.length === 0) ? null : filters.expenseCategory[0] === "all"? this.expenseCategoriesId.join(",") : filters.expenseCategory.join(",");
      let revenueCategoriesId = (filters.revenueCategory === null || filters.revenueCategory.length === 0) ? null : filters.revenueCategory[0] === "all"? this.revenueCategoriesId.join(",") : filters.revenueCategory.join(",");
      let request = {
        timeOption: filters.timeOption,
        expenseCategoriesId,
        revenueCategoriesId,
        customTimeRange: filters.customTimeRange,
      }
      await base.post(`/report/category-no-paging`, request).then((res) => {
        response = res.result;
      })
      return response;
    },
    async exportExcelForReportCategory(filters){
      let response = null;
      let expenseCategoriesId = (filters.expenseCategory === null || filters.expenseCategory.length === 0) ? null : filters.expenseCategory[0] === "all"? this.expenseCategoriesId.join(",") : filters.expenseCategory.join(",");
      let revenueCategoriesId = (filters.revenueCategory === null || filters.revenueCategory.length === 0) ? null : filters.revenueCategory[0] === "all"? this.revenueCategoriesId.join(",") : filters.revenueCategory.join(",");
      let request = {
        timeOption: filters.timeOption,
        expenseCategoriesId,
        revenueCategoriesId,
        customTimeRange: filters.customTimeRange,
      }
      try {
        response = await instance.post(`/report/category/export-excel`,request, {
          responseType: 'blob',
        });
        const blob = new Blob([response.data], {
          type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        });
        const link = document.createElement('a');
        link.href = window.URL.createObjectURL(blob);
        link.download = 'Báo cáo theo danh mục ' + (filters.timeOption && filters.timeOption !== 'Tùy chọn' ?' ' + filters.timeOption.toLowerCase() : '') + '.xlsx';
        document.body.appendChild(link);
        link.click();
        link.remove();
      } catch (error) {
        return Promise.reject(error);
      }
    },
  },
});
