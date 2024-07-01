package com.magma.core.data.repository;

import com.magma.core.data.entity.MagmaCodec;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MagmaCodecRepository extends MongoRepository<MagmaCodec, String> {

    MagmaCodec findByCodecName(String codecName);
}
