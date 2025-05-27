package com.nayonikaeyecare.api.repositories.referral;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import com.nayonikaeyecare.api.entities.Referral;
import com.nayonikaeyecare.api.repositories.patient.CustomReferralRepository;

@Repository
public interface ReferralRepositoryImpl extends MongoRepository<Referral, ObjectId>, CustomReferralRepository {

    @Query("{ 'patientId': ?0 }")
    List<Referral> findByPatientId(String patientId);

    @Query("{ 'patientId': ?0, 'hospitalId': ?1 }")
    List<Referral> findByPatientIdAndHospitalId(String patientId, String hospitalId);
}
