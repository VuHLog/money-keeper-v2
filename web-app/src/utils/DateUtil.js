export const getWeekday = (dateString) => {
  const date = new Date(dateString);
  const today = new Date();

  // Tạo ngày hôm qua
  const yesterday = new Date();
  yesterday.setDate(today.getDate() - 1);

  // Chuyển đổi về dạng YYYY-MM-DD để so sánh
  const formatDate = (d) => d.toISOString().split("T")[0];

  if (formatDate(date) === formatDate(today)) {
    return "Hôm nay";
  }

  if (formatDate(date) === formatDate(yesterday)) {
    return "Hôm qua";
  }

  const weekdays = [
    "Chủ Nhật",
    "Thứ Hai",
    "Thứ Ba",
    "Thứ Tư",
    "Thứ Năm",
    "Thứ Sáu",
    "Thứ Bảy",
  ];
  return weekdays[date.getDay()];
};

//Time zone -> yyyy/mm/dd hh:mm:ss
export const formatDate = (date) => {
  return date instanceof Date
    ? new Date(date.getTime() - (date.getTimezoneOffset() * 60000))
      .toISOString()
      .slice(0, 19)
      .replace('T', ' ')
    : date;
};

// date String yyyy-mm-dd -> Date Object
export const parseDateString = (dateStr) => {
  const [datePart] = dateStr.split(' ');
  const [yyyy, mm, dd] = datePart.split('-');
  return new Date(parseInt(yyyy), parseInt(mm) - 1, parseInt(dd)); //yyyy-mm-dd
}

// date String -> dd/mm or dd/mm/yyyy
export const formatDateStringToDate = (dateString, showYear = true) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  const dayMonth = `${date.getDate().toString().padStart(2, '0')}/${(date.getMonth() + 1).toString().padStart(2, '0')}`;
  return showYear ? `${dayMonth}/${date.getFullYear()}` : dayMonth;
};