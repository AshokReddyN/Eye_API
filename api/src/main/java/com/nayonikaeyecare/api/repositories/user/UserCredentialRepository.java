package com.nayonikaeyecare.api.repositories.user;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.nayonikaeyecare.api.entities.user.UserCredential;

@Repository
public interface UserCredentialRepository extends MongoRepository<UserCredential, ObjectId> {
    UserCredential findByCredential(String credential);
}