export const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(amount).replace(/,/g, '.')
}

export const formatCurrencyWithSymbol = (value, currencyCode, symbol) => {
  if (value === null || value === undefined || value === '') return ''
  const numberValue = Number(value)
  
  const formattedValue = new Intl.NumberFormat().format(numberValue).replace(/,/g, '.')
  
  if (currencyCode === 'VND') {
    return `${formattedValue} ${symbol}`
  } else {
    return `${symbol}${formattedValue}`
  }
}

export const formatMillions = (amount, decimals = 1) => {
  return `${(amount / 1000000).toFixed(decimals)}M`
}

export const formatNumber = (amount, locale = 'vi-VN') => {
  return new Intl.NumberFormat(locale).format(amount)
}

// Format a date string from yyyy-mm-dd to dd-mm-yyyy
export const formatReverseStringDate = (dateStr) => {
  if (!dateStr) return ''
  const [year, month, day] = dateStr.split('-')
  return `${day}-${month}-${year}`
}