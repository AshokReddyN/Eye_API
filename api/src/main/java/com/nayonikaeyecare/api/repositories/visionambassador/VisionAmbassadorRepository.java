package com.nayonikaeyecare.api.repositories.visionambassador;

import com.nayonikaeyecare.api.entities.VisionAmbassador;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisionAmbassadorRepository extends MongoRepository<VisionAmbassador, ObjectId>, CustomVisionAmbassador {
    VisionAmbassador findByUserId(String userId);
}
