package com.vms.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendResetPasswordEmail(String to, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Reset Your Password - Daily Activities Management");

            String htmlContent = String.format(
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; color: #333;'>" +
                "<div style='background: linear-gradient(135deg, #1e293b 0%%, #0f172a 100%%); padding: 30px; border-radius: 10px 10px 0 0; text-align: center;'>" +
                "<h1 style='color: white; margin: 0; font-size: 24px;'>Daily Activities Management</h1>" +
                "</div>" +
                "<div style='background: #f8fafc; padding: 30px; border-radius: 0 0 10px 10px; border: 1px solid #e2e8f0;'>" +
                "<h2 style='color: #1e293b; margin-top: 0;'>Password Reset Request</h2>" +
                "<p style='color: #64748b;'>Hello,</p>" +
                "<p style='color: #64748b;'>We received a request to reset your password. Click the button below to set a new password. This link expires in <strong>1 hour</strong>.</p>" +
                "<div style='text-align: center; margin: 30px 0;'>" +
                "<a href='%s' style='background-color: #2563eb; color: white; padding: 14px 32px; text-decoration: none; border-radius: 8px; font-weight: bold; font-size: 16px; display: inline-block;'>Reset Password</a>" +
                "</div>" +
                "<p style='color: #94a3b8; font-size: 13px;'>If the button doesn't work, copy and paste this link into your browser:</p>" +
                "<p style='color: #2563eb; font-size: 13px; word-break: break-all;'>%s</p>" +
                "<hr style='border: none; border-top: 1px solid #e2e8f0; margin: 20px 0;'>" +
                "<p style='color: #94a3b8; font-size: 12px;'>If you didn't request a password reset, you can safely ignore this email.</p>" +
                "<p style='color: #94a3b8; font-size: 12px;'>© 2026 Daily Activities Management. All rights reserved.</p>" +
                "</div>" +
                "</div>",
                resetLink, resetLink);

            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);

        } catch (MessagingException e) {
            log.error("Failed to send reset email to: {}", to, e);
            throw new RuntimeException("Failed to send reset email: " + e.getMessage());
        }
    }
}
