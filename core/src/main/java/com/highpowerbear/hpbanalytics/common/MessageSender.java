package com.highpowerbear.hpbanalytics.common;

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

    @Autowired public JavaMailSender emailSender;
    @Autowired private JmsTemplate jmsTemplate;
    @Autowired private SimpMessagingTemplate simpMessagingTemplate;

    public void sendEmailMessage(String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(CoreSettings.EMAIL_FROM);
        message.setTo(CoreSettings.EMAIL_TO);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

    public void sendJmsMesage(String destination, String message) {
        jmsTemplate.convertAndSend(destination, message);
    }

    public void sendWsMessage(String topic, String message) {
        simpMessagingTemplate.convertAndSend(topic, message);
    }
}