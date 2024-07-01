package com.magma.core.data.repository;

import com.magma.core.data.support.Message;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;


public interface MessageRepository extends MongoRepository<Message, String> {

    List<Message> findMessagesByDevice(String deviceId);

}
