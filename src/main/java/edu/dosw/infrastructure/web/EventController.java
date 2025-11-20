package edu.dosw.infrastructure.web;

import edu.dosw.application.ports.EventServicePort;
import edu.dosw.application.dto.command.NotificationCommand;
import edu.dosw.infrastructure.web.mappers.EventWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventServicePort eventServicePort;
    private final EventWebMapper eventWebMapper;

    @PostMapping("/successful-login")
    public ResponseEntity<Void> receiveSuccessfulLogin(@RequestBody NotificationCommand command) {
        try {
            eventServicePort.processSuccessfulLogin(command);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/new-order")
    public ResponseEntity<Void> receiveNewOrder(@RequestBody NotificationCommand command) {
        try {
            eventServicePort.processNewOrder(command);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/order-status-change")
    public ResponseEntity<Void> receiveOrderStatusChange(@RequestBody NotificationCommand command) {
        try {
            eventServicePort.processOrderStatusChange(command);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/test-notification")
    public ResponseEntity<Void> sendTestNotification(@RequestBody Map<String, Object> request) {
        try {
            // Usar mapper para convertir Map a NotificationCommand
            NotificationCommand command = eventWebMapper.toCommand(request);
            eventServicePort.processSuccessfulLogin(command);
            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/custom-event")
    public ResponseEntity<Void> receiveCustomEvent(@RequestBody Map<String, Object> request) {
        try {
            NotificationCommand command = eventWebMapper.toCommand(request);

            if (request.containsKey("orderId") && request.containsKey("orderStatus")) {
                eventServicePort.processOrderStatusChange(command);
            } else if (request.containsKey("orderId")) {
                eventServicePort.processNewOrder(command);
            } else {
                eventServicePort.processSuccessfulLogin(command);
            }

            return ResponseEntity.accepted().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}