package com.nayonikaeyecare.api.repositories.user;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.nayonikaeyecare.api.entities.user.User;

@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {
    /**
     * Find a user by their user credential.
     * 
     * @param userCredential
     * @return User
     * @throws IllegalArgumentException if userCredential is null
     * @throws UserNotFoundException    if no user is found with the given
     *                                  credential
     * 
     */
    User findByUserCredentialId(ObjectId userCredential);
}