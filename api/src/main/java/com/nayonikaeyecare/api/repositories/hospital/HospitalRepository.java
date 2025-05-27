package com.nayonikaeyecare.api.repositories.hospital;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.nayonikaeyecare.api.entities.Hospital;

@Repository
public interface HospitalRepository extends MongoRepository<Hospital, ObjectId>, CustomHospitalRepository {

}
    
