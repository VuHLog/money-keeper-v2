package com.vuhlog.money_keeper.service.ServiceImpl;

import com.vuhlog.money_keeper.common.UserCommon;
import com.vuhlog.money_keeper.dao.NotificationRepository;
import com.vuhlog.money_keeper.dto.request.NotificationRequest;
import com.vuhlog.money_keeper.dto.response.NotificationResponse;
import com.vuhlog.money_keeper.dto.response.responseinterface.ExpenseLimitNotification;
import com.vuhlog.money_keeper.entity.Notification;
import com.vuhlog.money_keeper.entity.Users;
import com.vuhlog.money_keeper.exception.AppException;
import com.vuhlog.money_keeper.exception.ErrorCode;
import com.vuhlog.money_keeper.mapper.NotificationMapper;
import com.vuhlog.money_keeper.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final UserCommon userCommon;
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public Page<NotificationResponse> getAllNotifications(Integer pageNumber, Integer pageSize) {
        Sort sortable = Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortable);
        return notificationRepository.findAll(pageable).map(notificationMapper::toNotificationResponse);
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
                .content("Hạn mức: <strong class='text-green-accent-4'>" + nameJoin + "</strong> đã bội chi. Bạn hãy điều chỉnh thói quen chi tiêu nhé!")
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
    public String updateReadStatus(String id, Integer readStatus) {
        Notification notification = notificationRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_EXISTED));
        notification.setReadStatus(readStatus);
        return "update read status successfully";
    }

    @Override
    public void deleteNotification(String id) {
        Notification notification = notificationRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_EXISTED));
        notificationRepository.deleteById(id);
    }
}
