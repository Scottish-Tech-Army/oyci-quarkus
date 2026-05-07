package org.scottishtecharmy.oyci.quarkus;

import io.quarkus.mailer.Mail;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@ApplicationScoped
@Path("/emails")
public class EmailViewerResource {

    // Thread-safe list to store sent emails in memory
    private static final List<Mail> sentEmails = new CopyOnWriteArrayList<>();

    // Call this method in your EmailService after sending an email
    public static void recordSentEmail(Mail mail) {
        sentEmails.add(mail);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Mail> getAllEmails() {
        return Collections.unmodifiableList(sentEmails);
    }
}
