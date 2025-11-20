package edu.dosw.application.services;

import edu.dosw.application.ports.EventServicePort;
import edu.dosw.domain.ports.NotificationRepositoryPort;
import edu.dosw.domain.ports.EmailServicePort;
import edu.dosw.domain.ports.WebSocketEmitterPort;
import edu.dosw.application.dto.command.NotificationCommand;
import edu.dosw.domain.model.Notification;
import edu.dosw.domain.model.ValueObject.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationApplicationService implements EventServicePort {

    private final NotificationRepositoryPort notificationRepositoryPort;
    private final EmailServicePort emailServicePort;
    private final WebSocketEmitterPort webSocketEmitterPort;

    @Override
    @Transactional
    public void processSuccessfulLogin(NotificationCommand command) {
        try {
            log.info("Processing login notification for user: {}", command.getUserId());

            Notification notification = createLoginNotification(command);
            Notification savedNotification = notificationRepositoryPort.save(notification);

            boolean emailSuccessful = emailServicePort.sendNotificationEmail(savedNotification);
            savedNotification.addDeliveryAttempt(Channel.EMAIL, emailSuccessful,
                    emailSuccessful ? null : "Error sending email");

            notificationRepositoryPort.save(savedNotification);
            webSocketEmitterPort.emitUserNotification(command.getUserId(), savedNotification);

            log.info("Login notification processed successfully: {}", savedNotification.getId().getValue());

        } catch (Exception e) {
            log.error("Error processing login notification: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing login notification", e);
        }
    }

    @Override
    @Transactional
    public void processNewOrder(NotificationCommand command) {
        try {
            log.info("Processing new order notification: {}", command.getOrderId());

            Notification notification = createNewOrderNotification(command);
            Notification savedNotification = notificationRepositoryPort.save(notification);

            webSocketEmitterPort.emitUserNotification(command.getUserId(), savedNotification);

            log.info("New order notification processed successfully: {}", command.getOrderId());

        } catch (Exception e) {
            log.error("Error processing new order notification: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing new order notification", e);
        }
    }

    @Override
    @Transactional
    public void processOrderStatusChange(NotificationCommand command) {
        try {
            log.info("Processing order status change: {}", command.getOrderStatus());

            Notification notification = createOrderStatusNotification(command);
            Notification savedNotification = notificationRepositoryPort.save(notification);

            boolean emailSuccessful = emailServicePort.sendNotificationEmail(savedNotification);
            savedNotification.addDeliveryAttempt(Channel.EMAIL, emailSuccessful,
                    emailSuccessful ? null : "Error sending email");

            notificationRepositoryPort.save(savedNotification);
            webSocketEmitterPort.emitUserNotification(command.getUserId(), savedNotification);

            log.info("Order status notification processed successfully: {}", command.getOrderId());

        } catch (Exception e) {
            log.error("Error processing order status notification: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing order status notification", e);
        }
    }

    private Notification createLoginNotification(NotificationCommand command) {
        return Notification.builder()
                .id(new NotificationId(UUID.randomUUID().toString()))
                .userId(command.getUserId())
                .title("Login detected")
                .message("Your account was accessed from IP: " + command.getIp())
                .type(NotificationType.SECURITY_LOGIN)
                .status(NotificationStatus.PENDING)
                .channels(java.util.List.of(Channel.EMAIL, Channel.WEB_SOCKET))
                .deliveryAttempts(java.util.List.of())
                .createdAt(java.time.LocalDateTime.now())
                .build();
    }

    private Notification createNewOrderNotification(NotificationCommand command) {
        return Notification.builder()
                .id(new NotificationId(UUID.randomUUID().toString()))
                .userId(command.getUserId())
                .title("New Order Received")
                .message("You have a new order #" + command.getOrderId() + " to prepare")
                .type(NotificationType.SELLER_NEW_ORDER)
                .status(NotificationStatus.PENDING)
                .channels(java.util.List.of(Channel.WEB_SOCKET, Channel.EMAIL))
                .deliveryAttempts(java.util.List.of())
                .createdAt(java.time.LocalDateTime.now())
                .metadata("{\"orderId\":\"" + command.getOrderId() + "\",\"pointOfSaleId\":\"" + command.getPointOfSaleId() + "\"}")
                .build();
    }

    private Notification createOrderStatusNotification(NotificationCommand command) {
        String statusMessage = getStatusMessage(command.getOrderStatus());
        return Notification.builder()
                .id(new NotificationId(UUID.randomUUID().toString()))
                .userId(command.getUserId())
                .title("Order Status Update")
                .message("Order #" + command.getOrderId() + " is now " + statusMessage)
                .type(NotificationType.ORDER_CONFIRMED)
                .status(NotificationStatus.PENDING)
                .channels(java.util.List.of(Channel.EMAIL, Channel.WEB_SOCKET))
                .deliveryAttempts(java.util.List.of())
                .createdAt(java.time.LocalDateTime.now())
                .metadata("{\"orderId\":\"" + command.getOrderId() + "\",\"status\":\"" + command.getOrderStatus() + "\"}")
                .build();
    }

    private String getStatusMessage(String status) {
        return switch (status.toLowerCase()) {
            case "confirmed" -> "confirmed and in preparation";
            case "preparation" -> "being prepared";
            case "ready" -> "ready for pickup";
            case "delivered" -> "delivered";
            case "refunded" -> "refunded";
            default -> status;
        };
    }
}