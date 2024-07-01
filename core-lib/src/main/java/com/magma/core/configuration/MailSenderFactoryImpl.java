package com.magma.core.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class MailSenderFactoryImpl implements MailSenderFactory {

    @Value("${mail.transport.protocol}")
    private String protocol;

    @Value("${mail.smtp.auth}")
    private boolean authEnabled;

    @Value("${mail.smtp.starttls.enable}")
    private boolean starttlsEnabled;

    @Value("${mail.debug}")
    private boolean debugEnabled;

    @Override
    public JavaMailSender getSender(String host, String email, String password) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

        Properties props = new Properties();
        props.put("mail.transport.protocol", protocol);
        props.put("mail.smtp.auth", authEnabled);
        props.put("mail.smtp.starttls.enable", starttlsEnabled);
        props.put("mail.debug", debugEnabled);

        mailSender.setJavaMailProperties(props);

        mailSender.setPort(587);
        mailSender.setHost(host);
        mailSender.setUsername(email);
        mailSender.setPassword(password);

        return mailSender;
    }

}
