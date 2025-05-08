package com.vuhlog.money_keeper.controller;

import com.vuhlog.money_keeper.dto.request.NotificationRequest;
import com.vuhlog.money_keeper.dto.response.ApiResponse;
import com.vuhlog.money_keeper.dto.response.NotificationResponse;
import com.vuhlog.money_keeper.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("")
    public ApiResponse<Page<NotificationResponse>> getAllNotifications(
            @RequestParam (name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam (name = "pageSize", required = false, defaultValue = "5") Integer pageSize,
            @RequestParam (name = "readStatus", required = false, defaultValue = "-1") Integer readStatus
    ) {
        return ApiResponse.<Page<NotificationResponse>>builder()
                .result(notificationService.getAllNotifications(pageNumber, pageSize, readStatus))
                .build();
    }

    @GetMapping("/count-read-status")
    public ApiResponse<Long> countAllNotificationsByReadStatus(@RequestParam(name = "readStatus",defaultValue = "0") Integer readStatus) {
        return ApiResponse.<Long>builder()
                .result(notificationService.countAllNotificationsByReadStatus(readStatus))
                .build();
    }

    @PostMapping()
    public ApiResponse<NotificationResponse> createNotification(@RequestBody NotificationRequest request) {
        return ApiResponse.<NotificationResponse>builder()
                .result(notificationService.createNotification(request))
                .build();
    }

    @PatchMapping("/{id}/read-status")
    public ApiResponse<String> updateReadStatus(@PathVariable String id, @RequestBody Integer readStatus) {
        return ApiResponse.<String>builder()
                .result(notificationService.updateReadStatus(id, readStatus))
                .build();
    }

    @PatchMapping("/update-all-read-status")
    public ApiResponse<String> updateAllReadStatus(@RequestBody Integer readStatus) {
        return ApiResponse.<String>builder()
                .result(notificationService.updateAllReadStatus(readStatus))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteNotification(@PathVariable String id) {
        notificationService.deleteNotification(id);
        return ApiResponse.<String>builder()
                .result("Delete notification successfully")
                .build();
    }
}
