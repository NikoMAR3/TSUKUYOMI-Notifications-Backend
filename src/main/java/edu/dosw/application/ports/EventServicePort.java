package edu.dosw.application.ports;

import edu.dosw.application.dto.command.NotificationCommand;

public interface EventServicePort {
    void processSuccessfulLogin(NotificationCommand command);
    void processNewOrder(NotificationCommand command);
    void processOrderStatusChange(NotificationCommand command);
}