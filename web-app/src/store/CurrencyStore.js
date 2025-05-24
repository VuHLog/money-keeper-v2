import { defineStore } from "pinia";
import {base} from "@/apis/ApiService.js"

export const useCurrencyStore = defineStore("currency", {
  state: () => {
    return {
    };
  },
  getters: {
  },
  actions: {
    // Get exchange rate between two currencies
    async getExchangeRate(toCurrency, fromCurrency, amount) {
      try {
        let response = null;
        await base
          .get("/currency/rates/lookup?isoTo=" + toCurrency + "&isoFrom=" + fromCurrency + "&amount=" + amount)
          .then((res) => {
            response = res.result;
          })
          .catch((err) => {
            console.log(err);
          });
        return response;
      } catch (error) {
        console.error("Error fetching exchange rate:", error);
        throw error;
      }
    },
    
    // async convertCurrency(amount, fromCurrency, toCurrency) {
    //   if (fromCurrency === toCurrency) {
    //     return amount;
    //   }
      
    //   try {
    //     const cacheKey = `${fromCurrency}_${toCurrency}`;
    //     const currentTime = new Date().getTime();
    //     const cacheExpiry = 3600000;
        
    //     if (
    //       this.exchangeRates[cacheKey] && 
    //       this.lastFetchTime && 
    //       (currentTime - this.lastFetchTime < cacheExpiry)
    //     ) {
    //       return amount * this.exchangeRates[cacheKey];
    //     }
        
    //     const response = await this.getExchangeRate(fromCurrency, toCurrency, amount);
        
    //     if (response && response.result) {
    //       return response.result;
    //     }
        
    //     return null;
    //   } catch (error) {
    //     console.error("Error converting currency:", error);
    //     throw error;
    //   }
    // },
    
    // async fetchAllRates(baseCurrency = "VND", currencies = ["USD", "EUR", "GBP", "JPY"]) {
    //   try {
    //     const rates = {};
        
    //     // Fetch rates for each currency
    //     for (const currency of currencies) {
    //       if (currency !== baseCurrency) {
    //         const response = await this.getExchangeRate(baseCurrency, currency);
    //         if (response && response.rate) {
    //           rates[currency] = response.rate;
    //         }
    //       }
    //     }
        
    //     return rates;
    //   } catch (error) {
    //     console.error("Error fetching all rates:", error);
    //     throw error;
    //   }
    // }
  },
});
