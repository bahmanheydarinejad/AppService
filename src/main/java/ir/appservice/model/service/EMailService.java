package ir.appservice.model.service;

import ir.appservice.model.entity.application.Email;
import ir.appservice.model.entity.domain.Document;
import ir.appservice.model.repository.EmailRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Date;

@Service
@ApplicationScope
public class EMailService extends CrudService<Email> {

    private JavaMailSender appMailSender;

    public EMailService(EmailRepository emailRepository, JavaMailSender appMailSender) {
        super(emailRepository, Email.class);
        this.appMailSender = appMailSender;
    }

    public EmailRepository getEmailRepository() {
        return (EmailRepository) this.crudRepository;
    }

    @Async
    public void sendMail(Email email) throws MessagingException {

        MimeMessage message = appMailSender.createMimeMessage();
        MimeMessageHelper helper;
        if (email.getAttachments() != null && !email.getAttachments().isEmpty()) {
            helper = new MimeMessageHelper(message, true);

            for (Document attachment : email.getAttachments()) {
                helper.addAttachment(attachment.getDisplayName(), new ByteArrayResource(attachment.getData()));
            }
        } else {
            helper = new MimeMessageHelper(message, false);
        }

        helper.setTo(email.getMailTo().toArray(new String[]{}));
        helper.setCc(email.getMailCc().toArray(new String[]{}));
        helper.setBcc(email.getMailBcc().toArray(new String[]{}));
        helper.setSubject(email.getSubject());
        helper.setText(email.getText(), true);
        helper.setFrom(email.getMailFrom());
        if (email.getReplyTo() != null) {
            helper.setReplyTo(email.getReplyTo());
        }
        appMailSender.send(message);
        email.setSentDate(new Date());
        edit(email);
    }
}