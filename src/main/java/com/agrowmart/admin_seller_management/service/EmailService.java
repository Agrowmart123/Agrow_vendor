package com.agrowmart.admin_seller_management.service;


import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service

public class EmailService {

    private final JavaMailSender mailSender;
    public EmailService (JavaMailSender mailSender)
    {
    	this.mailSender = mailSender;
    }

    /**
     * Sends a simple text-based email.
     *
     * @param to      Recipient's email address
     * @param subject Email subject
     * @param text    Email body text
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        message.setFrom("ani20250002@gmail.com"); // Change to your sender email (configured in properties)

        try {
            mailSender.send(message);
            System.out.println("Email sent successfully to: " + to);
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            // You can throw a custom exception here if needed
        }
    }

    // Optional: Add more methods if needed, e.g., for HTML emails
}