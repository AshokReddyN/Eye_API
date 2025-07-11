package com.nayonikaeyecare.api.repositories.patient;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.nayonikaeyecare.api.entities.Patient;
import com.nayonikaeyecare.api.entities.Gender;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends MongoRepository<Patient, ObjectId>, CustomPatientRepository {
    List<Patient> findByName(String name);

    List<Patient> findByAmbassadorId(String ambassadorId);

    Optional<Patient> findByNameAndAmbassadorIdAndPhoneSearchable(String name, String ambassadorId, String phoneSearchable);
    Optional<Patient> findFirstByAgeSearchableAndPhoneSearchableAndGenderOrderByCreatedAtDesc(String ageSearchable, String phoneSearchable, Gender gender);
}

