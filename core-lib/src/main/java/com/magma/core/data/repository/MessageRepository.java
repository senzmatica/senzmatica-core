package com.magma.core.data.repository;

import com.magma.core.data.entity.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    Message findById(String s);

    List<Message> findMessagesByDevice(String deviceId);
}
