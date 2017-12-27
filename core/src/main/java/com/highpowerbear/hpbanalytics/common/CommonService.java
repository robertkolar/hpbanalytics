package com.highpowerbear.hpbanalytics.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Created by robertk on 12/27/2017.
 */
@Service
public class CommonService {

    @Autowired public JavaMailSender emailSender;

    public void sendEmail(String subject, String text) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(CoreSettings.EMAIL_FROM);
        message.setTo(CoreSettings.EMAIL_TO);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }
}