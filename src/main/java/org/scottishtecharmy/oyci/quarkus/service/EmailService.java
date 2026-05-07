package org.scottishtecharmy.oyci.quarkus.service;
import org.scottishtecharmy.oyci.quarkus.EmailViewerResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import org.jboss.logging.Logger;

@ApplicationScoped
public class EmailService {

	private static final Logger LOG = Logger.getLogger(EmailService.class);
	@Inject
	Mailer mailer;

	private String loadTemplate(String templateName, Map<String, String> params) {
		try {
			String content = Files.readString(Paths.get("src/main/resources/email-templates/" + templateName));
			for (Map.Entry<String, String> entry : params.entrySet()) {
				content = content.replace("{" + entry.getKey() + "}", entry.getValue());
			}
			return content;
		} catch (Exception e) {
			throw new RuntimeException("Failed to load email template: " + templateName, e);
		}
	}


	public void sendEventRegistrationEmail(String to, String userName, String eventName, String eventDate, String eventLocation) {
		String body = loadTemplate("event-registration.txt", Map.of(
			"userName", userName,
			"eventName", eventName,
			"eventDate", eventDate,
			"eventLocation", eventLocation
		));
		try {
			String from = "OYCISupport <noreply@example.com>";
			LOG.infof("Attempting to send event registration email to %s...", to);
			Mail mail = Mail.withText(to, "Event Registration Confirmation", body).setFrom(from);
			mailer.send(mail);
			EmailViewerResource.recordSentEmail(mail);
			LOG.infof("Email send request submitted to %s.", to);
		} catch (Exception e) {
			LOG.errorf(e, "Failed to send event registration email to %s", to);
		}
	}

	public void sendLeaveStatusEmail(String to, String staffName, String status) {
		String body = loadTemplate("leave-status.txt", Map.of("staffName", staffName, "status", status));
		try {
			String from = "OYCISupport <noreply@example.com>";
			LOG.infof("Attempting to send leave status email to %s...", to);
			Mail mail = Mail.withText(to, "Leave Request " + status, body).setFrom(from);
			mailer.send(mail);
			EmailViewerResource.recordSentEmail(mail);
			LOG.infof("Email send request submitted to %s.", to);
		} catch (Exception e) {
			LOG.errorf(e, "Failed to send leave status email to %s", to);
		}
	}
	public void sendEventCreatedEmail(String to, String userName, String eventName, String eventDate, String eventLocation) {
		String body = loadTemplate("event-created.txt", Map.of(
			"userName", userName,
			"eventName", eventName,
			"eventDate", eventDate,
			"eventLocation", eventLocation
		));
		try {
			String from = "OYCISupport <noreply@example.com>";
			LOG.infof("Attempting to send event created email to %s...", to);
			Mail mail = Mail.withText(to, "New Event Created", body).setFrom(from);
			mailer.send(mail);
			EmailViewerResource.recordSentEmail(mail);
			LOG.infof("Email send request submitted to %s.", to);
		} catch (Exception e) {
			LOG.errorf(e, "Failed to send event created email to %s", to);
		}
	}

	public void sendStaffAvailabilityEmail(String to, String staffName, String availabilityStatus) {
		String body = loadTemplate("staff-availability.txt", Map.of(
			"staffName", staffName,
			"availabilityStatus", availabilityStatus
		));
		try {
			String from = "OYCISupport <noreply@example.com>";
			LOG.infof("Attempting to send staff availability email to %s...", to);
			Mail mail = Mail.withText(to, "Staff Availability Update", body).setFrom(from);
			mailer.send(mail);
			EmailViewerResource.recordSentEmail(mail);
			LOG.infof("Email send request submitted to %s.", to);
		} catch (Exception e) {
			LOG.errorf(e, "Failed to send staff availability email to %s", to);
		}
	}

	public void sendForgotPasswordEmail(String to, String userName, String unlockLink) {
		String body = loadTemplate("forgot-password.txt", Map.of(
			"userName", userName,
			"unlockLink", unlockLink
		));
		try {
			String from = "OYCISupport <noreply@example.com>";
			LOG.infof("Attempting to send forgot password email to %s...", to);
			Mail mail = Mail.withText(to, "Password Reset Request", body).setFrom(from);
			mailer.send(mail);
			EmailViewerResource.recordSentEmail(mail);
			LOG.infof("Email send request submitted to %s.", to);
		} catch (Exception e) {
			LOG.errorf(e, "Failed to send forgot password email to %s", to);
		}
	}
}
