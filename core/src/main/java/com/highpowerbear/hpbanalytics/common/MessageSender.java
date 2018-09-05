package com.highpowerbear.hpbanalytics.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Created by robertk on 12/27/2017.
 */
@Service
public class MessageSender {
    private static final Logger log = LoggerFactory.getLogger(MessageSender.class);

    private final JavaMailSender emailSender;
    private final JmsTemplate jmsTemplate;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public MessageSender(JavaMailSender emailSender, JmsTemplate jmsTemplate, SimpMessagingTemplate simpMessagingTemplate) {
        this.emailSender = emailSender;
        this.jmsTemplate = jmsTemplate;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public void sendEmailMessage(String subject, String text) {
        log.info("sending email message: " + subject);

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(CoreSettings.EMAIL_FROM);
        message.setTo(CoreSettings.EMAIL_TO);
        message.setSubject(subject);
        message.setText(text);

        emailSender.send(message);
    }

    public void sendJmsMesage(String destination, Object message) {
        log.info("sending jms message " + destination + ": " + message);

        jmsTemplate.convertAndSend(destination, message);
    }

    public void sendWsMessage(String topic, String message) {
        simpMessagingTemplate.convertAndSend(topic, message);
    }
}