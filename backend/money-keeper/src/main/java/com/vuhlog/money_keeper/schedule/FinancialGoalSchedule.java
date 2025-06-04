package com.vuhlog.money_keeper.schedule;

import com.vuhlog.money_keeper.dao.FinancialGoalRepository;
import com.vuhlog.money_keeper.entity.FinancialGoal;
import com.vuhlog.money_keeper.service.FinancialGoalService;
import com.vuhlog.money_keeper.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FinancialGoalSchedule {
    private final FinancialGoalRepository financialGoalRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Ho_Chi_Minh")
    public void checkDeadline() {
        List<FinancialGoal> financialGoals = financialGoalRepository.findByStatus(0);
        for (FinancialGoal financialGoal : financialGoals) {
            if (financialGoal.getStatus() == 0 && financialGoal.getDeadline() != null && financialGoal.getDeadline().isBefore(LocalDate.now())) {
                log.info("Financial goal {} has reached its deadline.", financialGoal.getName());
                financialGoal.setStatus(2);
                financialGoalRepository.save(financialGoal);

                notificationService.financialGoalDeadlineExpired(financialGoal);
            }
        }
    }
}
