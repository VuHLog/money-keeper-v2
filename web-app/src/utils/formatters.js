/**
 * Format a number as Vietnamese currency
 * @param {number} amount - The amount to format
 * @param {string} [currency='VND'] - The currency code
 * @param {string} [locale='vi-VN'] - The locale to use for formatting
 * @returns {string} The formatted currency string
 */
export const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(amount)
}

/**
 * Format a number to millions with M suffix
 * @param {number} amount - The amount to format
 * @param {number} [decimals=1] - Number of decimal places
 * @returns {string} The formatted string with M suffix
 */
export const formatMillions = (amount, decimals = 1) => {
  return `${(amount / 1000000).toFixed(decimals)}M`
}

/**
 * Format a number with thousand separators
 * @param {number} amount - The amount to format
 * @param {string} [locale='vi-VN'] - The locale to use for formatting
 * @returns {string} The formatted number string
 */
export const formatNumber = (amount, locale = 'vi-VN') => {
  return new Intl.NumberFormat(locale).format(amount)
} 