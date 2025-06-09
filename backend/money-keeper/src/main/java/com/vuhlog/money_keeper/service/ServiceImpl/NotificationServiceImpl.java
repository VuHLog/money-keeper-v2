package com.vuhlog.money_keeper.service.ServiceImpl;

import com.vuhlog.money_keeper.common.UserCommon;
import com.vuhlog.money_keeper.dao.NotificationRepository;
import com.vuhlog.money_keeper.dto.request.NotificationRequest;
import com.vuhlog.money_keeper.dto.response.NotificationResponse;
import com.vuhlog.money_keeper.dto.response.responseinterface.ExpenseLimitNotification;
import com.vuhlog.money_keeper.entity.*;
import com.vuhlog.money_keeper.exception.AppException;
import com.vuhlog.money_keeper.exception.ErrorCode;
import com.vuhlog.money_keeper.mapper.NotificationMapper;
import com.vuhlog.money_keeper.service.NotificationService;
import com.vuhlog.money_keeper.service.ReportService;
import com.vuhlog.money_keeper.util.FormatUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final UserCommon userCommon;
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ReportService reportService;

    @Override
    public Page<NotificationResponse> getAllNotifications(Integer pageNumber, Integer pageSize, Integer readStatus) {
        Specification<Notification> specs = Specification.where(null);
        if(readStatus == 1 || readStatus == 0) {
            specs = specs.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("readStatus"), readStatus));
        }
        Sort sortable = Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortable);
        return notificationRepository.findAll(specs, pageable).map(notificationMapper::toNotificationResponse);
    }

    @Override
    public Long countAllNotificationsByReadStatus(Integer readStatus) {
        return notificationRepository.countAllByReadStatusAndUser_Id(readStatus, userCommon.getMyUserInfo().getId());
    }

    @Override
    public NotificationResponse createNotification(NotificationRequest request) {
        Notification notification = notificationMapper.toNotification(request);
        notification.setUser(userCommon.getMyUserInfo());
        return notificationMapper.toNotificationResponse(notificationRepository.save(notification));
    }

    @Override
    public NotificationResponse expenseLimitNotification(List<ExpenseLimitNotification> expenseLimitNotifications) {
        List<String> name = expenseLimitNotifications.stream().map(ExpenseLimitNotification::getName).toList();
        String nameJoin = String.join(", ", name);
        Users user = userCommon.getMyUserInfo();
        Notification notification = Notification.builder()
                .title("Hạn mức chi")
                .content("Hạn mức chi : <strong class='text-primary'>" + nameJoin + "</strong> đã bội chi. Bạn hãy điều chỉnh thói quen chi tiêu nhé!")
                .type("expense limit")
                .readStatus(0)
                .iconUrl("https://res.cloudinary.com/cloud1412/image/upload/v1745068565/logo_mpkmjj.png")
                .user(user)
                .href("/expense-limit")
                .build();

        NotificationResponse notificationResponse = null;
        try {
            notificationResponse = notificationMapper.toNotificationResponse(notificationRepository.save(notification));
            messagingTemplate.convertAndSend("/topic/notifications/" + user.getId(), notificationResponse);
        } catch (Exception e) {
            log.error("WebSocket gửi thất bại: {}", e.getMessage());
        }
        return notificationResponse;
    }

    @Override
    public NotificationResponse expenseNotification(ExpenseRegular expenseRegular,Boolean isCreate) {
        Users user = userCommon.getMyUserInfo();
        Notification notification = Notification.builder()
                .title(isCreate?"Thêm mới chi tiêu":"Điều chỉnh chi tiêu")
                .content("Tài khoản <strong class='text-primary'>" + (expenseRegular.getDictionaryBucketPayment() == null? "không xác định" : expenseRegular.getDictionaryBucketPayment().getAccountName()) + "</strong> đã chi số tiền là " + "<span class='text-danger'>" + formatCurrency(expenseRegular.getAmount(), expenseRegular.getCurrency(), expenseRegular.getCurrencySymbol()) + "</span>. Số dư hiện tại là " + "<strong>" + formatCurrency(Math.round(expenseRegular.getConvertedBalance()), expenseRegular.getCurrency(), expenseRegular.getCurrencySymbol()) + "</strong>")
                .type("expense")
                .readStatus(0)
                .iconUrl("https://res.cloudinary.com/cloud1412/image/upload/v1745068565/logo_mpkmjj.png")
                .user(user)
                .href("/expense")
                .build();

        NotificationResponse notificationResponse = null;
        try {
            notificationResponse = notificationMapper.toNotificationResponse(notificationRepository.save(notification));
            messagingTemplate.convertAndSend("/topic/notifications/" + user.getId(), notificationResponse);
        } catch (Exception e) {
            log.error("WebSocket gửi thất bại: {}", e.getMessage());
        }
        return notificationResponse;
    }

    @Override
    public NotificationResponse expenseForGoalNotification(ExpenseRegular expenseRegular) {
        Users user = userCommon.getMyUserInfo();
        Notification notification = Notification.builder()
                .title("Thêm mới chi tiêu")
                .content("Tài khoản <strong class='text-primary'>" + (expenseRegular.getDictionaryBucketPayment() == null? "không xác định" : expenseRegular.getDictionaryBucketPayment().getAccountName()) + "</strong> đã chi số tiền cho mục tiêu <strong>" + expenseRegular.getFinancialGoal().getName() + "</strong> là " + "<span class='text-danger'>" + formatCurrency(expenseRegular.getAmount(), expenseRegular.getCurrency(), expenseRegular.getCurrencySymbol()) + "</span>. Số dư hiện tại là " + "<strong>" + formatCurrency(Math.round(expenseRegular.getConvertedBalance()), expenseRegular.getCurrency(), expenseRegular.getCurrencySymbol()) + "</strong>")
                .type("expense")
                .readStatus(0)
                .iconUrl("https://res.cloudinary.com/cloud1412/image/upload/v1745068565/logo_mpkmjj.png")
                .user(user)
                .href("/expense")
                .build();

        NotificationResponse notificationResponse = null;
        try {
            notificationResponse = notificationMapper.toNotificationResponse(notificationRepository.save(notification));
            messagingTemplate.convertAndSend("/topic/notifications/" + user.getId(), notificationResponse);
        } catch (Exception e) {
            log.error("WebSocket gửi thất bại: {}", e.getMessage());
        }
        return notificationResponse;
    }

    @Override
    public NotificationResponse financialGoalDeadlineExpired(FinancialGoal financialGoal) {
        Notification notification = Notification.builder()
                .title("Mục tiêu tài chính hết hạn")
                .content("Mục tiêu <strong class='text-primary'>" + financialGoal.getName() + "</strong> đã hết hạn để hoàn thành. Hãy gia hạn để tiếp tục nhé!")
                .type("financial_goal")
                .readStatus(0)
                .iconUrl("https://res.cloudinary.com/cloud1412/image/upload/v1745068565/logo_mpkmjj.png")
                .user(financialGoal.getBucketPayment().getUser())
                .href("/goals")
                .build();

        return notificationMapper.toNotificationResponse(notificationRepository.save(notification));
    }

    @Override
    public NotificationResponse deleteExpenseNotification(ExpenseRegular expenseRegular) {
        Users user = userCommon.getMyUserInfo();
        Notification notification = Notification.builder()
                .title("Xóa khoản chi tiêu")
                .content("Tài khoản <strong class='text-primary'>" + (expenseRegular.getDictionaryBucketPayment() == null? "không xác định" : expenseRegular.getDictionaryBucketPayment().getAccountName()) + "</strong> đã xóa khoản chi số tiền là " + "<span class='text-danger'>" + formatCurrency(expenseRegular.getAmount(), expenseRegular.getCurrency(), expenseRegular.getCurrencySymbol()) + "</span>. Số dư hiện tại là " + "<strong>" + formatCurrency(Math.round(expenseRegular.getConvertedBalance()), expenseRegular.getCurrency(), expenseRegular.getCurrencySymbol()) + "</strong>")
                .type("expense")
                .readStatus(0)
                .iconUrl("https://res.cloudinary.com/cloud1412/image/upload/v1745068565/logo_mpkmjj.png")
                .user(user)
                .href("/expense")
                .build();

        NotificationResponse notificationResponse = null;
        try {
            notificationResponse = notificationMapper.toNotificationResponse(notificationRepository.save(notification));
            messagingTemplate.convertAndSend("/topic/notifications/" + user.getId(), notificationResponse);
        } catch (Exception e) {
            log.error("WebSocket gửi thất bại: {}", e.getMessage());
        }
        return notificationResponse;
    }

    @Override
    public NotificationResponse expenseTransferNotification(ExpenseRegular expenseRegular) {
        Users user = userCommon.getMyUserInfo();
        Notification notification = Notification.builder()
                .title("Chuyển khoản")
                .content("Tài khoản <strong class='text-primary'>" + (expenseRegular.getDictionaryBucketPayment() == null? "không xác định" : expenseRegular.getDictionaryBucketPayment().getAccountName()) + "</strong>" + " tới tài khoản " + "<strong class='text-danger'>" + expenseRegular.getBeneficiaryAccount().getAccountName() + "</strong> với số tiền là " + "<span class='text-error'>" + formatCurrency(expenseRegular.getAmount(), expenseRegular.getCurrency(), expenseRegular.getCurrencySymbol()) + "</span>. Số dư hiện tại là " + "<strong>" + formatCurrency(Math.round(expenseRegular.getConvertedBalance()), expenseRegular.getCurrency(), expenseRegular.getCurrencySymbol()) + "</strong>")
                .type("expense")
                .readStatus(0)
                .iconUrl("https://res.cloudinary.com/cloud1412/image/upload/v1745068565/logo_mpkmjj.png")
                .user(user)
                .href("/expense")
                .build();

        NotificationResponse notificationResponse = null;
        try {
            notificationResponse = notificationMapper.toNotificationResponse(notificationRepository.save(notification));
            messagingTemplate.convertAndSend("/topic/notifications/" + user.getId(), notificationResponse);
        } catch (Exception e) {
            log.error("WebSocket gửi thất bại: {}", e.getMessage());
        }
        return notificationResponse;
    }

    @Override
    public NotificationResponse revenueNotification(RevenueRegular revenueRegular, Boolean isCreate) {
        Users user = userCommon.getMyUserInfo();
        Notification notification = Notification.builder()
                .title(isCreate?"Thêm mới thu nhập":"Điều chỉnh thu nhập")
                .content( "Tài khoản <strong class='text-primary'>" + (revenueRegular.getDictionaryBucketPayment() == null? "không xác định" : revenueRegular.getDictionaryBucketPayment().getAccountName()) + "</strong> đã nhận số tiền là " + "<span class='text-success'>" + formatCurrency(revenueRegular.getAmount(), revenueRegular.getCurrency(), revenueRegular.getCurrencySymbol()) + "</span>. Số dư hiện tại là " + "<strong>" + formatCurrency(Math.round(revenueRegular.getConvertedBalance()), revenueRegular.getCurrency(), revenueRegular.getCurrencySymbol()) + "</strong>")
                .type("revenue")
                .readStatus(0)
                .iconUrl("https://res.cloudinary.com/cloud1412/image/upload/v1745068565/logo_mpkmjj.png")
                .user(user)
                .href("/revenue")
                .build();

        NotificationResponse notificationResponse = null;
        try {
            notificationResponse = notificationMapper.toNotificationResponse(notificationRepository.save(notification));
            messagingTemplate.convertAndSend("/topic/notifications/" + user.getId(), notificationResponse);
        } catch (Exception e) {
            log.error("WebSocket gửi thất bại: {}", e.getMessage());
        }
        return notificationResponse;
    }

    @Override
    public NotificationResponse deleteRevenueNotification(RevenueRegular revenueRegular) {
        Users user = userCommon.getMyUserInfo();
        Notification notification = Notification.builder()
                .title("Xóa khoản thu nhập")
                .content("Tài khoản <strong class='text-primary'>" + (revenueRegular.getDictionaryBucketPayment() == null? "không xác định" : revenueRegular.getDictionaryBucketPayment().getAccountName()) + "</strong> đã xóa khoản thu số tiền là " + "<span class='text-success'>" + formatCurrency(revenueRegular.getAmount(), revenueRegular.getCurrency(), revenueRegular.getCurrencySymbol()) + "</span>. Số dư hiện tại là " + "<strong>" + formatCurrency(Math.round(revenueRegular.getConvertedBalance()), revenueRegular.getCurrency(), revenueRegular.getCurrencySymbol()) + "</strong>")
                .type("revenue")
                .readStatus(0)
                .iconUrl("https://res.cloudinary.com/cloud1412/image/upload/v1745068565/logo_mpkmjj.png")
                .user(user)
                .href("/revenue")
                .build();

        NotificationResponse notificationResponse = null;
        try {
            notificationResponse = notificationMapper.toNotificationResponse(notificationRepository.save(notification));
            messagingTemplate.convertAndSend("/topic/notifications/" + user.getId(), notificationResponse);
        } catch (Exception e) {
            log.error("WebSocket gửi thất bại: {}", e.getMessage());
        }
        return notificationResponse;
    }

    public static String formatCurrency(long amount, String currency, String currencySymbol) {
        String amountFormat = FormatUtil.formatCurrency(amount);
        if(currency.equals("VND")) {
            return amountFormat + currencySymbol;
        } else {
            return currencySymbol + amountFormat;
        }
    }

    @Override
    public String updateReadStatus(String id, Integer readStatus) {
        Notification notification = notificationRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_EXISTED));
        notification.setReadStatus(readStatus);
        notificationRepository.save(notification);
        return "update read status successfully";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateAllReadStatus(Integer readStatus) {
        notificationRepository.updateAllReadStatus(readStatus, userCommon.getMyUserInfo().getId());
        return "update read status successfully";
    }

    @Override
    public void deleteNotification(String id) {
        Notification notification = notificationRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_EXISTED));
        notificationRepository.deleteById(id);
    }
}
