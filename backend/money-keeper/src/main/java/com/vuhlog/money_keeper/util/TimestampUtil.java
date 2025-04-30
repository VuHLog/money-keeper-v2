package com.vuhlog.money_keeper.util;

import com.vuhlog.money_keeper.constants.TimeOptionType;
import com.vuhlog.money_keeper.model.CalculationTime;
import com.vuhlog.money_keeper.model.PeriodOfTime;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Calendar;

public class TimestampUtil {
    public static Timestamp stringToTimestamp(String dateString) {
        try {
            if(dateString == null || dateString.isEmpty()) return null;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return new Timestamp(dateFormat.parse(dateString).getTime());
        } catch (ParseException e) {
            throw new RuntimeException("Failed to parse date: " + dateString, e);
        }
    }

    public static String timestampToString(Timestamp timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(timestamp);
    }

    public static String timestampToStringOnlyDate(Timestamp timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return dateFormat.format(timestamp);
    }

    public static String timestampToStringIfTodayTime(Timestamp timestamp) {
        long elapseSeconds = ChronoUnit.SECONDS.between(timestamp.toInstant(), Instant.now());

        if (elapseSeconds < 60) {
            return elapseSeconds + " giây trước";
        } else if (elapseSeconds < 3600) {
            long elapseMinutes = ChronoUnit.MINUTES.between(timestamp.toInstant(), Instant.now());
            return elapseMinutes + " phút trước";
        } else if (elapseSeconds < 86400) {
            long elapseHours = ChronoUnit.HOURS.between(timestamp.toInstant(), Instant.now());
            return elapseHours + " giờ trước";
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        return dateFormat.format(timestamp);
    }

    public static PeriodOfTime getPeriodOfTime(String timeOption) {
        PeriodOfTime periodOfTime = new PeriodOfTime();
        LocalDateTime localDateTimeNow = LocalDateTime.now();

        if(timeOption.equals(TimeOptionType.TODAY.getType())){
            LocalDateTime startDateTimeToday = LocalDateTime.of(localDateTimeNow.getYear(), localDateTimeNow.getMonth(), localDateTimeNow.getDayOfMonth(), 0, 0, 0);
            periodOfTime.setStartDate(Timestamp.valueOf(startDateTimeToday));
            LocalDateTime nextDay = localDateTimeNow.plusDays(1);
            LocalDateTime endDateTimeToday = LocalDateTime.of(nextDay.getYear(), nextDay.getMonth(), nextDay.getDayOfMonth(), 0, 0, 0);
            periodOfTime.setEndDate(Timestamp.valueOf(endDateTimeToday));
        }else if(timeOption.equalsIgnoreCase(TimeOptionType.THIS_WEEK.getType())){
            LocalDateTime firstDayOfWeek = localDateTimeNow.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).withHour(0).withMinute(0).withSecond(0);
            periodOfTime.setStartDate(Timestamp.valueOf(firstDayOfWeek));
            LocalDateTime lastDayOfWeek = localDateTimeNow.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).plusDays(1).withHour(0).withMinute(0).withSecond(0);
            periodOfTime.setEndDate(Timestamp.valueOf(lastDayOfWeek));
        }else if(timeOption.equalsIgnoreCase(TimeOptionType.LAST_30_DAYS.getType())){
            LocalDateTime localDateTimeLast30Days = localDateTimeNow.minusDays(30);
            periodOfTime.setStartDate(Timestamp.valueOf(localDateTimeLast30Days));
            periodOfTime.setEndDate(Timestamp.valueOf(localDateTimeNow));
        }else if(timeOption.equalsIgnoreCase(TimeOptionType.THIS_MONTH.getType())){
            LocalDateTime startDateTimeThisMonth = LocalDateTime.of(localDateTimeNow.getYear(), localDateTimeNow.getMonth(), 1, 0, 0, 0);
            periodOfTime.setStartDate(Timestamp.valueOf(startDateTimeThisMonth));
            LocalDateTime endDateTimeThisMonth = LocalDateTime.of(localDateTimeNow.getYear(), localDateTimeNow.getMonth(), 1, 23, 59, 59).with(TemporalAdjusters.lastDayOfMonth());
            endDateTimeThisMonth = endDateTimeThisMonth.plusDays(1).withHour(0).withMinute(0).withSecond(0);
            periodOfTime.setEndDate(Timestamp.valueOf(endDateTimeThisMonth));
        } else if(timeOption.equalsIgnoreCase(TimeOptionType.LAST_MONTH.getType())){
            LocalDateTime startDateTimeLastMonth = LocalDateTime.of(localDateTimeNow.getYear(), localDateTimeNow.minusMonths(1).getMonth(), 1, 0, 0, 0);
            periodOfTime.setStartDate(Timestamp.valueOf(startDateTimeLastMonth));
            LocalDateTime endDateTimeLastMonth = LocalDateTime.of(localDateTimeNow.getYear(), localDateTimeNow.minusMonths(1).getMonth(), 1, 23, 59, 59).with(TemporalAdjusters.lastDayOfMonth());
            endDateTimeLastMonth = endDateTimeLastMonth.plusDays(1).withHour(0).withMinute(0).withSecond(0);
            periodOfTime.setEndDate(Timestamp.valueOf(endDateTimeLastMonth));
        } else if(timeOption.equalsIgnoreCase(TimeOptionType.THIS_QUARTER.getType())){
            int currentQuarter = (localDateTimeNow.getMonthValue() - 1) / 3 + 1;
            int year = localDateTimeNow.getYear();
            int startMonth = (currentQuarter - 1) * 3 + 1; //tháng bắt đầu của quý này
            int endMonth = startMonth + 2;
            LocalDateTime startDateTime = LocalDateTime.of(year, startMonth, 1,0,0,0);
            LocalDateTime endDateTime = LocalDateTime.of(year, endMonth, 1,23,59,59).with(TemporalAdjusters.lastDayOfMonth()).plusDays(1).withHour(0).withMinute(0).withSecond(0);
            periodOfTime.setStartDate(Timestamp.valueOf(startDateTime));
            periodOfTime.setEndDate(Timestamp.valueOf(endDateTime));
        }else if (timeOption.equalsIgnoreCase(TimeOptionType.LAST_QUARTER.getType())){
            int currentQuarter = (localDateTimeNow.getMonthValue() - 1) / 3 + 1;
            int previousQuarter = currentQuarter - 1; // quy truoc
            int year = localDateTimeNow.getYear();
            // neu currentQuarter la Quy 1
            if (previousQuarter == 0) {
                previousQuarter = 4;
                year -= 1;
            }
            int startMonth = (previousQuarter - 1) * 3 + 1; //tháng bắt đầu của quý trước
            int endMonth = startMonth + 2;
            LocalDateTime startDateTime = LocalDateTime.of(year, startMonth, 1,0,0,0);
            LocalDateTime endDateTime = LocalDateTime.of(year, endMonth, 1,23,59,59).with(TemporalAdjusters.lastDayOfMonth()).plusDays(1).withHour(0).withMinute(0).withSecond(0);
            periodOfTime.setStartDate(Timestamp.valueOf(startDateTime));
            periodOfTime.setEndDate(Timestamp.valueOf(endDateTime));
        } else if (timeOption.equalsIgnoreCase(TimeOptionType.THIS_YEAR.getType())) {
            int year = localDateTimeNow.getYear();
            LocalDateTime startDateTime = LocalDateTime.of(year, 1, 1, 0, 0, 0);
            LocalDateTime endDateTime = LocalDateTime.of(year, 12, 31, 23,59,59).with(TemporalAdjusters.lastDayOfMonth()).plusDays(1).withHour(0).withMinute(0).withSecond(0);
            periodOfTime.setStartDate(Timestamp.valueOf(startDateTime));
            periodOfTime.setEndDate(Timestamp.valueOf(endDateTime));
        }else if (timeOption.equalsIgnoreCase(TimeOptionType.LAST_YEAR.getType())) {
            int year = localDateTimeNow.getYear() - 1;
            LocalDateTime startDateTime = LocalDateTime.of(year, 1, 1, 0, 0, 0);
            LocalDateTime endDateTime = LocalDateTime.of(year, 12, 31, 23,59,59).with(TemporalAdjusters.lastDayOfMonth()).plusDays(1).withHour(0).withMinute(0).withSecond(0);
            periodOfTime.setStartDate(Timestamp.valueOf(startDateTime));
            periodOfTime.setEndDate(Timestamp.valueOf(endDateTime));
        }
        return periodOfTime;
    }

    public static PeriodOfTime handleTimeOption(String timeOption, String startDate, String endDate) {
        PeriodOfTime periodOfTime = new PeriodOfTime();
        if (timeOption != null && !timeOption.isEmpty()) {
            if (timeOption.equalsIgnoreCase(TimeOptionType.FULL.getType())) {
                return null;
            } else if (timeOption.equalsIgnoreCase(TimeOptionType.OPTIONAL.getType())) {
                if (startDate != null && !startDate.isEmpty()) {
                    periodOfTime.setStartDate(TimestampUtil.stringToTimestamp(startDate));
                }
                if (endDate != null && !endDate.isEmpty()) {
                    // time 23:59:59
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(TimestampUtil.stringToTimestamp(endDate));
                    calendar.set(Calendar.HOUR_OF_DAY, 23);
                    calendar.set(Calendar.MINUTE, 59);
                    calendar.set(Calendar.SECOND, 59);
                    calendar.set(Calendar.MILLISECOND, 0);
                    periodOfTime.setEndDate(new Timestamp(calendar.getTimeInMillis()));
                }
                return periodOfTime;
            } else {
                return TimestampUtil.getPeriodOfTime(timeOption);
            }
        }
        return null;
    }

    public static CalculationTime calculationTime(String startDateStr, String endDateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDate startDate = LocalDate.parse(startDateStr, formatter);
        LocalDate endDate = LocalDate.parse(endDateStr, formatter);

        // start date -> end day of month
        LocalDate endOfStartMonth = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // months in between
        YearMonth current = YearMonth.from(startDate).plusMonths(1);
        YearMonth endMonth = YearMonth.from(endDate);
        LocalDate startDateBetween = null;
        LocalDate endDateBetween = null;

        // start day of month end date -> end date
        LocalDate startDateOfMonthEndDate = null;

        if(endMonth.isAfter(current)){
            startDateBetween = current.atDay(1);
            endDateBetween = endMonth.minusMonths(1).atEndOfMonth();

            startDateOfMonthEndDate = endDate.withDayOfMonth(1);
        }else {
            endOfStartMonth = endDate;
        }
        return CalculationTime.builder()
                .startDate(startDate)
                .endOfStartMonth(endOfStartMonth)
                .startDateBetween(startDateBetween)
                .endDateBetween(endDateBetween)
                .startDateOfMonthEndDate(startDateOfMonthEndDate)
                .endDate(endDate)
                .build();
    }
}
