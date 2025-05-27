package com.nayonikaeyecare.api.repositories.application;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.nayonikaeyecare.api.entities.application.Application;

@Repository
public interface ApplicationRepository extends MongoRepository<Application, String> {
    Application findByCode(String code);
}