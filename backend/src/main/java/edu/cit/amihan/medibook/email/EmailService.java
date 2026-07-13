package edu.cit.amihan.medibook.email;

import edu.cit.amihan.medibook.appointment.entity.Appointment;
import edu.cit.amihan.medibook.appointment.entity.AppointmentStatus;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnBean(JavaMailSender.class)
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendStatusChangeEmail(Appointment appointment, AppointmentStatus oldStatus, AppointmentStatus newStatus) {
        try {
            String patientEmail = appointment.getPatient().getUser().getEmail();
            String patientName = appointment.getPatient().getFullName();
            String doctorName = appointment.getSchedule().getDoctor().getFullName();
            String date = appointment.getSchedule().getStartTime().toString().replace("T", " at ");

            String subject;
            String body;

            switch (newStatus) {
                case CONFIRMED -> {
                    subject = "MediBook — Appointment Confirmed";
                    body = buildHtml(patientName,
                            "Your appointment has been <strong style=\"color:#059669\">confirmed</strong>.",
                            "Doctor: " + doctorName,
                            "Scheduled: " + date);
                }
                case CANCELLED -> {
                    subject = "MediBook — Appointment Cancelled";
                    body = buildHtml(patientName,
                            "Your appointment has been <strong style=\"color:#DC2626\">cancelled</strong>.",
                            "Doctor: " + doctorName,
                            oldStatus == AppointmentStatus.CONFIRMED ? "Originally scheduled: " + date : "");
                }
                case COMPLETED -> {
                    subject = "MediBook — Appointment Completed";
                    body = buildHtml(patientName,
                            "Your appointment has been <strong style=\"color:#2563EB\">completed</strong>.",
                            "Doctor: " + doctorName,
                            "A health record has been filed for your visit.");
                }
                default -> {
                    return;
                }
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(patientEmail);
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(message);
            log.info("Email sent to {} for appointment {} ({} -> {})", patientEmail, appointment.getAppointmentId(), oldStatus, newStatus);

        } catch (Exception e) {
            log.error("Failed to send email for appointment {}: {}", appointment.getAppointmentId(), e.getMessage());
        }
    }

    private String buildHtml(String patientName, String statusLine, String detail1, String detail2) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: 'Segoe UI', Arial, sans-serif; background-color: #F6F9F9; padding: 32px;">
                  <div style="max-width: 480px; margin: auto; background: white; border-radius: 12px; overflow: hidden; border: 1px solid #DCE7E5;">
                    <div style="background-color: #0B3D3F; padding: 20px 24px;">
                      <span style="color: white; font-size: 20px; font-weight: 600;">MediBook</span>
                    </div>
                    <div style="padding: 24px;">
                      <p style="font-size: 15px; color: #10241F;">Hello %s,</p>
                      <p style="font-size: 15px; color: #10241F; margin-top: 12px;">%s</p>
                      <div style="background: #F6F9F9; border-radius: 8px; padding: 16px; margin-top: 16px;">
                        <p style="margin: 0; font-size: 14px; color: #4B615D;">%s</p>
                        %s
                      </div>
                      <p style="font-size: 13px; color: #94A3B8; margin-top: 24px;">This is an automated notification from MediBook.</p>
                    </div>
                  </div>
                </body>
                </html>
                """.formatted(
                patientName,
                statusLine,
                detail1,
                detail2.isEmpty() ? "" : "<p style=\"margin: 8px 0 0 0; font-size: 14px; color: #4B615D;\">" + detail2 + "</p>"
        );
    }
}