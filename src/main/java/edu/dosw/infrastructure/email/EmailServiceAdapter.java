package edu.dosw.infrastructure.email;

import edu.dosw.domain.ports.EmailServicePort;
import edu.dosw.domain.model.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailServiceAdapter implements EmailServicePort {

    private final JavaMailSender mailSender;

    @Override
    public boolean sendNotificationEmail(Notification notification) {
        try {
            String userEmail = notification.getUserEmail();

            if (userEmail == null || userEmail.trim().isEmpty()) {
                log.error("Cannot send email: User email is null or empty for userId: {}", notification.getUserId());
                return false;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(userEmail);
            message.setSubject(notification.getTitle());
            message.setText(buildEmailText(notification));

            mailSender.send(message);

            log.info("Email sent successfully to: {} for user: {}", userEmail, notification.getUserId());
            return true;

        } catch (Exception e) {
            log.error("Error sending email to {} for user {}: {}",
                    notification.getUserEmail(), notification.getUserId(), e.getMessage());
            return false;
        }
    }

    @Override
    public boolean sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("HTML email sent successfully to: {}", to);
            return true;

        } catch (MessagingException e) {
            log.error("Error sending HTML email to {}: {}", to, e.getMessage());
            return false;
        }
    }


    private String buildEmailText(Notification notification) {
        return String.format(
                "%s%n%n%s%n%nDate: %s%nType: %s%n%nThank you,%nECI Express Team",
                notification.getTitle(),
                notification.getMessage(),
                notification.getCreatedAt(),
                notification.getType().name()
        );
    }
}