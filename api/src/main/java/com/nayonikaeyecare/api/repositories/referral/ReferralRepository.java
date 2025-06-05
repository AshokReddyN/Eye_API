package com.nayonikaeyecare.api.repositories.referral;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.nayonikaeyecare.api.entities.Referral;

import java.util.List;

@Repository
public interface ReferralRepository extends MongoRepository<Referral, ObjectId> {

    List<Referral> findByAmbassadorId(ObjectId ambassadorId);

    List<Referral> findByHospitalId(ObjectId hospitalId);

    List<Referral> findByPatientId(ObjectId patientId);

    Page<Referral> findByHospitalId(ObjectId hospitalId, Pageable pageable);

    @Query("{ your_custom_query_here }")
    Page<Referral> filterReferrals(ObjectId ambassadorId, String state, String city, Boolean status, String name, Pageable pageable);
    
    List<Referral> findByPatientNameAndHospitalId(String patientName, ObjectId hospitalId);
    long countByHospitalId(ObjectId hospitalId);
    long countByHospitalIdAndIsSpectacleRequestedTrue(ObjectId hospitalId);
}
