package com.magma.core.data.repository;

import com.magma.core.data.entity.Vmq_acl_auth;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface Vmq_acl_authRepository extends MongoRepository<Vmq_acl_auth, String> {

    @Query("{ 'client_id': ?0}")
    Vmq_acl_auth findClientId(String id);

}
