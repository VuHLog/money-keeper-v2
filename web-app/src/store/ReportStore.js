import { defineStore } from "pinia";
import {base} from "@/apis/ApiService.js"
import { instance } from "@/apis/ApiService.js";

export const useReportStore = defineStore("report", {
  state: () => {
    return {
      totalExpense: 0,
      totalRevenue: 0,
    };
  },
  getters: {
  },
  actions: {
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
  },
});
