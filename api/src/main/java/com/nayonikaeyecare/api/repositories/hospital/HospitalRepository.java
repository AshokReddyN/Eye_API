package com.nayonikaeyecare.api.repositories.hospital;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.nayonikaeyecare.api.entities.Hospital;
import java.util.Optional;

@Repository
public interface HospitalRepository extends MongoRepository<Hospital, ObjectId>, CustomHospitalRepository {
    boolean existsByName(String name);
    boolean existsByHospitalCode(String hospitalCode);
    Optional<Hospital> findByName(String name);
    Optional<Hospital> findByHospitalCode(String hospitalCode);
}
    
