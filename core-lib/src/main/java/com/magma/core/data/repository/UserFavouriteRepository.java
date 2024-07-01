package com.magma.core.data.repository;

import com.magma.core.data.entity.UserFavourite;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFavouriteRepository extends MongoRepository<UserFavourite, String> {
    UserFavourite findByUserId(String userId);
}
