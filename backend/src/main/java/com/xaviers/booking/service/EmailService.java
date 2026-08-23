package com.xaviers.booking.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    @Value("${spring.mail.username:}")
    private String from;

    public EmailService(JavaMailSender mailSender) { this.mailSender = mailSender; }

    @Async
    public void send(String to, String subject, String body) {
        try {
            if (from == null || from.isBlank()) {
                log.info("[Email mock] To:{} Subject:{} Body:{}", to, subject, body);
                return;
            }
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(from);
            msg.setTo(to);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
            log.info("Email sent to {}", to);
        } catch (Exception e) {
            log.warn("Email failed to {}: {}", to, e.getMessage());
        }
    }
}
