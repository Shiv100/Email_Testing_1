package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class EmailService {

    private static final Logger logger = Logger.getLogger(EmailService.class.getName());

    @Autowired
    private JavaMailSender mailSender;

   public String sendEmail(EmailRequest request) {
    if (request.getTo() == null || request.getTo().isEmpty()) {
        return "❌ Error: Recipient email list is empty!";
    }

    String rawTrackingBaseURL = "https://c03f-116-72-196-16.ngrok-free.app/track/open";
    String trackingBaseURL = rawTrackingBaseURL.replaceAll("/$", ""); // Remove trailing slash if present

    int successCount = 0;

    for (String recipientEmail : request.getTo()) {
        try {
            // Encode email for safe URL
            String encodedEmail = URLEncoder.encode(recipientEmail, StandardCharsets.UTF_8);
            String timestamp = String.valueOf(System.currentTimeMillis());

            // Build personalized tracking URLs
            String trackingPixelURL = trackingBaseURL + "?email=" + encodedEmail + "&t=" + timestamp;
            String clickTrackingURL = trackingBaseURL.replace("/open", "/click") + "?email=" + encodedEmail
                    + "&url=" + URLEncoder.encode("https://avair.ai/", StandardCharsets.UTF_8);

            logger.info("Tracking Pixel URL: " + trackingPixelURL);
            logger.info("Click Tracking URL: " + clickTrackingURL);

            // HTML components
            String trackingPixel = "<img src='" + trackingPixelURL + "' width='1' height='1' style='opacity:0;' alt='.' />";
            String trackedLink = "<a href='" + clickTrackingURL + "' target='_blank'>Click Here</a>";

            String emailContent = "<html><body>"
                    + request.getBody()
                    + "<br><br>" + trackedLink
                    + "<br><br>" + trackingPixel
                    + "</body></html>";

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("info.avair.ai@gmail.com");
            helper.setTo(recipientEmail);
            helper.setSubject(request.getSubject());
            helper.setText(emailContent, true);

            mailSender.send(message);
            successCount++;
            logger.info("✅ Email sent successfully to " + recipientEmail);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "❌ Failed to send email to " + recipientEmail + ": " + e.getMessage(), e);
        }
    }

    return "📬 Emails sent: " + successCount + "/" + request.getTo().size();
}

}