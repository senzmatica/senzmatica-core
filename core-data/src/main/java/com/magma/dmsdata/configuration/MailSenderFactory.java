package com.magma.dmsdata.configuration;

import org.springframework.mail.javamail.JavaMailSender;

public interface MailSenderFactory {
    JavaMailSender getSender(String host, String email, String password);
}
