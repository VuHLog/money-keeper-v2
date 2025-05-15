package com.vuhlog.money_keeper.schedule;

import com.vuhlog.money_keeper.constants.ExpenseLimitTimeEnum;
import com.vuhlog.money_keeper.dao.ExpenseLimitRepository;
import com.vuhlog.money_keeper.entity.ExpenseLimit;
import com.vuhlog.money_keeper.service.ExpenseLimitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpenseLimitSchedule {
    private final ExpenseLimitRepository expenseLimitRepository;
    private final ExpenseLimitService expenseLimitService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Ho_Chi_Minh")
    public void checkExpenseLimit() {
        List<ExpenseLimit> expenseLimits = expenseLimitRepository.findAll();
        LocalDate now = LocalDate.now();
        for (ExpenseLimit expenseLimit : expenseLimits) {
            if(expenseLimit.getEndDateLimit().toLocalDateTime().toLocalDate().isBefore(now)){
                Timestamp newEndDateLimit = Timestamp.valueOf(now.atStartOfDay());
                if(expenseLimit.getRepeatTime().equals(ExpenseLimitTimeEnum.NO_REPEAT.getType())){
                    continue;
                }else if(expenseLimit.getRepeatTime().equals(ExpenseLimitTimeEnum.EVERY_DAY.getType())) {
                    newEndDateLimit = Timestamp.valueOf(now.atStartOfDay());
                }else if(expenseLimit.getRepeatTime().equals(ExpenseLimitTimeEnum.EVERY_WEEK.getType())) {
                    newEndDateLimit = Timestamp.valueOf(now.plusWeeks(1).atStartOfDay());
                }else if(expenseLimit.getRepeatTime().equals(ExpenseLimitTimeEnum.EVERY_MONTH.getType())) {
                    newEndDateLimit = Timestamp.valueOf(now.plusMonths(1).atStartOfDay());
                }else if(expenseLimit.getRepeatTime().equals(ExpenseLimitTimeEnum.EVERY_YEAR.getType())) {
                    newEndDateLimit = Timestamp.valueOf(now.plusYears(1).atStartOfDay());
                }else if (expenseLimit.getRepeatTime().equals(ExpenseLimitTimeEnum.EVERY_QUARTER.getType())) {
                    newEndDateLimit = Timestamp.valueOf(now.plusMonths(3).atStartOfDay());
                }
                if(expenseLimit.getEndDate() == null || expenseLimit.getEndDate().after(newEndDateLimit) ||
                        expenseLimit.getEndDate().equals(newEndDateLimit)){
                    expenseLimit.setStartDateLimit(Timestamp.valueOf(now.atStartOfDay()));
                    expenseLimit.setEndDateLimit(newEndDateLimit);
                    expenseLimitRepository.save(expenseLimit);
                }
            }
        }
    }
}
