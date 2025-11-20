package edu.dosw.infrastructure.event;

import edu.dosw.application.ports.EventServicePort;
import edu.dosw.application.dto.command.NotificationCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginEventListener implements MessageListener {

    private final EventServicePort eventServicePort;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(pattern);
            String body = new String(message.getBody());

            log.info("Event received - Channel: {}, Message: {}", channel, body);

            if ("login.exitoso".equals(channel)) {
                LoginEventData eventData = objectMapper.readValue(body, LoginEventData.class);

                NotificationCommand command = new NotificationCommand();
                command.setUserId(eventData.getUserId());
                command.setEmail(eventData.getEmail());
                command.setName(eventData.getNombre());
                command.setIp(eventData.getIp());

                eventServicePort.processSuccessfulLogin(command);

                log.info("Login event processed for user: {}", eventData.getUserId());
            }

        } catch (Exception e) {
            log.error("Error processing Redis event: {}", e.getMessage(), e);
        }
    }

    @lombok.Data
    private static class LoginEventData {
        private String eventId;
        private String email;
        private String userId;
        private String nombre;
        private String ip;
        private String timestamp;
        private String role;
    }
}