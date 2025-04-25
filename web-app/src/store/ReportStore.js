import { defineStore } from "pinia";
import {base} from "@/apis/ApiService.js"
import { formatDate } from "@/utils/DateUtil.js";

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
    async getTotalExpenseRevenueByBucketPaymentIdAndTimeOption(bucketPaymentIds, timeOption, startDate, endDate){
      let response = null;
      const request = {
        bucketPaymentIds: bucketPaymentIds,
        timeOption: timeOption,
        startDate: formatDate(startDate),
        endDate: formatDate(endDate)
      }
      await base.get("/report/total-expense-revenue", request).then((res) => {
        response = res.result;
      })
      return response;
    },
    async getTotalExpenseByBucketPaymentIdAndTimeOptionAndCategory(bucketPaymentIds, timeOption, startDate, endDate, isSelectAllBucketPayment = false){
      let response = null;
      const request = {
        bucketPaymentIds: bucketPaymentIds,
        timeOption: timeOption,
        startDate: formatDate(startDate),
        endDate: formatDate(endDate),
        isSelectAllBucketPayment: isSelectAllBucketPayment
      }
      await base.get("/report/total-expense-by-category", request).then((res) => {
        response = res.result;
      })
      return response;
    },
    async getTotalRevenueByBucketPaymentIdAndTimeOptionAndCategory(bucketPaymentIds, timeOption, startDate, endDate, isSelectAllBucketPayment = false){
      let response = null;
      const request = {
        bucketPaymentIds: bucketPaymentIds,
        timeOption: timeOption,
        startDate: formatDate(startDate),
        endDate: formatDate(endDate),
        isSelectAllBucketPayment: isSelectAllBucketPayment
      }
      await base.get("/report/total-revenue-by-category", request).then((res) => {
        response = res.result;
      })
      return response;
    },
    async getTotalExpenseByExpenseLimit(expenseLimitId, startDate, endDate){
      let response = null;
      await base.get("/report/total-expense-by-expense-limit?startDate=" + formatDate(startDate) + "&endDate=" + formatDate(endDate) + "&expenseLimitId=" + expenseLimitId).then((res) => {
        response = res.result;
      })
      return response;
    },
    async getTotalExpenseRevenueForExpenseRevenueSituation(bucketPaymentIds, timeOption, year, startYear, endYear, startDate, endDate){
      let response = null;
      const request = {
        bucketPaymentIds: bucketPaymentIds,
        timeOption: timeOption,
        year: year,
        startYear: startYear,
        endYear: endYear,
        startDate: formatDate(startDate),
        endDate: formatDate(endDate)
      }
      await base.post("/report/expense-revenue-situation", request).then((res) => {
        response = res.result;
      })
      return response;
    },
    async getTotalExpenseRevenueForCategory(bucketPaymentIds, timeOption, presentOption, month, quarter, year, startDate, endDate){
      let response = null;
      const request = {
        bucketPaymentIds: bucketPaymentIds,
        timeOption: timeOption,
        presentOption: presentOption,
        month: month,
        quarter: quarter,
        year: year,
        startDate: formatDate(startDate),
        endDate: formatDate(endDate)
      }
      await base.post("/report/expense-revenue-for-category", request).then((res) => {
        response = res.result;
      })
      return response;
    },
    async getTotalExpenseForSpendingAnalysis(timeOption, bucketPaymentIds, categoriesId, startDate, endDate, startMonth, endMonth, startYear, endYear){
      let response = null;
      const request = {
        timeOption: timeOption,
        bucketPaymentIds: bucketPaymentIds,
        categoriesId: categoriesId,
        startDate: formatDate(startDate),
        endDate: formatDate(endDate),
        startMonth: formatDate(startMonth),
        endMonth: formatDate(endMonth),
        startYear: startYear,
        endYear: endYear
      }
      await base.post("/report/spending-analysis", request).then((res) => {
        response = res.result;
      })
      return response;
    },
    async getTotalExpenseExactByTime(timeOption, bucketPaymentIds, categoriesId, date, month, year){
      let response = null;
      const request = {
        timeOption: timeOption,
        bucketPaymentIds: bucketPaymentIds,
        categoriesId: categoriesId,
        date: formatDate(date),
        month: month,
        year: year,
      }
      await base.post("/report/total-expense-exact-time", request).then((res) => {
        response = res.result;
      })
      return response;
    },
  },
});
