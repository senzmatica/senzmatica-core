package com.magma.core.data.repository;

import com.magma.core.data.entity.Http_acl_auth;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface Http_acl_authRepository extends MongoRepository<Http_acl_auth, String> {

    Http_acl_auth findById(String id);


    @Query("{ 'client_id': ?0}")
    Http_acl_auth findClientId(String clientId);

    @Query("{ 'backupClient_id': ?0}")
    Http_acl_auth findByBackupClientId(String backupClientId);

}
