package com.magma.core.data.repository;

import com.magma.core.data.entity.Http_acl_auth;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface Http_acl_authRepository extends MongoRepository<Http_acl_auth, String> {

    @Query("{ 'client_id': ?0}")
    Http_acl_auth findClientId(String id);

}
